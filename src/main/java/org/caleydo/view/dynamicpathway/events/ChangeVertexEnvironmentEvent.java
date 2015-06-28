package org.caleydo.view.dynamicpathway.events;

import org.caleydo.core.event.AEvent;

public class ChangeVertexEnvironmentEvent extends AEvent {
	
	private Integer newEnvSize;
	
	public ChangeVertexEnvironmentEvent(Integer newEnvSize) {
		this.newEnvSize = newEnvSize;
	}

	@Override
	public boolean checkIntegrity() {
		return true;
	}
	
	public int getNewEnvironmentSize() {
		return newEnvSize.intValue();
	}

}
