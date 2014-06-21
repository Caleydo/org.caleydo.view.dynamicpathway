package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;
import gleem.linalg.Vec3f;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.util.gleem.ColoredVec2f;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.jgrapht.graph.DefaultEdge;

public class EdgeElement extends GLElement implements IFRLayoutEdge {
	private static final double ARROW_WIDTH = 10.0;
	private static final double ARROW_HEIGHT = 15.0;
	
	private static final float[] ARROW_FILL_COLOR = { 0, 0, 0, 1 };
	private static final float[] ARROW_CONTOUR_COLOR = { 0, 0, 0, 1 };
	private static final float ARROW_CONTOUR_WIDTH = 1.0f; 
	
	private DefaultEdge edge;
	private NodeElement sourceNode;
	private NodeElement targetNode;
	private Line2D centerToCenterLine;
	private Line2D edgeToRender;
	
	private GL2 gl;

	public EdgeElement(DefaultEdge edge, NodeElement sourceNode, NodeElement targetNode, GL2 gl) {
		this.edge = edge;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		this.gl = gl;

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

		Point2D peakPoint = new Point2D.Double(edgeToRender.getX2(), edgeToRender.getY2());
		Point2D leftPoint = new Point2D.Double(peakPoint.getX() - ARROW_WIDTH, peakPoint.getY()
				- ARROW_HEIGHT);
		Point2D rightPoint = new Point2D.Double(peakPoint.getX() + ARROW_WIDTH, peakPoint.getY()
				+ ARROW_HEIGHT);
		
//		Vec3f rightLeft = new Vec3f((float) (leftPoint.getX() - rightPoint.getX()),
//				(float) (leftPoint.getY() - rightPoint.getY()), 1.0f);
//		Vec3f leftPeak = new Vec3f((float) (peakPoint.getX() - leftPoint.getX()),
//				(float) (peakPoint.getY() - leftPoint.getY()), 1.0f);
//		Vec3f peakRight = new Vec3f((float) (rightPoint.getX() - peakPoint.getX()),
//				(float) (rightPoint.getY() - peakPoint.getY()), 1.0f);
		
//		gl.glPushAttrib(GL.GL_COLOR_BUFFER_BIT | GL2.GL_LINE_BIT);
//
//		gl.glColor4fv(ARROW_FILL_COLOR, 0);
//		gl.glBegin(GL.GL_TRIANGLES);
//		gl.glVertex3f(rightLeft.x(), rightLeft.y(), rightLeft.z());
//		gl.glVertex3f(leftPeak.x(), leftPeak.y(), leftPeak.z());
//		gl.glVertex3f(peakRight.x(), peakRight.y(), peakRight.z());
//		gl.glEnd();
//		
//		gl.glColor4fv(ARROW_CONTOUR_COLOR, 0);
//		gl.glLineWidth(ARROW_CONTOUR_WIDTH);
//		gl.glBegin(GL.GL_LINE_LOOP);
//		gl.glVertex3f(rightLeft.x(), rightLeft.y(), rightLeft.z());
//		gl.glVertex3f(leftPeak.x(), leftPeak.y(), leftPeak.z());
//		gl.glVertex3f(peakRight.x(), peakRight.y(), peakRight.z());
//		gl.glEnd();
//		gl.glPopAttrib();

//		ColoredVec2f rightLeft = new ColoredVec2f(new Vec2f((float) (leftPoint.getX() - rightPoint.getX()),
//				(float) (leftPoint.getY() - rightPoint.getY())), Color.BLACK);
//		
//		ColoredVec2f leftPeak = new ColoredVec2f(new Vec2f((float) (peakPoint.getX() - leftPoint.getX()),
//				(float) (peakPoint.getY() - leftPoint.getY())), Color.BLACK);
//		
//		ColoredVec2f peakRight = new ColoredVec2f(new Vec2f((float) (rightPoint.getX() - peakPoint.getX()),
//				(float) (rightPoint.getY() - peakPoint.getY())), Color.BLACK);
//
//		g.fillPolygon(rightLeft, leftPeak, peakRight);

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
