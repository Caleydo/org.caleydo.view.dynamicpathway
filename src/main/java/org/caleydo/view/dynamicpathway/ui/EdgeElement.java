package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.Timer;
import java.util.TimerTask;

import javax.media.opengl.GL2;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.CalculateIntersectionUtil;
import org.jgrapht.graph.DefaultEdge;

/**
 * Wrapper for {@link DefaultEdge} <br />
 * Sets an edge from the center of the source node to the target node <br />
 * Calculates the intersection points between this line and the nodes and creates a line <br />
 * Calculates and draws the arrow head based on the position and the angle of this line.
 * 
 * @author Christiane Schwarzl
 *
 */
public class EdgeElement extends GLElement implements IFRLayoutEdge {
	private static final float EDGE_WIDTH = 1.5f;
	private static final float ARROW_SIZE = 5.0f;

	private DefaultEdge edge;


	private NodeElement sourceNode;
	private NodeElement targetNode;
	private Line2D centerToCenterLine;
	private Line2D edgeToRender;
	private Timer timer;

	public EdgeElement(DefaultEdge edge, NodeElement sourceNode, NodeElement targetNode, long drawEdgeDelay) {
		this.edge = edge;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;

		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();

		this.centerToCenterLine = new Line2D.Double(xSource, ySource, xTarget, yTarget);
		this.edgeToRender = new Line2D.Double();
		setVisibility(EVisibility.HIDDEN);

		/**
		 * edges are drawn after the nodes were drawn 
		 */
		timer = new Timer();
		setTimerDelay(drawEdgeDelay);
	}
	
	public void setTimerDelay(long drawEdgeDelay) {
		timer.schedule(new TimerTask() {
			
			@Override
			public void run() {
				setVisibility(EVisibility.VISIBLE);
				
			}
		}, drawEdgeDelay);
	}

	public DefaultEdge getDefaultEdge() {
		return edge;
	}

	public Line2D getEdgeToRender() {
		return edgeToRender;
	}

	@Override
	public IFRLayoutNode getSource() {
		return this.sourceNode;
	}

	public NodeElement getSourceNode() {
		return sourceNode;
	}

	@Override
	public IFRLayoutNode getTarget() {
		return this.targetNode;
	}


	public NodeElement getTargetNode() {
		return targetNode;
	}

	public void setSourceNode(NodeElement sourceNode) {
		this.sourceNode = sourceNode;
	}

	public void setTargetNode(NodeElement targetNode) {
		this.targetNode = targetNode;
	}

	@Override
	public String toString() {
		String output = "Edge: " + sourceNode.getLabel() + "->" + targetNode.getLabel();
		return output;
	}
	
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		calcDrawableEdge();

		g.incZ(-0.5f);
		g.gl.glEnable(GL2.GL_LINE_SMOOTH);
		g.gl.glEnable(GL2.GL_BLEND);
		g.gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);
		g.drawLine((float) edgeToRender.getX1(), (float) edgeToRender.getY1(), (float) edgeToRender.getX2(),
				(float) edgeToRender.getY2()).lineWidth(EDGE_WIDTH);
		g.incZ(0.5f);
		drawArrowHead(g);

	}

	private void calcDrawableEdge() {

		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();

		// TODO: sometimes null
		centerToCenterLine.setLine(xSource, ySource, xTarget, yTarget);

		Point2D sourcePoint;
		Point2D targetPoint;

		/**
		 * if the node shape is circle, the intersection points need to be calculated differently
		 */
		if (sourceNode.getVertexRep().getType() == EPathwayVertexType.compound) {
			
			Line2D reversedCenterToCenterLine = new Line2D.Double(xTarget, yTarget, xSource, ySource);
			double radius = sourceNode.getWidth();

			sourcePoint = CalculateIntersectionUtil.calcIntersectionPoint(reversedCenterToCenterLine, radius);

		} else
			sourcePoint = sourceNode.getIntersectionPointWithNodeBound(centerToCenterLine);

		if (targetNode.getVertexRep().getType() == EPathwayVertexType.compound) {

			Line2D reversedCenterToCenterLine = new Line2D.Double(xTarget, yTarget, xSource, ySource);

			double radius = targetNode.getWidth();

			targetPoint = CalculateIntersectionUtil.calcIntersectionPoint(centerToCenterLine, radius);

		} else
			targetPoint = targetNode.getIntersectionPointWithNodeBound(centerToCenterLine);

		//TODO: should not happen
		if (sourcePoint == null && targetPoint == null) {
			edgeToRender.setLine(centerToCenterLine);
		} else if (sourcePoint == null) {
			edgeToRender.setLine(xSource, ySource, targetPoint.getX(), targetPoint.getY());			
		} else if (targetPoint == null) {
			edgeToRender.setLine(sourcePoint.getX(), sourcePoint.getY(), xTarget, yTarget);
		} else {
//			edgeToRender.setLine(centerToCenterLine);
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

}
