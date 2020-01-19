package com.sleepy.jpql.directive;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.reflections.ReflectionUtils;
import org.springframework.core.annotation.AnnotationUtils;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author gehoubao
 * @create 2020-01-18 16:02
 **/
@Slf4j
public class UpdateDirective extends Directive {
    @Override
    public String getName() {
        return "update";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Object param = node.jjtGetChild(0).value(context);
        try {
            Class c = Class.forName(String.valueOf(param));
            Set<Field> fieldList = ReflectionUtils.getAllFields(c, (field -> !Modifier.isStatic(field.getModifiers()) && !Modifier.isFinal(field.getModifiers())));
            Table table = AnnotationUtils.findAnnotation(c, Table.class);
            String tableName = c.getSimpleName();
            if (table != null) {
                tableName = table.name();
            } else {
                tableName = String.join("_", StringUtils.splitByCharacterTypeCamelCase(tableName)).toLowerCase();
            }
            StringBuilder stringBuilder = new StringBuilder("update " + tableName + " set ");
            StringBuilder whereCause = new StringBuilder(" where ");
            fieldList.stream().forEach(field -> {
                String fieldName = String.join("_", StringUtils.splitByCharacterTypeCamelCase(field.getName())).toLowerCase();

                if (field.isAnnotationPresent(Id.class)) {
                    whereCause.append(fieldName).append("=:").append(field.getName());
                }
                stringBuilder.append(fieldName).append("=:").append(field.getName()).append(",");
            });
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            stringBuilder.append(whereCause.toString());

            writer.write(stringBuilder.toString());

        } catch (Exception e) {
            log.error("{}", e);
        }

        return true;
    }
}
