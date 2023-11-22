package org.wearefrank.Receiver;

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
import org.wearefrank.trace.SaxonTemplateTraceListener;

import java.util.Stack;

public class SaxonElementReceiver implements Receiver {
    @Getter
    @Setter
    private PipelineConfiguration pipelineConfiguration;
    @Getter
    @Setter
    private String systemId;

    private Stack<String> endElement;

    private SaxonTemplateTraceListener traceListener;

    public SaxonElementReceiver(SaxonTemplateTraceListener traceListener){
        this.traceListener = traceListener;
        this.endElement = new Stack<>();
    }

    @Override
    public void open() throws XPathException {
        //do nothing
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        //do nothing
    }

    @Override
    public void endDocument() throws XPathException {
        //do nothing
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        System.out.println("setUNPARSEDENTITY: " + name);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        traceListener.addElementContext("STARTELEMENT: " + "<" + elemName.getDisplayName() + ">");
        endElement.push("ENDELEMENT: </" + elemName.getDisplayName() + ">");
    }

    @Override
    public void endElement() throws XPathException {
        if(!endElement.isEmpty()) {
            traceListener.addElementContext(endElement.pop());
        }
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) throws XPathException {
        traceListener.addElementContext("CONTEXT: " + chars);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) throws XPathException {
        System.out.println("processingInstruction: " + name + "  " + data);
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) throws XPathException {
        System.out.println("COMMENT: " + content);
    }

    @Override
    public void close() throws XPathException {
        //do nothing
    }
}
