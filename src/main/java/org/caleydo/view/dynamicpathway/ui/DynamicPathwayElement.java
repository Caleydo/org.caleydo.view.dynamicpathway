/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.dynamicpathway.layout.ForceDirectedFRGraphLayout;
import org.caleydo.view.dynamicpathway.layout.PathwayGraphWrapper;
import org.jgrapht.graph.DefaultEdge;
/**
 * element of this view holding a {@link TablePerspective}
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayElement extends AnimatedGLElementContainer {
	
	private PathwayGraphWrapper focusPathway;
	
	public DynamicPathwayElement(Vec2f area) {
		PathwayGraph graph = PathwayManager.get().getPathwayByTitle("Alzheimer's disease",
				EPathwayDatabaseType.KEGG);
		focusPathway = new PathwayGraphWrapper();
		focusPathway.addGraph(graph);
		
		
		Rectangle2D drawingArea = new Rectangle();
		drawingArea.setFrame(10,10,area.x(),area.y());
		

		ForceDirectedFRGraphLayout.getInstance().layout(focusPathway, drawingArea);

		setLayout(GLLayouts.LAYERS);
		for(PathwayVertexRep vrep : focusPathway.vertexSet()) {
			
			if(vrep.getType() == EPathwayVertexType.gene)
				add(new NodeElement(vrep));
		}
		

	}

	@Override
	public Vec2f getMinSize() {
		return new Vec2f(100, 100);
	}

}
