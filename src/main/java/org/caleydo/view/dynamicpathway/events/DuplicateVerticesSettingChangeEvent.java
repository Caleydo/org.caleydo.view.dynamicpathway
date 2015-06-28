package org.caleydo.view.dynamicpathway.events;

import org.caleydo.core.event.AEvent;

public class DuplicateVerticesSettingChangeEvent extends AEvent {
	
	private final boolean allowDuplicateVertices;
	
	public DuplicateVerticesSettingChangeEvent(boolean allowDuplicateVertices) {
		this.allowDuplicateVertices = allowDuplicateVertices;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

	public boolean allowDuplicateVertices() {
		return allowDuplicateVertices;
	}

}
