package derek.tool.sql.dialect;

import derek.tool.sql.util.StringUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.relational.QualifiedName;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.Table;

import java.util.*;

/**
 * 生成sql
 *
 * @author Derek
 * @date 2022/6/17
 */
public class TableSqlExporter {

    private final Dialect dialect;

    public TableSqlExporter(Dialect dialect) {
        this.dialect = dialect;
    }

    /**
     * 建表语句
     *
     * @return
     */
    public String sqlCreationStatement() {
        return null;
    }

    protected String tableCreateString(boolean hasPrimaryKey) {
        return hasPrimaryKey ? dialect.getCreateTableString() : dialect.getCreateMultisetTableString();

    }

    public String sqlCreationStatement(derek.tool.sql.dialect.Table table, Dialect dialect) {
        return sqlCreationStatement(table, dialect, false, false, false);
    }

    public String sqlCreationStatement(derek.tool.sql.dialect.Table table, Dialect dialect,
                                       boolean tableUpperCase, boolean columnUpperCase, boolean pretty) {
        String tableName= table.getTableName();
        StringBuilder buf = new StringBuilder(dialect.getCreateTableString())
                .append(' ')
                .append(StringUtil.toUnderScoreCase(tableName, tableUpperCase))
                .append(" (");

        StringUtil.appendIf(pretty, buf, "\n");

        boolean isFirst = true;
        List<derek.tool.sql.dialect.Column> columnList = table.getColumnList();
        for (derek.tool.sql.dialect.Column col : columnList) {
            if (isFirst) {
                isFirst = false;
            } else {
                buf.append(", ");
                StringUtil.appendIf(pretty, buf, "\n");
            }
            String colName;
            if (columnUpperCase) {
                colName = col.getName().toUpperCase(Locale.ROOT);
            } else {
                colName = col.getName().toLowerCase(Locale.ROOT);
            }
            buf.append(colName).append(' ');
            //主键第一列是当前列
            boolean pkIdentify = Optional.of(table)
                    .map(derek.tool.sql.dialect.Table::getPrimaryKey)
                    .map(pk -> pk.getColumn(0))
                    .map(Column::getName)
                    .map(cn -> Objects.equals(cn, col.getName()))
                    .orElse(false);
            if (pkIdentify) {
                // to support dialects that have their own identity data type
                if (dialect.getIdentityColumnSupport().hasDataTypeInIdentityColumn()) {
                    buf.append(col.getSqlType(dialect));
                }
                try {
                    String identityColumnString =
                            dialect.getIdentityColumnSupport().getIdentityColumnString(col.getSqlTypeCode());
                    buf.append(' ')
                            .append(identityColumnString);
                } catch (Exception e) {
//                    e.printStackTrace();
                }

            } else {
                buf.append(col.getSqlType(dialect));

                String defaultValue = col.getDefaultValue();
                if (defaultValue != null) {
                    buf.append(" default ").append(defaultValue);
                }

                if (col.isNullable()) {
                    buf.append(dialect.getNullColumnString());
                } else {
                    buf.append(" not null");
                }

            }
            String columnComment = col.getComment();
            if (columnComment != null) {
                buf.append(dialect.getColumnComment(columnComment.trim()));
            }
        }
        if (table.hasPrimaryKey()) {
            buf.append(", ");
            if (pretty) {
                buf.append("\n");
            }
            buf.append(table.getPrimaryKey().sqlConstraintString(dialect));
        }
        StringUtil.appendIf(pretty, buf, "\n");
        buf.append(");");
        String comment = table.getComment();
        if (comment != null) {
            buf.append(dialect.getTableComment(comment));
        }
        String executeTag = executeTag(dialect);
        StringUtil.appendIf(StringUtils.isNotBlank(executeTag), buf,"\n"+executeTag);
        buf.append("\n");
        List<Index> indexList = table.getIndexList();
        if (CollectionUtils.isNotEmpty(indexList)) {
            for (Index index : indexList) {
                String indexCreationStr = IndexSqlExporter.getSqlCreateStrings(index, dialect,tableUpperCase);
                buf.append(indexCreationStr).append(";\n");
            }
        }
        buf.append(executeTag);
        return buf.toString();
    }

    private String executeTag(Dialect dialect) {
        if (dialect instanceof Oracle8iDialect || dialect instanceof Oracle9Dialect) {
            return "/";
        }
        if (dialect instanceof SQLServerDialect) {
            return "GO";
        }
        return "";
    }

    protected void applyTableTypeString(StringBuilder buf) {
        buf.append(dialect.getTableTypeString());
    }

    protected void applyComments(Table table, QualifiedName tableName, List<String> sqlStrings) {
        if (dialect.supportsCommentOn()) {
            if (table.getComment() != null) {
                sqlStrings.add("comment on table " + tableName + " is '" + table.getComment() + "'");
            }
            Iterator iter = table.getColumnIterator();
            while (iter.hasNext()) {
                org.hibernate.mapping.Column column = (Column) iter.next();
                String columnComment = column.getComment();
                if (columnComment != null) {
                    sqlStrings.add("comment on column " + tableName + '.' + column.getQuotedName(dialect) + " is '" + columnComment + "'");
                }
            }
        }
    }
}
