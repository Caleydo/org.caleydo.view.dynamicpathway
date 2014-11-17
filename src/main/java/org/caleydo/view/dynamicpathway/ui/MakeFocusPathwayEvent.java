package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.AEvent;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;

public class MakeFocusPathwayEvent extends AEvent {
	private ControllbarPathwayTitleEntry contextPathwayToMakeFocus;
	
	public MakeFocusPathwayEvent(ControllbarPathwayTitleEntry contextPathwayToMakeFocus) {
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
