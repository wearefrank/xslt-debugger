package org.wearefrank.xsltdebugger.util;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;
import static org.wearefrank.xsltdebugger.util.XPathUtil.fileHasNode;
import static org.wearefrank.xsltdebugger.util.XPathUtil.getNodesByXPath;

class XPathUtilTest {

    @Test
    void isFileHasNode() {
        try {
            String xmlString = "<root><element>Content</element></root>";

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(xmlString)));
            assertTrue(fileHasNode("element", document));

            assertFalse(true);
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }

    @Test
    void shouldGetNodesByXPath(){
        try {
            ;
            String xmlString = "<root><element>Content</element></root>";

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(xmlString)));

            NodeList nodeList = getNodesByXPath("//*[local-name()='element']", document);
            assertEquals("Content", nodeList.item(0).getTextContent());
        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
}