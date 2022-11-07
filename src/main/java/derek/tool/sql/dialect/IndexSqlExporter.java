package derek.tool.sql.dialect;

import derek.tool.sql.util.StringUtil;
import org.hibernate.dialect.Dialect;

import java.util.List;

public class IndexSqlExporter {

    public static String getSqlCreateStrings(Index index, Dialect dialect, boolean tableUpperCase) {
        String indexNameForCreation = index.getName(dialect,tableUpperCase);
        String tableName = index.getTable().getTableName();
        StringBuilder buf = new StringBuilder()
                .append("create ");
        if (index.isUnique()) {
            buf.append("unique ");
        }
        buf.append("index ")
                .append(indexNameForCreation)
                .append(" on ")
                .append(StringUtil.toUnderScoreCase(tableName,tableUpperCase))
                .append(" (");

        boolean first = true;
        List<Column> columns = index.getColumns();
        for (Column column : columns) {
            if (first) {
                first = false;
            } else {
                buf.append(", ");
            }
            buf.append((column.getName()));
        }
        buf.append(")");
        return buf.toString();
    }
}
