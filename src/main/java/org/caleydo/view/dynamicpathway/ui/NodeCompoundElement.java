package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeCompoundElement extends NodeElement {
	
	private static final int INNER_BOUNDS = 1;
	private static final int FONT_SIZE_MULTIPLIER = 8;
	private static final int TEXT_X_POS = -20;
	private static final int TEXT_Y_POS = 8;

	public NodeCompoundElement(PathwayVertexRep vertexRep) {
		super(vertexRep);
		
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		short width = vertexRep.getWidth();


		g.color(CONTOUR_COLOR).fillCircle(0, 0, width+INNER_BOUNDS);
		g.color(FILLING_COLOR).fillCircle(0, 0, width);

		
		g.drawText(vertexRep.getName(), TEXT_X_POS, TEXT_Y_POS, width*FONT_SIZE_MULTIPLIER, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub

	}

}
