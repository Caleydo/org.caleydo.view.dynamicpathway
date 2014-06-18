package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGeneElement extends NodeElement {
	
	private static final int INNER_BOUNDS = 2;
	private static final int OUTER_BOUNDS = 1;
	private static final int ROUND_EDGE_RADIUS = 2;	

	public NodeGeneElement(PathwayVertexRep vertexRep) {
		
		super(vertexRep);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		

		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();
		
		g.color(CONTOUR_COLOR).fillRoundedRect(0, 0, width+INNER_BOUNDS, height+INNER_BOUNDS,ROUND_EDGE_RADIUS);
		g.color(FILLING_COLOR).fillRoundedRect(OUTER_BOUNDS, OUTER_BOUNDS, width, height,ROUND_EDGE_RADIUS);
		
		g.drawText(vertexRep.getName(), 0, 0, width, FONT_SIZE);
		
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		renderImpl(g, w, h);
	}

}
