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
@AllArgsConstructor
public class ParserParameter {
    private String id;
    private Map<String, Object> parameter;
    private String dbType;
}