package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ILabelProvider;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.Coordinates;
import org.caleydo.view.enroute.EPickingType;


public class NodeElement extends GLElementContainer implements IFRLayoutNode {
	
	protected static final int FONT_SIZE = 12;
	protected static final Color CONTOUR_COLOR  = Color.LIGHT_GRAY;
	protected static final String FILLING_COLOR = "#F2F2F2";
	protected static final Color SELECTION_CONTOUR_COLOR = SelectionType.SELECTION.getColor();
	protected static final Color MOUSEROVER_CONTOUR_COLOR = SelectionType.MOUSE_OVER.getColor();
	
	protected PathwayVertexRep vertexRep;
	
	protected double centerX;
	protected double centerY;
	protected Coordinates coords;
	
	protected Boolean isThisNodeSelected;
	protected Boolean isMouseOver;
//	protected static NodeElement currentSelectedNode = null;
	
	protected DynamicPathwayGraphRepresentation parentGraph;
	
	public NodeElement(PathwayVertexRep vertexRep, DynamicPathwayGraphRepresentation parentGraph) {		
		this.vertexRep = vertexRep;
		this.centerX = vertexRep.getCenterX();
		this.centerY = vertexRep.getCenterY();
		this.coords = new Coordinates();
		this.isThisNodeSelected = false;
		this.isMouseOver = false;
		this.parentGraph = parentGraph;
		
		
		setVisibility(EVisibility.PICKABLE);
		

	}

	

	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
	}
	
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
	
	public void setIsNodeSelected(Boolean selection) {
		this.isThisNodeSelected = selection;
		repaint();
	}
	
	public Boolean getIsNodeSelected() {
		return this.isThisNodeSelected;
	}
	

	public PathwayVertexRep getVertexRep() {
		return vertexRep;
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
