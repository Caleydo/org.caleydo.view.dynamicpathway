package org.caleydo.view.dynamicpathway.ui;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.CalculateIntersectionUtil;
import org.caleydo.view.dynamicpathway.util.Coordinates;

public class NodeElement extends GLElementContainer implements IFRLayoutNode {

	protected String uid;

	protected static final int FONT_SIZE = 12;
	protected static final String CONTOUR_COLOR = Color.LIGHT_GRAY.getHEX();
//	protected static final String CONTEXT_FILLING_COLOR = "#F2F2F2";
	protected static final String PREVIOUS_FOCUS_NODE_COLOR = Color.YELLOW.getHEX();// "#3067C6";
	protected static final String NODE_FILLING_COLOR = "#F2F2F2";
	protected static final String SELECTION_CONTOUR_COLOR = "F8AB65";//SelectionType.SELECTION.getColor().getHEX();
	protected static final String MOUSEROVER_CONTOUR_COLOR = "#FDE7B9";//SelectionType.MOUSE_OVER.getColor().getHEX();
	protected static final String FILTER_CONTOUR_COLOR = "#F15C4C";
	
	protected String contourColor;
	protected String fillingColor;

	protected PathwayVertexRep vertexRep;
	protected List<PathwayVertex> vertices;
	// TODO: remove
	protected List<PathwayVertexRep> vrepsWithThisNodesVerticesList;
	private Set<PathwayGraph> representedPathways;

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

	protected DynamicPathwaysCanvas parentGraph;

	protected double height;
	protected double width;

	/**
	 * if this node element is merged (artificially created - not in original PathwayGraph) set false by default
	 */
	protected boolean isMerged = false;
	protected boolean wasMerged = false;

	protected boolean wasPreviouslyFocusNode = false;

	/**
	 * the context menu, which pops up, when a node is right clicked
	 */
	protected GenericContextMenuItem focusNodeMenu;
	protected ChangeFocusNodeEvent focusNodeEvent;
	protected GenericContextMenuItem filterPathwayMenu;
	
	

	public NodeElement(PathwayVertexRep vertexRep, List<PathwayVertex> pathwayVertices,
			final DynamicPathwaysCanvas parentGraph, Set<PathwayGraph> pathways, float widthAndHeightAddend) {
		this.uid = UUID.randomUUID().toString();
		this.vertexRep = vertexRep;
		this.centerX = vertexRep.getCenterX();
		this.centerY = vertexRep.getCenterY();
		this.coords = new Coordinates();
		this.isThisNodeSelected = false;
		this.isThisNodeUsedForFiltering = false;
		this.isMouseOver = false;
		this.parentGraph = parentGraph;
		this.vertices = new CopyOnWriteArrayList<PathwayVertex>(pathwayVertices);
		this.vrepsWithThisNodesVerticesList = new LinkedList<PathwayVertexRep>();
		this.focusNodeEvent = new ChangeFocusNodeEvent(this);
		this.representedPathways = new HashSet<PathwayGraph>(pathways);
		this.contourColor = CONTOUR_COLOR;
		this.fillingColor = NODE_FILLING_COLOR;

		if (vertices.size() > 0 && vertices.get(0).getType() != EPathwayVertexType.group) {
			this.displayedVertex = vertices.get(0);
			this.label = displayedVertex.getHumanReadableName();
			this.height = this.vertexRep.getHeight() + widthAndHeightAddend;
			this.width = this.vertexRep.getWidth() + widthAndHeightAddend;
			this.centerX = this.vertexRep.getLowerLeftCornerX()+ this.height/2.0f;
			this.centerY = this.vertexRep.getLowerLeftCornerY() + this.width/2.0f;
		}

		focusNodeMenu = new GenericContextMenuItem("Choose as focus node", focusNodeEvent);
		filterPathwayMenu = new GenericContextMenuItem("Filter pathway list by these node",
				new FilterPathwayEvent(this));

		setVisibility(EVisibility.PICKABLE);

	}

	public void addPathway(PathwayGraph pathway) {
		this.representedPathways.add(pathway);
	}

	public void addPathways(Set<PathwayGraph> pathways) {
		for (PathwayGraph pathway : pathways)
			addPathway(pathway);
	}

	public void addVrepWithThisNodesVerticesList(PathwayVertexRep vrepWithThisNodesVertices) {
		this.vrepsWithThisNodesVerticesList.add(vrepWithThisNodesVertices);
	}

	public Point2D.Double getCenter() {
		return (new Point2D.Double(centerX, centerY));
	}

	@Override
	public double getCenterX() {
		return this.centerX;
	}

	@Override
	public double getCenterY() {
		return this.centerY;
	}

	public Coordinates getCoords() {
		return coords;
	}

	public PathwayVertex getDisplayedVertex() {
		return displayedVertex;
	}

	@Override
	public double getHeight() {
		return this.height;
	}

	/**
	 * 
	 * calculate intersection if the node shape is rectangular used in
	 * {@link org.caleydo.view.dynamicpathway.ui.EdgeElement#renderImpl(GLGraphics, float, float)} for drawing the edges
	 * between 2 nodes
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

	public Boolean getIsNodeSelected() {
		return this.isThisNodeSelected;
	}

	public Boolean getIsThisNodeUsedForFiltering() {
		return this.isThisNodeUsedForFiltering;
	}

	public String getLabel() {
		return label;
	}

	public List<PathwayGraph> getPathways() {
		List<PathwayGraph> pathways = new ArrayList<PathwayGraph>(this.representedPathways);
		return pathways;
	}

	public EPathwayVertexType getType() {
		return vertexRep.getType();
	}

	public PathwayVertexRep getVertexRep() {
		return vertexRep;
	}

	public List<PathwayVertex> getVertices() {
		return vertices;
	}

	public List<PathwayVertexRep> getVrepsWithThisNodesVerticesList() {
		return vrepsWithThisNodesVerticesList;
	}

	@Override
	public double getWidth() {
		return this.width;
	}

	public boolean isMerged() {
		return isMerged;
	}

	public boolean isWasPreviouslyFocusNode() {
		return wasPreviouslyFocusNode;
	}

	public void makeThisFocusNode() {
		EventPublisher.trigger(focusNodeEvent);
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

	public boolean removePathway(PathwayGraph pathway) {
		return this.representedPathways.remove(pathway);
	}

	/**
	 * removes a vertex from the displayed vertex list -> needed to remove duplicate vertices within the graph
	 * 
	 * @param vertexToRemove
	 * @return returns true if this is the last vertex of the NodeElement (which means the whole node needs to be
	 *         removed) & false otherwise
	 */
	public boolean removeVertex(PathwayVertex vertexToRemove) {
		if (vertices.size() < 2)
			return false;
		boolean containedElement = vertices.remove(vertexToRemove);
		this.displayedVertex = vertices.get(0);
		this.label = displayedVertex.getHumanReadableName();
		return containedElement;
	}

	@Override
	public void setCenter(double centerX, double centerY) {
		this.centerX = centerX;
		this.centerY = centerY;

		coords.setCoords(centerX, centerY, this.width, this.height);
	}

	public void setDisplayedVertex(PathwayVertex displayedVertex) {
		this.displayedVertex = displayedVertex;
		this.label = displayedVertex.getHumanReadableName();
	}

	public void setIsMerged(boolean isMerged) {
		this.isMerged = isMerged;
	}

	public void setIsNodeSelected(Boolean selection) {
		this.isThisNodeSelected = selection;
		repaint();
	}

	public void setIsThisNodeUsedForFiltering(Boolean selection) {
		this.isThisNodeUsedForFiltering = selection;
		repaint();
	}

	public void setVertices(List<PathwayVertex> vertices) {
		this.vertices = vertices;
		this.displayedVertex = vertices.get(0);
		this.label = displayedVertex.getHumanReadableName();
	}

	public void setWasMerged(boolean wasMerged) {
		this.wasMerged = wasMerged;
	}

	public void setWasPreviouslyFocusNode(boolean wasPreviouslyFocusNode) {
		this.wasPreviouslyFocusNode = wasPreviouslyFocusNode;
	}

	@Override
	public String toString() {
		String outputString = uid + ": ";
		outputString += "Label(" + label + ") ";
		outputString += "wasMerged(" + wasMerged + ")";
		outputString += "VrepSize("
				+ ((vrepsWithThisNodesVerticesList != null) ? Integer.toString(vrepsWithThisNodesVerticesList.size())
						: "1") + ") ";
		outputString += "Vertices[" + vertices + "]";
		outputString += "Pathways[" + getPathwaySetTitles() + "]";

		return outputString;
	}

	public boolean wasMerged() {
		return wasMerged;
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
	
	/**
	 * sets the colors (contour & filling) based on the nodes state
	 */
	protected void setColors() {
		
		if (isThisNodeUsedForFiltering) {
			this.fillingColor = FILTER_CONTOUR_COLOR;
			this.contourColor = "#87332A";
		} else if (isThisNodeSelected) {
			this.fillingColor = SELECTION_CONTOUR_COLOR;
			this.contourColor = SelectionType.SELECTION.getColor().getHEX();
		} else if (isMouseOver) {
			this.fillingColor = MOUSEROVER_CONTOUR_COLOR;
			this.contourColor = "#F9C44F";
		} else if (wasPreviouslyFocusNode) {
			this.fillingColor = PREVIOUS_FOCUS_NODE_COLOR;
			this.contourColor = CONTOUR_COLOR;
		}
		else {
			this.fillingColor = NODE_FILLING_COLOR;
			this.contourColor = CONTOUR_COLOR;
		}
		
		
	}
	
	/**
	 * Checks if it somehow selected, because then it has a thicker contour
	 * 
	 * @return true if is selected, false otherwise
	 */
	protected boolean isSomehowSelected() {
		if(isThisNodeUsedForFiltering || isThisNodeSelected || isMouseOver || wasPreviouslyFocusNode)
			return true;
		else
			return false;
	}

	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
	}

	private String getPathwaySetTitles() {
		String pathwayListTitles = "";

		for (PathwayGraph pathway : this.representedPathways)
			pathwayListTitles += pathway.getTitle() + ",";

		pathwayListTitles = pathwayListTitles.substring(0, pathwayListTitles.length() - 1);

		return pathwayListTitles;
	}

}
