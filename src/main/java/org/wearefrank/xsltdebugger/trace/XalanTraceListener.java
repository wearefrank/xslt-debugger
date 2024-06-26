package org.wearefrank.xsltdebugger.trace;

import lombok.Getter;

import org.apache.xalan.templates.Constants;
import org.apache.xalan.templates.ElemTemplate;
import org.apache.xalan.templates.ElemTemplateElement;
import org.apache.xalan.trace.EndSelectionEvent;
import org.apache.xalan.trace.GenerateEvent;
import org.apache.xalan.trace.SelectionEvent;
import org.apache.xalan.trace.TraceListenerEx2;
import org.apache.xalan.trace.TracerEvent;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.ref.DTMNodeProxy;
import org.apache.xml.serializer.SerializerTrace;
import org.apache.xpath.objects.XObject;
import org.w3c.dom.Node;

import javax.xml.transform.SourceLocator;
import javax.xml.transform.TransformerException;
import java.io.File;
import java.util.Objects;

/**
 * The XalanTraceListener is a trace listener meant for tracing the transform of XSLT for XSLT version 1.0.
 * This trace listener can be attached to the underlying controller of a XALAN TransformerImpl object.
 */
/*Expansion of this class for supporting more feature would require altering the XALAN source code.
* Only from the XALAN library are the method calls made.
* This class is pretty much complete apart from optional changes for how the outcome looks.*/
public class XalanTraceListener implements TraceListenerEx2, LadybugTraceListener {

    @Getter
    private final Trace rootTrace = new Trace();
    @Getter
    private Trace selectedTrace;
    /**
     * This needs to be set to true if the listener is to print an event whenever a template is invoked.
     */
    public boolean m_traceTemplates = false;
    /**
     * Set to true if the listener is to print events that occur as each node is 'executed' in the stylesheet.
     */
    public boolean m_traceElements = false;
    /**
     * Set to true if the listener is to print information after each result-tree generation event.
     */
    public boolean m_traceGeneration = false;
    /**
     * Set to true if the listener is to print information after each selection event.
     */
    public boolean m_traceSelection = false;

    public XalanTraceListener() {
        selectedTrace = rootTrace;
    }

    /**
     * Called when a new XSLT 1.0 event starts
     *
     * @param ev Contains information about the XSLT 1.0 event
     */
    @Override
    public void trace(TracerEvent ev) {
        StringBuilder traceContext = new StringBuilder();
        if (ev.m_styleNode.getXSLToken() == Constants.ELEMNAME_TEMPLATE) {
            if (m_traceTemplates || m_traceElements) {
                boolean isBuiltIn = false;
                ElemTemplate et = (ElemTemplate) ev.m_styleNode;
                //showing SystemId once for file location
                if (et.getSystemId() != null) {
                    File file = new File(et.getSystemId());
                    if(file.getName().equals("XSL%20input")){
                        traceContext.append("Now using: XSL input");
                    }else {
                        traceContext.append("Now using: ").append(et.getSystemId());
                    }
                } else {
                    traceContext.append("Now using: built-in-rule");
                    isBuiltIn = true;
                }
                //changed to just file name. reading the whole SystemId everytime is hard to read
                String systemId = ev.m_styleNode.getSystemId();
                if (systemId != null) {
                    File file = new File(systemId);
                    if(file.getName().equals("XSL%20input")) {
                        traceContext.append("\n").append("XSL input").append(" Line #").append(et.getLineNumber())
                                .append(", ").append("Column #").append(et.getColumnNumber()).append(": ")
                                .append(et.getNodeName()).append(" ");
                    }else{
                        traceContext.append("\n").append(file.getName()).append(" Line #").append(et.getLineNumber())
                                .append(", ").append("Column #").append(et.getColumnNumber()).append(": ")
                                .append(et.getNodeName()).append(" ");
                    }
                } else {
                    traceContext.append("\n").append("built-in-rule ");
                    isBuiltIn = true;
                }
                if (null != et.getMatch()) {
                    traceContext.append("match='").append(et.getMatch().getPatternString()).append("' ");
                }

                if (null != et.getName()) {
                    traceContext.append("name='").append(et.getName()).append("' ");
                }
                traceContext.append("\n");
                //A trace id based on the line number, column number and system id is made which will be attached
                //to the trace object to later be reviewed to see if the selected trace needs to be changed
                //to the parent trace
                String traceId = et.getLineNumber() + "_" + et.getColumnNumber() + "_" + et.getSystemId();
                Trace trace = new Trace(et.getMatch().getPatternString(), et.getSystemId(), traceContext.toString(), traceId, selectedTrace);
                trace.setLineNumber(et.getLineNumber());
                trace.setColumnNumber(et.getColumnNumber());
                if (isBuiltIn) {
                    trace.setNodeType(NodeType.BUILT_IN_TEMPLATE);
                }
                selectedTrace.addChildTrace(trace);
                if (!isBuiltIn) {
                    trace.setNodeType(NodeType.MATCH_TEMPLATE);
                    selectedTrace = trace;
                }
            }
        } else if (ev.m_styleNode.getXSLToken() != Constants.ELEMNAME_TEXTLITERALRESULT) {
            if (m_traceElements) {
                //changed to just file name. reading the whole SystemId everytime is hard to read
                String systemId = ev.m_styleNode.getSystemId();
                if (systemId != null) {
                    File file = new File(systemId);
                    if(file.getName().equals("XSL%20input")) {
                        traceContext.append("\n").append("XSL input").append(" Line #").append(ev.m_styleNode.getLineNumber()).append(", ")
                                .append("Column #").append(ev.m_styleNode.getColumnNumber()).append(": ").append("<")
                                .append(ev.m_styleNode.getNodeName()).append(">");
                    }else{
                        traceContext.append("\n").append(file.getName()).append(" Line #").append(ev.m_styleNode.getLineNumber()).append(", ")
                                .append("Column #").append(ev.m_styleNode.getColumnNumber()).append(": ").append("<")
                                .append(ev.m_styleNode.getNodeName()).append(">");
                    }
                } else {
                    traceContext.append("\n").append("null");
                }
                selectedTrace.addTraceContext(traceContext.toString());
            }
        }
    }

    /**
     * Gets called when a new node is selected for the XSLT
     *
     * @param ev information about the selected node
     */
    @Override
    public void selected(SelectionEvent ev) throws TransformerException {
        if (m_traceSelection) {
            StringBuilder trace = new StringBuilder();
            ElemTemplateElement ete = ev.m_styleNode;
            Node sourceNode = ev.m_sourceNode;
            SourceLocator locator = null;
            if (sourceNode instanceof DTMNodeProxy) {
                int nodeHandler = ((DTMNodeProxy) sourceNode).getDTMNodeNumber();
                locator =
                        ((DTMNodeProxy) sourceNode).getDTM().getSourceLocatorFor(
                                nodeHandler);
            }
            if (locator != null) {
                //changed to just file name. reading the whole SystemId everytime is hard to read
                File file = new File(locator.getSystemId());
                if(file.getName().equals("XSL%20input")){
                    trace.append("\n").append("Selected source node '").append(sourceNode.getNodeName())
                            .append("', at XSL input").append(". ");
                }else {
                    trace.append("\n").append("Selected source node '").append(sourceNode.getNodeName()).append("', at ")
                            .append(file.getName()).append(". ");
                }
                selectedTrace.setSelectedNode(sourceNode.getNodeName());
            } else {
                trace.append("\n").append("Selected source node '").append(sourceNode.getNodeName()).append("'");
                selectedTrace.setSelectedNode(sourceNode.getNodeName());
            }
            if (ev.m_styleNode.getLineNumber() == 0) {
                // You may not have line numbers if the selection is occurring from a
                // default template.
                ElemTemplateElement parent = ete.getParentElem();
                if (parent == ete.getStylesheetRoot().getDefaultRootRule()) {
                    trace.append("(default root rule) ");
                } else if (
                        parent == ete.getStylesheetRoot().getDefaultTextRule()) {
                    trace.append("(default text rule) ");
                } else if (parent == ete.getStylesheetRoot().getDefaultRule()) {
                    trace.append("(default rule) ");
                }
                trace.append(ete.getNodeName()).append(", ").append(ev.m_attributeName).append("='").append(ev.m_xpath.getPatternString()).append("': ");
            } else {
                //changed to just file name. reading the whole SystemId everytime is hard to read
                String systemId = ev.m_styleNode.getSystemId();
                File file = new File(systemId);
                if(file.getName().equals("XSL%20input")){
                    trace.append("XSL input").append(" Line #").append(ev.m_styleNode.getLineNumber()).append(", ").append("Column #").append(ev.m_styleNode.getColumnNumber()).append(": ").append(ete.getNodeName()).append(", ").append(ev.m_attributeName).append("='").append(ev.m_xpath.getPatternString()).append("': ");
                }else {
                    trace.append(file.getName()).append(" Line #").append(ev.m_styleNode.getLineNumber()).append(", ").append("Column #").append(ev.m_styleNode.getColumnNumber()).append(": ").append(ete.getNodeName()).append(", ").append(ev.m_attributeName).append("='").append(ev.m_xpath.getPatternString()).append("': ");
                }
            }
            if (ev.m_selection.getType() == XObject.CLASS_NODESET) {
                trace.append("\n");
                org.apache.xml.dtm.DTMIterator nl = ev.m_selection.iter();
                // The following lines are added to fix bug#16222.
                // The main cause is that the following loop change the state of iterator, which is shared
                // with the transformer. The fix is that we record the initial state before looping, then
                // restore the state when we finish it, which is done in the following lines added.
                int currentPos = DTM.NULL;
                currentPos = nl.getCurrentPos();
                nl.setShouldCacheNodes(true); // This MUST be done before we clone the iterator!
                org.apache.xml.dtm.DTMIterator clone;
                // End of block
                try {
                    clone = nl.cloneWithReset();
                } catch (CloneNotSupportedException exception) {
                    trace.append("\n").append(
                            "     [Can't trace nodelist because it it threw a CloneNotSupportedException]");
                    return;
                }
                int pos = clone.nextNode();
                if (DTM.NULL == pos) {
                    trace.append("\n").append("     [Could not find match for ").append(ev.m_attributeName).append("=").append(ev.m_xpath.getPatternString()).append("]");
                    trace.append("\n").append("     [empty node list]");
                }
                // Restore the initial state of the iterator, part of fix for bug#16222.
                nl.runTo(-1);
                nl.setCurrentPos(currentPos);
                // End of fix for bug#16222
            } else {
                trace.append("\n").append(ev.m_selection.str());
            }
            selectedTrace.addTraceContext(trace.toString());
        }
    }

    /**
     * Whenever a value from XML is grabbed, affected or changed
     *
     * @param ev information about the affected value
     */
    @Override
    public void generated(GenerateEvent ev) {
        if (m_traceGeneration) {
            StringBuilder trace = new StringBuilder();
            switch (ev.m_eventtype) {
                case SerializerTrace.EVENTTYPE_STARTDOCUMENT:
                    trace.append("\n").append("STARTDOCUMENT");
                    break;
                case SerializerTrace.EVENTTYPE_ENDDOCUMENT:
                    trace.append("\n").append("ENDDOCUMENT");
                    break;
                case SerializerTrace.EVENTTYPE_STARTELEMENT:
                    trace.append("\n").append("STARTELEMENT: ").append("<").append(ev.m_name).append(">");
                    break;
                case SerializerTrace.EVENTTYPE_ENDELEMENT:
                    trace.append("\n").append("ENDELEMENT: ").append("</").append(ev.m_name).append(">");
                    break;
                case SerializerTrace.EVENTTYPE_CHARACTERS: {
                    String chars = new String(ev.m_characters, ev.m_start, ev.m_length);

                    trace.append("\n").append("CHARACTERS: ").append(chars);
                }
                break;
                case SerializerTrace.EVENTTYPE_CDATA: {
                    String chars = new String(ev.m_characters, ev.m_start, ev.m_length);

                    trace.append("\n").append("CDATA: ").append(chars);
                }
                break;
                case SerializerTrace.EVENTTYPE_COMMENT:
                    trace.append("\n").append("COMMENT: ").append(ev.m_data);
                    break;
                case SerializerTrace.EVENTTYPE_PI:
                    trace.append("\n").append("PI: ").append(ev.m_name).append(", ").append(ev.m_data);
                    break;
                case SerializerTrace.EVENTTYPE_ENTITYREF:
                    trace.append("\n").append("ENTITYREF: ").append(ev.m_name);
                    break;
                case SerializerTrace.EVENTTYPE_IGNORABLEWHITESPACE:
                    trace.append("\n").append("IGNORABLEWHITESPACE");
                    break;
            }
            selectedTrace.addTraceContext(trace.toString());
        }
    }


    /**
     * Called to show the end of the trace. The selected trace will be changed to the parent of the current selected
     * trace. It is only set to the parent trace if the trace id of the current selected trace is identical to the
     * trace id of the tracer event.
     *
     * @param ev information about the trace
     */
    @Override
    public void traceEnd(TracerEvent ev) {
        if (ev.m_styleNode.getXSLToken() == Constants.ELEMNAME_TEMPLATE) {
            if (m_traceTemplates || m_traceElements) {
                ElemTemplate et = (ElemTemplate) ev.m_styleNode;
                String traceId = et.getLineNumber() + "_" + et.getColumnNumber() + "_" + et.getSystemId();
                if (Objects.equals(selectedTrace.getTraceId(), traceId)) {
                    selectedTrace = selectedTrace.getParentTrace();
                }
            }
        }
    }

    /**
     * Deprecated
     */
    @Override
    public void selectEnd(EndSelectionEvent endSelectionEvent) {
    }
}
