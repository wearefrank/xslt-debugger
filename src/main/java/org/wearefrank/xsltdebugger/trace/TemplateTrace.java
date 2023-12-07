package org.wearefrank.xsltdebugger.trace;

import java.util.List;

public interface TemplateTrace extends Trace {
    String getTemplateMatch();
    void setTemplateMatch(String templateMatch);
    String getTemplateTrace();

    boolean isABuiltInTemplate();
    void setABuiltInTemplate(boolean isBuiltIn);
}
