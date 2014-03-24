package org.caleydo.view.dynamicpathway.layout;

import org.jgrapht.graph.DefaultEdge;

public class DefaultEdgeWrapper {
	
	DefaultEdge edge;
	String source;
	String target;
	
	public DefaultEdgeWrapper(DefaultEdge edge) {
		this.edge = edge;
		String sourceSplit = edge.toString().split(":")[0];
		String targetSpkit = edge.toString().split(":")[1];
		this.source = sourceSplit.substring(1, sourceSplit.length());
		this.target = targetSpkit.substring(0, targetSpkit.length() - 1);
	}

	
    public String getSource() {
        return source;
    }
    
    public Object getTarget() {
    	return target;
    }
    
    public String toString() {
    	return edge.toString();
    }
}
