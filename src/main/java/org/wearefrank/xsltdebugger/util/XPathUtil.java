package org.wearefrank.xsltdebugger.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;
import java.util.ArrayList;
import java.util.List;

/**
 * XPath utilities
 */
public class XPathUtil {

    /**
     * Creates an XPathExpression object based on the XPath expression string given
     *
     * @param xpath XPath expression string to make an XPathExpression object out of
     */
    public static XPathExpression createXPathExpression(String xpath) throws XPathExpressionException {
        return XPathFactory.newInstance().newXPath().compile(xpath);
    }

    /**
     * Searches for the given node in a document
     *
     * @param nodeName The name of the unprefixed node to look for
     * @param doc      document used to search
     * @return returns true if the given node exists
     */
    public static boolean fileHasNode(String nodeName, Document doc) {
        try {
            return !getNodesByXPath("//*[local-name()='" + nodeName + "']", doc).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the NodeList from a document with xPath expression
     *
     * @param doc             document to convert to NodeList
     * @param expression given xPathExpression to search by
     * @return return the NodeList from xPathExpression
     * @throws XPathExpressionException if there is an error in the XPath expression
     */
    public static List<Node> getNodesByXPath(String expression, Document doc) throws XPathExpressionException {
        return nodeListToList((NodeList)createXPathExpression(expression).evaluate(doc.getDocumentElement().getChildNodes(), XPathConstants.NODESET));
    }

    private static List<Node> nodeListToList(NodeList startList) {
        List<Node> newList = new ArrayList<>();
        for (int i = 0; i < startList.getLength(); i++) {
            newList.add(startList.item(i));
        }
        return newList;
    }
}