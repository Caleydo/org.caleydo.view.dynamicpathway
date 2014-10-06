package org.caleydo.view.dynamicpathway.internal;

public class NodeMergingException extends Exception {
	
	private static final String ERROR_PREFIX = "Internal Tool Error (Node Merging): ";

	public NodeMergingException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public NodeMergingException(String message) {
		super(ERROR_PREFIX + message);

	}

}
