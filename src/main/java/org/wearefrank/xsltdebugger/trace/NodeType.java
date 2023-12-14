package org.wearefrank.xsltdebugger.trace;

/**XSLT-Debugger currently has support for 3 types of traces that can be used for start points
 * - template matches
 * - built in templates
 * - for-each
 * extend list if more are necessary*/
public enum NodeType {
    MATCH_TEMPLATE,
    BUILT_IN_TEMPLATE,
    FOREACH
}
