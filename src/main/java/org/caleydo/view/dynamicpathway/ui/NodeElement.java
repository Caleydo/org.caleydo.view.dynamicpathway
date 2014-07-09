package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.List;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.CalculateIntersectionUtil;
import org.caleydo.view.dynamicpathway.util.Coordinates;

import com.google.common.collect.Lists;

public class NodeElement extends GLElementContainer implements IFRLayoutNode {

	protected static final int FONT_SIZE = 12;
	protected static final Color CONTOUR_COLOR = Color.LIGHT_GRAY;
	protected static final String FILLING_COLOR = "#F2F2F2";
	protected static final Color SELECTION_CONTOUR_COLOR = SelectionType.SELECTION.getColor();
	protected static final Color MOUSEROVER_CONTOUR_COLOR = SelectionType.MOUSE_OVER.getColor();

	protected PathwayVertexRep vertexRep;
	protected List<PathwayVertex> vertices;

	/**
	 * the Vertex, which's name is displayed in the graph by default, this is the first vertex in
	 * {@link org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep#getPathwayVertices()}
	 */
	protected PathwayVertex displayedVertex;

	protected double centerX;
	protected double centerY;
	protected Coordinates coords;

	/**
	 * needed for rendering node highlighting
	 */
	protected Boolean isThisNodeSelected;
	protected Boolean isMouseOver;

	protected String label;

	protected DynamicPathwayGraphRepresentation parentGraph;
	
	protected double height;
	protected double width;
	
	/**
	 * the context menu, which pops up, when a node is right clicked
	 */
	GenericContextMenuItem filterPathwayMenu;
	

	public NodeElement(PathwayVertexRep vertexRep, final DynamicPathwayGraphRepresentation parentGraph) {
		this.vertexRep = vertexRep;
		this.centerX = vertexRep.getCenterX();
		this.centerY = vertexRep.getCenterY();
		this.coords = new Coordinates();
		this.isThisNodeSelected = false;
		this.isMouseOver = false;
		this.parentGraph = parentGraph;
		this.vertices = vertexRep.getPathwayVertices();
		
		if (vertexRep.getType() != EPathwayVertexType.group) {
//			this.vertices = vertexRep.getPathwayVertices();
			this.displayedVertex = vertices.get(0);
			this.label = displayedVertex.getHumanReadableName();
			this.height = this.vertexRep.getHeight();
			this.width = this.vertexRep.getWidth();
		}

		filterPathwayMenu = new GenericContextMenuItem("Filter pathway list by this node",
				new FilterPathwayListByVertexEvent(NodeElement.this));
		
		
		
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
		return this.height;
	}

	@Override
	public double getWidth() {
		return this.width;
	}

	public Coordinates getCoords() {
		return coords;
	}
	
	public EPathwayVertexType getType() {
		return vertexRep.getType();
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

	public List<PathwayVertex> getVertices() {
		return vertices;
	}

	public void setVertices(List<PathwayVertex> vertices) {
		this.vertices = vertices;
	}

	public PathwayVertex getDisplayedVertex() {
		return displayedVertex;
	}

	public void setDisplayedVertex(PathwayVertex displayedVertex) {
		this.displayedVertex = displayedVertex;
	}

}
