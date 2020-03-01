package com.sleepy.jpql;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.List;
import java.util.Map;

/**
 * jpql执行工具类
 *
 * @author gehoubao
 * @create 2020-02-27 21:21
 **/
@Component
public class JpqlExecutor<T> {
    @Autowired
    JpqlParser jpqlParser;

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    public Session getSession() {
        return entityManagerFactory.unwrap(SessionFactory.class).openSession();
    }

    public JpqlResultSet<T> exec(String jpqlId, Map<String, Object> params, Class<T> clazz) {
        String sql = jpqlParser.parse(new ParserParameter(jpqlId, params, "mysql")).getExecutableSql();
        Session session = getSession();
        Query query = session.createNativeQuery(sql).addEntity(clazz);
        List<T> resultList = query.getResultList();
        JpqlResultSet<T> result = new JpqlResultSet<>();
        result.setResultList(resultList);
        result.setTotal(resultList.size());
        session.close();
        return result;
    }

    public JpqlResultSet<T> execPageable(String jpqlId, Map<String, Object> params, Class<T> clazz) {
        JpqlResultSet<T> result = exec(jpqlId, params, clazz);
        if (result.getTotal() > 0) {
            Session session = getSession();
            params.put("pageable", 1);
            String countSql = jpqlParser.parse(new ParserParameter(jpqlId, params, "mysql")).getExecutableSql();
            Query countQuery = session.createNativeQuery(countSql);
            result.setTotal(Long.parseLong(countQuery.getResultList().get(0).toString()));
            session.close();
        }
        return result;
    }
}