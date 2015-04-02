package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;

public class ChangeFocusNodeEvent extends AEvent {
	
	NodeElement newFocusNode;

	public ChangeFocusNodeEvent(NodeElement nodeElementToFilterBy) {	
		this.newFocusNode = nodeElementToFilterBy;
	}



	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}



	public NodeElement getNodeElementToFilterBy() {
		return newFocusNode;
	}



	public void setNewFocusNode(NodeElement newFocusNode) {
		this.newFocusNode = newFocusNode;
	}

}