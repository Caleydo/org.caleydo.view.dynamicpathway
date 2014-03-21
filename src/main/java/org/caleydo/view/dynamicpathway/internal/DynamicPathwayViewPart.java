/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.internal;

import org.caleydo.core.view.ARcpGLElementViewPart;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementView;
import org.caleydo.view.dynamicpathway.internal.serial.SerializedDynamicPathwayView;

/**
 *
 * @author Christiane Schwarzl
 *
 */
public class DynamicPathwayViewPart extends ARcpGLElementViewPart {

	public DynamicPathwayViewPart() {
		super(SerializedDynamicPathwayView.class);
	}

	@Override
	protected AGLElementView createView(IGLCanvas canvas) {
		return new DynamicPathwayView(glCanvas);
	}
}
