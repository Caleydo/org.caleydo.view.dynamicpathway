package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.jgrapht.graph.DefaultEdge;

public class EdgeElement extends GLElement implements IFRLayoutEdge {
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

		g.drawLine((float) edgeToRender.getX1(), (float) edgeToRender.getY1(), (float) edgeToRender.getX2(), (float) edgeToRender.getY2());


		
	}



	private void calcDrawableEdge() {

		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();
		
		centerToCenterLine.setLine(xSource, ySource, xTarget, yTarget);
		
		Point2D sourcePoint = sourceNode.getIntersectionPoint(centerToCenterLine);
		Point2D targetPoint = targetNode.getIntersectionPoint(centerToCenterLine);
		
		if(sourcePoint == null || targetPoint == null) {
			edgeToRender.setLine(this.centerToCenterLine);
		}
		else {
			edgeToRender.setLine(sourcePoint, targetPoint);
		}
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
