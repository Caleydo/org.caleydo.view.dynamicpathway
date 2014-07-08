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
import org.caleydo.view.dynamicpathway.util.CalculateIntersectionUtil;
import org.jgrapht.graph.DefaultEdge;

public class EdgeElement extends GLElement implements IFRLayoutEdge {
	private static final float ARROW_SIZE = 5.0f;

	private DefaultEdge edge;
	private NodeElement sourceNode;
	private NodeElement targetNode;
	private Line2D centerToCenterLine;
	private Line2D edgeToRender;

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
		
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		calcDrawableEdge();

		g.drawLine((float) edgeToRender.getX1(), (float) edgeToRender.getY1(), (float) edgeToRender.getX2(),
				(float) edgeToRender.getY2());

		drawArrowHead(g);

	}
	

	private void calcDrawableEdge() {

		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();


		centerToCenterLine.setLine(xSource, ySource, xTarget, yTarget);
		
		Point2D sourcePoint;
		Point2D targetPoint;
		
		/**
		 * if the node shape is circle, the intersection points need to be calculated differently
		 */
		if (sourceNode.getVertexRep().getType() == EPathwayVertexType.compound) {
			double radius = sourceNode.getWidth()-1.0;
			
			sourcePoint = CalculateIntersectionUtil.calcIntersectionPoint(centerToCenterLine, radius);
			
		}
		else 
			sourcePoint = sourceNode.getIntersectionPointWithNodeBound(centerToCenterLine);
		
		if (targetNode.getVertexRep().getType() == EPathwayVertexType.compound) {
			
			Line2D reversedCenterToCenterLine = new Line2D.Double(xTarget, yTarget, xSource, ySource);
			
			double radius = targetNode.getWidth();
			
			targetPoint = CalculateIntersectionUtil.calcIntersectionPoint(reversedCenterToCenterLine, radius);
			
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
