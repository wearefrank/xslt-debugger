package org.wearefrank.xsltdebugger.trace;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.wearefrank.xsltdebugger.XSLTReporterSetup;

import java.io.File;
import java.io.IOException;

class XalanTemplateTraceListenerTest {

    @Test
    void shouldTransformCorrectly() throws IOException {
        File xmlFile = new File("src/test/resources/foo.xml");
        File xslFile = new File("src/test/resources/foo.xsl");
        File expectedResultFile = new File("src/test/resources/expected_xalan_transform.txt");

        String expectedResult = FileUtils.readFileToString(expectedResultFile);

        XSLTReporterSetup report = new XSLTReporterSetup(xmlFile, xslFile, 1);
        report.transform();

        Assertions.assertEquals(expectedResult, report.getWriter().toString());
    }
}