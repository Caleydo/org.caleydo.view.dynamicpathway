package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGroupRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGroupElement extends NodeElement {
	
	private static final int GAP_BETWEEN_NODES = 2;

	private PathwayVertexGroupRep groupRep;
	private int groupSize;
	private LinkedList<NodeGeneElement> elementsOfThisGroup;

	public NodeGroupElement(PathwayVertexRep vrep, List<PathwayVertex> pathwayVertices, DynamicPathwayGraphRepresentation parentGraph, Set<PathwayGraph> pathways) {
		super(vrep, pathwayVertices, parentGraph, pathways);
		
		setLayout(GLLayouts.flowVertical(GAP_BETWEEN_NODES));


		this.groupRep = (PathwayVertexGroupRep) vrep;
		this.groupSize = groupRep.getGroupedVertexReps().size();
		this.elementsOfThisGroup = new LinkedList<NodeGeneElement>();

		
		double height = 0;
		

		for (PathwayVertexRep subVrep : groupRep.getGroupedVertexReps()) {
			
			NodeGeneElement node = new NodeGeneElement(subVrep, subVrep.getPathwayVertices(), parentGraph, pathways);
			
			height += node.getHeight() + GAP_BETWEEN_NODES;		
			
			elementsOfThisGroup.add(node);
			add(node);
		}
		
		
		this.height = height;
		
		for(NodeGeneElement node : elementsOfThisGroup) {
			if(node.getType() == EPathwayVertexType.gene) {
				this.width = node.getWidth();
				break;
			}
			
		}
		
		relayout();
		
		this.centerX = 0.0;
		this.centerY = 0.0;
		
		repaint();

	}
	


	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
	

	}

}
