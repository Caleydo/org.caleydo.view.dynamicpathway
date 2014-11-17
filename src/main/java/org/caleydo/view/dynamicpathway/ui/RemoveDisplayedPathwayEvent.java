package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;

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
	
	public PathwayGraph getPathway() {
		if(pathwayEntryToRemove != null)
			return pathwayEntryToRemove.getRepresentedPathway();
		//TODO: exception
		return null;
	}

}
