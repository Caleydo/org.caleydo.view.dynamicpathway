package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
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

		Point2D sourcePoint = sourceNode.getIntersectionPoint(centerToCenterLine);
		Point2D targetPoint = targetNode.getIntersectionPoint(centerToCenterLine);

		if (sourcePoint == null || targetPoint == null) {
			edgeToRender.setLine(this.centerToCenterLine);
		} else {
			edgeToRender.setLine(sourcePoint, targetPoint);
		}
	}
	
	private void drawArrowHead(GLGraphics g) {
		Vec2f source = new Vec2f((float) edgeToRender.getX1(), (float) edgeToRender.getY1());
		Vec2f target = new Vec2f((float) edgeToRender.getX2(), (float) edgeToRender.getY2());
		
		float dx = target.x() - source.x();
		float dy = target.y() - source.y();
		
		float length = (float)Math.sqrt(dx*dx + dy*dy);
		float unitDx = dx/length;
		float unitDy = dy/length;
		
		Vec2f arrowPoint1 = new Vec2f(target.x() - unitDx * ARROW_SIZE - unitDy * ARROW_SIZE, target.y() - unitDy * ARROW_SIZE + unitDx * ARROW_SIZE);
		Vec2f arrowPoint2 = new Vec2f(target.x() - unitDx * ARROW_SIZE + unitDy * ARROW_SIZE, target.y() - unitDy * ARROW_SIZE - unitDx * ARROW_SIZE);
		
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
