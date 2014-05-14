/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.layout.GLDynamicPathwayLayout;
import org.jgrapht.graph.DefaultEdge;
/**
 * element of this view holding a {@link TablePerspective}
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayElement extends AnimatedGLElementContainer {
	
	private DynamicPathwayGraph pathway;

	private GLDynamicPathwayLayout pathwayLayout;
	private GLElementContainer vertices;
	private GLElementContainer edges;
	
	public DynamicPathwayElement() {
//		PathwayGraph focusGraph = PathwayManager.get().getPathwayByTitle("Alzheimer's disease",
//				EPathwayDatabaseType.KEGG);		1
		
		
		
		pathway = new DynamicPathwayGraph();
		
		pathwayLayout = new GLDynamicPathwayLayout();
		//TODO: change Layout to GLLayouts.DYNAMICPATHWAY
		vertices = new GLElementContainer(pathwayLayout);
		
		//TODO: change Layout to GLLayouts.GRIDEDGES
		edges = new GLElementContainer(GLLayouts.LAYERS);

		setLayout(GLLayouts.LAYERS);
		
//		add(vertices);


	}
	
	public void addPathwayRep(PathwayGraph graph) {
		pathway.addFocusOrKontextPathway(graph);
		
		vertices.clear();
		edges.clear();
		
		
		for(PathwayVertexRep vrep : pathway.getCombinedVertexSet()) {
			NodeElement node = new NodeElement(vrep);
			pathway.addVertexNodeMapEntry(vrep, node);
			vertices.add(node);
		}
		
		for(DefaultEdge e : pathway.getCombinedEdgeSet()) {
			PathwayVertexRep vrepSource = pathway.getEdgeSource(e);
			PathwayVertexRep vrepTarget = pathway.getEdgeTarget(e);
			NodeElement nodeSource = pathway.getNodeOfVertex(vrepSource);
			NodeElement nodeTarget = pathway.getNodeOfVertex(vrepTarget);
			
			EdgeElement edgeElement = new EdgeElement(e, nodeSource, nodeTarget);		
			edges.add(edgeElement);
		}
		
		add(vertices);
		add(edges);
		
		
		
		
	}
	
	
	
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
	}

	@Override
	public Vec2f getMinSize() {
		return new Vec2f(100, 100);
	}
	
	public DynamicPathwayGraph getDynamicPathway() {
		return pathway;
	}

	

}
