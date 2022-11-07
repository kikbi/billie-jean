package derek.tool.sql.function;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import derek.tool.sql.settings.CustomDialectDTO;
import derek.tool.sql.util.JacksonUtil;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.net.URL;

/**
 * @author Derek
 * @date 2022/10/31
 */
public class XmlPaserTests {

    @Test
    public void test() throws Exception {
        String poem = "You were right there for me." +
                "In my dreams I'll always see you soar above the sky.";

        JsonMapper json = JacksonUtil.json();
        json.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        URL resource = getClass().getClassLoader().getResource("/custom_dialect.xml");
        File file = FileUtils.getFile(resource.getFile());
        XmlMapper xm = JacksonUtil.xml();
        CustomDialectDTO result = xm.readValue(file, CustomDialectDTO.class);
        String s = json.writeValueAsString(result);
        System.out.println(s);
        Object o = json.readValue(s, Object.class);
        System.out.println(o.getClass());
        System.out.println(o);
    }

}
