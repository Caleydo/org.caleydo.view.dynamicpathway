/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.internal;

import org.caleydo.core.view.ARcpGLViewPart;
import org.caleydo.view.dynamicpathway.internal.serial.SerializedDynamicPathwayView;
import org.caleydo.view.entourage.GLEntourage;
import org.eclipse.swt.widgets.Composite;

/**
 *
 * @author Christiane Schwarzl
 *
 */
public class DynamicPathwayViewPart extends ARcpGLViewPart {

	public DynamicPathwayViewPart() {
		super(SerializedDynamicPathwayView.class);
	}
	
	@Override
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);

		DynamicPathwayView subgraph = new DynamicPathwayView(glCanvas);
		view = subgraph;
		initializeView();

		createPartControlGL();

		// DataMappers.getDataMapper().show();
	}

	@Override
	public void createDefaultSerializedView() {
		serializedView = new SerializedDynamicPathwayView();
		determineDataConfiguration(serializedView);		
	}

}
