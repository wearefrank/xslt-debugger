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
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class DocumentUtil {

    private static DocumentBuilderFactory newDocumentBuilderFactory() { return DocumentBuilderFactory.newInstance(); }

    public static DocumentBuilder getDocumentBuilder() {
        try {
            return newDocumentBuilderFactory().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    public static Document buildDocument(File file) throws IOException, SAXException {
        Document newDocument = getDocumentBuilder().parse(file);
        newDocument.getDocumentElement().normalize();
        return newDocument;
    }

    public static List<String> readFile(Path filepath) throws IOException { return Files.readAllLines(filepath); }
}
