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
package org.wearefrank.xsltdebugger.trace;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class XalanTemplateTrace implements TemplateTrace{
    @Setter
    private String traceId;
    private TemplateTrace parentTrace;
    @Setter
    private String templateMatch;
    private String templateTrace;
    @Setter
    private String systemId;
    private final List<String> traceContext = new ArrayList<>();
    private final List<TemplateTrace> childTraces = new ArrayList<>();
    @Setter
    private String selectedNode;
    @Setter
    private boolean aBuiltInTemplate;
    @Setter
    private int lineNumber;
    @Setter
    private int columnNumber;

    public XalanTemplateTrace(String templateMatch, String systemId, String templateTrace, String id, TemplateTrace parentTrace) {
        this.templateMatch = templateMatch;
        this.templateTrace = templateTrace;
        this.systemId = systemId;
        this.traceId = id;
        this.parentTrace = parentTrace;
    }

    /**This method adds a child trace object to the list of child traces
     * @param trace TemplateTrace object that will be added to child traces*/
    public void addChildTrace(Trace trace){
        this.childTraces.add((SaxonTemplateTrace) trace);
    }

    /**This method adds a trace to the children traces of the parent template trace
     * @param context adds a context trace to this trace*/
    public void addTraceContext(String context) {
        this.traceContext.add(context);
    }

    /**@param showSeparator determines whether it shows a line to separate the traces.
     * @return Returns a string that holds the complete trace of the transform*/
    public String getWholeTrace(boolean showSeparator){
        StringBuilder result = new StringBuilder();

        if(showSeparator) {
            result.append("--------------------------------------------New template being applied--------------------------------------------\n");
        }
        result.append(templateTrace);

        for (String childrenTrace : traceContext) {
            result.append(childrenTrace);
        }

        return result.toString();
    }
}
