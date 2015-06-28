/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.internal.serial;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.ISingleTablePerspectiveBasedView;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwayView;

/**
 *
 * @author Christiane Schwarzl
 *
 */
@XmlRootElement
@XmlType
public class SerializedDynamicPathwayView extends ASerializedView {

	/**
	 * Default constructor with default initialization
	 */
	public SerializedDynamicPathwayView() {
	}

	public SerializedDynamicPathwayView(ISingleTablePerspectiveBasedView view) {
		super(view);
	}

	@Override
	public String getViewType() {
		return DynamicPathwayView.VIEW_TYPE;
	}
}
