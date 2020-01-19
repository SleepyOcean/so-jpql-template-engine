package com.sleepy.jpql;

/**
 * @author gehoubao
 * @create 2020-01-18 11:17
 **/
public class TemplateHelper {
    public String toLike(Object o) {
        return this.internalLike(o, true, true);
    }

    private String internalLike(Object o, boolean start, boolean end) {
        StringBuilder stringBuilder = new StringBuilder("'");
        if (start) {
            stringBuilder.append("%");
        }
        stringBuilder.append(SqlEscapeUtils.escapeSingleQuote(SqlEscapeUtils.escapeLike(o == null ? "" : o.toString())));
        if (end) {
            stringBuilder.append("%");
        }
        stringBuilder.append("'");
        stringBuilder.append(" escape '/'");
        return stringBuilder.toString();

    }

    public String toStart(Object o) {
        return this.internalLike(o, true, false);
    }

    public String toEnd(Object o) {
        return this.internalLike(o, false, true);
    }
}