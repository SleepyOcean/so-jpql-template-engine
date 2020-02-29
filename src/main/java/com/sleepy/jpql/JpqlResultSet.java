package com.sleepy.jpql;

import lombok.Data;

import java.util.List;

/**
 * jpql返回结果类
 *
 * @author gehoubao
 * @create 2020-02-27 23:34
 **/
@Data
public class JpqlResultSet<T> {
    private List<T> resultList;
    private T result;
    private long total;
}