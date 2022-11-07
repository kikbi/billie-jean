package derek.tool.sql;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import derek.tool.sql.config.PersistentDialogConfig;
import derek.tool.sql.dialect.DatabaseInfo;
import derek.tool.sql.dialect.DerekDialectResolver;
import derek.tool.sql.dialect.Table;
import derek.tool.sql.dialect.TableSqlExporter;
import derek.tool.sql.dialog.DialectInteractView;
import derek.tool.sql.dialog.DialectSqlDTO;
import derek.tool.sql.settings.CustomDialectDTO;
import derek.tool.sql.settings.CustomDialectSettings;
import derek.tool.sql.util.NotificationUtil;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.Database;
import org.hibernate.dialect.Dialect;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolver;
import org.hibernate.type.BasicTypeRegistry;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static derek.tool.sql.dialog.DialectSqlDTO.PROJECT_PATH_PREFIX;

/**
 * @author Derek
 * @date 2022/7/27
 */
@Data
public class DBContext {

    private static final Logger log = LoggerFactory.getLogger(DBContext.class);

    private static final Pattern DATE_PATTERN = Pattern.compile("(\\{\\$date((\\+|-)\\d+)?\\})");
    private static final Pattern YEAR_PATTERN = Pattern.compile("(\\{\\$year((\\+|-)\\d+)?\\})");
    private static final Pattern MONTH_PATTERN = Pattern.compile("(\\{\\$month((\\+|-)\\d+)?\\})");
    private static final Pattern DAY_PATTERN = Pattern.compile("(\\{\\$day((\\+|-)\\d+)?\\})");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("MM");
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.ofPattern("dd");

    private static final Set<Database> FIXED_DB = Arrays.stream(new Database[]{
                    Database.MYSQL, Database.ORACLE, Database.POSTGRESQL, Database.SQLSERVER
            })
            .collect(Collectors.toSet());

    private DialectResolver dialectResolver = new DerekDialectResolver();

    private List<DialectSqlDTO> infoList;

    private GenCreationStatementAction action;

    private AnActionEvent event;

    private PsiClass targetClass;

    private Project project;

    private PsiFile psiFile;

    private Editor editor;

    private PersistentDialogConfig pdc;

    private boolean tableUpperCase;
    private boolean prettier;
    private boolean columnUpperCase;

    public DBContext(GenCreationStatementAction action, AnActionEvent event, PsiClass targetClass) {
        this.action = action;
        this.event = event;
        project = event.getData(PlatformDataKeys.PROJECT);
        psiFile = event.getData(CommonDataKeys.PSI_FILE);
        editor = event.getData(CommonDataKeys.EDITOR);
        this.targetClass = targetClass;

        pdc = PersistentDialogConfig.getInstance(project);
        if (pdc != null) {
            infoList = mergeCustomDialects(pdc.getInfoList());
            tableUpperCase = pdc.isTableUpperCase();
            prettier = pdc.isPrettier();
            columnUpperCase = pdc.isColumnUpperCase();
        }
        if (CollectionUtils.isEmpty(infoList)) {
            infoList = initDefaultDialects();
        }
    }

    private List<DialectSqlDTO> mergeCustomDialects(List<DialectSqlDTO> infoList) {
        List<CustomDialectDTO> dtoList = CustomDialectSettings.getCustomDialect();
        if (infoList == null) {
            return null;
        }
        Map<String, DialectSqlDTO> cdMap = dtoList.stream()
                .map(DialectSqlDTO::new)
                .collect(Collectors.toMap(DialectSqlDTO::getDatabaseName, Function.identity(), (f, s) -> s));
        List<DialectSqlDTO> result = new ArrayList<>();
        for (DialectSqlDTO dialectSqlDTO : infoList) {
            if (!dialectSqlDTO.isCustom()) {
                result.add(dialectSqlDTO);
                continue;
            }
            DialectSqlDTO newCustomDialect = cdMap.get(dialectSqlDTO.getDatabaseName());
            if (newCustomDialect == null) {
                result.add(dialectSqlDTO);
                continue;
            }
            boolean sameVersion = Objects.equals(newCustomDialect.getMajorVersion(), dialectSqlDTO.getMajorVersion())
                    && Objects.equals(newCustomDialect.getMinorVersion(), dialectSqlDTO.getMinorVersion());
            if (sameVersion) {
                result.add(newCustomDialect);
                continue;
            }
            result.add(dialectSqlDTO);
        }
        return result;
    }

    public List<DialectSqlDTO> initDefaultDialects() {
        Stream<DialectSqlDTO> spStream = Stream.of(
                new DialectSqlDTO(DatabaseInfo.MYSQL, 5, 7, PROJECT_PATH_PREFIX + "/sql/mysql.sql"),
                new DialectSqlDTO(DatabaseInfo.ORACLE, 11, 0, PROJECT_PATH_PREFIX + "/sql/oracle.sql"),
                new DialectSqlDTO(DatabaseInfo.MS_SQLSERVER, 13, 0, PROJECT_PATH_PREFIX + "/sql/sqlserver.sql"),
                new DialectSqlDTO(DatabaseInfo.POSTGRESQL, 9, 0, PROJECT_PATH_PREFIX + "/sql/postgresql.sql"));

        Stream<DialectSqlDTO> otherDBStream = Arrays.stream(Database.values())
                .filter(Predicate.not(FIXED_DB::contains))
                .map(DialectSqlDTO::new);

        List<CustomDialectDTO> dtoList = CustomDialectSettings.getCustomDialect();

        Stream<DialectSqlDTO> cdStream = dtoList.stream()
                .map(DialectSqlDTO::new);

        List<DialectSqlDTO> collect = Stream.of(spStream, cdStream, otherDBStream)
                .flatMap(Function.identity())
                .collect(Collectors.toList());
        return collect;
    }

    public void openDialog() {
        DialectInteractView dialog = new DialectInteractView(this);
        dialog.showAndGet();
    }

    public void copyToClipboard(boolean tableUpperCase, boolean columnUpperCase,
                                boolean pretty) {
        StringBuilder sb = new StringBuilder();
        for (DialectSqlDTO dto : infoList) {
            if (!dto.isSelected()) {
                continue;
            }
            String sql = buildSql(dto, tableUpperCase, columnUpperCase, pretty);
            if (sql != null) {
                sb.append("#");
                String dbInfo = dto.dataBaseInfo().replace(" ", "_");
                sb.append(dbInfo).append("\n");
                sb.append(sql);
            }
            log.info("sql={}", sql);
        }

        StringSelection stringSelection = new StringSelection(sb.toString());
        Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        systemClipboard.setContents(stringSelection, stringSelection);
        NotificationUtil.info(targetClass.getName() + " creation SQL statement copied to clipboard.");
    }



    private String buildSql(DialectResolutionInfo dialectInfo, boolean tableUpperCase, boolean columnUpperCase,
                            boolean pretty) {
        StringBuilder sb = new StringBuilder();
        Dialect dialect = dialectResolver.resolveDialect(dialectInfo);
        if (dialect == null) {
            log.warn("dialect not found,info={}", dialectInfo);
            NotificationUtil.error("dialect " + dialectInfo + " not found");
            return null;
        }
        BasicTypeRegistry basicTypeRegistry = new BasicTypeRegistry();
        Table table = new Table(targetClass, basicTypeRegistry);
        TableSqlExporter sqlExporter = new TableSqlExporter(dialect);
        String sql = sqlExporter.sqlCreationStatement(table, dialect, tableUpperCase, columnUpperCase, pretty);
        sb.append(sql).append("\n\n");
        return sb.toString();
    }

    public void export(boolean tableUpperCase, boolean columnUpperCase,
                       boolean pretty) {
        for (DialectSqlDTO dto : infoList) {
            if (!dto.isSelected()) {
                continue;
            }
            String path = dto.getPath();
            if (StringUtils.isBlank(path)) {
                log.warn("路径无效", path);
                continue;
            }
            String sql = buildSql(dto, tableUpperCase, columnUpperCase, pretty);
            if (sql == null) {
                log.warn("未生成sql", dto.dataBaseInfo());
                continue;
            }
            writeToFile(path, sql);
        }
        NotificationUtil.info("Export " + targetClass.getName() + " creation SQL statements success.");
    }

    private void writeToFile(String inputPath, String sql) {
        String realPath = Stream.of(inputPath)
                .map(this::projectPathReplace)
                .map(this::datePathReplace)
                .map(this::yearPathReplace)
                .map(this::monthPathReplace)
                .map(this::dayPathReplace)
                .collect(Collectors.joining());


        try {
            FileUtils.write(new File(realPath), sql, StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("export file error", e);
        }
        log.info(realPath);
    }

    private String datePathReplace(String path) {
        return replaceDate(path, DATE_FORMATTER,DATE_PATTERN,ChronoUnit.DAYS);
    }

    private String yearPathReplace(String path) {
        return replaceDate(path, YEAR_FORMATTER,YEAR_PATTERN,ChronoUnit.YEARS);
    }

    private String monthPathReplace(String path) {
        return replaceDate(path, MONTH_FORMATTER,MONTH_PATTERN,ChronoUnit.MONTHS);
    }

    private String dayPathReplace(String path) {
        return replaceDate(path, DAY_FORMATTER,DAY_PATTERN,ChronoUnit.DAYS);
    }

    @NotNull
    private String replaceDate(String path,DateTimeFormatter formatter, Pattern datePattern,ChronoUnit unit) {
        //替换时间变量
        Matcher matcher = datePattern.matcher(path);
        String result = path;
        LocalDate now = LocalDate.now();
        while (matcher.find()) {
            String group = matcher.group();
            String offsetStr = matcher.group(2);
            LocalDate targetDate = now;
            if (offsetStr != null) {
                int offsetDays = Integer.parseInt(offsetStr);
                targetDate = now.plus(offsetDays, unit);
            }
            String formatDate = formatter.format(targetDate);
            result = result.replace(group, formatDate);
        }
        return result;
    }

    @NotNull
    private String projectPathReplace(String path) {
        //替换项目根路径
        boolean projectBase = path.startsWith(PROJECT_PATH_PREFIX);
        if (projectBase) {
            String basePath = project.getBasePath();
            return path.replace(PROJECT_PATH_PREFIX, basePath);
        }
        return path;
    }
}
