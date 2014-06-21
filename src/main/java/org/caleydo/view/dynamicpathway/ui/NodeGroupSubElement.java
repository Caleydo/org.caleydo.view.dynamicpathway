package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

/**
 * nodes which are part of a NodeGroupElement
 * @author Christiane Schwarzl
 *
 */
public class NodeGroupSubElement extends NodeElement {

	float width;
	float height;

	public NodeGroupSubElement(PathwayVertexRep vertexRep, DynamicPathwayGraphRepresentation parentGraph,
			double topLeftCornerX, double topLeftCornerY, float width, float height) {
		
		super(vertexRep, parentGraph);

		this.coords.setCoords(topLeftCornerX, topLeftCornerY, width, height);
		this.width = width;
		this.height = height;
		
		repaint();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		float x = this.coords.getTopLeft().getFirst().floatValue();
		float y = this.coords.getTopLeft().getSecond().floatValue();

		g.color(CONTOUR_COLOR).fillRoundedRect(x, y, width + 2, height + 2, 2);
		g.color(FILLING_COLOR).fillRoundedRect(x + 1, y + 1, width,
				height, 2);
		g.drawText(vertexRep.getShortName(), x, y, width, FONT_SIZE);

	}

}
