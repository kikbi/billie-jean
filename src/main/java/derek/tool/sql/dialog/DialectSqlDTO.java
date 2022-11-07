package derek.tool.sql.dialog;

import derek.tool.sql.dialect.DatabaseInfo;
import derek.tool.sql.settings.CustomDialectDTO;
import derek.tool.sql.settings.DbInfoDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.Database;
import org.hibernate.engine.jdbc.dialect.spi.DialectResolutionInfo;

import javax.swing.JCheckBox;
import javax.swing.JTextField;
import java.util.Optional;

/**
 * 一个数据库方言
 * @author Derek
 * @date 2022/6/22
 */
@Slf4j
@Data
@EqualsAndHashCode(callSuper = true)
public class DialectSqlDTO extends DatabaseInfo{

    public static final  String PROJECT_PATH_PREFIX = "{$project}";
    public static final  String DATE_PATH = "{$date}";

    private boolean custom = false;

    @SuppressWarnings("all")
    private boolean selected = false;

    /**
     * 导出路径
     */
    private String path;

    /**
     * 操作
     */
    private DBAction dbAction;

    /**
     * sql语句
     */
    private String[] sql;

    /**
     * 当适用{@link derek.tool.sql.dialect.DerekDialectResolver}时可以根据枚举名称查找最新版本
     * @param database
     */
    public DialectSqlDTO(Database database) {
        databaseName = database.name();
        majorVersion = null;
        minorVersion = null;
        path = PROJECT_PATH_PREFIX + "/sql/" + databaseName + ".sql";
    }

    public DialectSqlDTO(DialectResolutionInfo info) {
        databaseName = info.getDatabaseName();
        majorVersion = info.getDatabaseMajorVersion();
        minorVersion = info.getDatabaseMinorVersion();
        path = PROJECT_PATH_PREFIX + "/sql/" + databaseName + ".sql";
    }

    public DialectSqlDTO(CustomDialectDTO cdd) {
        DbInfoDTO dbInfo = cdd.getDbInfo();
        databaseName = dbInfo.getDatabaseName();
        majorVersion = Optional.ofNullable(dbInfo.getMajorVersion()).map(Integer::parseInt).orElse(null);
        minorVersion = Optional.ofNullable(dbInfo.getMinorVersion()).map(Integer::parseInt).orElse(null);
        path = PROJECT_PATH_PREFIX + "/sql/" + databaseName + ".sql";
        custom = true;
    }

    public DialectSqlDTO(String databaseName, Integer majorVersion, Integer minorVersion, String path) {
        this.databaseName = databaseName;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
        this.path = path;
    }

    public DialectSqlDTO(String databaseName, Integer majorVersion, Integer minorVersion) {
        this.databaseName = databaseName;
        this.majorVersion = majorVersion;
        this.minorVersion = minorVersion;
    }


    public void writeValueTo(JCheckBox checkBox, JTextField versionField, JTextField pathField) {
        checkBox.setText(databaseName);
        checkBox.setSelected(selected);
        String version = Optional.ofNullable(majorVersion)
                .map(m -> majorVersion + "." + minorVersion)
                .orElse("");
        versionField.setText(version);
        pathField.setText(path);
    }

    public static DialectSqlDTO viewToDTO(JCheckBox checkBox, JTextField version, JTextField path) {
        return viewToDTO(checkBox.getText(),checkBox,version,path);
    }

    public static DialectSqlDTO viewToDTO(String dbName, JCheckBox checkBox, JTextField version, JTextField path) {

        String versionText = version.getText();
        Integer majorVersion = null;
        Integer minorVersion = null;
        if (versionText != null && !versionText.isBlank()) {
            String[] versionSplit = versionText.split("\\.");
            try {
                majorVersion = Integer.parseInt(versionSplit[0]);
            } catch (Exception e) {
                log.error("error", e);
            }
            if (versionSplit.length > 1) {
                try {
                    minorVersion = Integer.parseInt(versionSplit[1]);
                } catch (Exception e) {
                    log.error("error", e);
                }
            }
        }
        String pathText = path.getText();
        DialectSqlDTO dto = new DialectSqlDTO(dbName, majorVersion, minorVersion, pathText);
        boolean selected = checkBox.isSelected();
        dto.setSelected(selected);
        return dto;
    }

}
