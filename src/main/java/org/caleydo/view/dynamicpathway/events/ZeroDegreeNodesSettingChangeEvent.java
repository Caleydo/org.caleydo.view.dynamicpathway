package org.caleydo.view.dynamicpathway.events;

import org.caleydo.core.event.AEvent;

public class ZeroDegreeNodesSettingChangeEvent extends AEvent {
	
	private final boolean allowZeroDegreeNodes;

	public ZeroDegreeNodesSettingChangeEvent(boolean allowZeroDegreeNodes) {
		this.allowZeroDegreeNodes = allowZeroDegreeNodes;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}

	public boolean allowZeroDegreeNodes() {
		return allowZeroDegreeNodes;
	}

}
