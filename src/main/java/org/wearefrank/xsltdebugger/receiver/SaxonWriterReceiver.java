package org.wearefrank.xsltdebugger.receiver;

import lombok.Getter;
import lombok.Setter;

import net.sf.saxon.event.PipelineConfiguration;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.type.SchemaType;

import java.io.StringWriter;
import java.util.Stack;

public class SaxonWriterReceiver implements Receiver {
    @Getter
    @Setter
    private PipelineConfiguration pipelineConfiguration;
    @Getter
    @Setter
    private String systemId;

    private final Stack<String> endElement;

    private final StringWriter writer;

    public SaxonWriterReceiver(StringWriter writer){
        this.writer = writer;
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
        System.out.println(name);
        writer.append(name);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) {
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
        this.endElement.push("</" + elemName.getDisplayName() + ">");
    }

    @Override
    public void endElement() {
        if(!endElement.isEmpty()){
            writer.append(endElement.pop());
        }
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) {
        writer.append(chars);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) {
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) {
        writer.append(content);
    }

    @Override
    public void close() {
    }
}
