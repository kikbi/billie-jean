package derek.tool.sql.util;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.intellij.ide.util.PropertiesComponent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;


/**
 * @author Derek
 * @date 2022/10/31
 */
@Slf4j
public class IdeaConfigUtil {

    private static final PropertiesComponent PROPERTIES_COMPONENT = PropertiesComponent.getInstance();
    private static final JsonMapper JSON_MAPPER = JacksonUtil.json();
    static {
        JSON_MAPPER.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
    }

    public static String getString(String key) {
        // key
        // key defaultValue
        return PROPERTIES_COMPONENT.getValue(key);
    }

    /**
     * 复杂对象保存为json字符串
     * @param key
     * @param value
     */
    public static void save(String key, Object value) {
        if (value instanceof String) {
            save(key, (String) value);
        } else if (value instanceof Integer) {
            save(key, (int) value);
        } else if (value instanceof Boolean) {
            save(key, (boolean) value);
        } else if (value instanceof Float) {
            save(key, (float) value);
        } else {
            try {
                String s = JSON_MAPPER.writeValueAsString(value);
                save(key,s);
            } catch (JsonProcessingException e) {
                log.error("write json failed;",e);
            }
        }
    }

    /**
     * key为类名
     * @param value
     */
    public static void save(@NonNull Object value) {
        String key = value.getClass().getName();
        save(key,value);
    }
    public static <T> T get(@NonNull String key){
        String json = getString(key);
        if (json == null) {
            return null;
        }
        try {
            return (T)JSON_MAPPER.readValue(json, Object.class);
        } catch (JsonProcessingException e) {
            log.error("parse json error;",e);
        }
        return null;
    }

    public static <T> T get(@NonNull Class<T> key){

        String keyName = key.getName();
        String json = getString(keyName);
        try {
            return JSON_MAPPER.readValue(json, key);
        } catch (JsonProcessingException e) {
            log.error("parse json error;",e);
        }
        return null;
    }




    public static void save(String key, String value) {
        // key value
        PROPERTIES_COMPONENT.setValue(key, value);
    }

    public static void save(String key, int value) {
        // key value defaultValue
        PROPERTIES_COMPONENT.setValue(key, value, 0);
    }

    public static void save(String key, boolean value) {
        // key value
        // key value defaultValue
        PROPERTIES_COMPONENT.setValue(key, value);
    }

    public static void save(String key, float value) {
        // key value defaultValue
        PROPERTIES_COMPONENT.setValue(key, value, 0.0f);
    }

}

