package derek.tool.sql.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import java.util.function.Function;

/**
 * @author Derek
 * @date 2022/6/14
 */
public abstract class OptionalUtils {

    public static Function<String, Integer> STR_TO_INT = str -> {
        if (!StringUtils.isNumeric(str)) {
            return null;
        }
        return Integer.parseInt(str);
    };

    /**
     * 静默映射，如果有异常返回空
     * @param func
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Function<T, R> silentMap(Function<T, R> func) {
        return t -> {
            R r = null;
            try {
                r = func.apply(t);
            } catch (Exception e) {
                //silence
            }
            return r;
        };
    }

    /**
     * 静默映射，如果有异常返回空
     * @param func
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Function<T, R> silentMap(Function<T, R> func, Logger log) {
        return t -> {
            R r = null;
            try {
                r = func.apply(t);
            } catch (Exception e) {
                log.warn("静默映射异常,参数={}",t);
            }
            return r;
        };
    }

    /**
     * 不抛出异常，异常时使用后一种映射方式,后一种异常则映射为null
     * @param func
     * @param elseFunc
     * @param <T>
     * @param <R>
     * @return
     */
    public static <T, R> Function<T, R> silentMapOrElse(Function<T, R> func,Function<T,R> elseFunc) {
        return t -> {
            R r = null;
            try {
                r = func.apply(t);
            } catch (Exception e) {
                try {
                    r = elseFunc.apply(t);
                } catch (Exception e1) {
                    //else
                }
            }
            return r;
        };
    }
}
