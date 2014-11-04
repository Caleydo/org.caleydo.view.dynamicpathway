package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;

public class RemoveDisplayedPathwayEvent extends AEvent {
	ControllbarPathwayTitleEntry pathwayEntryToRemove;

	public RemoveDisplayedPathwayEvent(ControllbarPathwayTitleEntry pathwayEntryToRemove) {	
		this.pathwayEntryToRemove = pathwayEntryToRemove;
	}

	@Override
	public boolean checkIntegrity() {
		// TODO Auto-generated method stub
		return true;
	}

}
