package com.sleepy.jpql.directive;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

/**
 * @author gehoubao
 * @create 2020-01-18 16:02
 **/
public class StripDirective extends Directive {
    @Override
    public String getName() {
        return "strip";
    }

    @Override
    public int getType() {
        return BLOCK;
    }

    @Override
    public boolean render(InternalContextAdapter context, Writer writer, Node node) throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        Object param = node.jjtGetChild(0).value(context);
        if (param == null) {
            throw new IllegalArgumentException("miss argument for #dynamic");
        }

        StringWriter stringWriter = new StringWriter();
        for (int i = 0; i < node.jjtGetNumChildren(); i++) {
            node.jjtGetChild(i).render(context, stringWriter);
        }
        String result = StringUtils.removeEnd(StringUtils.removeStart(StringUtils.trim(stringWriter.toString()), String.valueOf(param)), String.valueOf(param));
        writer.write(result);
        return true;
    }
}
