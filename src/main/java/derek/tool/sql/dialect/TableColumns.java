package derek.tool.sql.dialect;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.mapping.PrimaryKey;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 一个表的列信息
 */
public class TableColumns {

    org.hibernate.mapping.Table htable;

    Table table;

    List<Column> columns;
    /**
     * 主键列
     */
    Column pkColumn;
    /**
     * 单列索引列
     */
    List<Index> singleIdxColumns = new ArrayList<>();

    PrimaryKey tbPk;

    List<Index> tbIdx;

    Map<String, Column> colMap;

    public TableColumns(org.hibernate.mapping.Table htable, Table table) {
        this.htable = htable;
        this.table = table;
    }

    TableColumns addColumn(Column column) {
        if (columns == null) {
            columns = new ArrayList<>();
        }
        columns.add(column);
        return this;
    }

    TableColumns addColumn(Column column, boolean idx) {
        addColumn(column);
        if (idx) {
            addIdxColumn(column,false);
        }
        return this;
    }

    TableColumns addColumn(Column column, boolean idx, boolean unique, boolean pk) {
        addColumn(column);
        if (idx||unique) {
            addIdxColumn(column,unique);
        }
        if (pk) {
            setPKColumn(column);
        }
        return this;
    }

    TableColumns addIdxColumn(Column column, boolean unique) {
        Index index = new Index(unique, table,column);
        singleIdxColumns.add(index);
        return this;
    }

    TableColumns setPKColumn(Column column) {
        pkColumn = column;
        return this;
    }

    public void setTbPK(PrimaryKey tbPk) {
        this.tbPk = tbPk;
    }

    public void setTbIdx(List<Index> tbIdx) {
        this.tbIdx = tbIdx;
    }

    public PrimaryKey getFinalPk() {
        if (colMap == null) {
            initMap();
        }
        if (tbPk != null) {
            boolean valid = checkPkColumns(tbPk.getColumns());
            if (valid) {
                return tbPk;
            }
            return null;
        }
        if (pkColumn != null) {
            PrimaryKey primaryKey = new PrimaryKey(htable);
            org.hibernate.mapping.Column column = new org.hibernate.mapping.Column(pkColumn.getName());
            primaryKey.addColumn(column);
            return primaryKey;
        }
        return null;
    }

    private boolean checkPkColumns(List<org.hibernate.mapping.Column> tbPkColumns) {
        for (org.hibernate.mapping.Column tbPkColumn : tbPkColumns) {
            Column column = colMap.get(tbPkColumn.getName());
            if (column == null) {
                return false;
            }
        }
        return true;
    }

    public List<Index> getFinalIndexes() {
        if (colMap == null) {
            initMap();
        }
        List<Index> indexList = new ArrayList<>();
        Set<String> singleIdxColumnSet = new HashSet<>();
        //如果索引只有一列则添加到单列索引中，后续字段上的索引标签就不再添加
        if (CollectionUtils.isNotEmpty(tbIdx)) {
            List<Index> validIndex = checkIndexColumns(tbIdx);
            if (validIndex != null) {
                for (Index index : validIndex) {
                    if (index.singleIndex()) {
                        if (singleIdxColumnSet.contains(index)) {
                            continue;
                        }
                        singleIdxColumnSet.add(index.getColumns().get(0).getName());
                    }
                    indexList.add(index);
                }
            }
        }
        for (Index singleIdxColumn : singleIdxColumns) {
            String columnName = singleIdxColumn.getColumns().get(0).getName();
            if (singleIdxColumnSet.contains(columnName)) {
                continue;
            }
            indexList.add(singleIdxColumn);
        }
        return indexList;
    }

    private List<Index> checkIndexColumns(List<Index> tbIdx) {
        return tbIdx.stream()
                .filter(idx -> {
                    List<Column> idxColumns = idx.getColumns();
                    for (Column column : idxColumns) {
                        Column existColumn = colMap.get(column.getName());
                        if (existColumn == null) {
                            return false;
                        }
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }

    private void initMap() {
        colMap = columns.stream()
                .collect(Collectors.toMap(Column::getName, Function.identity(), (f, s) -> s));
    }
}
