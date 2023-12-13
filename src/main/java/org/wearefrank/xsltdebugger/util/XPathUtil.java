/*
   Copyright 2023 WeAreFrank!

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/


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

public class XPathUtil {

    public static XPathExpression createXPathExpression(String xpath) throws XPathExpressionException {
        return XPathFactory.newInstance().newXPath().compile(xpath);
    }

    /**
     * Searches for the given node in a document
     *
     * @param nodeName The name of the node to look for
     * @param doc      document used to search
     * @return returns true if the given node exists
     */
    public static boolean fileHasNode(String nodeName, Document doc) {
        try {
            if (nodeName.contains(":")) {
                //if the given node has a namespace prefix, strip the prefix.
                return getNodesByXPath("//*[local-name()='" + nodeName.substring(nodeName.indexOf(":") + 1) + "']", doc).isEmpty();
            }
            return getNodesByXPath("//*[local-name()='" + nodeName + "']", doc).isEmpty();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the nodelist from a document with xPath expression
     *
     * @param doc             document to convert to Nodelist
     * @param expression given xPathExpression to search by
     * @return return the nodelist from xPathExpression
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