package org.wearefrank.xsltdebugger;

import lombok.Getter;
import lombok.Setter;

import org.wearefrank.xsltdebugger.receiver.SaxonElementReceiver;
import org.wearefrank.xsltdebugger.receiver.SaxonOutputSplitter;
import org.wearefrank.xsltdebugger.receiver.SaxonWriterReceiver;
import org.wearefrank.xsltdebugger.trace.LadybugTraceListener;
import org.wearefrank.xsltdebugger.trace.SaxonTraceListener;
import org.wearefrank.xsltdebugger.trace.XalanTraceListener;

import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

@Getter
@Setter
public class XSLTReporterSetup {
    private StringWriter writer;
    private int xsltVersion;
    private XMLTransformationContext xmlContext;
    private XMLTransformationContext xslContext;
    private LadybugTraceListener traceListener;

    public XSLTReporterSetup(File xmlFile, File xslFile, int xsltVersion) {
        this.xmlContext = XMLTransformationContext.createContextFromFile(xmlFile);
        this.xslContext = XMLTransformationContext.createContextFromFile(xslFile);
        this.xsltVersion = xsltVersion;
        this.writer = new StringWriter();
    }

    public XSLTReporterSetup(String xmlContext, String xslContext, int xsltVersion) {
        this.xmlContext = new XMLTransformationContext(xmlContext);
        this.xslContext = new XMLTransformationContext(xslContext);
        this.xsltVersion = xsltVersion;
        this.writer = new StringWriter();
    }

    /*If Saxon-HE 12.3 ever is used, there will also need to be a check if the xslt version is 4.0*/
    public void transform() throws IOException {
        if (xsltVersion == 1) {
            writer = new StringWriter();
            xalanTransform();
        } else if (xsltVersion == 2 || xsltVersion == 3) {
            writer = new StringWriter();
            saxonTransform();
        } else {
            throw new RuntimeException("ERROR: Invalid xslt version");
        }
    }

    /*Since Xalan also has a TransformerImpl/TransformerFactoryImpl, the namespace will need to be completely written down as to avoid conflicts between
     * the two packages.*/
    private void xalanTransform() throws IOException {
        XalanTraceListener traceListener = new XalanTraceListener();
        this.traceListener = traceListener;
        traceListener.m_traceElements = true;
        traceListener.m_traceTemplates = true;
        traceListener.m_traceGeneration = true;
        traceListener.m_traceSelection = true;
        Result result = new StreamResult(writer);
        try {
            StreamSource xmlSource = xmlContext.getSourceObject();
            StreamSource xslSource = xslContext.getSourceObject();

            org.apache.xalan.processor.TransformerFactoryImpl transformerFactory = new org.apache.xalan.processor.TransformerFactoryImpl();
            org.apache.xalan.transformer.TransformerImpl transformer = (org.apache.xalan.transformer.TransformerImpl) transformerFactory.newTransformer(xslSource);
            transformer.getTraceManager().addTraceListener(traceListener);
            transformer.transform(xmlSource, result);
        } catch (Exception e) {
            writer.append(e.toString());
        } finally {
            writer.close();
        }
    }

    /*Saxon has multiple ways to get a transformer for XSLT. The only way to connect the SaxonOutputSplitter and the TraceListener at the same time is
     * by using the TransformerFactoryImpl and making a TransformImpl using that factory object.
     * Since Xalan also has a TransformerImpl/TransformerFactoryImpl, the namespace will need to be completely written down as to avoid conflicts between
     * the two packages.
     * */
    private void saxonTransform() throws IOException {
        try {
            net.sf.saxon.TransformerFactoryImpl transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
            net.sf.saxon.jaxp.TransformerImpl transformer = (net.sf.saxon.jaxp.TransformerImpl) transformerFactory.newTransformer(new StreamSource(new StringReader(xslContext.getContext())));
            SaxonTraceListener traceListener = new SaxonTraceListener();
            this.traceListener = traceListener;
            transformer.getUnderlyingController().setTraceListener(traceListener);
            SaxonElementReceiver elementReceiver = new SaxonElementReceiver(traceListener);
            SaxonWriterReceiver writerReceiver = new SaxonWriterReceiver(writer, xmlContext);
            SaxonOutputSplitter receiver = new SaxonOutputSplitter(transformer.getUnderlyingController().makeBuilder(), writerReceiver, elementReceiver);
            transformer.getUnderlyingController().getInitialMode().setModeTracing(true);
            StreamResult result = new StreamResult(writer);
            transformer.transform(xmlContext.getSourceObject(), result);

        } catch (Exception e) {
            writer.append(e.toString());
        } finally {
            writer.close();
        }
    }
}
