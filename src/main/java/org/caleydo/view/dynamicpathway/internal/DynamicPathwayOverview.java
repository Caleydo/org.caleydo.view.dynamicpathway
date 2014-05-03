package org.caleydo.view.dynamicpathway.internal;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.view.dynamicpathway.ui.DynamicPathwayElement;

public class DynamicPathwayOverview extends AnimatedGLElementContainer {
	private DynamicPathwayElement dynamicPathwayElem;
	
	public DynamicPathwayOverview() {
		dynamicPathwayElem = new DynamicPathwayElement();
		dynamicPathwayElem.setLocation(200, 0);
		
		setLayout(GLLayouts.LAYERS);
		add(dynamicPathwayElem);
	}
	
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
	}

}
