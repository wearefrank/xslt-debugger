package org.wearefrank.xsltdebugger;

import org.junit.jupiter.api.Test;

import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;

class XMLTransformationContextTest {

    @Test
    void shouldGetNameWithoutFile() {
        XMLTransformationContext context = new XMLTransformationContext("context");
        assertEquals("XSL input", context.getName());
    }

    @Test
    void shouldGetNameWithFile(){
        URL testUrl = XMLTransformationContextTest.class.getClassLoader().getResource("test.txt");
        assertNotNull(testUrl);
        XMLTransformationContext context = XMLTransformationContext.createContextFromFile(new File(testUrl.getFile()));
        assertEquals("test.txt", context.getName());
    }

    @Test
    void shouldGetAbsolutePathWithoutFile() {
        XMLTransformationContext context = new XMLTransformationContext("context");
        assertEquals("XSL input", context.getAbsolutePath());
    }

    @Test
    void shouldGetAbsolutePathWithFile(){
        URL testUrl = XMLTransformationContextTest.class.getClassLoader().getResource("test.txt");
        assertNotNull(testUrl);
        XMLTransformationContext context = XMLTransformationContext.createContextFromFile(new File(testUrl.getFile()));
        assertNotEquals("XSL input", context.getAbsolutePath());
    }

    @Test
    void shouldGetSystemIdWithoutFile() {
        XMLTransformationContext context = new XMLTransformationContext("context");
        assertEquals("XSL input", context.getSystemId());
    }

    @Test
    void shouldGetSystemIdWithFile() throws URISyntaxException {
        URL testUrl = XMLTransformationContextTest.class.getClassLoader().getResource("test.txt");
        assertNotNull(testUrl);
        XMLTransformationContext context = XMLTransformationContext.createContextFromFile(new File(testUrl.getFile()));
        assertEquals(testUrl.toURI().toString(), context.getSystemId());
    }

    @Test
    void shouldGetSourceObjectWithoutFile() {
        XMLTransformationContext context = new XMLTransformationContext("context");
        StreamSource source = context.getSourceObject();
        assertNotNull(source);
        assertEquals("XSL input", source.getSystemId());
    }

    @Test
    void shouldGetSourceObjectWithFile() throws URISyntaxException {
        URL testUrl = XMLTransformationContextTest.class.getClassLoader().getResource("test.txt");
        assertNotNull(testUrl);
        XMLTransformationContext context = XMLTransformationContext.createContextFromFile(new File(testUrl.getFile()));
        StreamSource source = context.getSourceObject();
        assertEquals(testUrl.toURI().toString(), source.getSystemId());
    }

    @Test
    void shouldGetContextWithoutFile() {
        XMLTransformationContext context = new XMLTransformationContext("context");
        assertEquals("context", context.getContext());
    }

    @Test
    void shouldGetContextWithFile() {
        URL testUrl = XMLTransformationContextTest.class.getClassLoader().getResource("test.txt");
        assertNotNull(testUrl);
        XMLTransformationContext context = XMLTransformationContext.createContextFromFile(new File(testUrl.getFile()));
        assertEquals("context", context.getContext());
    }
}
