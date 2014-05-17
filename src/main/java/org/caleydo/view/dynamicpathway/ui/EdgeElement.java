package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.jgrapht.graph.DefaultEdge;

public class EdgeElement extends GLElement {
	private DefaultEdge edge;
	private NodeElement sourceNode;
	private NodeElement targetNode;
	private Direction direction;
	private Line2D centerToCenterEdge;
	private Line2D edgeToRender;

	private enum Direction {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
	}

	public EdgeElement(DefaultEdge edge, NodeElement sourceNode, NodeElement targetNode) {
		this.edge = edge;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;

		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();

		this.centerToCenterEdge = new Line2D.Double(xSource, ySource, xTarget, yTarget);
		this.edgeToRender = new Line2D.Double();

		setDirection();

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		setDirection();

		calcDrawableEdge();

		g.drawLine((float) edgeToRender.getX1(), (float) edgeToRender.getY1(), (float) edgeToRender.getX2(), (float) edgeToRender.getY2());

	}

	/**
	 * determine which from which source edge to which target edge to draw, i.e.
	 * direction [source] [target] \ / \ / [target] [source] target node is on
	 * the bottom right target node is on the top right -> xDirection is
	 * positive -> xDirection is positive -> yDirection is positive ->
	 * yDirection is negative
	 * 
	 * note: origin (0,0) is on the top left corner
	 */
	private void setDirection() {
		double xDirection = targetNode.getCenterX() - sourceNode.getCenterX();
		double yDirection = targetNode.getCenterY() - sourceNode.getCenterY();

		if (xDirection >= 0.0) {
			if (yDirection >= 0.0)
				this.direction = Direction.BOTTOM_RIGHT;
			else
				this.direction = Direction.TOP_RIGHT;
		} else {
			if (yDirection >= 0.0)
				this.direction = Direction.BOTTOM_LEFT;
			else
				this.direction = Direction.TOP_LEFT;
		}
	}

	private void calcDrawableEdge() {

		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();
		
		centerToCenterEdge.setLine(xSource, ySource, xTarget, yTarget);
		
		Point2D sourcePoint = null;
		Point2D targetPoint = null;

		if(centerToCenterEdge.intersectsLine(sourceNode.getCoords().getTopBound())) {
			sourcePoint = calcIntersectionPoint(centerToCenterEdge, sourceNode.getCoords().getTopBound());
		}
		else if(centerToCenterEdge.intersectsLine(sourceNode.getCoords().getBottomBound())) {
			sourcePoint = calcIntersectionPoint(centerToCenterEdge, sourceNode.getCoords().getBottomBound());
		}
		else if(centerToCenterEdge.intersectsLine(sourceNode.getCoords().getLeftBound())) {
			sourcePoint = calcIntersectionPoint(centerToCenterEdge, sourceNode.getCoords().getLeftBound());
		}
		else if(centerToCenterEdge.intersectsLine(sourceNode.getCoords().getRightBound())) {
			sourcePoint = calcIntersectionPoint(centerToCenterEdge, sourceNode.getCoords().getRightBound());
		}
		
		if(centerToCenterEdge.intersectsLine(targetNode.getCoords().getTopBound())) {
			targetPoint = calcIntersectionPoint(centerToCenterEdge, targetNode.getCoords().getTopBound());
		}
		else if(centerToCenterEdge.intersectsLine(targetNode.getCoords().getBottomBound())) {
			targetPoint = calcIntersectionPoint(centerToCenterEdge, targetNode.getCoords().getBottomBound());
		}
		else if(centerToCenterEdge.intersectsLine(targetNode.getCoords().getLeftBound())) {
			targetPoint = calcIntersectionPoint(centerToCenterEdge, targetNode.getCoords().getLeftBound());
		}
		else if(centerToCenterEdge.intersectsLine(targetNode.getCoords().getRightBound())) {
			targetPoint = calcIntersectionPoint(centerToCenterEdge, targetNode.getCoords().getRightBound());
		}
		
		edgeToRender.setLine(sourcePoint, targetPoint);
		
//		switch (direction) {
//		case TOP_LEFT:
//			calcEdges(sourceNode.getCoords().getTopBound(), sourceNode.getCoords().getLeftBound(), targetNode
//					.getCoords().getBottomBound(), targetNode.getCoords().getRightBound());
//			break;
//		case TOP_RIGHT:
//			calcEdges(sourceNode.getCoords().getTopBound(), sourceNode.getCoords().getRightBound(), targetNode
//					.getCoords().getBottomBound(), targetNode.getCoords().getLeftBound());
//			break;
//		case BOTTOM_LEFT:
//			calcEdges(sourceNode.getCoords().getBottomBound(), sourceNode.getCoords().getLeftBound(), targetNode
//					.getCoords().getTopBound(), targetNode.getCoords().getRightBound());
//			break;
//		case BOTTOM_RIGHT:
//			calcEdges(sourceNode.getCoords().getBottomBound(), sourceNode.getCoords().getRightBound(), targetNode
//					.getCoords().getTopBound(), targetNode.getCoords().getLeftBound());
//			break;
//		}
//		
//		calcEdges(sourceNode.getCoords().getTopBound(), sourceNode.getCoords().getLeftBound(), targetNode
//				.getCoords().getBottomBound(), targetNode.getCoords().getRightBound());
//		calcEdges(sourceNode.getCoords().getTopBound(), sourceNode.getCoords().getRightBound(), targetNode
//				.getCoords().getBottomBound(), targetNode.getCoords().getLeftBound());
//		calcEdges(sourceNode.getCoords().getBottomBound(), sourceNode.getCoords().getLeftBound(), targetNode
//				.getCoords().getTopBound(), targetNode.getCoords().getRightBound());
//		calcEdges(sourceNode.getCoords().getBottomBound(), sourceNode.getCoords().getRightBound(), targetNode
//				.getCoords().getTopBound(), targetNode.getCoords().getLeftBound());
		
		
		
	}

	private void calcEdges(Line2D sourcePossibleLine1, Line2D sourcePossibleLine2, Line2D targetPossibleLine1, Line2D targetPossibleLine2) {

		Point2D sourcePoint = null;
		Point2D targetPoint = null;
		
		if(centerToCenterEdge.intersectsLine(sourcePossibleLine1)) {
			sourcePoint = calcIntersectionPoint(centerToCenterEdge, sourcePossibleLine1);
			assert(sourcePoint != null);
		}
		else {
			if(centerToCenterEdge.intersectsLine(sourcePossibleLine2)) {
				sourcePoint = calcIntersectionPoint(centerToCenterEdge, sourcePossibleLine2);
				assert(sourcePoint != null);
			}
		}
		
		if(centerToCenterEdge.intersectsLine(targetPossibleLine1)) {
			targetPoint = calcIntersectionPoint(centerToCenterEdge, targetPossibleLine1);
			assert(targetPoint != null);
		}
		else {
			if(centerToCenterEdge.intersectsLine(targetPossibleLine2)) {
				targetPoint = calcIntersectionPoint(centerToCenterEdge, targetPossibleLine2);
				assert(targetPoint != null);
			}
		}
		
		edgeToRender.setLine(sourcePoint, targetPoint);
	}
	
	private Point2D.Double calcIntersectionPoint(Line2D line1, Line2D line2) {
		double px = line1.getX1(),
				py = line1.getY1(),
		        rx = line1.getX2()-px,
		        ry = line1.getY2()-py;
		double qx = line2.getX1(),
				qy = line2.getY1(),
				sx = line2.getX2()-qx,
				sy = line2.getY2()-qy;
		
		double det = sx*ry - sy*rx;
//	    if (det == 0) 
//	    	return null;
		
        double z = (sx*(qy-py)+sy*(px-qx))/det;
       // if (z==0 ||  z==1) return null;  // intersection at end point!
        return new Point2D.Double((px+z*rx),(py+z*ry));
	      
	}

}
