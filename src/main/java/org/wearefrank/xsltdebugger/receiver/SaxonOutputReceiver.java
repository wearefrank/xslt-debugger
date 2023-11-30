package org.wearefrank.xsltdebugger.receiver;

import lombok.Getter;
import lombok.Setter;
import net.sf.saxon.event.ProxyReceiver;
import net.sf.saxon.event.Receiver;
import net.sf.saxon.om.AttributeMap;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.om.NodeName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.type.SchemaType;

public class SaxonOutputReceiver extends ProxyReceiver {
    @Getter
    @Setter
    private String systemId;

    private final Receiver writerReceiver;
    private final Receiver contentHandlerReceiver;

    public SaxonOutputReceiver(Receiver next, Receiver writerReceiver, Receiver contentHandlerReceiver){
        super(next);
        this.writerReceiver = writerReceiver;
        this.contentHandlerReceiver = contentHandlerReceiver;
    }

    @Override
    public void open() throws XPathException {
        writerReceiver.open();
        contentHandlerReceiver.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        writerReceiver.startDocument(properties);
        contentHandlerReceiver.startDocument(properties);
    }

    @Override
    public void endDocument() throws XPathException {
        writerReceiver.endDocument();
        contentHandlerReceiver.endDocument();
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        writerReceiver.setUnparsedEntity(name, systemID, publicID);
        contentHandlerReceiver.setUnparsedEntity(name, systemID, publicID);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        writerReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        contentHandlerReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        writerReceiver.endElement();
        contentHandlerReceiver.endElement();
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) throws XPathException {
        writerReceiver.characters(chars, location, properties);
        contentHandlerReceiver.characters(chars, location, properties);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) throws XPathException {
        writerReceiver.processingInstruction(name, data, location, properties);
        contentHandlerReceiver.processingInstruction(name, data, location, properties);
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) throws XPathException {
        writerReceiver.comment(content, location, properties);
        contentHandlerReceiver.comment(content, location, properties);
    }

    @Override
    public void close() throws XPathException {
        writerReceiver.close();
        contentHandlerReceiver.close();
    }
}
