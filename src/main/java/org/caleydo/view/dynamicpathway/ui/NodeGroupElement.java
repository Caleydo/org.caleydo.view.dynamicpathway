package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGroupRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGroupElement extends NodeElement {

	private PathwayVertexGroupRep groupRep;
	private int groupSize;

//	GLElementContainer subNodes;

	public NodeGroupElement(PathwayVertexRep vrep, DynamicPathwayGraphRepresentation parentGraph) {
		super(vrep, parentGraph);
		
		setLayout(GLLayouts.LAYERS);

//		subNodes = new GLElementContainer(GLLayouts.LAYERS);
//		add(subNodes);

		groupRep = (PathwayVertexGroupRep) vrep;
		groupSize = groupRep.getGroupedVertexReps().size();

		System.out.println(groupSize);
		
		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();		

		int oldHeight = 0;
		int oldWidth = 0;
		int i = 1;
		
		if(groupSize == 2) {
			oldWidth = (width/2);
			oldHeight = (height/2)-(groupRep.getGroupedVertexReps().get(0).getHeight()/2);
		}
		

		for (PathwayVertexRep subVrep : groupRep.getGroupedVertexReps()) {
			
		
			NodeGroupSubElement node = new NodeGroupSubElement(subVrep, parentGraph, oldWidth, oldHeight,
					subVrep.getWidth(), subVrep.getHeight());
			add(node);
			
			/**
			 * draw one part of group below the first
			 */
			
			if (groupSize == 2) {
				oldHeight = subVrep.getHeight() + 10;
			}
			else {
				if (Math.ceil(Math.sqrt(groupSize)) == i) {
					oldHeight = 0;
					oldWidth = subVrep.getWidth() + 3;
				} else {
					oldHeight = subVrep.getHeight() + 3;
				}
			}
		

			i++;
			
		}
		
		repaint();

	}
	

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
	

	}

}
