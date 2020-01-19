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
 * @create 2020-01-18 15:35
 **/
public class EmptyDirective extends Directive {
    @Override
    public String getName() {
        return "empty";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Object param = node.jjtGetChild(0).value(context);
        if (param == null) {
            renderOutput(context, writer, node);
        }
        if (param instanceof Collection && CollectionUtils.isEmpty((Collection) param)) {
            renderOutput(context, writer, node);
        }
        if (param instanceof String && StringUtils.isEmpty(param.toString())) {
            renderOutput(context, writer, node);
        }
        return true;
    }

    private void renderOutput(InternalContextAdapter context, Writer writer, Node node) throws IOException {
        StringWriter stringWriter = new StringWriter();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).render(context, stringWriter);
        }
        writer.write(stringWriter.toString());
    }
}