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

/**
 * Document utilities
 */
public class DocumentUtil {
    private static DocumentBuilderFactory newDocumentBuilderFactory() {
        return DocumentBuilderFactory.newInstance();
    }

    public static DocumentBuilder getDocumentBuilder() {
        try {
            return newDocumentBuilderFactory().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Creates a Document object based on the given file
     *
     * @param file File object to make a Document object out of
     */
    public static Document buildDocument(File file) throws IOException, SAXException {
        Document newDocument = getDocumentBuilder().parse(file);
        newDocument.getDocumentElement().normalize();
        return newDocument;
    }

    /**
     * Reads a file and returns a list of every line on string in the specified file
     *
     * @param filepath location of file
     */
    public static List<String> readFile(Path filepath) throws IOException {
        return Files.readAllLines(filepath);
    }
}
