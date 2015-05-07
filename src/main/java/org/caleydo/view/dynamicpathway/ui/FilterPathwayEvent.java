package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;

public class FilterPathwayEvent extends AEvent {

	ANodeElement newFocusNode;

	public FilterPathwayEvent(ANodeElement nodeElementToFilterBy) {	
		this.newFocusNode = nodeElementToFilterBy;
	}



	@Override
	public boolean checkIntegrity() {
		return true;
	}



	public ANodeElement getNodeElementToFilterBy() {
		return newFocusNode;
	}



	public void setNewFocusNode(ANodeElement newFocusNode) {
		this.newFocusNode = newFocusNode;
	}

}
