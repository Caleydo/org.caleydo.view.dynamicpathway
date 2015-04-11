/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import static org.caleydo.core.view.opengl.layout2.animation.InOutStrategies.OTHER;
import static org.caleydo.core.view.opengl.layout2.animation.InOutStrategies.ZERO;
import static org.caleydo.core.view.opengl.layout2.animation.MoveTransitions.MOVE_LINEAR;
import gleem.linalg.Vec2f;
import gleem.linalg.Vec4f;

import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.media.opengl.GL2;

import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.IEventBasedSelectionManagerUser;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.util.color.ColorBrewer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.Duration;
import org.caleydo.core.view.opengl.layout2.animation.InOutInitializers;
import org.caleydo.core.view.opengl.layout2.animation.InOutInitializers.IInOutInitializer;
import org.caleydo.core.view.opengl.layout2.animation.InOutInitializers.InOutInitializerBase;
import org.caleydo.core.view.opengl.layout2.animation.InOutStrategies.IInOutStrategy;
import org.caleydo.core.view.opengl.layout2.animation.InOutTransitions;
import org.caleydo.core.view.opengl.layout2.animation.InOutTransitions.IInTransition;
import org.caleydo.core.view.opengl.layout2.animation.MoveTransitions;
import org.caleydo.core.view.opengl.layout2.geom.Rect;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.util.spline.ITesselatedPolygon;
import org.caleydo.core.view.opengl.util.spline.TesselatedPolygons;
import org.caleydo.datadomain.genetic.EGeneIDTypes;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwayView;
import org.caleydo.view.dynamicpathway.internal.NodeMergingException;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutGraph;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.ui.NodeElement.ENodeState;
import org.caleydo.view.dynamicpathway.util.GraphMergeUtil;
import org.jgrapht.graph.DefaultEdge;

import setvis.SetOutline;
import setvis.bubbleset.BubbleSet;
import setvis.gui.CanvasComponent;
import setvis.shape.AbstractShapeGenerator;
import setvis.shape.BSplineShapeGenerator;

import com.jogamp.opengl.util.awt.TextureRenderer;

/**
 * Container, which is defined by the graph layout {@link GLFruchtermanReingoldLayout} contains the renderable Elements
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwaysCanvas extends AnimatedGLElementContainer implements IFRLayoutGraph,
		IEventBasedSelectionManagerUser {
	public static final int DEFAULT_ADD_PATHWAY_DURATION = 1000;
	private static final Duration DEFAULT_IN_TRANSITION_DURATION = new Duration(DEFAULT_ADD_PATHWAY_DURATION);
	private static final int DEFAULT_REMOVE_PATHWAY_DURATION = 200;

	private boolean displayOnlyVerticesWithEdges = true;
	private boolean removeDuplicateVertices = true;
	private boolean focusGraphWithDuplicateVertices = false;

	/**
	 * contains focus & kontextpathway informations
	 */
	private DynamicPathwayGraph pathway;

	/**
	 * contains nodes & edges used for defining and rendering the layout
	 */
	private Set<NodeElement> nodeSet;
	private Set<EdgeElement> edgeSet;

	/**
	 * A list of colors which are being used to color the bubble sets
	 */
	private List<Color> bubbleSetColors = ColorBrewer.Set1.getColors(100);
	// private List<Integer> usedColorIndezes = new LinkedList<Integer>();

	/**
	 * the currently selected node
	 */
	private NodeElement currentSelectedNode;

	/**
	 * is null if no node was selected, otherwise it is a reference to the currently selected node -> needed for merging
	 */
	private NodeElement focusNode;
	private PathwayVertex focusVertex = null;

	/**
	 * the view that hold the pathway list & the pathway representation
	 */
	private DynamicPathwayView view;

	/**
	 * informs other that (and which) vertex (nodeElement) was selected
	 */
	private EventBasedSelectionManager vertexSelectionManager;

	/**
	 * needed for node merging & edge representation
	 */
	private Map<PathwayVertex, NodeElement> uniqueVertexMap;
	private Map<PathwayVertexRep, NodeElement> vrepToGroupNodeMap;

	/**
	 * the bubble set
	 */
	private CanvasComponent bubblesetCanvas;

	/**
	 * mapping for the pathway graphs that contain only of a part of vertices & edges to the full pathway graphs <br />
	 * (only needed when context pathways are added partly)
	 */
	private Map<PathwayGraph, PathwayGraph> originalPathwaysOfSubpathwaysMap;

	Logger mergeLogger = Logger.getLogger("MergeLog");

	private List<PathwayVertex> previousFocusVertices;
	private Map<PathwayGraph, Integer> contextPathwayColorIndex;
	private Integer nextColorIndex;

	private TextureRenderer textureRenderer = null;

	public DynamicPathwaysCanvas(GLFruchtermanReingoldLayout layout, DynamicPathwayView view) {

		this.pathway = new DynamicPathwayGraph();

		this.nodeSet = new HashSet<NodeElement>();
		this.edgeSet = new HashSet<EdgeElement>();

		this.view = view;

		this.vertexSelectionManager = new EventBasedSelectionManager(this, IDType.getIDType(EGeneIDTypes.PATHWAY_VERTEX
				.name()));
		this.vertexSelectionManager.registerEventListeners();

		this.uniqueVertexMap = new HashMap<PathwayVertex, NodeElement>();
		this.vrepToGroupNodeMap = new HashMap<PathwayVertexRep, NodeElement>();

		this.originalPathwaysOfSubpathwaysMap = new HashMap<PathwayGraph, PathwayGraph>();

		this.previousFocusVertices = new ArrayList<PathwayVertex>();
		this.contextPathwayColorIndex = new HashMap<PathwayGraph, Integer>();
		this.nextColorIndex = 0;

		setLayout(layout);

		// Pathways animation setting
		setDefaultDuration(DEFAULT_ADD_PATHWAY_DURATION);
		IInOutInitializer GROW = new InOutInitializerBase(OTHER, OTHER, ZERO, ZERO);
		setDefaultInTransition(new InOutTransitions.InOutTransitionBase(GROW, MoveTransitions.GROW_LINEAR));
		setDefaultOutTransition(new InOutTransitions.InOutTransitionBase(GROW, MoveTransitions.GROW_LINEAR));

		setUpBubbleSet();
		
	}

	/**
	 * add a new pathway set
	 * 
	 * @param subPathway
	 * @param originalPathway
	 */
	public void addOriginalPathwayAndSubpathwayToMap(PathwayGraph subPathway, PathwayGraph originalPathway) {
		this.originalPathwaysOfSubpathwaysMap.put(subPathway, originalPathway);
	}

	/**
	 * 
	 * Add a (new) pathway to the canvas
	 * 
	 * 
	 * @param pathwayToAdd
	 *            if a new pathway was added, a new combined (focus + parts of kontext pathways) pathway is created
	 * @param isFocusPathway
	 *            true if a context pathway should be added, false if a focus pathway should be added, if null, it is
	 *            defined later
	 * @param clearOriginalSubwaysMap
	 *            ONLY for focus pathway: true if nothing should be kept (false for switching roles)
	 * @param keepOldFocusNode
	 *            ONLY for focus pathway: true if the focus node should be recovered after the pathway was added
	 */
	public void addPathwayToCanvas(PathwayGraph pathwayToAdd, Boolean isFocusPathway, Boolean clearOriginalSubwaysMap,
			Boolean keepOldFocusNode) {

		pathway.addFocusOrKontextPathway(pathwayToAdd, !isFocusPathway);

		Color nodeColor;

		if (isFocusPathway)
			nodeColor = null;
		else {
			setColorOfPathway(pathwayToAdd);
			nodeColor = getColorOfPathway(pathwayToAdd);
		}
		view.addPathwayToControllBar(pathwayToAdd, isFocusPathway, nodeColor);

		if (focusNode != null && (keepOldFocusNode || isFocusPathway)) {
			focusVertex = focusNode.getDisplayedVertex();
			previousFocusVertices = new ArrayList<PathwayVertex>(focusNode.getVertices());
			previousFocusVertices.remove(focusVertex);
		} else
			System.out.println("Filtering node was null");

		// if you want to add a new focus graph
		if (isFocusPathway) {

			// clears all from past selection
			clearCanvasAndInfo(clearOriginalSubwaysMap, keepOldFocusNode);

			setDefaultDuration(DEFAULT_ADD_PATHWAY_DURATION);

			/**
			 * if duplicate are allowed or not
			 */
			if (removeDuplicateVertices == false) {
				focusGraphWithDuplicateVertices = true;
				addPathwayWithDuplicates(pathwayToAdd);
			} else {
				focusGraphWithDuplicateVertices = false;
				addPathwayWithoutDuplicates(pathwayToAdd, true, pathway.getCombinedGraph());

				if (focusVertex != null && keepOldFocusNode)
					findAndCreateNewFocusNodeBasedOnOldFocusVertex();

			}
		} else {

			setDefaultDuration(DEFAULT_ADD_PATHWAY_DURATION);

			// if the focus graph was added with duplicates, it needs to be
			// added without them
			if (focusGraphWithDuplicateVertices) {

				removeDuplicateVertices = true;
				addPathwayToCanvas(pathway.getFocusPathway(), true, true, keepOldFocusNode);

				if (focusVertex != null)
					focusNode = uniqueVertexMap.get(focusVertex);
			}

			boolean noNodesAdded = addPathwayWithoutDuplicates(pathwayToAdd, false, pathway.getCombinedGraph());

			if (focusVertex != null)
				findAndCreateNewFocusNodeBasedOnOldFocusVertex();

			if (!noNodesAdded)
				resetEdges();

		}

		for (NodeElement node : nodeSet)
			node.setIsMerged(false);

	}

	private void resetEdges() {
		for (EdgeElement edge : this.edgeSet) {
			edge.setVisibility(EVisibility.HIDDEN);
			edge.setTimerDelay(DEFAULT_ADD_PATHWAY_DURATION);
		}
	}

	/**
	 * clears all sets, view & maps, so it can be set again
	 * 
	 * @param clearOriginalPathwaysMap
	 *            true if all information needs to be reset
	 * @param keepFocusNodeAndVertex
	 *            if true, the references to the focus node & focus vertex are kept
	 */
	public void clearCanvasAndInfo(Boolean clearOriginalPathwaysMap, Boolean keepFocusNodeAndVertex) {
		setDefaultDuration(DEFAULT_REMOVE_PATHWAY_DURATION);

		// clear all selection
		currentSelectedNode = null;
		if (!keepFocusNodeAndVertex) {
			focusNode = null;
			focusVertex = null;
		}

		// clear all sets -> might be reset again
		nodeSet.clear();
		edgeSet.clear();
		vrepToGroupNodeMap.clear();
		uniqueVertexMap.clear();
		contextPathwayColorIndex.clear();
		// usedColorIndezes.clear();
		nextColorIndex = 0;

		if (clearOriginalPathwaysMap)
			originalPathwaysOfSubpathwaysMap.clear();

		// clear the canvas
		clear();

		view.unfilterPathwayList();
	}

	/**
	 * checks which vertices of verticesToCheckList already exist in {@link #uniqueVertexMap}
	 * 
	 * @param verticesToCheckList
	 *            check if these vertices already exist
	 * @return Map<NodeElement, List<PathwayVertex>>: NodeElement - this NodeElement has vertices duplicate to some in
	 *         verticesToCheckList, List &lt;PathwayVertex&gt;: all vertices of NodeElement, that are also in
	 *         verticesToCheckList
	 */
	public Map<NodeElement, List<PathwayVertex>> getNodeElementsContainingSameVertices(
			Map<PathwayVertex, NodeElement> vertexNodeMap, List<PathwayVertex> verticesToCheckList) {

		Map<NodeElement, List<PathwayVertex>> equivalentVerticesMap = new HashMap<NodeElement, List<PathwayVertex>>();

		for (PathwayVertex vertexToCheck : verticesToCheckList) {
			if (vertexNodeMap.containsKey(vertexToCheck)) {
				NodeElement existingNodeElement = vertexNodeMap.get(vertexToCheck);
				if (!equivalentVerticesMap.containsKey(existingNodeElement)) {
					List<PathwayVertex> duplicateVertices = new LinkedList<PathwayVertex>();
					duplicateVertices.add(vertexToCheck);
					equivalentVerticesMap.put(existingNodeElement, duplicateVertices);
				} else {
					equivalentVerticesMap.get(existingNodeElement).add(vertexToCheck);
				}
			}
		}

		return equivalentVerticesMap;
	}

	public List<PathwayGraph> getContextPathways() {
		return pathway.getContextPathways();
	}

	public DynamicPathwayGraph getDynamicPathway() {
		return pathway;
	}

	@Override
	public Set<IFRLayoutEdge> getEdgeSet() {
		Set<IFRLayoutEdge> interfaceSet = new HashSet<IFRLayoutEdge>();

		for (EdgeElement edge : edgeSet) {
			interfaceSet.add((IFRLayoutEdge) edge);
		}

		return interfaceSet;
	}

	public NodeElement getFocusNode() {
		return focusNode;
	}

	public PathwayGraph getFocusPathway() {
		return pathway.getFocusPathway();
	}

	@Override
	public Vec2f getMinSize() {
		return new Vec2f(100, 100);
	}

	@Override
	public Set<IFRLayoutNode> getNodeSet() {
		Set<IFRLayoutNode> interfaceSet = new HashSet<IFRLayoutNode>();

		for (NodeElement node : nodeSet) {
			interfaceSet.add((IFRLayoutNode) node);
		}

		return interfaceSet;
	}

	/**
	 * get the full pathway of the pathway that was just added partly
	 * 
	 * @param subPathway
	 *            the partly added pathway
	 * @return the full pathway
	 */
	public PathwayGraph getOriginalPathwaysOfSubpathway(PathwayGraph subPathway) {
		return originalPathwaysOfSubpathwaysMap.get(subPathway);
	}

	public DynamicPathwayView getView() {
		return view;
	}

	public boolean isDisplayOnlyVerticesWithEdges() {
		return displayOnlyVerticesWithEdges;
	}

	public boolean isPathwayPresent(PathwayGraph pathway) {
		if (this.pathway.isFocusGraph(pathway)) {
			return true;
		}
		if (this.pathway.isContextGraph(pathway)) {
			return true;
		}
		if (this.originalPathwaysOfSubpathwaysMap.containsValue(pathway)) {
			return true;
		}

		return false;
	}

	/**
	 * checks whether the given pathway is a sub pathway (limited to a certain node range) or a full pathway graph
	 * 
	 * @param subPathway
	 *            the pathway to check
	 * @return true if the given pathway is a sub pathway, false otherwise
	 */
	public boolean isSubPathway(PathwayGraph subPathway) {
		return this.originalPathwaysOfSubpathwaysMap.containsKey(subPathway);
	}

	@Override
	public void notifyOfSelectionChange(EventBasedSelectionManager selectionManager) {
		repaint();
	}

	/**
	 * if a vertex was called, other views are informed
	 * 
	 * called by NodeElement
	 * 
	 * @param vertex
	 *            which was selected
	 * @param node
	 *            to the which the vertex belongs
	 * @param pick
	 */
	public void onSelect(List<PathwayVertex> vertices, NodeElement node, Pick pick) {
		switch (pick.getPickingMode()) {

		case MOUSE_OVER:
			vertexSelectionManager.clearSelection(SelectionType.MOUSE_OVER);
			for (PathwayVertex vertex : vertices)
				vertexSelectionManager.addToType(SelectionType.MOUSE_OVER, vertex.getID());
			break;

		case MOUSE_OUT:
			for (PathwayVertex vertex : vertices)
				vertexSelectionManager.removeFromType(SelectionType.MOUSE_OVER, vertex.getID());
			break;

		case CLICKED:
			vertexSelectionManager.clearSelection(SelectionType.SELECTION);
			for (PathwayVertex vertex : vertices)
				vertexSelectionManager.addToType(SelectionType.SELECTION, vertex.getID());
			break;

		case RIGHT_CLICKED:
			vertexSelectionManager.clearSelection(SelectionType.SELECTION);
			for (PathwayVertex vertex : vertices)
				vertexSelectionManager.addToType(SelectionType.SELECTION, vertex.getID());
			break;

		default:
			// Do not trigger a selection update for other picking modes
			return;
		}

		vertexSelectionManager.triggerSelectionUpdateEvent();
		repaint();
	}

	/**
	 * removes a partly added pathway from the map (i.e. if a (partly added) context pathway was removed
	 * 
	 * @param subPathway
	 * @return TODO
	 */
	public PathwayGraph removeOriginalPathwayAndSubpathwayOfMap(PathwayGraph subPathway) {
		System.out.println(originalPathwaysOfSubpathwaysMap);
		PathwayGraph fullPw = this.originalPathwaysOfSubpathwaysMap.remove(subPathway);
		System.out.println(originalPathwaysOfSubpathwaysMap);
		return fullPw;
	}

	/**
	 * Removed from the map, so that the color can be reused
	 * 
	 * @param pathway
	 *            the pathway to be removed
	 * @return TODO
	 */
	public Integer removePathwayFromContextPathwayColorIndexMap(PathwayGraph pathway) {
		// TODO: find better solution!! -> list of freed indexes
		Integer index = contextPathwayColorIndex.get(pathway);

		if (index == null)
			return -1;

		if (index.intValue() == (nextColorIndex.intValue() - 1))
			nextColorIndex--;
		// else
		// usedColorIndezes.add(index);

		return contextPathwayColorIndex.remove(pathway);
	}

	/**
	 * remove the current focus node
	 */
	public void removeFocusNode() {
		if (focusNode != null) {
			focusNode.setNodeState(ENodeState.DEFAULT);
			focusNode = null;
		}
	}

	/**
	 * if a node (wrapper for PathwayVertexRep) is selected via the filtering command, it is highlighted and the pathway
	 * list on the left is filtered by pathways, which contain this element
	 * 
	 * @param newFilteringNode
	 *            the node, which the pathway list should be filtered with
	 */
	public boolean setFocusNode(NodeElement newFilteringNode) {

		boolean focusNodeChanged = false;

		/**
		 * if nothing was selected, just set the new node
		 */
		if (focusNode == null) {
			// System.out.println("Setting new filtering node without old: " + newFilteringNode);
			focusNode = newFilteringNode;
			focusNode.setNodeState(ENodeState.FOCUS);
			focusNodeChanged = false;
		}

		/**
		 * if another node was selected before, deselect it and selected the new node
		 */
		else if (newFilteringNode != null) {
			// System.out.println("Changing filtering node from  " + currentFilteringNode + " to " + newFilteringNode);
			focusNode.setNodeState(ENodeState.DEFAULT);

			focusNode = newFilteringNode;
			focusNode.setNodeState(ENodeState.FOCUS);
			focusNodeChanged = true;
			System.out.println("New Focus Node: " + focusNode);

		} else
			try {
				throw new Exception("new filtering node was null");
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}

		return focusNodeChanged;

	}

	/**
	 * set the current focus pathway
	 * 
	 * @param newFocusPathway
	 */
	public void setFocusPathway(PathwayGraph newFocusPathway) {
		pathway.setFocusPathway(newFocusPathway);
	}

	/**
	 * if a node (wrapper for PathwayVertexRep) is selected, it is highlighted and the pathway list on the left is
	 * filtered by pathways, which contain this element
	 * 
	 * @param newSelectedNode
	 */
	public void setOrResetSelectedNode(NodeElement newSelectedNode) {
		/**
		 * if nothing was selected, just set the new node
		 */
		if (currentSelectedNode == null) {
			currentSelectedNode = newSelectedNode;
			currentSelectedNode.setNodeState(ENodeState.SELECTED);
		}

		/**
		 * if another node was selected before, deselect it and selected the new node
		 */
		else {
			currentSelectedNode.setNodeState(ENodeState.DESELECT);
			
			currentSelectedNode = newSelectedNode;
			currentSelectedNode.setNodeState(ENodeState.SELECTED);
		}

	}

	/**
	 * if true only vertices that have edges (i.e. of degree > 0) are being displayed
	 * 
	 * @param displayOnlyVerticesWithEdges
	 */
	public void setDisplayOnlyVerticesWithEdges(boolean displayOnlyVerticesWithEdges) {
		this.displayOnlyVerticesWithEdges = displayOnlyVerticesWithEdges;
	}

	/**
	 * if true, only vertices are unique within the canvas -> is automatically true, if context pathways were added
	 * 
	 * @param removeUniqueVertices
	 */
	public void setRemoveDuplicateVertices(boolean removeUniqueVertices) {
		this.removeDuplicateVertices = removeUniqueVertices;
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		super.renderImpl(g, w, h);

		// TODO: fix or remove
		if (textureRenderer == null)
			textureRenderer = new TextureRenderer((int) w, (int) h, true);

		Map<PathwayGraph, Integer> pathwayBubbleSetIndexMap = calcAndSetBubbleSet(g);

		for (PathwayGraph pathway : pathwayBubbleSetIndexMap.keySet()) {
			Integer index = pathwayBubbleSetIndexMap.get(pathway);
			Integer colorIndex = contextPathwayColorIndex.get(pathway);

			List<Vec2f> points = new ArrayList<>();
			Graphics2D g2d = textureRenderer.createGraphics();

			bubblesetCanvas.setSelection(index);
			points = bubblesetCanvas.getShapePoints(g2d);
			g2d.dispose();

			Color color = bubbleSetColors.get(colorIndex);
			Color transparentColor = new Color(color.getRGBA());
			transparentColor.a = 0.2f;

			ITesselatedPolygon polygon = TesselatedPolygons.polygon2(points);
			g.incZ(-0.3f); // move to 0
			g.gl.glPushAttrib(GL2.GL_LINE_BIT);
			g.color(transparentColor).fillPolygon(polygon).color(color).lineWidth(4).drawPath(polygon);
			g.gl.glPopAttrib();
			// g.color(color).drawPath(polygon).lineWidth(4).drawPath(polygon);
			g.incZ(0.3f);
		}

	}

	@Override
	protected void takeDown() {
		vertexSelectionManager.unregisterEventListeners();
		vertexSelectionManager = null;
		super.takeDown();
	}

	/**
	 * represent graph biologically correct, with duplicates <br />
	 * used when only one graph is displayed
	 * 
	 * @param pathwayToAdd
	 */
	private void addPathwayWithDuplicates(PathwayGraph pathwayToAdd) {

		Map<PathwayVertexRep, NodeElement> vrepToNodeElementMap = new HashMap<PathwayVertexRep, NodeElement>();

		/**
		 * VERTICES <br />
		 * ===========
		 */
		for (PathwayVertexRep vrep : pathwayToAdd.vertexSet()) {
			/**
			 * EPathwayVertexType.map is the a vrep that contains the title of the pathway -> ignore it
			 */
			if (vrep.getType() == EPathwayVertexType.map)
				continue;

			/**
			 * ignore 0° vertices, if the option was selected
			 */
			if (displayOnlyVerticesWithEdges && (pathwayToAdd.inDegreeOf(vrep) < 1)
					&& (pathwayToAdd.outDegreeOf(vrep) < 1))
				continue;

			NodeElement nodeElement = GraphMergeUtil.createNewNodeElement(vrep, vrep.getPathwayVertices(), null, this,
					pathwayToAdd);

			if (nodeElement == null) {
				System.err.println("Node creation of vrep " + vrep + "failed");
				continue;
			}

			vrepToNodeElementMap.put(vrep, nodeElement);
			addNodeToContainers(nodeElement, false);
		}

		/**
		 * EDGES <br />
		 * ===========
		 */
		for (DefaultEdge edge : pathwayToAdd.edgeSet()) {
			NodeElement sourceNode = vrepToNodeElementMap.get(pathwayToAdd.getEdgeSource(edge));
			NodeElement targetNode = vrepToNodeElementMap.get(pathwayToAdd.getEdgeTarget(edge));

			if (sourceNode == null || targetNode == null) {
				System.err.println("Source (" + sourceNode + ") or Target Node (" + targetNode + ") of edge (" + edge
						+ ") not in map");
				continue;
			}

			EdgeElement edgeElement = new EdgeElement(edge, sourceNode, targetNode, DEFAULT_ADD_PATHWAY_DURATION);

			edgeSet.add(edgeElement);
			add(edgeElement);
		}
	}

	/**
	 * add this new pathway, but remove all duplicate PathwayVerticeso
	 * 
	 * @param pathwayToAdd
	 *            the new pathway to add
	 * @param nodeSet
	 *            all displayed nodes are add to this set -> needed for layout algorithm
	 * @param edgeSetToAdd
	 *            all displayed edges are add to this set -> needed for layout algorithm
	 * @param addMergedNodesToSamePathway
	 *            if <b>false<b/> the merged nodes are added part of the combined pathway (i.e. create vertex uniqueness
	 *            within 2 or more pathways)<br />
	 *            if <b>true<b/> they are part of the pathwayToAdd (i.e. create vertex uniqueness within 1 pathway
	 *            itself)
	 * @param combinedPathway
	 *            the pathway to add the merged nodes to (if addMergedNodesToSamePathway is false)
	 * @param nodeColor
	 *            the color of the created nodes
	 */
	private boolean addPathwayWithoutDuplicates(PathwayGraph pathwayToAdd, boolean addMergedNodesToSamePathway,
			PathwayGraph combinedPathway) {

		boolean noNodeDisplayed = true;
		/**
		 * VERTICES <br />
		 * ===========
		 */
		for (Iterator<PathwayVertexRep> vRepIterator = pathwayToAdd.vertexSet().iterator(); vRepIterator.hasNext();) {
			PathwayVertexRep vrep = vRepIterator.next();

			if (vrep.getType() == EPathwayVertexType.map)
				continue;

			try {
				noNodeDisplayed &= checkAndMergeNodes(pathwayToAdd, vrep, addMergedNodesToSamePathway, combinedPathway);
			} catch (NodeMergingException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}

		}

		/**
		 * EDGES <br />
		 * ===========
		 */
		for (DefaultEdge edge : pathwayToAdd.edgeSet()) {
			try {
				GraphMergeUtil.addEdgeToEdgeSet(edge, pathwayToAdd, uniqueVertexMap, edgeSet, vrepToGroupNodeMap, this,
						DEFAULT_ADD_PATHWAY_DURATION);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
		}

		return noNodeDisplayed;

	}

	/**
	 * calculates and sets the surrounding borders (bubble set) of the pathways
	 * 
	 * @param g
	 * @return
	 */
	private final Map<PathwayGraph, Integer> calcAndSetBubbleSet(GLGraphics g) {
		Map<PathwayGraph, Integer> pathwayBubbleSetIndexMap = new HashMap<PathwayGraph, Integer>();

		bubblesetCanvas.removeAllGroups();

		PathwayGraph focusPathway = this.getFocusPathway();
		int colorIndex = 0;
		for (NodeElement node : this.nodeSet) {

			List<PathwayGraph> pathways = node.getPathways();

			Rect nodeBounds = node.getRectBounds();

			double centerX, centerY;
			if (node.getType() == EPathwayVertexType.compound) {
				centerX = nodeBounds.x();
				centerY = nodeBounds.y();
			} else {
				centerX = nodeBounds.x() + (nodeBounds.width() / 2.0f);
				centerY = nodeBounds.y() + (nodeBounds.height() / 2.0f);
			}

			for (PathwayGraph pathway : pathways) {
				if (pathway.equals(focusPathway))
					continue;

				Integer index = pathwayBubbleSetIndexMap.get(pathway);
				if (index == null) {
					Color bubbleSetColor = bubbleSetColors.get(colorIndex);
					colorIndex++;
					bubblesetCanvas.addGroup(bubbleSetColor.getAWTColor(), 2, true);
					index = bubblesetCanvas.getGroupCount() - 1;
					pathwayBubbleSetIndexMap.put(pathway, index);
				}
				bubblesetCanvas.addItem(index, centerX, centerY, nodeBounds.width(), nodeBounds.height());
			}

		}

		return pathwayBubbleSetIndexMap;

	}

	/**
	 * convenience method for adding new nodes to the parent container and the node set
	 * 
	 * @param nodeToAdd
	 *            which should be added
	 * @param isNodeMerged
	 *            different transition animations for merged & un-merged nodes
	 */
	private void addNodeToContainers(NodeElement nodeToAdd, Boolean isNodeMerged) {

		nodeSet.add(nodeToAdd);

		InOutTransitions.IInTransition inTransition;
		if (isNodeMerged)
			inTransition = calcInTransitionMergedNodes(nodeToAdd);
		else
			inTransition = calcInTransitionUnmergedNodes(nodeToAdd);

		add(size(), nodeToAdd, DEFAULT_IN_TRANSITION_DURATION, inTransition);
	}

	/**
	 * calculation the InTransition with the original node's position as base (when splitting)
	 * 
	 * @param originalNode
	 *            the original node (before splitting)
	 * @param originalNodeVrep
	 *            if the position of the original node wasn't set yet, the position of the vrep is used instread
	 * @return the In Transition
	 */
	private InOutTransitions.IInTransition calcInTransitionMergedNodes(NodeElement originalNode) {

		PathwayVertexRep originalNodeVrep = originalNode.getVertexRep();
		final Vec4f originNodePosition;
		if (originalNode.getBounds().get(0) < 1 && originalNode.getBounds().get(1) < 1)
			originNodePosition = new Vec4f(originalNodeVrep.getCenterX() - (originalNodeVrep.getWidth() / 2.0f),
					originalNodeVrep.getCenterY() - (originalNodeVrep.getHeight() / 2.0f), originalNodeVrep.getWidth(),
					originalNodeVrep.getHeight());
		else
			originNodePosition = new Vec4f(originalNode.getBounds());

		InOutInitializers.IInOutInitializer ioInit = new InOutInitializers.IInOutInitializer() {

			@Override
			public Vec4f get(Vec4f to_from, float w, float h) {

				return originNodePosition;
			}
		};
		InOutTransitions.IInTransition inTransition = new InOutTransitions.InOutTransitionBase(ioInit, MOVE_LINEAR);

		return inTransition;
	}

	/**
	 * 
	 * calculation the InTransition for un-merged nodes: <br />
	 * Growing in all direction and moving to final position
	 * 
	 * @param unmergedNode
	 *            needed to get the center
	 * @return the in-transition
	 */
	private InOutTransitions.IInTransition calcInTransitionUnmergedNodes(NodeElement unmergedNode) {

		final float widthHalf = (float) (unmergedNode.getWidth()) / 2.0f;
		IInOutStrategy xInStrategy = new IInOutStrategy() {

			@Override
			public float compute(float other, float max) {
				// TODO Auto-generated method stub
				return other + widthHalf;
			}
		};

		final float heightHalf = (float) (unmergedNode.getHeight()) / 2.0f;
		IInOutStrategy yInStrategy = new IInOutStrategy() {

			@Override
			public float compute(float other, float max) {
				// TODO Auto-generated method stub
				return other + heightHalf;
			}
		};
		IInOutInitializer GROW = new InOutInitializerBase(xInStrategy, yInStrategy, ZERO, ZERO);
		IInTransition inTransition = new InOutTransitions.InOutTransitionBase(GROW,
				MoveTransitions.MOVE_AND_GROW_LINEAR);

		return inTransition;
	}

	/**
	 * method checks if pathwayVertexRepToCheck's vertices already exist in the uniqueVertexMap, if so the surrounding
	 * nodes are merged, if not a new node for pathwayVertexRepToCheck is added
	 * 
	 * @param addingPathway
	 *            the graph that should be added without duplicates
	 * @param addingVrep
	 *            check its vertices for duplicates within the uniqueVertexMap
	 * @throws NodeMergingException
	 *             internal error - tool didn't behave as expected
	 */
	private boolean checkAndMergeNodes(PathwayGraph addingPathway, PathwayVertexRep addingVrep, boolean addToSameGraph,
			PathwayGraph combinedPathway) throws NodeMergingException {

		if (displayOnlyVerticesWithEdges && (addingPathway.inDegreeOf(addingVrep) < 1)
				&& (addingPathway.outDegreeOf(addingVrep) < 1))
			return true;

		if (addingVrep.getType() == EPathwayVertexType.group) {

			NodeElement groupNode = GraphMergeUtil.createNewNodeElement(addingVrep, addingVrep.getPathwayVertices(),
					null, this, addingPathway);
			this.vrepToGroupNodeMap.put(addingVrep, groupNode);
			addNodeToContainers(groupNode, false);
			return false;
		}

		List<PathwayVertex> verticesToCheckList = new ArrayList<PathwayVertex>(addingVrep.getPathwayVertices());

		if (verticesToCheckList.size() < 1) {
			return false;
		}

		/**
		 * ------------------------------------------------------------------------------------------------ <br />
		 * STEP 1: Get map with all duplicate nodes (i.e. NodeElement that contain same vertices as in the pathway to
		 * add) ------------------------------------------------------------------------------------------------
		 */
		Map<NodeElement, List<PathwayVertex>> nodesWithSameVerticesMap = getNodeElementsContainingSameVertices(
				uniqueVertexMap, verticesToCheckList);

		/**
		 * ------------------------------------------------------------------------------------------------ <br/>
		 * STEP 2: Get list of all non duplicate vertices of verticesToCheckList by removing all duplicate vertices <br />
		 * ------------------------------------------------------------------------------------------------
		 */
		List<PathwayVertex> alreadyExistingPathwayVertexList = new ArrayList<PathwayVertex>();
		for (NodeElement nodeContainingDuplicateVertices : nodesWithSameVerticesMap.keySet())
			alreadyExistingPathwayVertexList.addAll(nodesWithSameVerticesMap.get(nodeContainingDuplicateVertices));
		List<PathwayVertex> nonDuplicateVertexList = new ArrayList<PathwayVertex>(verticesToCheckList);
		nonDuplicateVertexList.removeAll(alreadyExistingPathwayVertexList);

		if (nonDuplicateVertexList.size() > 0) {
			NodeElement node = GraphMergeUtil.createNewNodeElement(addingVrep, nonDuplicateVertexList, null, this,
					addingPathway);
			addNodeToContainers(node, false);

			for (PathwayVertex vertex : nonDuplicateVertexList) {
				uniqueVertexMap.put(vertex, node);
			}
		}

		/**
		 * -------------------------------------------------------------------- ---------------------------- <br />
		 * STEP 3: Merge nodes that contain same vertices <br />
		 * -------------------------------------------------------------- ----------------------------------
		 */
		for (Iterator<NodeElement> nodeWithDuplicateVerticesIter = nodesWithSameVerticesMap.keySet().iterator(); nodeWithDuplicateVerticesIter
				.hasNext();) {
			NodeElement mergeWithNode = nodeWithDuplicateVerticesIter.next();

			List<PathwayVertex> sameVerticesList = nodesWithSameVerticesMap.get(mergeWithNode);
			PathwayVertexRep nodeVrep = mergeWithNode.getVertexRep();

			if (sameVerticesList.size() < 1) {
				throw new NodeMergingException("Node(" + mergeWithNode.getLabel()
						+ ") was added to nodesWithSameVerticesMap, but didn't contain same vertices");
			}

			boolean mergeWithinSameGraph = (addingVrep.getPathway().equals(nodeVrep.getPathway())) ? true : false;

			/**
			 * ---------------------------------------------------------------- -------------------------------- <br />
			 * STEP 3.1a: If this node is not a merged one, it will be deleted & replaced by a merged node <br />
			 * -------- ---------------------------------------------------------- ------------------------------
			 */
			if (!mergeWithNode.isMerged()) {
				
//				addNotMerged(addingVrep, addingPathway, mergeWithNode, combinedPathway, sameVerticesList, addToSameGraph, mergeWithinSameGraph);

				/**
				 * STEP 3.1a.1: Create a merged node element & remove the duplicate vertices from the existing node (or
				 * the delete the existing node) <br />
				 * ------------------------------------------------ ---------------------------------------------
				 */
				PathwayVertexRep mergedVrep = new PathwayVertexRep(sameVerticesList.get(0).getHumanReadableName(),
						nodeVrep.getShapeType().name(), nodeVrep.getCenterX(), nodeVrep.getCenterY(),
						nodeVrep.getWidth(), nodeVrep.getHeight());
				/**
				 * get all common vertices between the two nodes
				 */
				for (PathwayVertex mergedVertex : sameVerticesList)
					mergedVrep.addPathwayVertex(mergedVertex);
				if (addToSameGraph || mergeWithinSameGraph)
					mergedVrep.setPathway(addingPathway);
				else
					mergedVrep.setPathway(combinedPathway);

				combinedPathway.addVertex(mergedVrep);
				List<PathwayVertexRep> vreps = new LinkedList<PathwayVertexRep>();
				vreps.add(addingVrep);
				// TODO: add all vertices!!
				vreps.add(mergeWithNode.getVertexRep());

				List<PathwayVertexRep> vrepList = mergeWithNode.getVreps();
				if (vrepList.size() > 0) {
					for (PathwayVertexRep vrep : vrepList) {
						if (!vreps.contains(vrep))
							vreps.add(vrep);
					}
				}
				NodeElement mergedNode;
				if (mergeWithinSameGraph) {
					mergedNode = GraphMergeUtil.createNewNodeElement(mergedVrep, sameVerticesList, vreps, this,
							addingPathway);
				} else {

					Set<PathwayGraph> pathways = new HashSet<PathwayGraph>(mergeWithNode.getPathways());
					pathways.add(addingPathway);

					mergedNode = GraphMergeUtil.createNewNodeElement(mergedVrep, sameVerticesList, vreps, this,
							pathways);
				}

				if (!addToSameGraph && mergeWithinSameGraph)
					mergedNode.setIsMerged(false);

				mergedNode.setCenter(mergeWithNode.getCenterX(), mergeWithNode.getCenterY());

				addNodeToContainers(mergedNode, !mergeWithinSameGraph);

				/**
				 * STEP 3.1a.2a: if the duplicate vertices are all of the (not merged) node's vertices, it needs to be
				 * deleted, because the new merged node took its purpose <br />
				 * ------------------------------ -------------------------------- -------------------------------
				 */
				if (mergeWithNode.getVertices().size() == sameVerticesList.size()) {

					boolean containedNode = nodeSet.remove(mergeWithNode);
					if (containedNode) {
						remove(mergeWithNode);
					}

					List<Pair<EdgeElement, Boolean>> edgesContainingThisNode = GraphMergeUtil
							.getEdgeWithThisNodeAsSourceOrTarget(edgeSet, mergeWithNode);
					for (Pair<EdgeElement, Boolean> edgePair : edgesContainingThisNode) {
						// node was edge source
						if (edgePair.getSecond())
							edgePair.getFirst().setSourceNode(mergedNode);
						else
							edgePair.getFirst().setTargetNode(mergedNode);
					}

					if (containedNode == false)
						throw new NodeMergingException("nodeSet didn't contain node(" + mergeWithNode
								+ ") to remove");

				}

				/**
				 * STEP 3.1a.2b: remove all duplicate vertices of the existing node, since they are already in the new
				 * merged node <br />
				 * ---------- ---------------------------------------------------- -------------------------------
				 */
				else {

					/**
					 * STEP 3.1a.2b: get all edges of the unmerged already existing node & add its edges also to the
					 * merged node <br />
					 * e.g. <br/>
					 * already in nodeSet: |a,b|--->|c| <br />
					 * to add: |a,d|--->|e| <br />
					 * result of previous steps: |d|--->|e|, |a[Merged]| <br />
					 * result of this step: |b|--->|c|<---|a[Merged]| <br />
					 * result of next steps: |d|--->|e|<---|a[Merged]|--->|c|--->|b| <br />
					 * ------------------------------------------------ ---------------------------------------------
					 */
					List<Pair<EdgeElement, Boolean>> edgesContainingThisNode = GraphMergeUtil
							.getEdgeWithThisNodeAsSourceOrTarget(edgeSet, mergeWithNode);
					for (Pair<EdgeElement, Boolean> edgePair : edgesContainingThisNode) {
						EdgeElement edgeOfUnmergedNode = edgePair.getFirst();
						NodeElement sourceNode;
						NodeElement targetNode;
						if (edgePair.getSecond()) {
							sourceNode = mergedNode;
							targetNode = edgeOfUnmergedNode.getTargetNode();
						} else {
							sourceNode = edgeOfUnmergedNode.getSourceNode();
							targetNode = mergedNode;
						}
						EdgeElement mergedEdge = new EdgeElement(edgeOfUnmergedNode.getDefaultEdge(), sourceNode,
								targetNode, DEFAULT_ADD_PATHWAY_DURATION);
						mergedEdge.setLayoutData(false);
						this.edgeSet.add(mergedEdge);
						add(mergedEdge);
					}

					boolean containedNodes = mergeWithNode.removeMultipleVertices(sameVerticesList);

					if (containedNodes == false)
						throw new NodeMergingException("nodeWithDuplicateVertices(" + mergeWithNode
								+ ") didn't contain at leat one of sameVerticesList");
				}

				/**
				 * STEP 3.1a.3: add new vertices, node elements, replace old node (if it existed) with new merged<br />
				 * -------------------------- ------------------------------------ -------------------------------
				 */
				for (PathwayVertex sameVertex : sameVerticesList) {
					uniqueVertexMap.put(sameVertex, mergedNode);
				}

			}

			/**
			 * ---------------------------------------------------------------- -------------------------------- <br />
			 * STEP 3.1b: If the node to check with is already a merged node <br />
			 * ------------------------------------ ------------------------------------------------------------
			 */
			else {
				List<PathwayVertex> nodeWithDuplicateVerticesList = mergeWithNode.getVertices();
				/**
				 * STEP 3.1b.1a: If the node to check with contains also vertices than are not common: <br />
				 * 1. Make new node for all these non duplicate vertices & remove them from the existing node <br />
				 * 2. link vreps of nodeWithDuplicateVertices.vreps to it & 3. link current vrep to
				 * nodeWithDuplicateVertices <br />
				 * ---------------- ---------------------------------------------- -------------------------------
				 */
				if (nodeWithDuplicateVerticesList.size() != sameVerticesList.size()) {

					List<PathwayVertex> nonDuplicateVerticesOfExistingNode = new LinkedList<PathwayVertex>();

					/**
					 * get all vertices that are not in sameVerticesList & remove them from the merged node
					 */
					for (Iterator<PathwayVertex> nodeVerticesIter = nodeWithDuplicateVerticesList.iterator(); nodeVerticesIter
							.hasNext();) {
						PathwayVertex nodeVertex = nodeVerticesIter.next();

						if (!sameVerticesList.contains(nodeVertex)) {
							nonDuplicateVerticesOfExistingNode.add(nodeVertex);
							nodeWithDuplicateVerticesList.remove(nodeVertex);
							mergeWithNode.setDisplayedVertex(nodeWithDuplicateVerticesList.get(0));
						}
					}

					if (nonDuplicateVerticesOfExistingNode.size() < 1) {
						throw new NodeMergingException("splitOfMergedVertexList didn't contain elements");
					}

					/**
					 * get pathways before the new pathway is added -> needed for split off node <br />
					 * add the new pathway to the merged node
					 */
					Set<PathwayGraph> pathwaysFromMergedNode = new HashSet<PathwayGraph>(
							mergeWithNode.getPathways());
					// nodeWithDuplicateVertices.addPathway(pathwayToAdd);

					PathwayVertexRep vrepOfNonDuplicateVertices = new PathwayVertexRep(
							nonDuplicateVerticesOfExistingNode.get(0).getHumanReadableName(), nodeVrep.getShapeType()
									.name(), nodeVrep.getCenterX(), nodeVrep.getCenterY(), nodeVrep.getWidth(),
							nodeVrep.getHeight());

					vrepOfNonDuplicateVertices.setPathway(nodeVrep.getPathway());

					List<PathwayVertexRep> vrepsOfNode = mergeWithNode.getVreps();
					if (vrepsOfNode == null) {
						throw new NodeMergingException("nodeWithDuplicateVertices didn't contain Vreps");
					}
					vrepsOfNode.add(nodeVrep);

					NodeElement newNodeForNonDuplicateVertices = GraphMergeUtil.createNewNodeElement(
							vrepOfNonDuplicateVertices, nonDuplicateVerticesOfExistingNode, vrepsOfNode, this,
							pathwaysFromMergedNode);

					for (PathwayVertex mergedVertex : nonDuplicateVerticesOfExistingNode) {
						vrepOfNonDuplicateVertices.addPathwayVertex(mergedVertex);
						uniqueVertexMap.put(mergedVertex, newNodeForNonDuplicateVertices);
					}

					newNodeForNonDuplicateVertices.setCenter(mergeWithNode.getCenterX(),
							mergeWithNode.getCenterY());

					addNodeToContainers(newNodeForNonDuplicateVertices, !mergeWithinSameGraph);

				}
				// else {

				// TODO: check
				mergeWithNode.addPathway(addingPathway);
				/**
				 * new vrep is added to the merged node
				 */
				mergeWithNode.addVrepWithThisNodesVerticesList(addingVrep);
				if (!addToSameGraph)
					mergeWithNode.getVertexRep().setPathway(combinedPathway);
				// }

			}

		}

		return false;
	}
	
	
	private void addNotMerged(PathwayVertexRep addingVrep, PathwayGraph pathwayToAdd, NodeElement mergingWithNode, PathwayGraph combinedPathway, List<PathwayVertex> sameVerticesList, boolean addToSameGraph, boolean mergeWithinSameGraph) {
		PathwayVertexRep mergingWithVrep = mergingWithNode.getVertexRep();
		
		/**
		 * 1.Create new merged vrep & node element with common vertices
		 */
		NodeElement mergedNode = GraphMergeUtil.createNewMergedNodeElement(sameVerticesList, pathwayToAdd, addingVrep, mergingWithNode, mergeWithinSameGraph, addToSameGraph, this);
		PathwayVertexRep mergedVrep = mergedNode.getVertexRep();

		combinedPathway.addVertex(mergedVrep);

		addNodeToContainers(mergedNode, !mergeWithinSameGraph);

		/**
		 * STEP 3.1a.2a: if the duplicate vertices are all of the (not merged) node's vertices, it needs to be
		 * deleted, because the new merged node took its purpose <br />
		 * ------------------------------ -------------------------------- -------------------------------
		 */
		if (mergingWithNode.getVertices().size() == sameVerticesList.size()) {

			boolean containedNode = nodeSet.remove(mergingWithNode);
			if (containedNode) {
				remove(mergingWithNode);
			}

			/**
			 * redirect all edges from the to be deleted node to the new merged node
			 */
			GraphMergeUtil.redirectEdges(mergingWithNode, mergedNode, edgeSet);
			
		}

		/**
		 * STEP 3.1a.2b: remove all duplicate vertices of the existing node, since they are already in the new
		 * merged node <br />
		 * ---------- ---------------------------------------------------- -------------------------------
		 */
		else {

			/**
			 * STEP 3.1a.2b: get all edges of the unmerged already existing node & add its edges also to the
			 * merged node <br />
			 * e.g. <br/>
			 * already in nodeSet: |a,b|--->|c| <br />
			 * to add: |a,d|--->|e| <br />
			 * result of previous steps: |d|--->|e|, |a[Merged]| <br />
			 * result of this step: |b|--->|c|<---|a[Merged]| <br />
			 * result of next steps: |d|--->|e|<---|a[Merged]|--->|c|--->|b| <br />
			 * ------------------------------------------------ ---------------------------------------------
			 */
			GraphMergeUtil.copyEdges(mergingWithNode, mergedNode, edgeSet, this);

			mergingWithNode.removeMultipleVertices(sameVerticesList);

		}

		/**
		 * STEP 3.1a.3: add new vertices, node elements, replace old node (if it existed) with new merged<br />
		 * -------------------------- ------------------------------------ -------------------------------
		 */
		for (PathwayVertex sameVertex : sameVerticesList) {
			uniqueVertexMap.put(sameVertex, mergedNode);
		}

	}



	private final Color getColorOfPathway(PathwayGraph pathway) {
		Integer colorIndex = contextPathwayColorIndex.get(pathway);

		Color color = null;
		if (colorIndex != null)
			color = bubbleSetColors.get(colorIndex.intValue());

		return color;
	}

	/**
	 * Gets the node, which holds the old focus vertex and makes it into the new node
	 * 
	 */
	private void findAndCreateNewFocusNodeBasedOnOldFocusVertex() {

		NodeElement newFocusNode = uniqueVertexMap.get(focusVertex);

		if (newFocusNode != null) {
			setFocusNode(newFocusNode);
			view.filterPathwayList(newFocusNode.getVertices());

		}

		for (PathwayVertex previousFocusVertex : previousFocusVertices) {
			NodeElement wasPreviouslyFocusNode = uniqueVertexMap.get(previousFocusVertex);
			if (wasPreviouslyFocusNode != null && !wasPreviouslyFocusNode.equals(newFocusNode))
				wasPreviouslyFocusNode.setWasPreviouslyFocusNode(true);
		}
	}

	/**
	 * Set color of new pathway
	 * 
	 * @param pathwayToAdd
	 */
	private void setColorOfPathway(PathwayGraph pathwayToAdd) {

		Integer colorIndex;
		// if(usedColorIndezes.size() > 0)
		// colorIndex = usedColorIndezes.remove(0);
		// else
		colorIndex = nextColorIndex++;

		contextPathwayColorIndex.put(pathwayToAdd, colorIndex);
	}

	private final void setUpBubbleSet() {
		SetOutline setOutline = new BubbleSet(100, 20, 3, 10.0, 7.0, 0.5, 2.5, 15.0, 8);
		((BubbleSet) setOutline).useVirtualEdges(true);
		AbstractShapeGenerator shaper = new BSplineShapeGenerator(setOutline);
		this.bubblesetCanvas = new CanvasComponent(shaper);
		this.bubblesetCanvas.setDefaultView();
	}
	
	
	/**
	 * TOOD: remove
	 * @param contextPathway
	 */
	public void printCommonNodes(PathwayGraph contextPathway) {
		System.out.println("=================" + "\nContext PW:\n" + "--------------");
		for(PathwayVertexRep vrep : contextPathway.vertexSet()) {
			for(PathwayVertex vertex : vrep.getPathwayVertices()) {
				NodeElement commonNode = uniqueVertexMap.get(vertex);
				if(commonNode == null)
					continue;
				
				System.out.println("Network Node: " + commonNode + " ContextPW Vrep: " + vrep);
			}
		}
	}

}
