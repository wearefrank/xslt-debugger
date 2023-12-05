package org.wearefrank.xsltdebugger.trace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TemplateTraceTest {
    TemplateTrace rootTrace = new TemplateTrace();

    @BeforeEach
    void setup(){
        rootTrace.addChildtrace(new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace));
        rootTrace.addChildtrace(new TemplateTrace("unnamed_trace", rootTrace));
        rootTrace.getChildTraces().get(1).setABuiltInTemplate(true);
    }

    @Test
    void addChildtrace() {
        rootTrace.addChildtrace(new TemplateTrace());
        assertNotNull(rootTrace.getChildTraces().get(2));
    }

    @Test
    void addTraceContext() {
        TemplateTrace trace = new TemplateTrace();
        trace.addTraceContext("inserted data");
        assertEquals("inserted data", trace.getTraceContext().get(0));
    }

    @Test
    void getWholeTraceWhenTrue() {
        TemplateTrace trace = new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        trace.addTraceContext("\ntest data");
        String actualTrace = trace.getWholeTrace(true);
        String expectedTrace = "--------------------------------------------New template being applied--------------------------------------------\n" +
                "template_trace\n" +
                "test data";
        assertEquals(expectedTrace, actualTrace);
    }

    @Test
    void getWholeTraceWhenFalse(){
        TemplateTrace trace = new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        trace.addTraceContext("\ntest data");
        String actualTrace = trace.getWholeTrace(false);
        String expectedTrace = "template_trace\ntest data";
        assertEquals(expectedTrace, actualTrace);
    }

    @Test
    void getTraceId() {
        TemplateTrace trace = new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        assertEquals("id_name", trace.getTraceId());
    }

    @Test
    void getParentTrace() {
        TemplateTrace parentTrace = new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        TemplateTrace childTrace = new TemplateTrace("match_name", "system_id_string", "template_trace", "different id_name", parentTrace);
        parentTrace.addChildtrace(childTrace);

        assertEquals(parentTrace.getTraceId(), childTrace.getParentTrace().getTraceId());
    }

    @Test
    void getTemplateMatch() {
        TemplateTrace trace = new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);

        assertEquals("match_name", trace.getTemplateMatch());
    }

    @Test
    void getTemplateTrace() {
        TemplateTrace trace = new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);

        assertEquals("template_trace", trace.getTemplateTrace());
    }

    @Test
    void getSystemId() {
        TemplateTrace trace = new TemplateTrace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        assertEquals("system_id_string", trace.getSystemId());
    }
}