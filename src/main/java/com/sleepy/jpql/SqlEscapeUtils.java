package com.sleepy.jpql;

import org.apache.commons.lang3.StringUtils;

/**
 * 用于过滤sql的特殊字符等
 *
 * @author gehoubao
 * @create 2020-01-18 11:24
 **/
public class SqlEscapeUtils {
    public static String escapeSingleQuote(String name) {
        return name == null ? null : StringUtils.replace(name, "'", "''");
    }

    public static String escapeLike(String name) {
        if (name == null) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (char c : name.toCharArray()) {
            switch (c) {
                case '/':
                    stringBuilder.append("//");
                    break;
                case '%':
                    stringBuilder.append("/%");
                    break;
                case '_':
                    stringBuilder.append("/_");
                    break;
                default:
                    stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }
}