package org.wearefrank.xsltdebugger.trace;

import lombok.Getter;

import net.sf.saxon.Controller;
import net.sf.saxon.Version;
import net.sf.saxon.expr.Expression;
import net.sf.saxon.expr.LetExpression;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.expr.instruct.*;
import net.sf.saxon.lib.Logger;
import net.sf.saxon.lib.NamespaceConstant;
import net.sf.saxon.lib.StandardDiagnostics;
import net.sf.saxon.lib.TraceListener;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trace.Traceable;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.tree.util.FastStringBuffer;
import net.sf.saxon.tree.util.Navigator;
import net.sf.saxon.value.StringValue;
import net.sf.saxon.value.Whitespace;

import java.util.Map;
import java.util.Objects;


/**
 * The SaxonTraceListener is a trace listener meant for tracing the transform of XSLT for XSLT version 1.0, 2.0 and 3.0.
 * This trace listener can be attached to the underlying controller of a SAXON TransformerImpl object.
 */
public class SaxonTraceListener extends StandardDiagnostics implements TraceListener, LadybugTraceListener {
    @Getter
    private final Trace rootTrace = new Trace();
    private Trace selectedTrace;
    //needed because the order of the methods to end a trace is reversed for some reason by saxon
    private boolean end;

    public SaxonTraceListener() {
        this.selectedTrace = rootTrace;
    }

    /**
     * Called at start
     *
     * @param controller controller of the transformer
     */
    @Override
    public void open(Controller controller) {
        String traceContext = "<trace " + "saxon-version=\"" + Version.getProductVersion() + "\" " + getOpeningAttributes() + ">\n";
        Trace trace = new Trace(traceContext, selectedTrace);
        selectedTrace.addChildTrace(trace);
        selectedTrace = trace;
    }

    /**
     * Adds opening attribute of XSLT
     *
     * @return returns the XSLT namespace stylesheet
     */
    protected String getOpeningAttributes() {
        return "xmlns:xsl=\"" + NamespaceConstant.XSLT + '\"';
    }

    /**
     * Called at end
     */
    @Override
    public void close() {
        selectedTrace.addTraceContext("</trace>");
    }

    /**
     * Called when an instruction in the stylesheet gets processed
     *
     * @param info       information about the trace
     * @param properties properties of the trace
     * @param context    given xpath context
     */
    /*Enter method is being called from classes that inherit the Traceable interface in SAXON.
    * To add more support for other elements to the TraceListener, add the necessary object type in the if-statement list below and
    * call the startCurrentItem, enter and endCurrentItem method respectively where necessary in the specified TraceAble object.*/
    @Override
    public void enter(Traceable info, Map<String, Object> properties, XPathContext context) {
        StringBuilder trace = new StringBuilder();
        if (info instanceof Expression) {
            Expression expr = (Expression) info;
            if (expr instanceof FixedElement) {
                String tag = "LRE";
                trace.append(CreateTraceContext(info, tag, properties));
                selectedTrace.addTraceContext(trace + "\n");
            } else if (expr instanceof FixedAttribute) {
                String tag = "ATTR";
                trace.append(CreateTraceContext(info, tag, properties));
                selectedTrace.addTraceContext(trace + "\n");
            } else if (expr instanceof LetExpression) {
                String tag = "xsl:variable";
                trace.append(CreateTraceContext(info, tag, properties));
                selectedTrace.addTraceContext(trace + "\n");
            } else if (expr instanceof ForEach) {
                ForEach forEach = (ForEach) expr;
                String traceId = forEach.getLocation().getLineNumber() + "_" + forEach.getLocation().getColumnNumber() + "_" + forEach.getLocation().getSystemId();
                selectedTrace.setTraceId(traceId);
                selectedTrace.setSystemId(forEach.getLocation().getSystemId());
                selectedTrace.setTraceMatch(forEach.getSelectValue());
                selectedTrace.setLineNumber(forEach.getLocation().getLineNumber());
                selectedTrace.setColumnNumber(forEach.getLocation().getColumnNumber());
                String tag = "xsl:for-each select=" + forEach.getSelectValue();
                trace.append(CreateTraceContext(info, tag, properties));
                selectedTrace.addTraceContext(trace + "\n");
                selectedTrace.setNodeType(NodeType.FOREACH);
            } else if (expr.isCallOn(net.sf.saxon.functions.Trace.class)) {
                String tag = "fn:trace";
                trace.append(CreateTraceContext(info, tag, properties));
                selectedTrace.addTraceContext(trace + "\n");
            } else {
                trace.append(expr.getExpressionName());
                selectedTrace.addTraceContext(trace + "\n");
            }
        } else if (info instanceof UserFunction) {
            String tag = "xsl:function";
            trace.append(CreateTraceContext(info, tag, properties));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof TemplateRule) {
            String traceId = ((TemplateRule) info).getLineNumber() + "_" + ((TemplateRule) info).getColumnNumber() + "_" + ((TemplateRule) info).getSystemId();
            if(selectedTrace.getTraceId() == null) {
                selectedTrace.setTraceId(traceId);
                selectedTrace.setSystemId(((TemplateRule) info).getSystemId());
                selectedTrace.setTraceMatch(((TemplateRule) info).getMatchPattern().getOriginalText());
                selectedTrace.setLineNumber(((TemplateRule) info).getLineNumber());
                selectedTrace.setColumnNumber(((TemplateRule) info).getColumnNumber());
                String tag = "xsl:template match=" + ((TemplateRule) info).getMatchPattern().getOriginalText();
                trace.append(CreateTraceContext(info, tag, properties));
                selectedTrace.addTraceContext(trace + "\n");
                selectedTrace.setNodeType(NodeType.MATCH_TEMPLATE);
            }
        } else if (info instanceof NamedTemplate) {
            String traceId = ((NamedTemplate) info).getLineNumber() + "_" + ((NamedTemplate) info).getColumnNumber() + "_" + ((NamedTemplate) info).getSystemId();
            selectedTrace.setTraceId(traceId);
            selectedTrace.setSystemId(((NamedTemplate) info).getSystemId());
            selectedTrace.setTraceMatch(((NamedTemplate) info).getTemplateName().getDisplayName());
            selectedTrace.setLineNumber(((NamedTemplate) info).getLineNumber());
            selectedTrace.setColumnNumber(((NamedTemplate) info).getColumnNumber());
            String tag = "xsl:template match=" + ((NamedTemplate) info).getTemplateName().getDisplayName();
            trace.append(CreateTraceContext(info, tag, properties));
            selectedTrace.addTraceContext(trace + "\n");
            selectedTrace.setNodeType(NodeType.MATCH_TEMPLATE);
        } else if (info instanceof GlobalParam) {
            String tag = "xsl:param";
            trace.append(CreateTraceContext(info, tag, properties));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof GlobalVariable) {
            String tag = "xsl:variable";
            trace.append(CreateTraceContext(info, tag, properties));
            selectedTrace.addTraceContext(trace + "\n");
        } else if (info instanceof net.sf.saxon.functions.Trace) {
            String tag = "fn:trace";
            trace.append(CreateTraceContext(info, tag, properties));
            selectedTrace.addTraceContext(trace + "\n");
        } else {
            String tag = "misc";
            trace.append(CreateTraceContext(info, tag, properties));
            selectedTrace.addTraceContext(trace + "\n");
        }
    }

    /**
     * Creates  a trace context based on the given information
     *
     * @param info       A trace object that the trace context will be based on
     * @param tag        A tag that will be put on the end of the context
     * @param properties Properties of the trace object
     */
    private String CreateTraceContext(Traceable info, String tag, Map<String, Object> properties) {
        Location loc = info.getLocation();
        String file = abbreviateLocationURI(loc.getSystemId());
        StringBuilder trace = new StringBuilder();
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
        if (info instanceof TemplateRule) {
            String traceId = ((TemplateRule) info).getLineNumber() + "_" + ((TemplateRule) info).getColumnNumber() + "_" + ((TemplateRule) info).getSystemId();
            if (Objects.equals(selectedTrace.getTraceId(), traceId)) {
                end = true;
            }
        } else if (info instanceof NamedTemplate) {
            String traceId = ((NamedTemplate) info).getLineNumber() + "_" + ((NamedTemplate) info).getColumnNumber() + "_" + ((NamedTemplate) info).getSystemId();
            if (Objects.equals(selectedTrace.getTraceId(), traceId)) {
                end = true;
            }
        } else if (info instanceof ForEach) {
            String traceId = info.getLocation().getLineNumber() + "_" + info.getLocation().getColumnNumber() + "_" + info.getLocation().getSystemId();
            if (Objects.equals(selectedTrace.getTraceId(), traceId)) {
                end = true;
            }
        }
    }

    /**
     * Called when an item becomes the context item
     *
     * @param item information about given node
     */
    @Override
    public void startCurrentItem(Item item) {
        //startCurrentItem is currently only searching for TinyElementImpl.
        // This class is extended by most of the important classes to show trace.
        //must be the same if-statement for both endCurrentItem and startCurrentItem methods
        if (item instanceof TinyElementImpl) {
            NodeInfo curr = (NodeInfo) item;
            String traceContext = "<source node=\"" + Navigator.getPath(curr)
                    + "\" file=\"" + curr.getSystemId()
                    + "\">\n";
            Trace trace = new Trace(traceContext, selectedTrace);
            selectedTrace.addChildTrace(trace);
            selectedTrace = trace;
        }
    }

    /**
     * Called after a node of the source tree got processed
     *
     * @param item information about given node
     */
    @Override
    public void endCurrentItem(Item item) {
        //endCurrentItem is currently only searching for TinyElementImpl.
        //This class is extended by most of the important classes to show trace.
        //must be the same if-statement for both endCurrentItem and startCurrentItem methods
        if (item instanceof TinyElementImpl) {
            NodeInfo curr = (NodeInfo) item;
            String trace = "</source><!-- " + Navigator.getPath(curr) + " -->";
            selectedTrace.addTraceContext(trace);
            if (end) {
                selectedTrace = selectedTrace.getParentTrace();
                end = false;
            }
        }
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

    public void addElementContext(String context) {
        selectedTrace.addTraceContext(context + "\n");
    }
}
