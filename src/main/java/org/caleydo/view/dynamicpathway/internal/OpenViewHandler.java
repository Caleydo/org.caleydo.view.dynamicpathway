/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.internal;

import org.caleydo.core.gui.command.AOpenViewHandler;

/**
 * simple command handler for opening this view
 *
 * @author Christiane Schwarzl
 *
 */
public class OpenViewHandler extends AOpenViewHandler {
	public OpenViewHandler() {
		super(DynamicPathwayView.VIEW_TYPE, MULTIPLE);
	}
}
