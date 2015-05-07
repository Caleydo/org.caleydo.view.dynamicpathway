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
		// TODO Auto-generated method stub
		return true;
	}
	
	public PathwayGraph getPathway() {
		if(contextPathwayToMakeFocus != null)
			return contextPathwayToMakeFocus.getRepresentedPathway();
		//TODO: exception
		return null;
	}

}
