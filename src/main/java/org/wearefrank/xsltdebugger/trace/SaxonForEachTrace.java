package org.wearefrank.xsltdebugger.trace;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SaxonForEachTrace implements Trace{
    @Setter
    private String traceId;
    @Setter
    private int lineNumber;
    @Setter
    private int columnNumber;
    @Setter
    private String selectedNode;
    private final List<String> traceContext = new ArrayList<>();
    private final List<Trace> childTraces = new ArrayList<>();
    @Setter
    private String forEachTrace;
    private Trace parentTrace;
    @Setter
    private String systemId;

    public void addTraceContext(String context){
        this.traceContext.add(context);
    }

    public void addChildTrace(Trace trace){
        this.childTraces.add(trace);
    }

    public String getWholeTrace(boolean showSeparator){
        StringBuilder result = new StringBuilder();

        if(showSeparator) {
            result.append("--------------------------------------------New template being applied--------------------------------------------\n");
        }
        result.append(forEachTrace);

        for (String childrenTrace : traceContext) {
            result.append(childrenTrace);
        }

        return result.toString();
    }
}
