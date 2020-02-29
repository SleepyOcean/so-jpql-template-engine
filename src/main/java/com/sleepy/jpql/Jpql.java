package com.sleepy.jpql;

/**
 * jpql对象
 *
 * @author gehoubao
 * @create 2020-01-18 10:01
 **/
public class Jpql {

    private String id;
    private String jpql;
    private String module;
    private String resultClass;
    private boolean nativeSql;
    private boolean rawSql;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public boolean isNativeSql() {
        return nativeSql;
    }

    public void setNativeSql(boolean nativeSql) {
        this.nativeSql = nativeSql;
    }

    public boolean isRawSql() {
        return rawSql;
    }

    public void setRawSql(boolean rawSql) {
        this.rawSql = rawSql;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setJpql(String jpql) {
        this.jpql = jpql;
    }

    public void setResultClass(String resultClass) {
        this.resultClass = resultClass;
    }

    public void setNative(boolean nativeSql) {
        this.nativeSql = nativeSql;
    }

    public String getId() {
        return id;
    }

    public String getJpql() {
        return jpql;
    }

    public String getResultClass() {
        return resultClass;
    }

    public boolean isNative() {
        return nativeSql;
    }
}