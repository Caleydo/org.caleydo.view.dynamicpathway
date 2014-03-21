package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeElement extends GLElement {
	
	private PathwayVertexRep vertexRep;
	private double disparity;

	public NodeElement(PathwayVertexRep vertexRep) {
		
		this.vertexRep = vertexRep;
		
		setVisibility(EVisibility.PICKABLE);
		
		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				if (pick.getPickingMode() == PickingMode.CLICKED)
					System.out.println("clicked!");
				if (pick.getPickingMode() == PickingMode.MOUSE_OVER)
					System.out.println("over!");
				if (pick.getPickingMode() == PickingMode.MOUSE_OUT)
					System.out.println("out!");

			}
		});
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

//		float x = vertexRep.getCenterX()-(vertexRep.getWidth()/2);
//		float y = vertexRep.getCenterY()-(vertexRep.getHeight()/2);
		
		//vertexRep.getCoords().get(0).getFirst() is the upper left coordinate
		
		short x = vertexRep.getCoords().get(0).getFirst();
		short y = vertexRep.getCoords().get(0).getSecond();
		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();
		
		g.color("#F3C649").fillRoundedRect(x, y, width, height,2);
		g.drawText(vertexRep.getName(), x, y, width, 12);
		
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		renderImpl(g, w, h);
	}

}
