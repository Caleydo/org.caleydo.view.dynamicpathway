package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.Coordinates;

public class NodeElement extends GLElement implements IFRLayoutNode{
	
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
//		g.color("#F3C649").fillRoundedRect(0, 0, width, height, 2);
		g.color(Color.LIGHT_GRAY).fillRoundedRect(0, 0, width+2, height+2,2);
		g.color("#F2F2F2").fillRoundedRect(1, 1, width, height,2);
		
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
	
	public Point2D.Double getIntersectionPoint(Line2D intersectingLine) {
		for(Line2D bound : coords.getBounds()) {
			if(intersectingLine.intersectsLine(bound)) {
				return calcIntersectionPoint(intersectingLine, bound);
			}
		}	
		return null;
	}
	
	
	private Point2D.Double calcIntersectionPoint(Line2D line1, Line2D line2) {
		double px = line1.getX1();
		double py = line1.getY1();
		double rx = line1.getX2()-px;
		double ry = line1.getY2()-py;
		
		double qx = line2.getX1();
		double qy = line2.getY1();
		double sx = line2.getX2()-qx;
		double sy = line2.getY2()-qy;
		
		double determinate = sx*ry - sy*rx;		
        double z = (sx*(qy-py)+sy*(px-qx))/determinate;
        
        double xIntersect = px + z * rx;
        double yIntersect = py + z * ry;
        

        return new Point2D.Double(xIntersect, yIntersect);
	      
	}

	@Override
	public double getHeight() {
		return this.getVertex().getHeight();
	}

	@Override
	public double getWidth() {
		return this.getVertex().getWidth();
	}

}
