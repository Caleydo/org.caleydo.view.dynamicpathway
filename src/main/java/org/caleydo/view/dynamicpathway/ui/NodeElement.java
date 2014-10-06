package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.CalculateIntersectionUtil;
import org.caleydo.view.dynamicpathway.util.Coordinates;

public class NodeElement extends GLElementContainer implements IFRLayoutNode {

	protected String uid;

	protected static final int FONT_SIZE = 12;
	protected static final Color CONTOUR_COLOR = Color.LIGHT_GRAY;
	protected static final String KONTEXT_FILLING_COLOR = "#F2F2F2";
	protected static final String COMBINED_FILLING_COLOR = "#3067C6";
	protected static final Color FOCUS_FILLING_COLOR = Color.LIGHT_BLUE;
	protected static final Color SELECTION_CONTOUR_COLOR = SelectionType.SELECTION.getColor();
	protected static final Color MOUSEROVER_CONTOUR_COLOR = SelectionType.MOUSE_OVER.getColor();
	protected static final Color FILTER_CONTOUR_COLOR = Color.RED;

	protected PathwayVertexRep vertexRep;
	protected List<PathwayVertex> vertices;
	protected List<PathwayVertexRep> vrepsWithThisNodesVerticesList;

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
	protected Boolean isThisNodeUsedForFiltering;
	protected Boolean isMouseOver;

	protected String label;

	protected DynamicPathwayGraphRepresentation parentGraph;

	protected double height;
	protected double width;

	/**
	 * if this node element is merged (artificially created - not in original PathwayGraph) set false by
	 * default
	 */
	protected boolean isMerged = false;

	/**
	 * the context menu, which pops up, when a node is right clicked
	 */
	GenericContextMenuItem filterPathwayMenu;

	public NodeElement(PathwayVertexRep vertexRep, final DynamicPathwayGraphRepresentation parentGraph) {
		this.uid = UUID.randomUUID().toString();
		this.vertexRep = vertexRep;
		this.centerX = vertexRep.getCenterX();
		this.centerY = vertexRep.getCenterY();
		this.coords = new Coordinates();
		this.isThisNodeSelected = false;
		this.isThisNodeUsedForFiltering = false;
		this.isMouseOver = false;
		this.parentGraph = parentGraph;
		this.vertices = new LinkedList<PathwayVertex>(vertexRep.getPathwayVertices());

		if (vertexRep.getType() != EPathwayVertexType.group) {
			// this.vertices = vertexRep.getPathwayVertices();
			this.displayedVertex = vertices.get(0);
			this.label = displayedVertex.getHumanReadableName();
			this.height = this.vertexRep.getHeight();
			this.width = this.vertexRep.getWidth();
		}

		filterPathwayMenu = new GenericContextMenuItem("Filter pathway list by this node",
				new FilterPathwayListByVertexEvent(NodeElement.this));

		setVisibility(EVisibility.PICKABLE);

	}

	@Override
	protected void init(IGLElementContext context) {

		super.init(context);

		// create a tooltip listener to render the tooltip of this element
		this.onPick(context.getSWTLayer().createTooltip(new ILabeled() {
			@Override
			public String getLabel() {
				StringBuilder builder = new StringBuilder();
				Set<PathwayVertex> vertices = new LinkedHashSet<>();
				for (PathwayVertex vRep : NodeElement.this.vertices) {
					vertices.add(vRep);
				}
				List<String> names = new ArrayList<>(vertices.size());
				for (PathwayVertex v : vertices) {
					names.add(v.getHumanReadableName());
				}
				Collections.sort(names);
				for (int i = 0; i < names.size(); i++) {
					builder.append(names.get(i));
					if (i < names.size() - 1)
						builder.append(", ");
				}
				return builder.toString();
				// return NodeGeneElement.this.vertexRep.getName();
			}
		}));
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

	public void setIsThisNodeUsedForFiltering(Boolean selection) {
		this.isThisNodeUsedForFiltering = selection;
		repaint();
	}

	public Boolean getIsThisNodeUsedForFiltering() {
		return this.isThisNodeUsedForFiltering;
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

	public String getLabel() {
		return label;
	}

	/**
	 * removes a vertex from the displayed vertex list -> needed to remove duplicate vertices within the graph
	 * 
	 * @param vertexToRemove
	 * @return returns true if this is the last vertex of the NodeElement (which means the whole node needs to
	 *         be removed) & false otherwise
	 */
	public boolean removeVertex(PathwayVertex vertexToRemove) {
		if (vertices.size() < 2)
			return false;
		boolean containedElement = vertices.remove(vertexToRemove);
		this.displayedVertex = vertices.get(0);
		this.label = displayedVertex.getHumanReadableName();
		return containedElement;
	}

	/**
	 * remove multiple vertices from the nodes displayed vertex list
	 * 
	 * @param node
	 * @param verticesToRemove
	 */
	public boolean removeMultipleVertices(List<PathwayVertex> verticesToRemove) {

		boolean success = true;
		for (PathwayVertex vertexToRemove : verticesToRemove) {
			boolean tmp = this.removeVertex(vertexToRemove);
			success = success && tmp;
		}

		return success;
	}

	@Override
	public String toString() {
		String outputString = uid + ": ";
		outputString += "Label(" + label + ") ";
		outputString += "VrepSize("
				+ ((vrepsWithThisNodesVerticesList != null) ? Integer.toString(vrepsWithThisNodesVerticesList
						.size()) : "1") + ") ";
		outputString += "Vertices[" + vertices + "]";

		return outputString;
	}

	public boolean isMerged() {
		return isMerged;
	}

	public void setIsMerged(boolean isMerged) {
		this.isMerged = isMerged;
	}

	public List<PathwayVertexRep> getVrepsWithThisNodesVerticesList() {
		return vrepsWithThisNodesVerticesList;
	}

	public void setVrepsWithThisNodesVerticesList(List<PathwayVertexRep> vrepsWithThisNodesVerticesList) {
		this.vrepsWithThisNodesVerticesList = vrepsWithThisNodesVerticesList;
	}

	public void addVrepWithThisNodesVerticesList(PathwayVertexRep vrepWithThisNodesVertices) {
		this.vrepsWithThisNodesVerticesList.add(vrepWithThisNodesVertices);
	}

}
