package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.util.LinkedList;
import java.util.List;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGroupRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGroupElement extends NodeElement {
	
	private static final int GAP_BETWEEN_NODES = 2;

	private PathwayVertexGroupRep groupRep;
	private int groupSize;
	private LinkedList<NodeGeneElement> elementsOfThisGroup;

	public NodeGroupElement(PathwayVertexRep vrep, DynamicPathwayGraphRepresentation parentGraph) {
		super(vrep, parentGraph);
		
		setLayout(GLLayouts.flowVertical(GAP_BETWEEN_NODES));


		this.groupRep = (PathwayVertexGroupRep) vrep;
		this.groupSize = groupRep.getGroupedVertexReps().size();
		this.elementsOfThisGroup = new LinkedList<NodeGeneElement>();

		
		double height = 0;
		double width = 0;

		for (PathwayVertexRep subVrep : groupRep.getGroupedVertexReps()) {
			
			NodeGeneElement node = new NodeGeneElement(subVrep, parentGraph);
			
			height += node.getHeight() + GAP_BETWEEN_NODES;
			width = node.getWidth();
			
			elementsOfThisGroup.add(node);
			add(node);
		}
		
		
		this.height = height;
		this.width = width;
		
		relayout();
		
		this.centerX = 0.0;
		this.centerY = 0.0;
		
		
//		/**
//		 * calculating the center getting the vector from the first
//		 */
//		Vec2f lastElem = elementsOfThisGroup.getLast().getLocation();
//		Vec2f firstElem = elementsOfThisGroup.getFirst().getLocation();
//		
//		Vec2f a = lastElem.minus(firstElem);
//		float b = a.length()/2.0f;
//		a.normalize();
//		a.scale(b);
//		Vec2f center = firstElem.plus(a);
//		this.centerX = center.x();
//		this.centerY = center.y();
//		
//		System.out.println("x: " + centerX + " y: " + centerY);
		
		repaint();

	}
	


	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
	

	}

}
