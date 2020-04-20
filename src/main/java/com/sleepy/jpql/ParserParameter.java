package com.sleepy.jpql;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

/**
 * jpql参数
 *
 * @author gehoubao
 * @create 2020-01-18 10:46
 **/
@Data
public class ParserParameter {
    private String id;
    private Map<String, Object> parameter;
    private String dbType;

    public ParserParameter(String id, Map<String, Object> parameter, String dbType) {
        this.id = id;
        this.parameter = parameter;
        this.dbType = dbType;
    }

    public ParserParameter(String id, Map<String, Object> parameter) {
        this(id, parameter, "mysql");
    }

    public ParserParameter() {
    }
}