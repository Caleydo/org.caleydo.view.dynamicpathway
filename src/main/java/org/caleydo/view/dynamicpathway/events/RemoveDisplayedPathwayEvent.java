package org.caleydo.view.dynamicpathway.events;

import org.caleydo.core.event.AEvent;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.dynamicpathway.ui.ControlbarPathwayTitleEntry;

public class RemoveDisplayedPathwayEvent extends AEvent {
	ControlbarPathwayTitleEntry pathwayEntryToRemove;

	public RemoveDisplayedPathwayEvent(ControlbarPathwayTitleEntry pathwayEntryToRemove) {	
		this.pathwayEntryToRemove = pathwayEntryToRemove;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
	
	public PathwayGraph getPathway() {
			return pathwayEntryToRemove.getRepresentedPathway();
	}

}
