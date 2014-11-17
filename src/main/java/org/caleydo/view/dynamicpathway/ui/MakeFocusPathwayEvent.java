package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;

public class MakeFocusPathwayEvent extends AEvent {
	private ControllbarPathwayTitleEntry contextPathwayToMakeFocus;
	
	public MakeFocusPathwayEvent(ControllbarPathwayTitleEntry contextPathwayToMakeFocus) {
		this.contextPathwayToMakeFocus = contextPathwayToMakeFocus;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public String getPathwayTitle() {
		if(contextPathwayToMakeFocus != null)
			return contextPathwayToMakeFocus.getPathwayTitle();
		//TODO: exception
		return null;
	}

}
