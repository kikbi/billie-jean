package derek.tool.sql.dialect;

import derek.tool.sql.util.OptionalUtils;
import lombok.Getter;
import org.hibernate.dialect.Dialect;
import org.hibernate.type.Type;

import java.util.Optional;

/**
 * 数据库列
 *
 * @author Derek
 * @date 2022/6/9
 */
@Getter
public class Column {

    private final String name;

    private Type type;

    private String comment;

    private Integer length = 255;

    private boolean hasScale;

    private Integer precision=19;

    private Integer scale =2;

    private boolean nullable = true;

    private String defaultValue;

    private String sqlType;

    public Column(String name, Type type, Integer length){
        this.name = name;
        this.type = type;
        this.length = Optional.ofNullable(length).orElse(255);
    }

    public Column(String name, Type type, Integer length, Integer scale){
        this.name = name;
        this.type = type;
        this.length = Optional.ofNullable(length).orElse(255);
        this.scale = Optional.ofNullable(scale).orElse(2);
    }

    public Column(String name, Type type, String length, String scale,
                  Boolean nullable, String defaultValue, String comment) {
        this.name = name;
        this.type = type;

        this.length = Optional.ofNullable(length).map(OptionalUtils.STR_TO_INT).orElse(255);
        this.scale = Optional.ofNullable(scale).map(OptionalUtils.STR_TO_INT).orElse(2);
        hasScale = scale != null;
        this.nullable = nullable;
        this.defaultValue = defaultValue;
        this.comment = comment;
    }

    public Column(String name) {
        this.name = name;
    }

    public Column(String name, Type type) {
        this.name = name;
        this.type = type;
    }


    public String getSqlType(Dialect dialect) {
        if (sqlType == null) {
            int code = type.sqlTypes(null)[0];
            sqlType = dialect.getTypeName(code, length, precision, scale);
        }
        return sqlType;

    }

    public int getSqlTypeCode(){
        return Optional.ofNullable(type)
                .map(type -> type.sqlTypes(null))
                .filter(arr -> arr.length > 0)
                .map(arr -> arr[0])
                .orElse(0);
    }


}
