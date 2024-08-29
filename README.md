XSLT debugger based on Ladybug that shows how an XSLT transformation is applied, supporting the debugging of XSLT transformations.

This tool cooperates with https://github.com/wearefrank/xslt-debugger-webapp to be a web application that debugs XSLT stylesheets. The user can enter an XSLT and an XML document. The result is a Ladybug report that can be viewed by the UI of Ladybug, which is available as a dependency, see http://github.com/wearefrank/ladybug. The user can also use whether to use XSLT 1 (Xalan) or XSLT 2/3 (Saxon).

WeAreFrank! hosts a server that runs this project. It can be reached at https://xslt-debugger.wearefrank.org/. Please note that the code is under development, so do not expect perfect quality yet. At this URL, the ladybug reports produced can also be viewed.

When new commits are done on this repository or on the webapp, a Jenkins server hosted by WeAreFrank! builds the code and deploys it on the server.

# Implementation

Related projects are:
* https://github.com/wearefrank/xslt-debugger-webapp - The frontend of this tool.
* http://github.com/wearefrank/ladybug - The tool that captures and shows Ladybug reports, the results of executing this tool.
* https://github.com/wearefrank/saxon - A fork of the Saxon project edited to provide information about intermediate processing steps. This information is captured by this project. Mark van der Vorst recommended not to update this fork to base it on the newest Saxon. That code has been refactored so that extracting the required debug information is more complicated. For Xalan, no fork is needed to provide this information.

The central class of this tool is `org.wearefrank.xsltdebugger.trace.Trace`. This class carries the information provided by Saxon or Xalan about how an XSLT was applied. Instances of this class are created by classes `SaxonTraceListener` or `XalanTraceListener`, which are implementations of interfaces provided by Saxon / Xalan. Both of these classes also implement interface `LadybugTraceListener`, which provides methods to browse the `Trace` objects. Objects of class `Trace` form a tree - all elements except the root have a parent and each `Trace` can have child objects of type `Trace`.

To build a Ladybug report, the `Trace` objects are browsed by `XSLTTraceReporter`. This class calls methods on an instance of `nl.nn.testtool.TestTool` to create the checkpoints of the produced Ladybug report.

`Trace` objects have a `org.wearefrank.xsltdebugger.trace.NodeType` that indicates what kind of XSLT code was executed for a checkpoint. This tool uses the `NodeType` to omit some information from Ladybug reports. Please check the code for details.

In the frontend, XML and XSLT texts can be typed in edit fields or they can be provided as uploaded files. This is made transparent using the `XMLTransformationContext` class.