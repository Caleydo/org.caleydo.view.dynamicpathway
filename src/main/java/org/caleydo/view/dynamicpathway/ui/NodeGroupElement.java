package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGroupRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGroupElement extends NodeElement {

	private PathwayVertexGroupRep groupRep;
	private int groupSize;

	public NodeGroupElement(PathwayVertexRep vrep, DynamicPathwayGraphRepresentation parentGraph) {
		super(vrep, parentGraph);

		groupRep = (PathwayVertexGroupRep) vrep;
		groupSize = groupRep.getGroupedVertexReps().size();

		System.out.println(groupSize);

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();

		// if(groupSize == 2) {

		int oldHeight = 0;
		int oldWidth = 0;
		int i = 1;

		for (PathwayVertexRep subVrep : groupRep.getGroupedVertexReps()) {
			g.color(CONTOUR_COLOR).fillRoundedRect(oldWidth, oldHeight, subVrep.getWidth() + 2, subVrep.getHeight() + 2, 2);
			g.color(FILLING_COLOR).fillRoundedRect(oldWidth+1, oldHeight + 1, subVrep.getWidth(), subVrep.getHeight(), 2);
			g.drawText(subVrep.getShortName(), oldWidth, oldHeight, width, FONT_SIZE);

			/**
			 * draw one part of group below the first
			 */
			if(Math.ceil(Math.sqrt(groupSize)) == i) {
				oldHeight = 0;
				oldWidth = subVrep.getWidth() + 3;
			}
			else {
				oldHeight = subVrep.getHeight() + 3;
			}
			
			i++;
		}
		// }

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub

	}

}
