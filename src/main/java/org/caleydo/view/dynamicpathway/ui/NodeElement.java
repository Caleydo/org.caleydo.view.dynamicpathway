package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.CalculateIntersectionUtil;
import org.caleydo.view.dynamicpathway.util.Coordinates;

public class NodeElement extends GLElementContainer implements IFRLayoutNode {

	protected static final int FONT_SIZE = 12;
	protected static final Color CONTOUR_COLOR = Color.LIGHT_GRAY;
	protected static final String FILLING_COLOR = "#F2F2F2";
	protected static final Color SELECTION_CONTOUR_COLOR = SelectionType.SELECTION.getColor();
	protected static final Color MOUSEROVER_CONTOUR_COLOR = SelectionType.MOUSE_OVER.getColor();

	protected PathwayVertexRep vertexRep;

	/**
	 * the Vertex, which's name is displayed in the graph by default, this is the first vertex in
	 * {@link org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep#getPathwayVertices()}
	 */
	protected PathwayVertex displayedVertex;

	protected double centerX;
	protected double centerY;
	protected Coordinates coords;

	protected Boolean isThisNodeSelected;
	protected Boolean isMouseOver;

	protected String label;

	protected DynamicPathwayGraphRepresentation parentGraph;

	public NodeElement(PathwayVertexRep vertexRep, DynamicPathwayGraphRepresentation parentGraph) {
		this.vertexRep = vertexRep;
		this.centerX = vertexRep.getCenterX();
		this.centerY = vertexRep.getCenterY();
		this.coords = new Coordinates();
		this.isThisNodeSelected = false;
		this.isMouseOver = false;
		this.parentGraph = parentGraph;

		if (vertexRep.getType() != EPathwayVertexType.group) {
			this.displayedVertex = vertexRep.getPathwayVertices().get(0);
			this.label = displayedVertex.getHumanReadableName();
		}

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

		coords.setCoords(centerX, centerY, vertexRep.getWidth(), vertexRep.getHeight());
	}

	public Point2D.Double getCenter() {
		return (new Point2D.Double(centerX, centerY));
	}

	@Override
	public double getHeight() {
		return this.vertexRep.getHeight();
	}

	@Override
	public double getWidth() {
		return this.vertexRep.getWidth();
	}

	public Coordinates getCoords() {
		return coords;
	}

	/**
	 * 
	 * calculate intersection if the node shape is rectangular used in
	 * {@link org.caleydo.view.dynamicpathway.ui.EdgeElement#renderImpl(GLGraphics, float, float)} for drawing
	 * the edges between 2 nodes
	 * 
	 * @param intersectingLine
	 * @return
	 */
	public Point2D.Double getIntersectionPointWithNodeBound(Line2D intersectingLine) {
		for (Line2D bound : coords.getBounds()) {
			if (intersectingLine.intersectsLine(bound)) {
				return CalculateIntersectionUtil.calcIntersectionPoint(intersectingLine, bound);
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

	// private final Point2D.Double calcIntersectionPoint(Line2D line1, Line2D line2) {
	// double px = line1.getX1();
	// double py = line1.getY1();
	// double rx = line1.getX2() - px;
	// double ry = line1.getY2() - py;
	//
	// double qx = line2.getX1();
	// double qy = line2.getY1();
	// double sx = line2.getX2() - qx;
	// double sy = line2.getY2() - qy;
	//
	// double determinate = sx * ry - sy * rx;
	// double z = (sx * (qy - py) + sy * (px - qx)) / determinate;
	//
	// double xIntersect = px + z * rx;
	// double yIntersect = py + z * ry;
	//
	// return new Point2D.Double(xIntersect, yIntersect);
	// }

	private final Point2D.Double calcIntersectionPoint(Line2D line, Point2D.Double center, double radius,
			Boolean getFirstIntersectionPoint) {
		double baX = line.getX2() - line.getX1();
		double baY = line.getY2() - line.getY1();
		double caX = center.getX() - line.getX1();
		double caY = center.getY() - line.getY1();

		double a = baX * baX + baY * baY;
		double bBy2 = baX * caX + baY * caY;
		double c = caX * caX + caY * caY - radius * radius;

		double pBy2 = bBy2 / a;
		double q = c / a;

		double disc = pBy2 * pBy2 - q;

		if (disc < 0)
			return null;

		double tmpSqrt = Math.sqrt(disc);
		double abScalingFactor1 = -pBy2 + tmpSqrt;
		double abScalingFactor2 = -pBy2 - tmpSqrt;

		Point2D.Double p1 = new Point2D.Double(line.getX1() - baX * abScalingFactor1, line.getY1() - baY
				* abScalingFactor2);
		Point2D.Double p2 = new Point2D.Double(line.getX1() - baX * abScalingFactor2, line.getY1() - baY
				* abScalingFactor2);

		if (getFirstIntersectionPoint)
			return p1;

		return p2;
	}

	// private final List<Point2D.Double> calcIntersectionPoint(Line2D line, Point2D.Double center, double
	// radius, Boolean getFirstIntersectionPoint) {
	// double baX = line.getX2() - line.getX1();
	// double baY = line.getY2() - line.getY1();
	// double caX = center.getX() - line.getX1();
	// double caY = center.getY() - line.getY1();
	//
	// double a = baX * baX + baY * baY;
	// double bBy2 = baX * caX + baY * caY;
	// double c = caX * caX + caY * caY - radius * radius;
	//
	// double pBy2 = bBy2 / a;
	// double q = c / a;
	//
	// double disc = pBy2 * pBy2 - q;
	//
	// if(disc < 0)
	// return null;
	//
	// double tmpSqrt = Math.sqrt(disc);
	// double abScalingFactor1 = -pBy2 + tmpSqrt;
	// double abScalingFactor2 = -pBy2 - tmpSqrt;
	//
	// Point2D.Double p1 = new Point2D.Double(line.getX1() - baX * abScalingFactor1, line.getY1() - baY
	// * abScalingFactor2);
	// Point2D.Double p2 = new Point2D.Double(line.getX1() - baX * abScalingFactor2, line.getY1() - baY
	// * abScalingFactor2);
	//
	// List<Point2D.Double> pointsList = new LinkedList<Point2D.Double>();
	// pointsList.add(p1);
	// pointsList.add(p2);
	//
	// return pointsList;
	// }

	public PathwayVertex getDisplayedVertex() {
		return displayedVertex;
	}

	public void setDisplayedVertex(PathwayVertex displayedVertex) {
		this.displayedVertex = displayedVertex;
	}

}
