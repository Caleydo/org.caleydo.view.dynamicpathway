/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import gleem.linalg.Vec2f;

import org.caleydo.core.data.perspective.table.TablePerspective;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout2;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutGraph;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.jgrapht.graph.DefaultEdge;
/**
 * Container, which defines the graph layout {@link GLFruchtermanReingoldLayout2}
 * contains the renderable Elements
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayGraphRepresentation extends AnimatedGLElementContainer implements IFRLayoutGraph {
	
	private DynamicPathwayGraph pathway;	
	Set<NodeElement> nodeElementSet;
	Set<EdgeElement> edgeElementSet;
	
	Set<IFRLayoutNode> nodeSet;
	Set<IFRLayoutEdge> edgeSet;
	
//	private GLFruchtermanReingoldLayout pathwayLayout;
//	private GLElementContainer vertices;
//	private GLElementContainer edges;
	
	
	public DynamicPathwayGraphRepresentation(GLFruchtermanReingoldLayout2 layout) {
		
		pathway = new DynamicPathwayGraph();
		
		nodeElementSet = new HashSet<NodeElement>();
		edgeElementSet = new HashSet<EdgeElement>();
		
		nodeSet = new HashSet<IFRLayoutNode>();
		edgeSet = new HashSet<IFRLayoutEdge>();
		
//		pathwayLayout = new GLFruchtermanReingoldLayout();
//		vertices = new GLElementContainer(pathwayLayout);		
//		edges = new GLElementContainer(GLLayouts.LAYERS);

		setLayout(layout);
		
//		add(vertices);


	}
	
	public void addPathwayRep(PathwayGraph graph) {
		pathway.addFocusOrKontextPathway(graph);
		
		nodeElementSet.clear();
		edgeElementSet.clear();
		
		nodeSet.clear();
		edgeSet.clear();
		
		clear();
		
		for(PathwayVertexRep vrep : pathway.getCombinedVertexSet()) {
			NodeElement node = new NodeElement(vrep);
			pathway.addVertexNodeMapEntry(vrep, node);
			nodeElementSet.add(node);
			nodeSet.add((IFRLayoutNode)node);
			add(node);
		}

		
		for(DefaultEdge e : pathway.getCombinedEdgeSet()) {
			PathwayVertexRep vrepSource = pathway.getEdgeSource(e);
			PathwayVertexRep vrepTarget = pathway.getEdgeTarget(e);
			NodeElement nodeSource = pathway.getNodeOfVertex(vrepSource);
			NodeElement nodeTarget = pathway.getNodeOfVertex(vrepTarget);
			
			nodeSource.getHeight();
			nodeTarget.getDisplacementX();
			
			EdgeElement edgeElement = new EdgeElement(e, nodeSource, nodeTarget);		
			edgeElementSet.add(edgeElement);
			edgeSet.add((IFRLayoutEdge)edgeElement);
			add(edgeElement);
		}
		
//		add(vertices);
//		add(edges);
		
		
		
		
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

	@Override
	public Set<IFRLayoutNode> getNodeSet() {
		return this.nodeSet;
	}

	@Override
	public Set<IFRLayoutEdge> getEdgeSet() {
		return this.edgeSet;
	}

	

}
