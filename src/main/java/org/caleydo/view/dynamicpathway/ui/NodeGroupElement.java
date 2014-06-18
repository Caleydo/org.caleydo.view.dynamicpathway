package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGroupRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGroupElement extends NodeElement {

	private PathwayVertexGroupRep groupRep;
	private int groupSize;

	public NodeGroupElement(PathwayVertexRep vrep) {
		super(vrep);
		
		
		
		groupRep = (PathwayVertexGroupRep) vrep;
		groupSize = groupRep.getGroupedVertexReps().size();

		System.out.println(groupSize);
				
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		
		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();
		
		if(groupSize == 2) {
//			PathwayVertexRep subVrep = groupRep.getGroupedVertexReps().get(0);
//			g.color(CONTOUR_COLOR).fillRoundedRect(0, 0, subVrep.getWidth()+2, subVrep.getHeight()+2,2);
//			g.color(FILLING_COLOR).fillRoundedRect(1, 1, subVrep.getWidth(), subVrep.getHeight(),2);
//			g.drawText(subVrep.getShortName(), 0, 0, width, FONT_SIZE);
//			
//			
//			int oldHeight = subVrep.getHeight()+2;
//			
//			subVrep = groupRep.getGroupedVertexReps().get(1);
//			g.color(CONTOUR_COLOR).fillRoundedRect(0, oldHeight+1, subVrep.getWidth()+2, subVrep.getHeight()+2,2);
//			g.color(FILLING_COLOR).fillRoundedRect(1, oldHeight+2, subVrep.getWidth(), subVrep.getHeight(),2);
//			g.drawText(subVrep.getShortName(), 0, oldHeight+1, width, FONT_SIZE);
			
			int oldHeight = 0;
			
			for(PathwayVertexRep subVrep : groupRep.getGroupedVertexReps()) {
				g.color(CONTOUR_COLOR).fillRoundedRect(0, oldHeight, subVrep.getWidth()+2, subVrep.getHeight()+2,2);
				g.color(FILLING_COLOR).fillRoundedRect(1, oldHeight+1, subVrep.getWidth(), subVrep.getHeight(),2);
				g.drawText(subVrep.getShortName(), 0, oldHeight, width, FONT_SIZE);
				
				oldHeight = subVrep.getHeight()+3;			
			}
		}
		
//		for(PathwayVertexRep subVrep : groupRep.getGroupedVertexReps()) {
//			
//		}
		
//		g.color(Color.LIGHT_GRAY).fillRoundedRect(0, 0, width+2, height+2,2);
//		g.color("#F2F2F2").fillRoundedRect(1, 1, width, height,2);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub

	}

}
