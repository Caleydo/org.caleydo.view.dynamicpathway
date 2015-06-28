package org.caleydo.view.dynamicpathway.events;

import org.caleydo.core.event.AEvent;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;

public class ChangeBubbleSetVisibilityEvent extends AEvent{
	
	private final PathwayGraph pathwayWithVisibilityToChange;
	private final boolean newBubbleSetsVisibilityStatus;
	
	public ChangeBubbleSetVisibilityEvent(PathwayGraph pathwayWithVisibilityToChange, boolean newBubbleSetsVisibilityStatus) {
		this.pathwayWithVisibilityToChange = pathwayWithVisibilityToChange;
		this.newBubbleSetsVisibilityStatus = newBubbleSetsVisibilityStatus;
	}

	public PathwayGraph getPathwayWithVisibilityToChange() {
		return pathwayWithVisibilityToChange;
	}

	public boolean getNewBubbleSetVisibilityStatus() {
		return newBubbleSetsVisibilityStatus;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

}
