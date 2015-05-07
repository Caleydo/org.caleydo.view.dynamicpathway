package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;

public class ChangeFocusNodeEvent extends AEvent {
	
	ANodeElement newFocusNode;

	public ChangeFocusNodeEvent(ANodeElement nodeElementToFilterBy) {	
		this.newFocusNode = nodeElementToFilterBy;
	}



	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}



	public ANodeElement getNodeElementToFilterBy() {
		return newFocusNode;
	}



	public void setNewFocusNode(ANodeElement newFocusNode) {
		this.newFocusNode = newFocusNode;
	}

}
