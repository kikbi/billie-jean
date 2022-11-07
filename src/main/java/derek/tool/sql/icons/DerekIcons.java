package derek.tool.sql.icons;

import com.intellij.openapi.util.IconLoader;
import com.intellij.util.ReflectionUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import java.net.URL;

/**
 * @author Derek
 * @date 2022/10/31
 */
@Slf4j
public class DerekIcons {


//    public static Icon BJ_ACTION = tryLoad(
//            "image/database-icon16-2.png"
//    );

    public static  Icon tryLoad(String... paths) {
        for (String path : paths) {
            try {
                return getIcon(path);
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static Icon getIcon(String path) {
        Class<?> callerClass = null;
        try {
            callerClass = ReflectionUtil.getGrandCallerClass();
        } catch (Exception e) {
            throw new IllegalArgumentException("invalidated path");
        }
        if (callerClass == null) {
            throw new IllegalArgumentException("invalidated path");
        }
        return IconLoader.findIcon(path, callerClass);
    }

    public static Icon tryLoadByUrl(URL... paths) {
        for (URL path : paths) {
            try {
                return IconLoader.findIcon(path);
            } catch (Exception e) {
                log.error("could not find icon {}", path);
            }
        }
        return null;
    }

    public static Icon iconOnly(AbstractButton component,Icon icon) {
        if (icon == null || component == null) {
            return null;
        }
        component.setIcon(icon);
        component.setText("");
        return icon;
    }

}
