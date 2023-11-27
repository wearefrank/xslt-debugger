package org.wearefrank;

import lombok.Getter;
import lombok.Setter;
import org.wearefrank.Receiver.SaxonElementReceiver;
import org.wearefrank.Receiver.SaxonOutputReceiver;
import org.wearefrank.Receiver.SaxonWriterReceiver;
import org.wearefrank.trace.LadybugTraceListener;
import org.wearefrank.trace.SaxonTemplateTraceListener;
import org.wearefrank.trace.XalanTemplateTraceListener;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.StringWriter;

@Getter
@Setter
public class XSLTReporterSetup {
    private StringWriter writer;
    private int xsltVersion;
    private File xmlFile;
    private File xslFile;
    private LadybugTraceListener traceListener;

    public XSLTReporterSetup(File xmlFile, File xslFile, int xsltVersion) {
        this.xmlFile = xmlFile;
        this.xslFile = xslFile;
        this.xsltVersion = xsltVersion;
        this.writer = new StringWriter();
    }

    public void transform() {
        if (xsltVersion == 1) {
            writer = new StringWriter();
            xalanTransform();
        } else if (xsltVersion == 2 || xsltVersion == 3) {
            writer = new StringWriter();
            saxonTramsform();
        } else {
            throw new RuntimeException("ERROR: Invalid xslt version");
        }
    }

    private void xalanTransform() {
        XalanTemplateTraceListener traceListener = new XalanTemplateTraceListener();
        this.traceListener = traceListener;

        traceListener.m_traceElements = true;
        traceListener.m_traceTemplates = true;
        traceListener.m_traceGeneration = true;
        traceListener.m_traceSelection = true;

        Result result = new StreamResult(writer);

        try {
            org.apache.xalan.processor.TransformerFactoryImpl transformerFactory = new org.apache.xalan.processor.TransformerFactoryImpl();
            org.apache.xalan.transformer.TransformerImpl transformer = (org.apache.xalan.transformer.TransformerImpl) transformerFactory.newTransformer(new StreamSource(xslFile.getAbsolutePath()));
            transformer.getTraceManager().addTraceListener(traceListener);
            transformer.transform(new StreamSource(xmlFile.getAbsolutePath()), result);

            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void saxonTramsform() {
        try {
            net.sf.saxon.TransformerFactoryImpl transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
            net.sf.saxon.jaxp.TransformerImpl transformer = (net.sf.saxon.jaxp.TransformerImpl) transformerFactory.newTransformer(new StreamSource(xslFile.getAbsolutePath()));

            SaxonTemplateTraceListener traceListener = new SaxonTemplateTraceListener();
            this.traceListener = traceListener;

            transformer.getUnderlyingController().setTraceListener(traceListener);

            SaxonElementReceiver elementReceiver = new SaxonElementReceiver(traceListener);
            SaxonWriterReceiver writerReceiver = new SaxonWriterReceiver(writer);
            SaxonOutputReceiver receiver = new SaxonOutputReceiver(transformer.getUnderlyingController().makeBuilder(), writerReceiver, elementReceiver);

            transformer.getUnderlyingController().getInitialMode().setModeTracing(true);


            transformer.transform(new StreamSource(xmlFile.getAbsolutePath()), receiver);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
