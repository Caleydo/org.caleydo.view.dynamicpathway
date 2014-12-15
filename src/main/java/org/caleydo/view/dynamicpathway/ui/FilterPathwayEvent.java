package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;

public class FilterPathwayEvent extends AEvent {

	NodeElement newFocusNode;

	public FilterPathwayEvent(NodeElement nodeElementToFilterBy) {	
		this.newFocusNode = nodeElementToFilterBy;
	}



	@Override
	public boolean checkIntegrity() {
		return true;
	}



	public NodeElement getNodeElementToFilterBy() {
		return newFocusNode;
	}



	public void setNewFocusNode(NodeElement newFocusNode) {
		this.newFocusNode = newFocusNode;
	}

}
