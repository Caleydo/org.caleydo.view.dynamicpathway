/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D;
import java.util.Vector;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
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
	
	private PathwayGraphWrapper pathway;
	private Rectangle2D area;
	
	
	public DynamicPathwayElement() {
		PathwayGraph graph = PathwayManager.get().getPathwayByTitle("Alzheimer's disease",
				EPathwayDatabaseType.KEGG);
		pathway = new PathwayGraphWrapper(graph);
		
		
		area = new Rectangle(graph.getWidth(), graph.getHeight());	
//		area = new Rectangle();
		
//		ForceDirectedFRGraphLayout.getInstance().layout(pathway, area);
//		
		setLayout(GLLayouts.LAYERS);
		for(NodeElement node : pathway.nodeElementSet()) {

			add(node);
		}
		

	}
	
	Boolean firstRun = true;
	
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
//		if(area.getHeight() != h || area.getWidth() != w) {
			area.setFrame(0, 0, w, h);
			//System.out.println("width: " + w + " height:" + h);
			
		ForceDirectedFRGraphLayout.getInstance().layout(pathway, area);
		

		
		
		

//		repaintAll();
//		relayout();

		super.renderImpl(g, w, h);
	}

	@Override
	public Vec2f getMinSize() {
		return new Vec2f(100, 100);
	}

}
