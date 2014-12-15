package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;

public class ChangeFocusNodeEvent extends AEvent {
	
	NodeElement nodeElementToFilterBy;

	public ChangeFocusNodeEvent(NodeElement nodeElementToFilterBy) {	
		this.nodeElementToFilterBy = nodeElementToFilterBy;
	}



	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}



	public NodeElement getNodeElementToFilterBy() {
		return nodeElementToFilterBy;
	}



	public void setNodeElementToFilterBy(NodeElement nodeElementToFilterBy) {
		this.nodeElementToFilterBy = nodeElementToFilterBy;
	}

}
