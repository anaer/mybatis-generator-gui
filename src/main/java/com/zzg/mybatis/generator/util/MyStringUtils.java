package com.zzg.mybatis.generator.util;

import cn.hutool.core.text.NamingCase;
import cn.hutool.core.util.StrUtil;

/**
 * Created by Owen on 6/18/16.
 */
public class MyStringUtils {

    private static final String[] ignoreTablePrefix = { "t_" };

    /**
     *
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

}
