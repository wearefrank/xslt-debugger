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

/**The SaxonOutputSplitter is responsible for splitting the output information to the two receiver objects that were given in the constructor*/
public class SaxonOutputSplitter extends ProxyReceiver {
    @Getter
    @Setter
    private String systemId;

    private final Receiver firstReceiver;
    private final Receiver secondReceiver;

    /**The constructor of the SaxonOutputSplitter to assign the Receivers
     * @param next The builder receiver that can be gotten from the underlying controller inside a transformer
     * @param firstReceiver The first receiver to send the information to
     * @param secondReceiver The second receiver to send the information to*/
    public SaxonOutputSplitter(Receiver next, Receiver firstReceiver, Receiver secondReceiver){
        super(next);
        this.firstReceiver = firstReceiver;
        this.secondReceiver = secondReceiver;
    }

    @Override
    public void open() throws XPathException {
        firstReceiver.open();
        secondReceiver.open();
    }

    @Override
    public void startDocument(int properties) throws XPathException {
        firstReceiver.startDocument(properties);
        secondReceiver.startDocument(properties);
    }

    @Override
    public void endDocument() throws XPathException {
        firstReceiver.endDocument();
        secondReceiver.endDocument();
    }

    @Override
    public void setUnparsedEntity(String name, String systemID, String publicID) throws XPathException {
        firstReceiver.setUnparsedEntity(name, systemID, publicID);
        secondReceiver.setUnparsedEntity(name, systemID, publicID);
    }

    @Override
    public void startElement(NodeName elemName, SchemaType type, AttributeMap attributes, NamespaceMap namespaces, Location location, int properties) throws XPathException {
        firstReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
        secondReceiver.startElement(elemName, type, attributes, namespaces, location, properties);
    }

    @Override
    public void endElement() throws XPathException {
        firstReceiver.endElement();
        secondReceiver.endElement();
    }

    @Override
    public void characters(CharSequence chars, Location location, int properties) throws XPathException {
        firstReceiver.characters(chars, location, properties);
        secondReceiver.characters(chars, location, properties);
    }

    @Override
    public void processingInstruction(String name, CharSequence data, Location location, int properties) throws XPathException {
        firstReceiver.processingInstruction(name, data, location, properties);
        secondReceiver.processingInstruction(name, data, location, properties);
    }

    @Override
    public void comment(CharSequence content, Location location, int properties) throws XPathException {
        firstReceiver.comment(content, location, properties);
        secondReceiver.comment(content, location, properties);
    }

    @Override
    public void close() throws XPathException {
        firstReceiver.close();
        secondReceiver.close();
    }
}
