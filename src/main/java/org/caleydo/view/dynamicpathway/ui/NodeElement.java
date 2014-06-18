package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.Coordinates;

public abstract class NodeElement extends GLElement implements IFRLayoutNode {
	
	protected static final int FONT_SIZE = 12;
	protected static final Color CONTOUR_COLOR  = Color.LIGHT_GRAY;
	protected static final String FILLING_COLOR = "#F2F2F2";
	
	protected PathwayVertexRep vertexRep;
	protected double centerX;
	protected double centerY;
	protected Coordinates coords;
	
	public NodeElement(PathwayVertexRep vertexRep) {		
		this.vertexRep = vertexRep;
		this.centerX = vertexRep.getCenterX();
		this.centerY = vertexRep.getCenterY();
		this.coords = new Coordinates();
		
		
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
	abstract protected void renderImpl(GLGraphics g, float w, float h);
	
	@Override
	abstract protected void renderPickImpl(GLGraphics g, float w, float h);
	
	@Override
	public double getCenterX() {
		return this.centerX;
	}

	@Override
	public double getCenterY() {
		return this.centerY;
	}

	@Override
	public void setCenter(double centerX, double centerY) {
		this.centerX = centerX;
		this.centerY = centerY;
		
		coords.setCoords(centerX,centerY, vertexRep.getWidth(), vertexRep.getHeight());
	}

	@Override
	public double getHeight() {
		return this.vertexRep.getHeight();
	}

	@Override
	public double getWidth() {
		return this.vertexRep.getWidth();
	}
	
	public Point2D.Double getIntersectionPoint(Line2D intersectingLine) {
		for(Line2D bound : coords.getBounds()) {
			if(intersectingLine.intersectsLine(bound)) {
				return calcIntersectionPoint(intersectingLine, bound);
			}
		}	
		return null;
	}
	
	private final Point2D.Double calcIntersectionPoint(Line2D line1, Line2D line2) {
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
	


}
