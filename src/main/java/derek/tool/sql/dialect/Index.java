package derek.tool.sql.dialect;

import derek.tool.sql.util.StringUtil;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.dialect.Dialect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 建表索引
 */
@Data
public class Index {

    /**
     * 索引名
     */
    private String name;

    /**
     * 列名
     */
    private List<Column> columns;

    /**
     * 是否唯一索引
     */
    private boolean unique;

    /**
     * 索引所属表
     */
    private Table table;


    public Index(boolean unique, Table table,Column... columns) {
        this.unique = unique;
        this.table = table;
        if (columns != null) {
            this.columns = new ArrayList<>();
            this.columns.addAll(Arrays.asList(columns));
        }

    }

    /**
     * 是否是单列索引
     * @return
     */
    public boolean singleIndex(){
        return columns.size() == 1;
    }

    public void addColumn(Column column){
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.add(column);
    }

    public String getName(Dialect dialect,boolean tableUpperCase){
        String prefix = "idx";
        String delimiter = "_";
        String tableName = StringUtil.toUnderScoreCase(table.getTableName(),tableUpperCase);
        StringBuilder sb = new StringBuilder();
        sb.append(tableName).append(delimiter).append("||");
        for (Column column : columns) {
            String columnName = column.getName();
            sb.append(delimiter).append(columnName);
        }
        String semiIndex = sb.toString();
        String abbreviation = StringUtil.abbreviate(semiIndex, "_", 26);
        String[] split = StringUtils.split(abbreviation, "|");

        return split[0]+prefix+split[1];
    }
}
