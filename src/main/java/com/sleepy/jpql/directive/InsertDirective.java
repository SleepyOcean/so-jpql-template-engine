package com.sleepy.jpql.directive;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.reflections.ReflectionUtils;
import org.springframework.core.annotation.AnnotationUtils;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Set;

/**
 * @author gehoubao
 * @create 2020-01-18 15:36
 **/
public class InsertDirective extends Directive {
    @Override
    public String getName() {
        return "insert";
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
            StringBuilder stringBuilder = new StringBuilder(this.getName() + " into " + tableName);
            stringBuilder.append("(");
            StringBuilder values = new StringBuilder();
            fieldList.stream().forEach(field -> {
                if (field.isAnnotationPresent(Id.class)) {
                    GeneratedValue generatedValue = AnnotationUtils.findAnnotation(field, GeneratedValue.class);
                    if (generatedValue != null) {
                        if (generatedValue.strategy().equals(GenerationType.IDENTITY)) {
                            return;
                        }
                        if (generatedValue.generator().equals("composite")) {

                        }
                    }
                }
                String fieldName = String.join("_", StringUtils.splitByCharacterTypeCamelCase(field.getName())).toLowerCase();
                stringBuilder.append(fieldName).append(",");
                values.append(":").append(field.getName()).append(",");

            });
            stringBuilder.delete(stringBuilder.length() - 1, stringBuilder.length());
            values.delete(values.length() - 1, values.length());
            stringBuilder.append(") values (");
            stringBuilder.append(values).append(")");
            writer.write(stringBuilder.toString());

        } catch (Exception e) {
            log.error("{}", e);
        }


        return true;
    }
}
