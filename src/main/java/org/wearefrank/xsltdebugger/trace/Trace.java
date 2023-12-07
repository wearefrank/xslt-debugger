package org.wearefrank.xsltdebugger.trace;

import java.util.List;

public interface Trace {
    String getTraceId();
    void setTraceId(String traceId);
    int getLineNumber();
    void setLineNumber(int lineNumber);
    int getColumnNumber();
    void setColumnNumber(int columnNumber);
    String getSelectedNode();
    void setSelectedNode(String node);
    List<Trace> getChildTraces();
    void addChildTrace(Trace trace);
    void addTraceContext(String context);
    List<String> getTraceContext();
    String getWholeTrace(boolean showSeparator);
    Trace getParentTrace();
    String getSystemId();
    void setSystemId(String systemid);
}
