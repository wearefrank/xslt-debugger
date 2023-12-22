package org.wearefrank.xsltdebugger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wearefrank.xsltdebugger.trace.NodeType;
import org.wearefrank.xsltdebugger.util.DocumentUtil;
import org.wearefrank.xsltdebugger.util.XPathUtil;
import org.wearefrank.xsltdebugger.trace.Trace;

import nl.nn.testtool.TestTool;

import org.xml.sax.SAXException;

import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class XSLTTraceReporter {
    private final TestTool testTool;
    private final File xmlFile;
    private final File xslFile;
    private final String xsltResult;
    private final Trace rootTrace;
    private final List<File> allXSLFiles;
    private final String correlationId;

    public XSLTTraceReporter(TestTool testTool, File xmlFile, File xslFile, Trace rootTrace, String xsltResult, String correlationId) {
        this.testTool = testTool;
        this.xmlFile = xmlFile;
        this.xslFile = xslFile;
        this.rootTrace = rootTrace;
        this.xsltResult = xsltResult;
        this.allXSLFiles = new ArrayList<>();
        this.allXSLFiles.add(this.xslFile);
        this.correlationId = correlationId;
    }

    public static void initiate(TestTool testTool, XSLTReporterSetup reporterSetup, String correlationId, String reportName) {
        XSLTTraceReporter reporter = new XSLTTraceReporter(testTool, reporterSetup.getXmlFile(), reporterSetup.getXslFile(), reporterSetup.getTraceListener().getRootTrace(), reporterSetup.getWriter().toString(), correlationId);
        testTool.startpoint(correlationId, null, reportName, "XSLT Trace");
        reporter.start();
        testTool.endpoint(correlationId, null, reportName, "XSLT Trace");
    }

    private void start() {
        testTool.startpoint(correlationId, xmlFile.getName(), "Start XSLT", "Start XSLT");
        try {
            List<String> xmlList = Files.readAllLines(Paths.get(xmlFile.getAbsolutePath()));
            StringWriter writer = new StringWriter();
            for (String xml : xmlList) {
                writer.append(xml).append("\n");
            }
            testTool.infopoint(correlationId, null, "XML input file", writer.toString());

            List<String> xslList = Files.readAllLines(Paths.get(xslFile.getAbsolutePath()));
            writer = new StringWriter();
            for (String xsl : xslList) {
                writer.append(xsl).append("\n");
            }
            testTool.infopoint(correlationId, xmlFile.getName(), "XSL input file", writer.toString());

            printImportedXsl();

            printCompleteTraceFromStack(rootTrace);

            printTransformedXml();

            testTool.startpoint(correlationId, null, "Trace layout", null);
            loopThroughAllTemplates(rootTrace);
            testTool.endpoint(correlationId, null, "Trace layout", null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        testTool.endpoint(correlationId, xmlFile.getName(), "Start XSLT", "End of XSLT");
    }

    /**
     * If there are XSL files being imported in the head file,
     * this method will create an import startpoint and show them in that point
     */
    private void printImportedXsl() {
        try {
            Document xslDocument = DocumentUtil.buildDocument(xslFile);
            if (!XPathUtil.fileHasNode("import", xslDocument))
                return; //If there are no import nodes present in the file, return.

            List<Node> nodeList = XPathUtil.getNodesByXPath("//*[local-name()='import']",xslDocument);
            testTool.startpoint(correlationId, xslFile.getName(), "Imported XSL", "Imported XSL files");
            // Loop over all the 'import' nodes (each node references 1 XSL file in its 'href' attribute)
            for (Node node : nodeList) {
                Element element = (Element) node; // Get the import element from current import node
                String importPath = element.getAttribute("href"); // Grab the file path from the 'href' attribute
                Path xslFilePath = Paths.get(importPath);
                this.allXSLFiles.add(xslFilePath.toFile()); // Add the imported XSL file to global variable for later reference
                writeFileToInfopoint(xslFilePath); //write the entire XSL file to the report as an infopoint
            }
            testTool.endpoint(correlationId, xslFile.getName(), "Imported XSL", "Imported XSL files");
        } catch (Exception e) {
            throw new RuntimeException(e);
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
        testTool.infopoint(correlationId, xslFile.getName(), filepath.getFileName().toString(), writer.toString());
    }


    /**
     * Prints the transformed xml in an infopoint with the titles
     */
    private void printTransformedXml() {
        testTool.infopoint(correlationId, xmlFile.getName(), "XML after full transformation", xsltResult);
    }

    /**
     * @param trace the trace where the search should start for the recursive method
     */
    private void printCompleteTraceFromStack(Trace trace) {
        String result = getAllTraces(trace);
        testTool.infopoint(correlationId, xslFile.getName(), "Complete XSLT Trace", result);
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
    private void printTraceXSL(Trace trace) throws IOException, SAXException, XPathExpressionException {
        for (File file : allXSLFiles) {
            boolean hasMatchAttribute = false;
            Document doc = DocumentUtil.buildDocument(file);
            StringWriter result = new StringWriter();
            if (trace.getNodeType() == NodeType.MATCH_TEMPLATE || trace.getNodeType() == NodeType.BUILT_IN_TEMPLATE) {
                List<Node> nodeList = XPathUtil.getNodesByXPath("//*[local-name()='template']", doc);
                for (Node node : nodeList) {
                    Element element = (Element) node;
                    if (element.getAttribute("match").equals(trace.getTraceMatch())) {
                        hasMatchAttribute = true;
                        StringBuilder stringBuilder = new StringBuilder();
                        getNodeIndentation(stringBuilder, node, 0, true);
                        result.append(stringBuilder).append("\n");
                    }
                }
                if (!hasMatchAttribute) continue;
            } else if (trace.getNodeType() == NodeType.FOREACH) {
                List<Node> nodeList = XPathUtil.getNodesByXPath("//*[local-name()='for-each']", doc);

                for (Node node : nodeList) {
                    Element element = (Element) node;

                    if (element.getAttribute("select").equals(trace.getTraceMatch())) {
                        hasMatchAttribute = true;
                        StringBuilder stringBuilder = new StringBuilder();
                        getNodeIndentation(stringBuilder, node, 0, true);
                        result.append(stringBuilder).append("\n");
                    }
                }
                if (!hasMatchAttribute) continue;
            }

            testTool.infopoint(correlationId, null,
                    "Line #" + trace.getLineNumber() + " Column #" + trace.getColumnNumber() + ": " + file.getName(),
                    result.toString());
        }
    }

    /**
     * Shows the affected XML of the XSLT trace
     */
    private String printTemplateXml(Trace trace) {
        try {
            List<Node> nodeList;

            Document doc = DocumentUtil.buildDocument(xmlFile);
            if (trace.getTraceMatch().startsWith("/")) {
                nodeList = XPathUtil.getNodesByXPath(trace.getTraceMatch(), doc);
            } else {
                nodeList = XPathUtil.getNodesByXPath("//" + trace.getTraceMatch(), doc);
            }
            StringWriter result = new StringWriter();

            for (Node node : nodeList) {
                StringBuilder stringBuilder = new StringBuilder();
                getNodeIndentation(stringBuilder, node, 0, false);
                result.append(stringBuilder).append("\n");
            }
            return result.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts the node into string
     *
     * @param indent      amount of indents it needs
     * @param node        Node to convert
     * @param needsIndent True/False if indents should be used
     * @param result      attached stringbuilder to write to
     */
    private void getNodeIndentation(StringBuilder result, Node node, int indent, boolean needsIndent) {
        if (needsIndent) {
            for (int i = 0; i < indent; i++) {
                result.append("\t");
            }
        }
        if (node.getNodeType() == Node.TEXT_NODE) {
            result.append(node.getNodeValue());
        } else {
            result.append("<").append(node.getNodeName());
            if (node.hasAttributes()) {
                NamedNodeMap attributes = node.getAttributes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    Node attribute = attributes.item(j);
                    result.append(" ").append(attribute.getNodeName()).append("=\"").append(attribute.getNodeValue()).append("\"");
                }
            }
            result.append(">");
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                getNodeIndentation(result, children.item(i), indent + 1, needsIndent);
            }
            if (needsIndent) {
                for (int i = 0; i < indent; i++) {
                    result.append("\t");
                }
            }
            result.append("</").append(node.getNodeName()).append(">");
        }
    }
}