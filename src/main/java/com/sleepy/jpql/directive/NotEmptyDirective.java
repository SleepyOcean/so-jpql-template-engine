package com.sleepy.jpql.directive;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;

/**
 * @author gehoubao
 * @create 2020-01-18 16:01
 **/
public class NotEmptyDirective extends Directive {
    @Override
    public String getName() {
        return "notEmpty";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {

        Object param = node.jjtGetChild(0).value(context);
        if (param == null) {
            return true;
        }
        if (param instanceof Collection && CollectionUtils.isEmpty((Collection) param)) {
            return true;
        }
        if (param instanceof String && StringUtils.isEmpty(param.toString())) {
            return true;
        }
        StringWriter stringWriter = new StringWriter();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).render(context, stringWriter);
        }
        writer.write(stringWriter.toString());
        return true;
    }
}
