package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.List;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.jgrapht.graph.DefaultEdge;

public class EdgeElement extends GLElement implements IFRLayoutEdge {
	private static final float ARROW_SIZE = 5.0f;

	private DefaultEdge edge;
	private NodeElement sourceNode;
	private NodeElement targetNode;
	private EDirection direction;
	private Line2D centerToCenterLine;
	private Line2D edgeToRender;
	
	private enum EDirection {
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

		this.centerToCenterLine = new Line2D.Double(xSource, ySource, xTarget, yTarget);
		this.edgeToRender = new Line2D.Double();
		
		setDirection();

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		setDirection();

		calcDrawableEdge();

		g.drawLine((float) edgeToRender.getX1(), (float) edgeToRender.getY1(), (float) edgeToRender.getX2(),
				(float) edgeToRender.getY2());

		drawArrowHead(g);

	}
	
	/**
	 * determine which from which source edge to which target edge to draw, i.e.
	 * direction 
	 *			[source] 							  		  [target] 
	 * 					\							 		 /
	 *					 \									/
	 *                    [target] 					[source] 
	 *      target node is on the bottom right  	target node is on the top right 
	 *      -> xDirection is positive 				-> xDirection is positive 
	 *		-> yDirection is positive 				-> yDirection is negative
	 * 
	 * note: origin (0,0) is on the top left corner
	 */
	private void setDirection() {
		double xDirection = targetNode.getCenterX() - sourceNode.getCenterX();
		double yDirection = targetNode.getCenterY() - sourceNode.getCenterY();

		if (xDirection >= 0.0) {
			if (yDirection >= 0.0)
				this.direction = EDirection.BOTTOM_RIGHT;
			else
				this.direction = EDirection.TOP_RIGHT;
		} else {
			if (yDirection >= 0.0)
				this.direction = EDirection.BOTTOM_LEFT;
			else
				this.direction = EDirection.TOP_LEFT;
		}
	}


	private void calcDrawableEdge() {

		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();

		centerToCenterLine.setLine(xSource, ySource, xTarget, yTarget);
		
		Point2D sourcePoint;
		Point2D targetPoint;
		
		if (sourceNode.getVertexRep().getType() == EPathwayVertexType.compound) {
			Boolean getFirstIntersectionPoint = false;
			if(direction == EDirection.TOP_LEFT || direction == EDirection.BOTTOM_LEFT)
				getFirstIntersectionPoint = true;
			
			sourcePoint = sourceNode.getIntersectionPointWithNodeBound(sourceNode.getCenter(), sourceNode.getWidth()-2.5,getFirstIntersectionPoint);

		}
		else 
			sourcePoint = sourceNode.getIntersectionPointWithNodeBound(centerToCenterLine);
		
		if (targetNode.getVertexRep().getType() == EPathwayVertexType.compound) {
			Boolean getFirstIntersectionPoint = false;
			
			if(direction == EDirection.TOP_LEFT || direction == EDirection.BOTTOM_LEFT)
				getFirstIntersectionPoint = true;
			
			targetPoint = targetNode.getIntersectionPointWithNodeBound(targetNode.getCenter(), targetNode.getWidth()-2.5, getFirstIntersectionPoint);
			
		}
		else	
			targetPoint = targetNode.getIntersectionPointWithNodeBound(centerToCenterLine);

		/**
		 * check if a sourcePoint and/or a targetPoint was found
		 */
		if(sourcePoint == null && targetPoint == null) {
			edgeToRender.setLine(centerToCenterLine);
		} else if (sourcePoint == null) {				
			edgeToRender.setLine(xSource, ySource, targetPoint.getX(), targetPoint.getY());
			
		} else if(targetPoint == null) {
			edgeToRender.setLine(sourcePoint.getX(), sourcePoint.getY(), xTarget, yTarget);
		}
		else {
			edgeToRender.setLine(sourcePoint, targetPoint);
		}
	}

	private void drawArrowHead(GLGraphics g) {
		Vec2f source = new Vec2f((float) edgeToRender.getX1(), (float) edgeToRender.getY1());
		Vec2f target = new Vec2f((float) edgeToRender.getX2(), (float) edgeToRender.getY2());

		float dx = target.x() - source.x();
		float dy = target.y() - source.y();

		float length = (float) Math.sqrt(dx * dx + dy * dy);
		float unitDx = dx / length;
		float unitDy = dy / length;

		Vec2f arrowPoint1 = new Vec2f(target.x() - unitDx * ARROW_SIZE - unitDy * ARROW_SIZE, target.y()
				- unitDy * ARROW_SIZE + unitDx * ARROW_SIZE);
		Vec2f arrowPoint2 = new Vec2f(target.x() - unitDx * ARROW_SIZE + unitDy * ARROW_SIZE, target.y()
				- unitDy * ARROW_SIZE - unitDx * ARROW_SIZE);

		g.color(Color.BLACK).fillPolygon(target, arrowPoint1, arrowPoint2);
	}
	
	private Point2D.Double getPointWithMinDistance(Point2D.Double referencePoint, List<Point2D.Double> pointsToCheck) {
		
		if(pointsToCheck == null)
			return null;
		
		Point2D.Double pointWithMinDistance = null;
		double minDistance = Double.POSITIVE_INFINITY;
		
		for(Point2D.Double pointToCheck : pointsToCheck) {
			double distance = referencePoint.distance(pointToCheck);
			
			if(distance < minDistance) {
				minDistance = distance;
				pointWithMinDistance = pointToCheck;
			}
		}
		
		return pointWithMinDistance;
	}

	@Override
	public IFRLayoutNode getSource() {
		return this.sourceNode;
	}

	@Override
	public IFRLayoutNode getTarget() {
		return this.targetNode;
	}

	@Override
	public Line2D getCenterToCenterLine() {
		return this.centerToCenterLine;
	}

	@Override
	public void setCenterToCenterLine(Line2D centerToCenterLine) {
		this.centerToCenterLine.setLine(centerToCenterLine);
	}

}
