/*
   Copyright 2023 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package org.wearefrank.trace;


import lombok.Getter;
import net.sf.saxon.Controller;
import net.sf.saxon.Version;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.*;
import net.sf.saxon.functions.Trace;
import net.sf.saxon.lib.*;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.trace.TraceableComponent;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

import java.util.Map;
import java.util.Objects;


/**The SaxonTemplateTraceListener is a trace listener meant for tracing the transform of XSLT.
 * This trace listener can be attached to the underlying controller of a SAXON TransformerImpl object.*/
public class SaxonTemplateTraceListener extends StandardDiagnostics implements TraceListener, LadybugTraceListener {
    @Getter
    private final TemplateTrace rootTrace = new TemplateTrace();
    private TemplateTrace selectedTrace;
    protected int indent = 0;
    private final int detail = 3; // none=0; low=1; normal=2; high=3
    /*@NotNull*/ private static StringBuffer spaceBuffer = new StringBuffer("                ");

    //needed because the order of the methods to end a trace is reversed for some reason by saxon
    private boolean end;

    public SaxonTemplateTraceListener(){
        this.selectedTrace = rootTrace;
    }

    /**
     * Called at start
     *
     * @param controller controller of the transformer
     */
    @Override
    public void open(Controller controller) {
        String trace = "<trace " + "saxon-version=\"" + Version.getProductVersion() + "\" " + getOpeningAttributes() + ">\n";
        TemplateTrace templateTrace = new TemplateTrace(trace, selectedTrace);

        selectedTrace.addChildtrace(templateTrace);
        selectedTrace = templateTrace;
    }

    /**Adds opening attribute of XSLT
     * @return returns the XSLT namespace stylesheet*/
    protected String getOpeningAttributes() {
        return "xmlns:xsl=\"" + NamespaceConstant.XSLT + '\"';
    }

    /**
     * Called at end
     */
    @Override
    public void close() {
        indent--;

        selectedTrace.addTraceContext("</trace>");
    }

    /**
     * Called when an instruction in the stylesheet gets processed
     *
     * @param info       information about the trace
     * @param properties properties of the trace
     * @param context    given xpath context
     */
    @Override
    public void enter(Traceable info, Map<String, Object> properties, XPathContext context) {
        if (isApplicable(info)) {
            trace(info, properties, context);
        }
    }

    private void trace(Traceable info, Map<String, Object> properties, XPathContext context) {
        StringBuilder trace = new StringBuilder();
        if (info instanceof Expression) {
            Expression expr = (Expression) info;
            if (expr instanceof FixedElement) {
                String tag = "LRE";
                trace.append(CreateTrace(info, tag, properties, true));
                selectedTrace.addTraceContext(trace + "\n");
            } else if (expr instanceof FixedAttribute) {
                String tag = "ATTR";
                trace.append(CreateTrace(info, tag, properties, true));
                selectedTrace.addTraceContext(trace + "\n");
            } else if (expr instanceof LetExpression) {
                String tag = "xsl:variable";
                trace.append(CreateTrace(info, tag, properties, true));
                selectedTrace.addTraceContext(trace + "\n");
            } else if (expr.isCallOn(Trace.class)) {
                String tag = "fn:trace";
                trace.append(CreateTrace(info, tag, properties, true));
                selectedTrace.addTraceContext(trace + "\n");
            } else {
                trace.append(expr.getExpressionName());
                selectedTrace.addTraceContext(trace + "\n");
            }
        } else if (info instanceof UserFunction) {
            String tag = "xsl:function";
            trace.append(CreateTrace(info, tag, properties, true));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof TemplateRule) {
            String traceId = ((TemplateRule) info).getLineNumber() + "_" + ((TemplateRule) info).getColumnNumber() + "_" + ((TemplateRule) info).getSystemId();
            selectedTrace.setTraceId(traceId);
            selectedTrace.setSystemId(((TemplateRule) info).getSystemId());
            selectedTrace.setTemplateMatch(((TemplateRule) info).getMatchPattern().getOriginalText());

            selectedTrace.setLineNumber(((TemplateRule) info).getLineNumber());
            selectedTrace.setColumnNumber(((TemplateRule) info).getColumnNumber());

            String tag = "xsl:template match=" + ((TemplateRule) info).getMatchPattern().getOriginalText();
            trace.append(CreateTrace(info, tag, properties, false));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof NamedTemplate) {
            String traceId = ((NamedTemplate) info).getLineNumber() + "_" + ((NamedTemplate) info).getColumnNumber() + "_" + ((NamedTemplate) info).getSystemId();
            selectedTrace.setTraceId(traceId);
            selectedTrace.setSystemId(((NamedTemplate) info).getSystemId());
            selectedTrace.setTemplateMatch(((NamedTemplate) info).getTemplateName().getDisplayName());

            selectedTrace.setLineNumber(((NamedTemplate) info).getLineNumber());
            selectedTrace.setColumnNumber(((NamedTemplate) info).getColumnNumber());

            String tag = "xsl:template match=" + ((NamedTemplate) info).getTemplateName().getDisplayName();
            trace.append(CreateTrace(info, tag, properties, false));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof GlobalParam) {
            String tag = "xsl:param";
            trace.append(CreateTrace(info, tag, properties, true));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof GlobalVariable) {
            String tag = "xsl:variable";
            trace.append(CreateTrace(info, tag, properties, true));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof Trace) {
            String tag = "fn:trace";
            trace.append(CreateTrace(info, tag, properties, true));
            selectedTrace.addTraceContext(trace + "\n");
        } else {
            String tag = "misc";
            trace.append(CreateTrace(info, tag, properties, true));
            selectedTrace.addTraceContext(trace + "\n");
        }

        if(!context.getContextItem().getStringValue().isEmpty()){
            String contextOutput = "CONTEXT: " + context.getContextItem().getStringValue() + "\n";
            selectedTrace.addTraceContext(contextOutput);
        }
    }

    private String CreateTrace(Traceable info, String tag, Map<String, Object> properties, boolean useIndents){
        Location loc = info.getLocation();
        String file = abbreviateLocationURI(loc.getSystemId());
        StringBuilder trace = new StringBuilder();
        if(useIndents){
            trace.append(spaces(indent));
        }
        trace.append('<').append(tag).append(" ");
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof StructuredQName) {
                val = ((StructuredQName) val).getDisplayName();
            } else if (val instanceof StringValue) {
                val = ((StringValue) val).getStringValue();
            }
            if (val != null) {
                trace.append(' ').append(entry.getKey()).append("=\"").append(escape(val.toString())).append('"');
            }
        }

        trace.append(" line=\"").append(loc.getLineNumber()).append('"');

        int col = loc.getColumnNumber();
        if (col >= 0) {
            trace.append(" column=\"").append(loc.getColumnNumber()).append('"');
        }

        trace.append(" module=\"").append(escape(file)).append('"');
        trace.append(">");
        indent++;

        return trace.toString();
    }

    /**
     * Escape a string for XML output (in an attribute delimited by double quotes).
     * This method also collapses whitespace (since the value may be an XPath expression that
     * was originally written over several lines).
     *
     * @param in input string
     * @return output string
     */
    public String escape(/*@Nullable*/ String in) {
        if (in == null) {
            return "";
        }
        CharSequence collapsed = Whitespace.collapseWhitespace(in);
        FastStringBuffer sb = new FastStringBuffer(collapsed.length() + 10);
        for (int i = 0; i < collapsed.length(); i++) {
            char c = collapsed.charAt(i);
            if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '\"') {
                sb.append("&#34;");
            } else if (c == '\n') {
                sb.append("&#xA;");
            } else if (c == '\r') {
                sb.append("&#xD;");
            } else if (c == '\t') {
                sb.append("&#x9;");
            } else {
                sb.cat(c);
            }
        }
        return sb.toString();
    }

    /**
     * Called after an instruction of the stylesheet got processed
     *
     * @param info information about trace
     */

    @Override
    public void leave(Traceable info) {
        if (isApplicable(info)) {
            indent--;
        }

        if (info instanceof TemplateRule) {
            String traceId = ((TemplateRule) info).getLineNumber() + "_" + ((TemplateRule) info).getColumnNumber() + "_" + ((TemplateRule) info).getSystemId();
            if(Objects.equals(selectedTrace.getTraceId(), traceId)){
                end = true;
            }
        } else if (info instanceof NamedTemplate) {
            String traceId = ((NamedTemplate) info).getLineNumber() + "_" + ((NamedTemplate) info).getColumnNumber() + "_" + ((NamedTemplate) info).getSystemId();
            if(Objects.equals(selectedTrace.getTraceId(), traceId)){
                end = true;
            }
        }
    }

    /**
     * @param info shows traceable info
     * @return bool to see if trace should be written to output stream
     */
    protected boolean isApplicable(Traceable info) {
        return level(info) <= detail;
    }

    /**
     * @param info information about the trace
     * @return the level of detail that is allowed
     */
    protected int level(Traceable info) {
        if (info instanceof TraceableComponent) {
            return 1;
        }
        if (info instanceof Instruction) {
            return 2;
        } else {
            return 3;
        }
    }

    /**
     * Called when an item becomes the context item
     *
     * @param item information about given node
     */
    @Override
    public void startCurrentItem(Item item) {
        if (item instanceof TinyElementImpl && detail > 0) {
            TinyElementImpl curr = (TinyElementImpl) item;
            String trace = "<source node=\"" + Navigator.getPath(curr)
                    + "\" file=\"" + curr.getSystemId()
                    + "\">\n";
            TemplateTrace templateTrace = new TemplateTrace(trace, selectedTrace);

            selectedTrace.addChildtrace(templateTrace);
            selectedTrace = templateTrace;
        }
        indent++;
    }

    /**
     * Called after a node of the source tree got processed
     *
     * @param item information about given node
     */
    @Override
    public void endCurrentItem(Item item) {
        indent--;
        if (item instanceof NodeInfo && detail > 0) {
            NodeInfo curr = (NodeInfo) item;

            String trace = "</source><!-- " + Navigator.getPath(curr) + " -->";
            selectedTrace.addTraceContext(trace);

            if(end){
                selectedTrace = selectedTrace.getParentTrace();
                end = false;
            }
        }
    }

    /**
     * Get n spaces
     *
     * @param n determines how much whitespace is to be added
     * @return returns a certain amount of whitespace
     */

    protected static String spaces(int n) {
        while (spaceBuffer.length() < n) {
            spaceBuffer.append(spaceBuffer);
        }
        return spaceBuffer.substring(0, n);
    }

    /**
     * This method is deprecated
     *
     * @param stream does not do anything
     */
    @Override
    @Deprecated
    public void setOutputDestination(Logger stream) {
    }
}
