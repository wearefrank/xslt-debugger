package org.wearefrank.xsltdebugger.trace;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TraceTest {
    Trace rootTrace = new Trace();

    @BeforeEach
    void setup(){
        rootTrace.addChildTrace(new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace));
        rootTrace.addChildTrace(new Trace("unnamed_trace", rootTrace));
        rootTrace.getChildTraces().get(1).setNodeType(NodeType.BUILT_IN_TEMPLATE);
    }

    @Test
    void shouldAddChildtrace() {
        rootTrace.addChildTrace(new Trace());
        assertNotNull(rootTrace.getChildTraces().get(2));
    }

    @Test
    void shouldAddTraceContext() {
        Trace trace = new Trace();
        trace.addTraceContext("inserted data");
        assertEquals("inserted data", trace.getTraceContext().get(0));
    }

    @Test
    void shouldGetWholeTraceWhenTrue() {
        Trace trace = new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        trace.addTraceContext("\ntest data");
        String actualTrace = trace.getWholeTrace(true);
        String expectedTrace = "--------------------------------------------New trace instruction being applied--------------------------------------------\n" +
                "template_trace\n" +
                "test data";
        assertEquals(expectedTrace, actualTrace);
    }

    @Test
    void shouldGetWholeTraceWhenFalse(){
        Trace trace = new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        trace.addTraceContext("\ntest data");
        String actualTrace = trace.getWholeTrace(false);
        String expectedTrace = "template_trace\ntest data";
        assertEquals(expectedTrace, actualTrace);
    }

    @Test
    void shouldGetTraceId() {
        Trace trace = new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        assertEquals("id_name", trace.getTraceId());
    }

    @Test
    void shouldGetParentTrace() {
        Trace parentTrace = new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        Trace childTrace = new Trace("match_name", "system_id_string", "template_trace", "different id_name", parentTrace);
        parentTrace.addChildTrace(childTrace);
        assertEquals(parentTrace.getTraceId(), childTrace.getParentTrace().getTraceId());
    }

    @Test
    void shouldGetTemplateMatch() {
        Trace trace = new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        assertEquals("match_name", trace.getTraceMatch());
    }

    @Test
    void shouldGetTemplateTrace() {
        Trace trace = new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        assertEquals("template_trace", trace.getHeadTraceContext());
    }

    @Test
    void shouldGetSystemId() {
        Trace trace = new Trace("match_name", "system_id_string", "template_trace", "id_name", rootTrace);
        assertEquals("system_id_string", trace.getSystemId());
    }
}