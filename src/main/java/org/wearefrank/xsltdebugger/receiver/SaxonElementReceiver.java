package org.wearefrank.xsltdebugger.receiver;

import lombok.Getter;
import lombok.Setter;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;
import org.wearefrank.xsltdebugger.trace.SaxonTraceListener;

import java.io.File;
import java.io.StringWriter;
import java.util.Stack;

public class SaxonElementReceiver implements Receiver {
    @Getter
    @Setter
    private PipelineConfiguration pipelineConfiguration;
    @Getter
    @Setter
    private String systemId;

    private final Stack<String> endElement;

    private final SaxonTraceListener traceListener;

    public SaxonElementReceiver(SaxonTraceListener traceListener){
        this.traceListener = traceListener;
        this.endElement = new Stack<>();
    }

    @Override
    public void open() {
    }

    @Override
    public void startDocument(int properties) {
    }

    @Override
    public void endDocument() {
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) {
        traceListener.addElementContext(systemID + ": " + name);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        StringWriter writer = new StringWriter();

        writer.append("<");
        writer.append(elemName.getDisplayName());
        for (int i = 0; i < attributes.size(); i++) {
            writer.append(" ");
            writer.append(attributes.itemAt(i).getNodeName().getDisplayName());
            writer.append("=\"");
            writer.append(attributes.itemAt(i).getValue());
            writer.append("\"");
        }
        writer.append(">");

        if(location.getSystemId() != null){
            File file = new File(location.getSystemId());
            traceListener.addElementContext(file.getName() + " Line #" + location.getLineNumber() + ", Column #" + location.getColumnNumber() + ": " + writer);
        }else{
            traceListener.addElementContext("null Line #" + location.getLineNumber() + ", Column #" + location.getColumnNumber() + ": " + writer);
        }


        traceListener.addElementContext("STARTELEMENT: " + writer);
        endElement.push("ENDELEMENT: </" + elemName.getDisplayName() + ">");
    }

    @Override
    public void endElement() {
        if(!endElement.isEmpty()) {
            traceListener.addElementContext(endElement.pop());
        }
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) {
        traceListener.addElementContext("CONTEXT: " + chars);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) {
        traceListener.addElementContext("processingInstruction: " + name + "  " + data);
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) {
        traceListener.addElementContext("COMMENT: " + content);
    }

    @Override
    public void close() {
    }
}
