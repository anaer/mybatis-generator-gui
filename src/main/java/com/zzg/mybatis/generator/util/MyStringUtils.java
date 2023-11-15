package com.zzg.mybatis.generator.util;

import org.dromara.hutool.core.array.ArrayUtil;
import org.dromara.hutool.core.text.NamingCase;
import org.dromara.hutool.core.text.StrUtil;

/**
 * Created by Owen on 6/18/16.
 */
public class MyStringUtils {

    private static final String[] ignoreTablePrefix = { "t_" };

    private static final String[] MYSQL_KEYWORD = { "type", "timestamp" };

    /**
     * 下划线转驼峰
     * 如果存在需忽略的表前缀, 则进行忽略
     * convert string from slash style to camel style, such as my_course will convert to MyCourse
     *
     * @param str
     * @return
     */
    public static String dbStringToCamelStyle(String str) {

        // 判断是否存在需忽略的表前缀, 如果存在则去除
        if (ignoreTablePrefix != null && ignoreTablePrefix.length > 0) {
            for (String prefix : ignoreTablePrefix) {
                if (StrUtil.startWith(str, prefix)) {
                    str = StrUtil.removePrefix(str, prefix);
                    break;
                }
            }
        }

        return NamingCase.toPascalCase(str);
    }

    /**
     * 判断字段名 是否需要包装
     * @param columnName
     * @return
     */
    public static String wrapColumnName(String columnName) {
        String result = columnName;

        if (ArrayUtil.contains(MYSQL_KEYWORD, columnName)) {
            result = "`" + columnName + "`";
        }
        return result;
    }
}
