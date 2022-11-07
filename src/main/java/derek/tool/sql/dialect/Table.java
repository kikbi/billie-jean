package derek.tool.sql.dialect;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocTag;
import com.intellij.psi.javadoc.PsiDocTagValue;
import com.intellij.psi.javadoc.PsiDocToken;
import derek.tool.sql.util.StringUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.mapping.PrimaryKey;
import org.hibernate.type.BasicType;
import org.hibernate.type.BasicTypeRegistry;
import org.hibernate.type.StringType;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 根据idea psi java类生成表实体
 *
 * @author Derek
 * @date 2022/6/9
 */
@Slf4j
@Data
public class Table {
    /**
     * 允许建表映射的实体类型，其他类型直接忽略
     */
    private final static Set<String> allowableTypes;

    static {
        allowableTypes = Arrays.stream(new Class[]{
                        Boolean.class,
                        Byte.class,
                        Character.class,
                        Short.class,
                        Integer.class,
                        Long.class,
                        Float.class,
                        Double.class,
                        Enum.class,
                        String.class,
                        BigDecimal.class,
                        Date.class,
                        LocalDate.class,
                        LocalDateTime.class
                }).map(Class::getName)
                .collect(Collectors.toSet());
    }

    private final static String PRIMARY_KEY_TAG = "primaryKey";
    private final static String INDEX_TAG = "index";
    private final static String SCALE_TAG = "scale";
    private final static String LENGTH_TAG = "length";
    private final static String UNIQUE_TAG = "unique";
    private final static String DEFAULT_TAG = "default";
    private final static String NON_NULL_TAG = "nonNull";
    private final static String TABLE_TAG = "table";

    private final BasicTypeRegistry basicTypeRegistry;

    private final String tableName;


    private Map<String, Column> columnMap;

    private List<Column> columnList;

    private List<Index> indexList;

    private String comment;

    /**
     * 主键约束
     */
    private PrimaryKey primaryKey;

    /**
     * hibernate表实现，用于生成主键
     */
    private org.hibernate.mapping.Table htable;


    public Table(PsiClass psiClass, BasicTypeRegistry basicTypeRegistry) {
        this.basicTypeRegistry = basicTypeRegistry;
        tableName = detectTableName(psiClass);
        htable = new org.hibernate.mapping.Table(tableName);
        PrimaryKey tbPK = parseTablePK(psiClass);
        List<Index> tbIdx = parseIndex(psiClass);
        TableColumns dto = parseField(psiClass);
        columnList = dto.columns;
        dto.setTbPK(tbPK);
        dto.setTbIdx(tbIdx);
        primaryKey = dto.getFinalPk();
        indexList = dto.getFinalIndexes();
    }


    private List<Index> parseIndex(PsiClass psiClass) {
        String indexStr = findDocByTag(psiClass, INDEX_TAG);
        if (StringUtils.isBlank(indexStr)) {
            return null;
        }
        List<Index> indexList = new ArrayList<>();
        String[] indexes = indexStr.split(",");
        Index index = new Index(false, this);
        for (String idx : indexes) {
            String name = idx.strip();
            if (StringUtils.isBlank(name)) {
                continue;
            }
            index.addColumn(new Column(name));
        }
        indexList.add(index);
        return indexList;
    }

    private PrimaryKey parseTablePK(PsiClass psiClass) {
        String pkString = findDocByTag(psiClass, PRIMARY_KEY_TAG);
        if (StringUtils.isBlank(pkString)) {
            return null;
        }
        String[] pks = pkString.split(",");
        PrimaryKey primaryKey = new PrimaryKey(htable);
        for (String pk : pks) {
            org.hibernate.mapping.Column column = new org.hibernate.mapping.Column();
            column.setName(pk.strip());
            primaryKey.addColumn(column);
        }
        return primaryKey;
    }

    /**
     * 解析psiField
     *
     * @param psiField
     * @param dto      解析成功的列会加入到dto中
     * @return
     */
    private Column psiFieldToColumn(PsiField psiField, TableColumns dto) {
        PsiType psiType = psiField.getType();
        String name = psiField.getName();
        String columName = StringUtil.toUnderScoreCase(name, false);
        String qualifiedName = null;
        BasicType sqlType = null;
        if (psiType instanceof PsiPrimitiveType) {
            qualifiedName = ((PsiPrimitiveType) psiType).getBoxedTypeName();
            sqlType = basicTypeRegistry.getRegisteredType(qualifiedName);
        } else if (psiType instanceof PsiClassType) {
            PsiClass psiClass = Optional.ofNullable(psiType)
                    .map(PsiClassType.class::cast)
                    .map(PsiClassType::resolve)
                    .orElse(null);
            qualifiedName = Optional.ofNullable(psiClass)
                    .map(PsiClass::getQualifiedName)
                    .orElse(null);
            sqlType = basicTypeRegistry.getRegisteredType(qualifiedName);
            if (sqlType == null) {
                //检查是否是枚举
                qualifiedName = Optional.ofNullable(psiClass)
                        .map(PsiClass::getSuperClass)
                        .map(PsiClass::getQualifiedName)
                        .orElse(null);
                if (Objects.equals("java.lang.Enum",qualifiedName)) {
                    //枚举转字符串类型
                    sqlType = StringType.INSTANCE;
                }
                if (sqlType == null) {
                    log.warn("类型不存在:" + qualifiedName);
                }
            }
        }
        if (allowableTypes.contains(qualifiedName)) {
//                String dbType = dialect.getDbType(qualifiedName);
            if (sqlType == null) {
                return null;
            }
            String scale = findDocByTag(psiField, SCALE_TAG);
            String length = findDocByTag(psiField, LENGTH_TAG);
            boolean primaryKey = hasTag(psiField, PRIMARY_KEY_TAG);
            boolean index = hasTag(psiField, INDEX_TAG);
            boolean unique = hasTag(psiField, UNIQUE_TAG);
            String comment = findRestDoc(psiField);
            String defaultValue = findDocByTag(psiField, DEFAULT_TAG);
            boolean nonNull = hasTag(psiField, NON_NULL_TAG);
            Column column = new Column(columName, sqlType, length, scale, !nonNull, defaultValue, comment);
            dto.addColumn(column, index, unique, primaryKey);
            return column;
        }
        log.warn("unknown column, name={}", name);
        return null;
    }

    private String findRestDoc(PsiField psiField) {
        return Optional.ofNullable(psiField)
                .map(PsiField::getDocComment)
                .map(doc -> {
                    PsiElement[] elements = doc.getDescriptionElements();
                    if (elements != null && elements.length > 0) {
                        return Arrays.stream(elements);
                    }
                    return null;
                })
                .map(stream -> {
                    return stream.filter(ele -> ele instanceof PsiDocToken)
                            .findFirst()
                            .map(PsiDocToken.class::cast)
                            .orElse(null);
                })
                .map(PsiDocToken::getText)
                .orElse(null);
    }


    private String findDocByTag(PsiJavaDocumentedElement psiDocEle, String tag) {
        return getPsiDocTag(psiDocEle, tag)
                .map(PsiDocTag::getValueElement)
                .map(PsiDocTagValue::getText)
                .orElse(null);
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    @NotNull
    private Optional<PsiDocTag> getPsiDocTag(PsiJavaDocumentedElement psiDocEle, String tag) {
        return Optional.ofNullable(psiDocEle)
                .map(PsiJavaDocumentedElement::getDocComment)
                .map(doc -> doc.findTagByName(tag));
    }

    public boolean hasPrimaryKey() {
        return primaryKey != null;
    }

    private boolean hasTag(PsiJavaDocumentedElement psiDocEle, String tag) {
        return getPsiDocTag(psiDocEle, tag)
                .isPresent();
    }


    private String detectTableName(PsiClass psiClass) {
        PsiAnnotation annotation = psiClass.getAnnotation("com.baomidou.mybatisplus.annotation.TableName");
        String tableNameStr = Optional.ofNullable(annotation)
                .map(anno -> anno.findAttributeValue("value"))
                .map(value -> StringUtils.substringBetween(value.getText(), "\""))
                .orElse(null);
        if (StringUtils.isNotBlank(tableNameStr)) {
            return tableNameStr;
        }
        tableNameStr = findDocByTag(psiClass, TABLE_TAG);
        if (StringUtils.isNotBlank(tableNameStr)) {
            return tableNameStr;
        }
        String className = psiClass.getName();
        tableNameStr = StringUtil.toUnderScoreCase(className, false);
        return tableNameStr;
    }

    private TableColumns parseField(PsiClass targetClass) {
        TableColumns dto = new TableColumns(htable, this);

        PsiField[] allFields = targetClass.getAllFields();
        for (PsiField psiField : allFields) {
            psiFieldToColumn(psiField, dto);
        }
        return dto;
    }

    private Column getColumn(String name) {
        return getColumnMap().get(name);
    }

    private Map<String, Column> getColumnMap() {
        if (columnMap == null) {
            columnMap = getColumnList().stream()
                    .collect(Collectors.toMap(Column::getName, Function.identity(), (f, s) -> s));
        }
        return columnMap;
    }

}
