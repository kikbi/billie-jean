package derek.tool.sql.dialect;

import derek.tool.sql.settings.CustomDialectDTO;
import derek.tool.sql.settings.DbInfoDTO;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.TypeNames;

import java.lang.reflect.Field;
import java.util.*;

/**
 * 代理方言，用于自定义方言生成
 * @author Derek
 * @date 2022/9/13
 */
@Slf4j
public class DelegateDialect extends Dialect {

    private List<SqlType> types;
    private final DbInfoDTO dbInfo;
    private Dialect extendDb;


    public DelegateDialect(CustomDialectDTO customDialectDTO) {
        dbInfo = customDialectDTO.getDbInfo();
        types = Optional.ofNullable(customDialectDTO.getTypes()).orElse(Collections.emptyList());
        DbInfoDTO extendDbDTO = customDialectDTO.getExtendDb();
        //先处理继承方言,继承方言和当前方言不能是相同数据库方言
        if (extendDbDTO != null && !Objects.equals(extendDbDTO.getDatabaseName(),dbInfo.getDatabaseName())) {
            DerekDialectResolver derekDialectResolver = new DerekDialectResolver();
            extendDb = derekDialectResolver.resolveDialect(extendDbDTO);
            replaceDefault();
        }
        //再处理自定义类型
        for (SqlType type : types) {
            register(type);
        }
    }

    /**
     * 如果有继承的数据库方言，则将继承方言替换默认的一些类型配置
     */
    private void replaceDefault() {
        if (extendDb == null) {
            return;
        }
        try {
            Class<? extends Dialect> dialectClass = extendDb.getClass();
            Field declaredField = dialectClass.getDeclaredField("typeNames");
            declaredField.setAccessible(true);
            TypeNames exTypeNames = (TypeNames) declaredField.get(extendDb);
            Class<TypeNames> typeNamesClass = TypeNames.class;
            Field defaultsField = typeNamesClass.getDeclaredField("defaults");
            Map<Integer, String> defaultsType = (Map<Integer, String>) defaultsField.get(exTypeNames);
            if (defaultsType != null) {
                for (Map.Entry<Integer, String> entry : defaultsType.entrySet()) {
                    registerColumnType(entry.getKey(), entry.getValue());
                }
            }
            Field weightedField = typeNamesClass.getDeclaredField("weighted");
            Map<Integer, Map<Integer, String>> weightedType =
                    (Map<Integer, Map<Integer, String>>) weightedField.get(exTypeNames);
            if (weightedType != null) {
                for (Map.Entry<Integer, Map<Integer, String>> en : weightedType.entrySet()) {
                    Integer code = en.getKey();
                    for (Map.Entry<Integer, String> wen : en.getValue()
                            .entrySet()) {
                        Integer cap = wen.getKey();
                        String value = wen.getValue();
                        registerColumnType(code, cap, value);
                    }
                }
            }
        } catch (Exception e) {
            log.error("extend database dialect could not found", e);
        }
    }

    private void register(SqlType type) {
        Integer code = type.jdbcTypeCode();
        if (code == null) {
            return;
        }
        Integer capacity = type.getCapacity();
        String typePattern = type.getTypePattern();
        if (capacity == null) {
            registerColumnType(code, typePattern);
            return;
        }
        registerColumnType(code, capacity, typePattern);
    }

}
