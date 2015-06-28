package org.caleydo.view.dynamicpathway.events;

import org.caleydo.core.event.AEvent;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.dynamicpathway.ui.ControlbarPathwayTitleEntry;

public class MakeFocusPathwayEvent extends AEvent {
	private ControlbarPathwayTitleEntry contextPathwayToMakeFocus;
	
	public MakeFocusPathwayEvent(ControlbarPathwayTitleEntry contextPathwayToMakeFocus) {
		this.contextPathwayToMakeFocus = contextPathwayToMakeFocus;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
	
	public PathwayGraph getPathway() {
		if(contextPathwayToMakeFocus != null)
			return contextPathwayToMakeFocus.getRepresentedPathway();
		return null;
	}

}
