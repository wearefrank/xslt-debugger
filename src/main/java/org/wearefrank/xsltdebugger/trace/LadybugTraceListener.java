package org.wearefrank.xsltdebugger.trace;

/*Interface is necessary so that SaxonTraceListener or XalanTraceListener can be given to the same parameter in XSLTReporterSetup.
* Since the only thing that matters is the RootTrace object, this will be the only requirement for a TraceListener to have.*/
public interface LadybugTraceListener {
    /**Root trace of the tree that that holds all the traces.
     * @return returns the root trace*/
    Trace getRootTrace();
    Trace getSelectedTrace();
}
