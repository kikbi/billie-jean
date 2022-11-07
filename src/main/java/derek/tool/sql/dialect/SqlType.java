package derek.tool.sql.dialect;

import lombok.Data;

import java.lang.reflect.Field;
import java.sql.Types;

/**
 * @author Derek
 * @date 2022/10/31
 */
@Data
public class SqlType {

    private String jdbcType;
    private String typePattern;
    private Integer capacity;

    public Integer jdbcTypeCode(){
        Class<Types> typesClass = Types.class;
        try {
            Field declaredField = typesClass.getDeclaredField(jdbcType);
            return (int)declaredField.get(null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
