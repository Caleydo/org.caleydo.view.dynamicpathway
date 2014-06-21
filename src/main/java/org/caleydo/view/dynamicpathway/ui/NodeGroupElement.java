package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGroupRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGroupElement extends NodeElement {

	private PathwayVertexGroupRep groupRep;
	private int groupSize;

	GLElementContainer subNodes;

	public NodeGroupElement(PathwayVertexRep vrep, DynamicPathwayGraphRepresentation parentGraph) {
		super(vrep, parentGraph);

		subNodes = new GLElementContainer(GLLayouts.LAYERS);

		groupRep = (PathwayVertexGroupRep) vrep;
		groupSize = groupRep.getGroupedVertexReps().size();

		System.out.println(groupSize);
		
		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();

		// if(groupSize == 2) {

		int oldHeight = 0;
		int oldWidth = 0;
		int i = 1;
		

		for (PathwayVertexRep subVrep : groupRep.getGroupedVertexReps()) {
			NodeGroupSubElement node = new NodeGroupSubElement(subVrep, parentGraph, oldWidth, oldHeight,
					subVrep.getWidth(), subVrep.getHeight());
			subNodes.add(node);
			
			/**
			 * draw one part of group below the first
			 */
			if (Math.ceil(Math.sqrt(groupSize)) == i) {
				oldHeight = 0;
				oldWidth = subVrep.getWidth() + 3;
			} else {
				oldHeight = subVrep.getHeight() + 3;
			}

			i++;
			
		}

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		subNodes.repaintChildren();

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);

	}

}
