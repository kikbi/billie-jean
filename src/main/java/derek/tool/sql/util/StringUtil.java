package derek.tool.sql.util;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Derek
 * @date 2022/6/9
 */
public class StringUtil {


    /**
     * 转驼峰命名
     *
     * @param label
     * @param isCapitalized
     * @return
     */
    public static String toCamelCase(String label, boolean isCapitalized) {
        if (StringUtils.isBlank(label)) {
            return label;
        }
        StringBuilder sb = new StringBuilder();
        String[] sarr = split(label);
        boolean first = true;
        for (String str : sarr) {
            str = str.toLowerCase();
            char c = str.charAt(0);
            if (first) {
                if (isCapitalized) {
                    c = Character.toUpperCase(c);
                }
            } else {
                c = Character.toUpperCase(c);
            }
            sb.append(c);
            str = str.substring(1);
            sb.append(str);
            first = false;
        }
        return sb.toString();
    }

    /**
     * eg: AbCd -> A_B_C_D or a_b_c_d
     *
     * @param label
     * @param isCapitalized
     * @return
     */
    public static String toUnderScoreCase(String label, boolean isCapitalized) {
        return toTokenJoin(label, isCapitalized, "_");
    }

    /**
     * eg: AbCd -> A-B-C-D or a-b-c-d
     *
     * @param label
     * @param isCapitalized
     * @return
     */
    public static String toSnakeCase(String label, boolean isCapitalized) {
        return toTokenJoin(label, isCapitalized, "-");
    }

    @Nullable
    private static String toTokenJoin(String label, boolean isCapitalized, String token) {
        if (StringUtils.isBlank(label)) {
            return label;
        }
        String[] sarr = split(label);
        return Arrays.stream(sarr)
                .map(str -> {
                    if (isCapitalized) {
                        return str.toUpperCase();
                    }
                    return str.toLowerCase();
                })
                .collect(Collectors.joining(token));
    }

    private static String[] split(String label) {
        if (StringUtils.contains(label, "_")) {
            return StringUtils.splitByWholeSeparator(label, "_");
        }
        if (StringUtils.contains(label, "-")) {
            return StringUtils.splitByWholeSeparator(label, "-");
        }
        return splitByCharacterType(label,true,true);
    }

    /**
     * @see org.apache.commons.lang3.StringUtils
     * @param str
     * @param camelCase 是否按照驼峰拆分
     * @param ignoreNumber
     * @return
     */
    private static String[] splitByCharacterType(String str, boolean camelCase, boolean ignoreNumber) {
        if (str == null) {
            return null;
        }
        if (str.isEmpty()) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        char[] c = str.toCharArray();
        List<String> list = new ArrayList<>();
        int tokenStart = 0;
        int currentType = Character.getType(c[tokenStart]);
        for (int pos = tokenStart + 1; pos < c.length; pos++) {
            int ct = Character.getType(c[pos]);
            int type = (ignoreNumber && ct == Character.DECIMAL_DIGIT_NUMBER)?Character.LOWERCASE_LETTER:ct;
            if (type == currentType) {
                continue;
            }
            if (camelCase && type == Character.LOWERCASE_LETTER && currentType == Character.UPPERCASE_LETTER) {
                int newTokenStart = pos - 1;
                if (newTokenStart != tokenStart) {
                    list.add(new String(c, tokenStart, newTokenStart - tokenStart));
                    tokenStart = newTokenStart;
                }
            } else {
                list.add(new String(c, tokenStart, pos - tokenStart));
                tokenStart = pos;
            }
            currentType = type;
        }
        list.add(new String(c, tokenStart, c.length - tokenStart));
        return list.toArray(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public static void appendIf(boolean condition, StringBuilder buf, String s) {
        if (condition) {
            buf.append(s);
        }
    }

    public static String addIf(boolean condition, CharSequence cs, String s) {
        if (condition) {
            return cs + s;
        }
        return cs.toString();
    }

    /**
     * 缩写
     * 如：abc.def.ghi.jkl.mnoPqr -> a.d.g.j.m.P
     * @param str
     * @param max
     * @return
     */
    public static String abbreviate(String str, int max) {
        int length = str.length();
        if (length <= max) {
            return str;
        }
        String[] split = org.apache.commons.lang3.StringUtils.splitByCharacterTypeCamelCase(str);
        return abbreviate(split, ".", max, false);
    }

    /**
     * 缩写
     * 如：abc.def.ghi.jkl.mnopqr -> a.d.g.j.mopqr
     * @param str
     * @param separator
     * @param max
     * @return
     */
    public static String abbreviate(String str, String separator, int max) {
        return abbreviate(str, separator, max, false);
    }

    /**
     * 缩写
     * 如：abc.def.ghi.jkl.mnopqr -> a.d.g.j.mopqr
     * @param str
     * @param separator
     * @param max
     * @param ignoreTail 是否忽略最后一段，如果false，则最后一段也会缩写为1字符
     * @return
     */
    public static String abbreviate(String str, String separator, int max, boolean ignoreTail) {
        int length = str.length();
        if (length <= max) {
            return str;
        }
        String[] split = org.apache.commons.lang3.StringUtils.split(str, separator);
        return abbreviate(split, separator, max, ignoreTail);
    }

    /**
     * 分组缩写合并
     * 如：abc.def.ghi.jkl.mnopqr -> a.d.g.j.mopqr
     *
     * @param split 分组字符串
     * @param separator 分隔器
     * @param max 最大长度
     * @param ignoreTail 是否忽略最后一段，如果false，则最后一段也会缩写为1字符
     * @return 缩写后的字符串
     */
    public static String abbreviate(String[] split, String separator, int max, boolean ignoreTail) {
        int[] counter = new int[split.length];
        for (int i = split.length - 1; i >= 0; i--) {
            int rightLength = 0;
            if (i < split.length - 1) {
                rightLength = counter[i + 1] + 1;
            }
            counter[i] = rightLength + split[i].length();
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < split.length; i++) {
            String s = split[i];
            if (s.length() <= 1) {
                continue;
            }
            if (i != 0) {
                sb.append(separator);
            }
            if (sb.length() + counter[i] <= max || (ignoreTail && i == split.length - 1)) {
                sb.append(s);
            } else {
                sb.append(s.charAt(0));
            }
        }
        return sb.toString();
    }
}
