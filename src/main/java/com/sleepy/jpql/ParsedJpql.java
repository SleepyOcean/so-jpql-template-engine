package com.sleepy.jpql;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

/**
 * 解析后的Jpql信息
 *
 * @author gehoubao
 * @create 2020-01-18 11:01
 **/
@Slf4j
public class ParsedJpql extends Jpql {
    private String parsed;
    private Map<String, Object> parameterMap;

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }

    public String getParsed() {
        return parsed;
    }

    public void setParsed(String parsed) {
        this.parsed = parsed;
    }

    public String getExecutableSql() {
        String jpqlStr = this.getParsed();
        try {
            Map<String, Object> params = this.getParameterMap();
            for (int i = params.keySet().size() - 1; i >= 0; i--) {
                String key = "argument" + i;
                Object obj = params.get(key);
                if (obj instanceof Date) {
                    jpqlStr = jpqlStr.replaceAll(":" + key, "\'" + toDateTimeStr((Date) obj) + "\'");
                } else if (obj instanceof String) {
                    jpqlStr = jpqlStr.replaceAll(":" + key, "\'" + obj + "\'");
                } else if (obj instanceof Collection) {
                    StringBuilder list = new StringBuilder();
                    if (((Collection) obj).size() > 0) {
                        if (((Collection) obj).iterator().next() instanceof String) {
                            for (String o : ((Collection<String>) obj)) {
                                list.append("\'" + o + "\',");
                            }
                            list.deleteCharAt(list.length() - 1);
                        } else {
                            list.append(obj.toString().substring(1, ((Collection) obj).toString().length() - 1));
                        }
                    }
                    jpqlStr = jpqlStr.replaceAll(":" + key, list.toString());
                } else {
                    jpqlStr = jpqlStr.replaceAll(":" + key, obj.toString());
                }
            }
            if (jpqlStr.contains("like") && (jpqlStr.contains(".") || jpqlStr.contains("*"))) {
                this.setParsed(jpqlStr);
            }

            // 测试CDB预留自定义SQL解析处理
            if (jpqlStr.contains("select * from so_article limit 1")) {
                jpqlStr = jpqlStr.substring(jpqlStr.indexOf("'") + 1, jpqlStr.lastIndexOf("'"));
                this.setParsed(jpqlStr);
            }
            log.info("{\"QueryId\": \"{}\"," + "\"SQL\": \"{}\"}", this.getId(), jpqlStr.replaceAll("\\s+", " "));
        } catch (Exception e) {
            log.error("SQL转换失败：" + e.getMessage());
        }
        return jpqlStr.replaceAll("\\s+", " ");
    }

    private static String toDateTimeStr(Date date) {
        if (date == null) {
            throw new NullPointerException("Please provide a valid Date.");
        }

        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }
}