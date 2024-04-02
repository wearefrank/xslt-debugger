package org.wearefrank.xsltdebugger;

import nl.nn.testtool.TestTool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.wearefrank.xsltdebugger.trace.NodeType;
import org.wearefrank.xsltdebugger.trace.Trace;
import org.wearefrank.xsltdebugger.util.DocumentUtil;
import org.wearefrank.xsltdebugger.util.XPathUtil;
import org.wearefrank.xsltdebugger.util.XmlUtil;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class XSLTTraceReporter {
    private final TestTool testTool;
    private final XMLTransformationContext xmlContext;
    private final XMLTransformationContext xslContext;
    private final String xsltResult;
    private final Trace rootTrace;
    private final List<XMLTransformationContext> allXSLContext;
    private final String correlationId;

    private XSLTTraceReporter(TestTool testTool, XMLTransformationContext xmlContext, XMLTransformationContext xslContext, Trace rootTrace, String xsltResult, String correlationId) {
        this.testTool = testTool;
        this.xmlContext = xmlContext;
        this.xslContext = xslContext;
        this.rootTrace = rootTrace;
        this.xsltResult = xsltResult;
        this.allXSLContext = new ArrayList<>();
        this.allXSLContext.add(this.xslContext);
        this.correlationId = correlationId;
    }

    public static void initiate(TestTool testTool, XSLTReporterSetup reporterSetup, String correlationId, String reportName) {
        XSLTTraceReporter reporter = new XSLTTraceReporter(testTool, reporterSetup.getXmlContext(), reporterSetup.getXslContext(), reporterSetup.getTraceListener().getRootTrace(), reporterSetup.getWriter().toString(), correlationId);
        testTool.startpoint(correlationId, null, reportName, "XSLT Trace");
        reporter.start();
        testTool.endpoint(correlationId, null, reportName, "XSLT Trace");
    }

    private void start() {
        testTool.startpoint(correlationId, xmlContext.getName(), "Start XSLT", "Start XSLT");
        try {
            testTool.infopoint(correlationId, null, "XML input", xmlContext.getContext());
            testTool.infopoint(correlationId, xmlContext.getName(), "XSL input", xslContext.getContext());
            printImportedXsl();
            printCompleteTraceFromStack(rootTrace);
            printTransformedXml();

            testTool.startpoint(correlationId, null, "Trace layout", null);
            loopThroughAllTemplates(rootTrace);
            testTool.endpoint(correlationId, null, "Trace layout", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        testTool.endpoint(correlationId, xmlContext.getName(), "Start XSLT", "End of XSLT");
    }

    /**
     * If there are XSL files being imported in the head file,
     * this method will create an import startpoint and show them in that point
     */
    private void printImportedXsl() {
        try {
            Document xslDocument = DocumentUtil.buildDocument(xslContext);
            if (XPathUtil.fileHasNode("include", xslDocument)) {

                List<Node> nodeList = XPathUtil.getNodesByXPath("//*[local-name()='include']", xslDocument);
                testTool.startpoint(correlationId, xslContext.getName(), "Included XSL", "Included XSL files");
                // Loop over all the 'included' nodes (each node references 1 XSL file in its 'href' attribute)
                loopThroughImportedXsl(nodeList);
                testTool.endpoint(correlationId, xslContext.getName(), "Included XSL", "Included XSL files");
            }
            if (XPathUtil.fileHasNode("import", xslDocument)) {
                List<Node> nodeList = XPathUtil.getNodesByXPath("//*[local-name()='import']", xslDocument);
                testTool.startpoint(correlationId, xslContext.getName(), "Imported XSL", "Imported XSL files");
                // Loop over all the 'import' nodes (each node references 1 XSL file in its 'href' attribute)
                loopThroughImportedXsl(nodeList);
                testTool.endpoint(correlationId, xslContext.getName(), "Imported XSL", "Imported XSL files");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void loopThroughImportedXsl(List<Node> nodeList) throws IOException {
        for (Node node : nodeList) {
            Element element = (Element) node; // Get the import element from current import node
            String importPath = element.getAttribute("href"); // Grab the file path from the 'href' attribute
            Path xslFilePath = Paths.get(importPath);
            this.allXSLContext.add(XMLTransformationContext.createContextFromFile(xslFilePath.toFile())); // Add the imported XSL file to global variable for later reference
            writeFileToInfopoint(xslFilePath); //write the entire XSL file to the report as an infopoint
        }
    }

    /**
     * Reads the file and puts all lines into one string to show the contents in an infopoint
     */
    private void writeFileToInfopoint(Path filepath) throws IOException {
        StringWriter writer = new StringWriter();
        for (String xsl : DocumentUtil.readFile(filepath)) {
            writer.append(xsl).append("\n");
        }
        testTool.infopoint(correlationId, xslContext.getName(), filepath.getFileName().toString(), writer.toString());
    }


    /**
     * Prints the transformed xml in an infopoint with the titles
     */
    private void printTransformedXml() {
        testTool.infopoint(correlationId, xmlContext.getName(), "XML after full transformation", xsltResult);
    }

    /**
     * @param trace the trace where the search should start for the recursive method
     */
    private void printCompleteTraceFromStack(Trace trace) {
        String result = getAllTraces(trace);
        testTool.infopoint(correlationId, xslContext.getName(), "Complete XSLT Trace", result);
    }

    /**
     * Recursively going through all traces that are in the child nodes of the given trace object.
     *
     * @param trace the trace where it looks through the child nodes
     */
    private String getAllTraces(Trace trace) {
        StringBuilder result = new StringBuilder();
        if (trace.getChildTraces().isEmpty()) return "";
        for (Trace childTrace : trace.getChildTraces()) {
            result.append(childTrace.getWholeTrace(true)).append("\n");
            result.append(getAllTraces(childTrace));
        }
        return result.toString();
    }

    /**
     * This method iterates over all trace nodes recursively
     *
     * @param trace the trace where it should start looking through the child nodes
     */
    private void loopThroughAllTemplates(Trace trace) {
        try {
            if (trace.getChildTraces().isEmpty()) return;
            for (Trace childTrace : trace.getChildTraces()) {
                if (childTrace.getNodeType() == NodeType.MATCH_TEMPLATE) {
                    if (childTrace.getTraceMatch() != null) {
                        testTool.startpoint(correlationId, childTrace.getTraceId(), "template match=" + childTrace.getTraceMatch(), printTemplateXml(childTrace));
                        printTraceXSL(childTrace);
                    }
                    loopThroughAllTemplates(childTrace);
                    if (childTrace.getTraceMatch() != null) {
                        testTool.endpoint(correlationId, childTrace.getTraceId(), "template match=" + childTrace.getTraceMatch(), childTrace.getWholeTrace(false));
                    }
                } else if (childTrace.getNodeType() == NodeType.FOREACH) {
                    testTool.startpoint(correlationId, childTrace.getTraceId(), "for-each select=" + childTrace.getTraceMatch(), printTemplateXml(childTrace));
                    printTraceXSL(childTrace);
                    loopThroughAllTemplates(childTrace);
                    testTool.endpoint(correlationId, childTrace.getTraceId(), "for-each select=" + childTrace.getTraceMatch(), childTrace.getWholeTrace(false));
                }
                //todo: save this code until solution for optional built-in-rules has been made
//                else {
//                    testTool.startpoint(correlationId, templateTrace.getTraceId(), "built-in-rule match=" + templateTrace.getTemplateMatch() + " node=" + templateTrace.getSelectedNode(), templateTrace.getWholeTrace(false));
//                    printTemplateXsl(templateTrace.getTemplateMatch());
//                    loopThroughAllTemplates(templateTrace);
//                    testTool.endpoint(correlationId, templateTrace.getTraceId(), "built-in-rule match=" + templateTrace.getTemplateMatch() + " node=" + templateTrace.getSelectedNode(), templateTrace.getWholeTrace(false));
//                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Show the given template XSL from all XSL files that contain the given template match
     *
     * @param trace template match inside trace object to look for in XSL files
     */
    private void printTraceXSL(Trace trace) throws IOException, SAXException, XPathExpressionException, TransformerException {
        for (XMLTransformationContext context : allXSLContext) {
            Document doc = DocumentUtil.buildDocument(context);
            StringBuilder stringBuilder = new StringBuilder();
            if (trace.getNodeType() == NodeType.MATCH_TEMPLATE || trace.getNodeType() == NodeType.BUILT_IN_TEMPLATE) {
                List<Node> nodeList = XPathUtil.getNodesByXPath("//*[local-name()='template']", doc);
                for (Node node : nodeList) {
                    Element element = (Element) node;
                    if (element.getAttribute("match").equals(trace.getTraceMatch())) {
                        stringBuilder.append(XmlUtil.getXmlFormatFromNode(node));
                    }
                }
            } else if (trace.getNodeType() == NodeType.FOREACH) {
                List<Node> nodeList = XPathUtil.getNodesByXPath("//*[local-name()='for-each']", doc);
                for (Node node : nodeList) {
                    Element element = (Element) node;
                    if (element.getAttribute("select").equals(trace.getTraceMatch())) {
                        stringBuilder.append(XmlUtil.getXmlFormatFromNode(node));
                    }
                }
            }
            if (stringBuilder.length() != 0) {
                String result = stringBuilder.toString();
                result = result.replace("xmlns:xsl=\"http://www.w3.org/1999/XSL/Transform\" ", "");
                testTool.infopoint(correlationId, null,
                        "Line #" + trace.getLineNumber() + " Column #" + trace.getColumnNumber() + ": " + context.getName(),
                        result);
            }
        }
    }

    /**
     * Shows the affected XML of the XSLT trace
     */
    private String printTemplateXml(Trace trace) throws TransformerException, IOException, SAXException, XPathExpressionException {
        StringWriter writer = new StringWriter();
        Document doc = DocumentUtil.buildDocument(xmlContext);
        List<Node> nodeList;
        if (trace.getTraceMatch().startsWith("/")) {
            nodeList = XPathUtil.getNodesByXPath(trace.getTraceMatch(), doc);
        } else if (trace.getTraceMatch().startsWith("*")) {
            nodeList = XPathUtil.getNodesByXPath("/" + trace.getTraceMatch(), doc);
        } else {
            nodeList = XPathUtil.getNodesByXPath("//" + trace.getTraceMatch(), doc);
        }
        for (Node node : nodeList) {
            writer.append(XmlUtil.getXmlFormatFromNode(node));
        }
        return writer.toString();
    }
}
