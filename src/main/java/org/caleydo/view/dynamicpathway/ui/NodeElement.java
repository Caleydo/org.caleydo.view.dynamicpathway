package org.caleydo.view.dynamicpathway.ui;

import java.util.ArrayList;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.util.Coordinates;

public class NodeElement extends GLElement {
	
	private PathwayVertexRep vertexRep;
	private double displacementX;
	private double displacementY;
	private double centerX;
	private double centerY;
	private DynamicPathwayGraph parentGraph;	
	private Coordinates coords;

	public NodeElement(PathwayVertexRep vertexRep) {
		
		this.vertexRep = vertexRep;
//		this.coordinates = vertexRep.getCoords();
		this.displacementX = 0.0f;
		this.displacementY = 0.0f;
		this.centerX = vertexRep.getCenterX();
		this.centerY = vertexRep.getCenterY();
		this.coords = new Coordinates();
		
		coords.setCoords(centerX,centerY, vertexRep.getWidth(), vertexRep.getHeight());
		
		
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
		

		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();
		
//		g.color("#F3C649").fillRoundedRect(0, 0, width, height,2);
		g.color("#F3C649").fillRoundedRect(0, 0, width, height, 2);
		g.drawText(vertexRep.getName(), 0, 0, width, 12);
		
		
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		renderImpl(g, w, h);
	}
	
	public PathwayVertexRep getVertex() {
		return vertexRep;
	}
	

	public double getCenterX() {
		return this.centerX;
	}
	
	public double getCenterY() {
		return this.centerY;
	}
	
	public void setCenter(double x, double y) {
		this.centerX = x;
		this.centerY = y;
		
		coords.setCoords(x,y, vertexRep.getWidth(), vertexRep.getHeight());
	}
	
	public void setDisplacement(double dispX, double dispY) {
		this.displacementX = dispX;
		this.displacementY = dispY;
	}
	
	public double getDisplacementX() {
		return displacementX;
	}
	
	public double getDisplacementY() {
		return displacementY;
	}
	
	public Coordinates getCoords() {
		return coords;
	}

}
