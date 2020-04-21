package com.sleepy.jpql;

import com.sleepy.jpql.annotation.JpqlCol;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

/**
 * jpql执行工具类
 *
 * @author gehoubao
 * @create 2020-02-27 21:21
 **/
@Slf4j
@Component
public class JpqlExecutor<T> {
    @Autowired
    JpqlParser jpqlParser;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    /**
     * 执行普通查询
     *
     * @param jpqlId
     * @param params
     * @param clazz
     * @return
     */
    public JpqlResultSet<T> exec(String jpqlId, Map<String, Object> params, Class<T> clazz) {
        String sql = jpqlParser.parse(new ParserParameter(jpqlId, params)).getExecutableSql();
        return select(sql, clazz);
    }

    /**
     * 执行分页查询
     *
     * @param jpqlId
     * @param params
     * @param clazz
     * @return
     */
    public JpqlResultSet<T> execPageable(String jpqlId, Map<String, Object> params, Class<T> clazz, PageRequest page) {
        long offset = page.getOffset();
        int limit = page.getPageSize();
        params.put("limit", limit);
        params.put("offset", offset);
        String sql = jpqlParser.parse(new ParserParameter(jpqlId, params)).getExecutableSql();
        String pageSql = sql + "limit " + limit + " offset " + offset;
        JpqlResultSet<T> result = select(pageSql, clazz);
        if (result.getTotal() > 0) {
            Session session = getSession();
            String selectCols = getSelectColumnsStr(sql);
            String countSql = sql.substring(0, sql.indexOf(selectCols)) + "count(*)" + sql.substring(sql.indexOf(selectCols) + selectCols.length());
            Query countQuery = session.createNativeQuery(countSql);
            result.setTotal(Long.parseLong(countQuery.getResultList().get(0).toString()));
            session.close();
        }
        return result;
    }

    private Session getSession() {
        return entityManagerFactory.unwrap(SessionFactory.class).openSession();
    }

    private JpqlResultSet<T> select(String sql, Class<T> clazz) {
        List<String> selectCols = getSelectColumns(sql);
        if (selectCols.contains("*")) {
            if (selectCols.size() == 1) {
                return selectAll(sql, clazz);
            } else {
                throw new IllegalArgumentException("Illegal SQL: select is not executable," + sql);
            }
        } else if (selectCols.size() > 0) {
            try {
                return selectPart(sql, selectCols, clazz);
            } catch (Exception e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
            return new JpqlResultSet<T>();
        } else {
            throw new IllegalArgumentException("Illegal SQL: select is not executable," + sql);
        }
    }

    private JpqlResultSet<T> selectAll(String sql, Class<T> clazz) {
        Session session = getSession();
        Query query = session.createNativeQuery(sql, clazz);
        List<T> resultList = query.getResultList();
        JpqlResultSet<T> result = new JpqlResultSet<>(resultList, resultList.size());
        session.close();
        return result;
    }

    private JpqlResultSet<T> selectPart(String sql, List<String> selectCols, Class<T> clazz) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        Session session = getSession();
        Query query = session.createNativeQuery(sql);
        List list = query.getResultList();

        // 对象转换
        List<T> resultList = transformList(list, selectCols, clazz);

        JpqlResultSet<T> result = new JpqlResultSet<>(resultList, resultList.size());
        session.close();
        return result;
    }

    private List<T> transformList(List list, List<String> selectCols, Class<T> clazz) throws IllegalAccessException,
            NoSuchMethodException, InstantiationException, InvocationTargetException {
        List<T> res = new ArrayList<>();
        for (Object o : list) {
            res.add(transformObject(o, selectCols, clazz));
        }
        return res;
    }

    /**
     * 转换对象
     *
     * @param obj
     * @param selectCols
     * @param clazz
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     */
    private T transformObject(Object obj, List<String> selectCols, Class<T> clazz) throws IllegalAccessException,
            InstantiationException, NoSuchMethodException, InvocationTargetException {
        Field[] fields = clazz.getDeclaredFields();
        Object[] objs = (Object[]) obj;
        T item = clazz.newInstance();
        // 通过sql字段的名称转换为目标对象的字段名称
        List<String> transFields = selectCols.stream().map(col -> {
            if (col.toLowerCase().contains(" as ")) {
                return col.substring(col.toLowerCase().indexOf(" as ") + 4);
            } else {
                col = col.replaceAll("\\s", "");
                if (col.contains("_")) {
                    String[] strings = col.split("_");
                    StringBuilder s = new StringBuilder(strings[0]);
                    for (int i = 1; i < strings.length; i++) {
                        s.append(strings[i].substring(0, 1).toUpperCase() + strings[i].substring(1));
                    }
                    return s.toString();
                } else {
                    return col;
                }
            }
        }).collect(Collectors.toList());
        // 赋值目标对象
        for (int i = 0; i < fields.length; i++) {
            String field = fields[i].getName();
            JpqlCol jpqlCol = fields[i].getAnnotation(JpqlCol.class);
            int index = -1;
            if (transFields.contains(field)) {
                // sql字段标识
                index = transFields.indexOf(field);
            } else if (jpqlCol != null && selectCols.contains(jpqlCol.value())) {
                // 注解标识
                index = selectCols.indexOf(jpqlCol.value());
            } else {
                continue;
            }
            // 赋值
            String method = "set" + (field.substring(0, 1).toUpperCase() + field.substring(1));
            Method addMethod = clazz.getMethod(method, fields[i].getType());
            try {
                addMethod.invoke(item, objs[index]);
            } catch (IllegalArgumentException e) {
                // 处理常用类型转换
                Class type = fields[i].getType();
                if (type == String.class) {
                    addMethod.invoke(item, String.valueOf(objs[index]));
                } else if (type == Integer.class) {
                    addMethod.invoke(item, Integer.parseInt(String.valueOf(objs[index])));
                } else if (type == Long.class) {
                    addMethod.invoke(item, Long.parseLong(String.valueOf(objs[index])));
                } else if (type == Float.class) {
                    addMethod.invoke(item, Float.parseFloat(String.valueOf(objs[index])));
                } else if (type == Double.class) {
                    addMethod.invoke(item, Double.parseDouble(String.valueOf(objs[index])));
                } else {
                    throw new IllegalArgumentException(" arguments type mismatch: field [" + field + "] should be " + objs[index].getClass().toString());
                }
            }
        }
        return item;
    }

    /**
     * 获取sql返回的字段列表
     *
     * @param sql
     * @return
     */
    private List<String> getSelectColumns(String sql) {
        String selectCols = getSelectColumnsStr(sql);
        return Arrays.asList(selectCols.split(",")).stream().map(s -> {
            if (s.toLowerCase().contains(" as ")) {
                return s;
            } else {
                return s.replaceAll("\\s", "");
            }
        }).collect(Collectors.toList());
    }

    /**
     * 获取sql返回的字段列表字符串
     *
     * @param sql
     * @return
     */
    private String getSelectColumnsStr(String sql) {
        String lowerCaseSql = sql.toLowerCase();
        String selectCols = sql.substring(lowerCaseSql.indexOf("select") + 7, lowerCaseSql.indexOf(" from"));
        return selectCols;
    }
}