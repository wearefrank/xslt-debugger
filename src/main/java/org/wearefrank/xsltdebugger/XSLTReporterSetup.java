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
            saxonTransform();
        } else {
            throw new RuntimeException("ERROR: Invalid xslt version");
        }
    }

    private void xalanTransform() {
        XalanTraceListener traceListener = new XalanTraceListener();
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

    private void saxonTransform() {
        try {
            net.sf.saxon.TransformerFactoryImpl transformerFactory = new net.sf.saxon.TransformerFactoryImpl();
            net.sf.saxon.jaxp.TransformerImpl transformer = (net.sf.saxon.jaxp.TransformerImpl) transformerFactory.newTransformer(new StreamSource(xslFile.getAbsolutePath()));

            SaxonTraceListener traceListener = new SaxonTraceListener();
            this.traceListener = traceListener;

            transformer.getUnderlyingController().setTraceListener(traceListener);

            SaxonElementReceiver elementReceiver = new SaxonElementReceiver(traceListener);
            SaxonWriterReceiver writerReceiver = new SaxonWriterReceiver(writer);
            SaxonOutputSplitter receiver = new SaxonOutputSplitter(transformer.getUnderlyingController().makeBuilder(), writerReceiver, elementReceiver);

            transformer.getUnderlyingController().getInitialMode().setModeTracing(true);


            transformer.transform(new StreamSource(xmlFile.getAbsolutePath()), receiver);
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
