package org.caleydo.view.dynamicpathway.internal;

public class NodeMergingException extends Exception {
	

	private static final long serialVersionUID = 1L;
	private static final String ERROR_PREFIX = "Internal Tool Error (Node Merging): ";

	public NodeMergingException() {
		super();
	}

	public NodeMergingException(String message) {
		super(ERROR_PREFIX + message);

	}

}
