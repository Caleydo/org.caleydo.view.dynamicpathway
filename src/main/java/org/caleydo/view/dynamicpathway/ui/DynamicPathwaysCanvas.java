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
import org.caleydo.view.dynamicpathway.ui.ANodeElement.ENodeState;
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
	 * contains focus & context pathway informations
	 */
	private DynamicPathwayGraph pathway;

	/**
	 * contains nodes & edges used for defining and rendering the layout
	 */
	private Set<ANodeElement> nodeSet;
	private Set<EdgeElement> edgeSet;

	/**
	 * the bubble set
	 */
	private CanvasComponent bubblesetCanvas;

	/**
	 * A list of colors which are being used to color the bubble sets
	 */
	private List<Color> bubbleSetColors = ColorBrewer.Set1.getColors(100);
	private final Set<PathwayGraph> bubbleSetPathways;

	/**
	 * the currently selected node
	 */
	private ANodeElement currentSelectedNode;

	/**
	 * is null if no node was selected, otherwise it is a reference to the currently selected node -> needed for merging
	 */
	private ANodeElement focusNode;
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
	private Map<PathwayVertex, ANodeElement> uniqueVertexMap;
	private Map<PathwayVertexRep, ANodeElement> vrepToGroupNodeMap;

	/**
	 * mapping for the pathway graphs that contain only of a part of vertices & edges to the full pathway graphs <br />
	 * (only needed when context pathways are added partly)
	 */
	private Map<PathwayGraph, PathwayGraph> originalPathwaysOfSubpathwaysMap;

	private List<PathwayVertex> previousFocusVertices;
	private Map<PathwayGraph, Integer> contextPathwayColorIndex;
	private Integer nextColorIndex;

	private TextureRenderer textureRenderer = null;

	public DynamicPathwaysCanvas(GLFruchtermanReingoldLayout layout, DynamicPathwayView view) {

		this.pathway = new DynamicPathwayGraph();

		this.nodeSet = new HashSet<ANodeElement>();
		this.edgeSet = new HashSet<EdgeElement>();

		this.view = view;

		this.vertexSelectionManager = new EventBasedSelectionManager(this, IDType.getIDType(EGeneIDTypes.PATHWAY_VERTEX
				.name()));
		this.vertexSelectionManager.registerEventListeners();

		this.uniqueVertexMap = new HashMap<PathwayVertex, ANodeElement>();
		this.vrepToGroupNodeMap = new HashMap<PathwayVertexRep, ANodeElement>();

		this.originalPathwaysOfSubpathwaysMap = new HashMap<PathwayGraph, PathwayGraph>();

		this.previousFocusVertices = new ArrayList<PathwayVertex>();
		this.contextPathwayColorIndex = new HashMap<PathwayGraph, Integer>();
		this.bubbleSetPathways = new HashSet<PathwayGraph>();
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
	 * @param clearOriginalPathwaysMap
	 *            ONLY for focus pathway: true if nothing should be kept (false for switching roles)
	 * @param keepOldFocusNode
	 *            ONLY for focus pathway: true if the focus node should be recovered after the pathway was added
	 */
	public void addPathwayToCanvas(PathwayGraph pathwayToAdd, boolean isFocusPathway, boolean clearOriginalPathwaysMap,
			boolean keepOldFocusNode) {

		pathway.addFocusOrContextPathway(pathwayToAdd, !isFocusPathway);

		Color nodeColor;

		if (isFocusPathway)
			nodeColor = DynamicPathwayView.FOCUS_PATHWAY_COLOR;
		else {
			setColorOfPathway(pathwayToAdd);
			nodeColor = getColorOfPathway(pathwayToAdd);
		}
		view.addPathwayToControlBar(pathwayToAdd, isFocusPathway, nodeColor);

		if (focusNode != null && (keepOldFocusNode || isFocusPathway)) {
			focusVertex = focusNode.getDisplayedVertex();
			previousFocusVertices = new ArrayList<PathwayVertex>(focusNode.getVertices());
			previousFocusVertices.remove(focusVertex);
		} else
			System.out.println("Filtering node was null");

		// if you want to add a new focus graph
		if (isFocusPathway) {

			// clears all from past selection
			clearCanvasAndInfo(clearOriginalPathwaysMap, keepOldFocusNode);

			setDefaultDuration(DEFAULT_ADD_PATHWAY_DURATION);

			/**
			 * if duplicate are allowed or not
			 */
			if (removeDuplicateVertices == false) {
				focusGraphWithDuplicateVertices = true;
				addPathwayWithDuplicates(pathwayToAdd);
			} else {
				focusGraphWithDuplicateVertices = false;
				addPathwayWithoutDuplicates(pathwayToAdd, true);

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

			boolean noNodesAdded = addPathwayWithoutDuplicates(pathwayToAdd, false);

			if (focusVertex != null)
				findAndCreateNewFocusNodeBasedOnOldFocusVertex();

			if (!noNodesAdded)
				resetEdges();

			bubbleSetPathways.add(pathwayToAdd);
		}

		for (ANodeElement node : nodeSet)
			node.setIsMerged(false);

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

		bubbleSetPathways.clear();
		contextPathwayColorIndex.clear();
		nextColorIndex = 0;

		if (clearOriginalPathwaysMap) {
			originalPathwaysOfSubpathwaysMap.clear();
		}

		// clear the canvas
		clear();

		view.unfilterPathwayList();
	}

	public void displayBubbleSetOfPathway(PathwayGraph pathway) {
		bubbleSetPathways.add(pathway);
	}

	/**
	 * Change the visibility of the bubble set of the given pathway
	 * 
	 * @param pathwayToChange
	 *            it's bubble sets is going to enabled/disabled
	 * @param newVisibilityValue
	 *            true if it should be displayed, false otherwise
	 */
	public final void enableOrDisableBubbleSetOfPathway(PathwayGraph pathwayToChange, boolean newVisibilityValue) {
		if (pathwayToChange == null)
			System.exit(-1);

		// if the bubble set should be displayed, but is currently not
		if (newVisibilityValue && !bubbleSetPathways.contains(pathwayToChange)) {
			bubbleSetPathways.add(pathwayToChange);
			repaint();
		}
		// if the bubble set should be not displayed, but is currently
		else if (!newVisibilityValue && bubbleSetPathways.contains(pathwayToChange)) {
			bubbleSetPathways.remove(pathwayToChange);
			repaint();
		}

		// else do nothing
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

	public ANodeElement getFocusNode() {
		return focusNode;
	}

	public PathwayGraph getFocusPathway() {
		return pathway.getFocusPathway();
	}

	@Override
	public Vec2f getMinSize() {
		return new Vec2f(100, 100);
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
	public Map<ANodeElement, List<PathwayVertex>> getNodeElementsContainingSameVertices(
			Map<PathwayVertex, ANodeElement> vertexNodeMap, List<PathwayVertex> verticesToCheckList) {

		Map<ANodeElement, List<PathwayVertex>> equivalentVerticesMap = new HashMap<ANodeElement, List<PathwayVertex>>();

		for (PathwayVertex vertexToCheck : verticesToCheckList) {
			if (vertexNodeMap.containsKey(vertexToCheck)) {
				ANodeElement existingNodeElement = vertexNodeMap.get(vertexToCheck);
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

	@Override
	public Set<IFRLayoutNode> getNodeSet() {
		Set<IFRLayoutNode> interfaceSet = new HashSet<IFRLayoutNode>();

		for (ANodeElement node : nodeSet) {
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

	public boolean isBubbleSetDisplayed(PathwayGraph pathway) {
		return bubbleSetPathways.contains(pathway);
	}

	public boolean isDisplayOnlyVerticesWithEdges() {
		return displayOnlyVerticesWithEdges;
	}

	public boolean isPathwayPresent(PathwayGraph pathway) {
		if (this.pathway.isFocusGraph(pathway) || this.pathway.isContextGraph(pathway)
				|| this.originalPathwaysOfSubpathwaysMap.containsValue(pathway)) {

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
	public void onSelect(List<PathwayVertex> vertices, ANodeElement node, Pick pick) {
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
	 * remove the current focus node
	 */
	public void removeFocusNode() {
		if (focusNode != null) {
			focusNode.setNodeState(ENodeState.DEFAULT);
			focusNode = null;
		}
	}

	/**
	 * removes a partly added pathway from the map (i.e. if a (partly added) context pathway was removed
	 * 
	 * @param subPathway
	 * @return the full pathway
	 */
	public PathwayGraph removeOriginalPathwayAndSubpathwayOfMap(PathwayGraph subPathway) {
		PathwayGraph fullPw = this.originalPathwaysOfSubpathwaysMap.remove(subPathway);
		return fullPw;
	}

	/**
	 * Removed from the map, so that the color can be reused
	 * 
	 * @param pathway
	 *            the pathway to be removed
	 * @return index of the pathway in the pathway colors map
	 */
	public Integer removePathwayFromContextPathwayColorIndexMapAndBubbleSetList(PathwayGraph pathway) {
		Integer index = contextPathwayColorIndex.get(pathway);

		if (index == null)
			return -1;

		bubbleSetPathways.remove(pathway);

		return contextPathwayColorIndex.remove(pathway);
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
	 * if a node (wrapper for PathwayVertexRep) is selected via the filtering command, it is highlighted and the pathway
	 * list on the left is filtered by pathways, which contain this element
	 * 
	 * @param newFilteringNode
	 *            the node, which the pathway list should be filtered with
	 */
	public boolean setFocusNode(ANodeElement newFilteringNode) {

		boolean focusNodeChanged = false;

		/**
		 * if nothing was selected, just set the new node
		 */
		if (focusNode == null) {
			focusNode = newFilteringNode;
			focusNode.setNodeState(ENodeState.FOCUS);
			focusNodeChanged = false;
		}

		/**
		 * if another node was selected before, deselect it and selected the new node
		 */
		else if (newFilteringNode != null) {
			focusNode.setNodeState(ENodeState.DEFAULT);

			focusNode = newFilteringNode;
			focusNode.setNodeState(ENodeState.FOCUS);
			focusNodeChanged = true;
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
	public void setOrResetSelectedNode(ANodeElement newSelectedNode) {
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

			Color color;
			if (pathway.equals(this.getFocusPathway()))
				color = DynamicPathwayView.FOCUS_PATHWAY_COLOR;
			else
				color = bubbleSetColors.get(colorIndex);
			Color transparentColor = new Color(color.getRGBA());
			transparentColor.a = 0.2f;

			ITesselatedPolygon polygon = TesselatedPolygons.polygon2(points);
			g.incZ(-0.3f); // move to 0
			g.gl.glPushAttrib(GL2.GL_LINE_BIT);
			g.color(transparentColor).fillPolygon(polygon).color(color).lineWidth(4).drawPath(polygon);
			g.gl.glPopAttrib();
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
	 * Merge new node with a node that was already merged
	 * 
	 * @param nodeToMergeWith
	 *            merge new node with this node
	 * @param commonVerticesList
	 *            the common vertices between the new node and mergeWithNode
	 * @param addingVrep
	 *            vrep of the new node
	 * @param addingPathway
	 *            pathway which should be added
	 * @param mergeWithinSamePathway
	 *            true if the merging nodes are part of the same pathway
	 * @param addToSamePathway
	 *            whether the new node should be added to the new pathway or the combined pathway
	 * @throws NodeMergingException
	 */
	private void addMerged(ANodeElement nodeToMergeWith, List<PathwayVertex> commonVerticesList,
			PathwayVertexRep addingVrep, PathwayGraph addingPathway, boolean mergeWithinSamePathway,
			boolean addToSamePathway) throws NodeMergingException {

		List<PathwayVertex> nodeWithDuplicateVerticesList = nodeToMergeWith.getVertices();
		PathwayGraph combinedPathway = pathway.getCombinedGraph();
		PathwayVertexRep nodeVrep = nodeToMergeWith.getVertexRep();

		/**
		 * STEP 3.1b.1a: If the node to check with contains also vertices than are not common: <br />
		 * 1. Make new node for all these non duplicate vertices & remove them from the existing node <br />
		 * 2. link vreps of nodeWithDuplicateVertices.vreps to it & 3. link current vrep to nodeWithDuplicateVertices <br />
		 * ---------------- ---------------------------------------------- -------------------------------
		 */
		if (nodeWithDuplicateVerticesList.size() != commonVerticesList.size()) {

			List<PathwayVertex> nonDuplicateVerticesOfExistingNode = new LinkedList<PathwayVertex>();

			/**
			 * get all vertices that are not in sameVerticesList & remove them from the merged node
			 */
			for (Iterator<PathwayVertex> nodeVerticesIter = nodeWithDuplicateVerticesList.iterator(); nodeVerticesIter
					.hasNext();) {
				PathwayVertex nodeVertex = nodeVerticesIter.next();

				if (!commonVerticesList.contains(nodeVertex)) {
					nonDuplicateVerticesOfExistingNode.add(nodeVertex);
					nodeWithDuplicateVerticesList.remove(nodeVertex);
					nodeToMergeWith.setDisplayedVertex(nodeWithDuplicateVerticesList.get(0));
				}
			}

			if (nonDuplicateVerticesOfExistingNode.size() < 1) {
				throw new NodeMergingException("splitOfMergedVertexList didn't contain elements");
			}

			/**
			 * get pathways before the new pathway is added -> needed for split off node <br />
			 * add the new pathway to the merged node
			 */
			Set<PathwayGraph> pathwaysFromMergedNode = new HashSet<PathwayGraph>(nodeToMergeWith.getPathways());

			PathwayVertexRep vrepOfNonDuplicateVertices = new PathwayVertexRep(nonDuplicateVerticesOfExistingNode
					.get(0).getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
					nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());

			vrepOfNonDuplicateVertices.setPathway(nodeVrep.getPathway());

			List<PathwayVertexRep> vrepsOfNode = nodeToMergeWith.getVreps();
			if (vrepsOfNode == null) {
				throw new NodeMergingException("nodeWithDuplicateVertices didn't contain Vreps");
			}
			vrepsOfNode.add(nodeVrep);

			ANodeElement newNodeForNonDuplicateVertices = GraphMergeUtil.createNewNodeElement(
					vrepOfNonDuplicateVertices, nonDuplicateVerticesOfExistingNode, vrepsOfNode, this,
					pathwaysFromMergedNode);

			for (PathwayVertex mergedVertex : nonDuplicateVerticesOfExistingNode) {
				vrepOfNonDuplicateVertices.addPathwayVertex(mergedVertex);
				uniqueVertexMap.put(mergedVertex, newNodeForNonDuplicateVertices);
			}

			newNodeForNonDuplicateVertices.setCenter(nodeToMergeWith.getCenterX(), nodeToMergeWith.getCenterY());

			addNodeToContainers(newNodeForNonDuplicateVertices, !mergeWithinSamePathway);

		}

		nodeToMergeWith.addPathway(addingPathway);
		/**
		 * new vrep is added to the merged node
		 */
		nodeToMergeWith.addVrepWithThisNodesVerticesList(addingVrep);
		if (!addToSamePathway)
			nodeToMergeWith.getVertexRep().setPathway(combinedPathway);
	}

	/**
	 * convenience method for adding new nodes to the parent container and the node set
	 * 
	 * @param nodeToAdd
	 *            which should be added
	 * @param isNodeMerged
	 *            different transition animations for merged & un-merged nodes
	 */
	private void addNodeToContainers(ANodeElement nodeToAdd, Boolean isNodeMerged) {

		nodeSet.add(nodeToAdd);

		InOutTransitions.IInTransition inTransition;
		if (isNodeMerged)
			inTransition = calcInTransitionMergedNodes(nodeToAdd);
		else
			inTransition = calcInTransitionUnmergedNodes(nodeToAdd);

		add(size(), nodeToAdd, DEFAULT_IN_TRANSITION_DURATION, inTransition);
	}

	/**
	 * Merge new node with a node that was not merged yet
	 * 
	 * 
	 * @param nodeToMergeWith
	 *            merge new node with this node
	 * @param commonVerticesList
	 *            the common vertices between the new node and mergeWithNode
	 * @param addingVrep
	 *            vrep of the new node
	 * @param addingPathway
	 *            pathway which should be added
	 * @param addToSamePathway
	 *            whether the new node should be added to the new pathway or the combined pathway
	 * @param mergeWithinSamePathway
	 *            true if the merging nodes are part of the same pathway
	 * @throws NodeMergingException
	 */
	private void addNotMerged(ANodeElement nodeToMergeWith, List<PathwayVertex> sameVerticesList,
			PathwayVertexRep addingVrep, PathwayGraph addingPathway, boolean addToSamePathway,
			boolean mergeWithinSamePathway) throws NodeMergingException {

		PathwayVertexRep nodeVrep = nodeToMergeWith.getVertexRep();
		PathwayGraph combinedPathway = pathway.getCombinedGraph();

		/**
		 * STEP 3.1a.1: Create a merged node element & remove the duplicate vertices from the existing node (or the
		 * delete the existing node) <br />
		 * ------------------------------------------------ ---------------------------------------------
		 */
		PathwayVertexRep mergedVrep = new PathwayVertexRep(sameVerticesList.get(0).getHumanReadableName(), nodeVrep
				.getShapeType().name(), nodeVrep.getCenterX(), nodeVrep.getCenterY(), nodeVrep.getWidth(),
				nodeVrep.getHeight());
		/**
		 * get all common vertices between the two nodes
		 */
		for (PathwayVertex mergedVertex : sameVerticesList)
			mergedVrep.addPathwayVertex(mergedVertex);
		if (addToSamePathway || mergeWithinSamePathway)
			mergedVrep.setPathway(addingPathway);
		else
			mergedVrep.setPathway(combinedPathway);

		combinedPathway.addVertex(mergedVrep);
		List<PathwayVertexRep> vreps = new LinkedList<PathwayVertexRep>();
		vreps.add(addingVrep);
		vreps.add(nodeToMergeWith.getVertexRep());

		List<PathwayVertexRep> vrepList = nodeToMergeWith.getVreps();
		if (vrepList.size() > 0) {
			for (PathwayVertexRep vrep : vrepList) {
				if (!vreps.contains(vrep))
					vreps.add(vrep);
			}
		}
		ANodeElement mergedNode;
		if (mergeWithinSamePathway) {
			mergedNode = GraphMergeUtil.createNewNodeElement(mergedVrep, sameVerticesList, vreps, this, addingPathway);
		} else {

			Set<PathwayGraph> pathways = new HashSet<PathwayGraph>(nodeToMergeWith.getPathways());
			pathways.add(addingPathway);

			mergedNode = GraphMergeUtil.createNewNodeElement(mergedVrep, sameVerticesList, vreps, this, pathways);
		}

		if (!addToSamePathway && mergeWithinSamePathway)
			mergedNode.setIsMerged(false);

		mergedNode.setCenter(nodeToMergeWith.getCenterX(), nodeToMergeWith.getCenterY());

		addNodeToContainers(mergedNode, !mergeWithinSamePathway);

		/**
		 * STEP 3.1a.2a: if the duplicate vertices are all of the (not merged) node's vertices, it needs to be deleted,
		 * because the new merged node took its purpose <br />
		 * ------------------------------ -------------------------------- -------------------------------
		 */
		if (nodeToMergeWith.getVertices().size() == sameVerticesList.size()) {

			boolean containedNode = nodeSet.remove(nodeToMergeWith);
			if (containedNode) {
				remove(nodeToMergeWith);
			}

			List<Pair<EdgeElement, Boolean>> edgesContainingThisNode = GraphMergeUtil
					.getEdgeWithThisNodeAsSourceOrTarget(edgeSet, nodeToMergeWith);
			for (Pair<EdgeElement, Boolean> edgePair : edgesContainingThisNode) {
				// node was edge source
				if (edgePair.getSecond())
					edgePair.getFirst().setSourceNode(mergedNode);
				else
					edgePair.getFirst().setTargetNode(mergedNode);
			}

			if (containedNode == false)
				throw new NodeMergingException("nodeSet didn't contain node(" + nodeToMergeWith + ") to remove");

		}

		/**
		 * STEP 3.1a.2b: remove all duplicate vertices of the existing node, since they are already in the new merged
		 * node <br />
		 * ---------- ---------------------------------------------------- -------------------------------
		 */
		else {

			/**
			 * STEP 3.1a.2b: get all edges of the unmerged already existing node & add its edges also to the merged node <br />
			 * e.g. <br/>
			 * already in nodeSet: |a,b|--->|c| <br />
			 * to add: |a,d|--->|e| <br />
			 * result of previous steps: |d|--->|e|, |a[Merged]| <br />
			 * result of this step: |b|--->|c|<---|a[Merged]| <br />
			 * result of next steps: |d|--->|e|<---|a[Merged]|--->|c|--->|b| <br />
			 * ------------------------------------------------ ---------------------------------------------
			 */
			List<Pair<EdgeElement, Boolean>> edgesContainingThisNode = GraphMergeUtil
					.getEdgeWithThisNodeAsSourceOrTarget(edgeSet, nodeToMergeWith);
			for (Pair<EdgeElement, Boolean> edgePair : edgesContainingThisNode) {
				EdgeElement edgeOfUnmergedNode = edgePair.getFirst();
				ANodeElement sourceNode;
				ANodeElement targetNode;
				if (edgePair.getSecond()) {
					sourceNode = mergedNode;
					targetNode = edgeOfUnmergedNode.getTargetNode();
				} else {
					sourceNode = edgeOfUnmergedNode.getSourceNode();
					targetNode = mergedNode;
				}
				EdgeElement mergedEdge = new EdgeElement(edgeOfUnmergedNode.getDefaultEdge(), sourceNode, targetNode,
						DEFAULT_ADD_PATHWAY_DURATION);
				mergedEdge.setLayoutData(false);
				this.edgeSet.add(mergedEdge);
				add(mergedEdge);
			}

			boolean containedNodes = nodeToMergeWith.removeMultipleVertices(sameVerticesList);

			if (containedNodes == false)
				throw new NodeMergingException("nodeWithDuplicateVertices(" + nodeToMergeWith
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
	 * represent graph biologically correct, with duplicates <br />
	 * used when only one graph is displayed
	 * 
	 * @param pathwayToAdd
	 */
	private void addPathwayWithDuplicates(PathwayGraph pathwayToAdd) {

		Map<PathwayVertexRep, ANodeElement> vrepToNodeElementMap = new HashMap<PathwayVertexRep, ANodeElement>();

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

			ANodeElement nodeElement = GraphMergeUtil.createNewNodeElement(vrep, vrep.getPathwayVertices(), null, this,
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
			ANodeElement sourceNode = vrepToNodeElementMap.get(pathwayToAdd.getEdgeSource(edge));
			ANodeElement targetNode = vrepToNodeElementMap.get(pathwayToAdd.getEdgeTarget(edge));

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
	private boolean addPathwayWithoutDuplicates(PathwayGraph pathwayToAdd, boolean addMergedNodesToSamePathway) {

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
				noNodeDisplayed &= checkAndMergeNodes(pathwayToAdd, vrep, addMergedNodesToSamePathway);
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
	 * @return the borders of the bubble set
	 */
	private final Map<PathwayGraph, Integer> calcAndSetBubbleSet(GLGraphics g) {
		Map<PathwayGraph, Integer> pathwayBubbleSetIndexMap = new HashMap<PathwayGraph, Integer>();

		bubblesetCanvas.removeAllGroups();

		PathwayGraph focusPathway = this.getFocusPathway();
		int colorIndex = 0;
		for (ANodeElement node : this.nodeSet) {

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

				if (!bubbleSetPathways.contains(pathway))
					continue;

				Integer index = pathwayBubbleSetIndexMap.get(pathway);
				if (index == null) {
					Color bubbleSetColor;
					if (pathway.equals(focusPathway)) {
						bubbleSetColor = DynamicPathwayView.FOCUS_PATHWAY_COLOR;
					} else {
						bubbleSetColor = bubbleSetColors.get(colorIndex);
						colorIndex++;
					}

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
	 * calculation the InTransition with the original node's position as base (when splitting)
	 * 
	 * @param originalNode
	 *            the original node (before splitting)
	 * @param originalNodeVrep
	 *            if the position of the original node wasn't set yet, the position of the vrep is used instread
	 * @return the In Transition
	 */
	private InOutTransitions.IInTransition calcInTransitionMergedNodes(ANodeElement originalNode) {

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
	private InOutTransitions.IInTransition calcInTransitionUnmergedNodes(ANodeElement unmergedNode) {

		final float widthHalf = (float) (unmergedNode.getWidth()) / 2.0f;
		IInOutStrategy xInStrategy = new IInOutStrategy() {

			@Override
			public float compute(float other, float max) {
				return other + widthHalf;
			}
		};

		final float heightHalf = (float) (unmergedNode.getHeight()) / 2.0f;
		IInOutStrategy yInStrategy = new IInOutStrategy() {

			@Override
			public float compute(float other, float max) {
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
	private boolean checkAndMergeNodes(PathwayGraph addingPathway, PathwayVertexRep addingVrep, boolean addToSamePathway)
			throws NodeMergingException {

		if (displayOnlyVerticesWithEdges && (addingPathway.inDegreeOf(addingVrep) < 1)
				&& (addingPathway.outDegreeOf(addingVrep) < 1))
			return true;

		if (addingVrep.getType() == EPathwayVertexType.group) {

			ANodeElement groupNode = GraphMergeUtil.createNewNodeElement(addingVrep, addingVrep.getPathwayVertices(),
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
		Map<ANodeElement, List<PathwayVertex>> nodesWithSameVerticesMap = getNodeElementsContainingSameVertices(
				uniqueVertexMap, verticesToCheckList);

		/**
		 * ------------------------------------------------------------------------------------------------ <br/>
		 * STEP 2: Get list of all non duplicate vertices of verticesToCheckList by removing all duplicate vertices <br />
		 * ------------------------------------------------------------------------------------------------
		 */
		List<PathwayVertex> alreadyExistingPathwayVertexList = new ArrayList<PathwayVertex>();
		for (ANodeElement nodeContainingDuplicateVertices : nodesWithSameVerticesMap.keySet())
			alreadyExistingPathwayVertexList.addAll(nodesWithSameVerticesMap.get(nodeContainingDuplicateVertices));
		List<PathwayVertex> nonDuplicateVertexList = new ArrayList<PathwayVertex>(verticesToCheckList);
		nonDuplicateVertexList.removeAll(alreadyExistingPathwayVertexList);

		if (nonDuplicateVertexList.size() > 0) {
			ANodeElement node = GraphMergeUtil.createNewNodeElement(addingVrep, nonDuplicateVertexList, null, this,
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
		for (Iterator<ANodeElement> nodeWithDuplicateVerticesIter = nodesWithSameVerticesMap.keySet().iterator(); nodeWithDuplicateVerticesIter
				.hasNext();) {
			ANodeElement mergeWithNode = nodeWithDuplicateVerticesIter.next();

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
				addNotMerged(mergeWithNode, sameVerticesList, addingVrep, addingPathway, addToSamePathway,
						mergeWithinSameGraph);
			}

			/**
			 * ---------------------------------------------------------------- -------------------------------- <br />
			 * STEP 3.1b: If the node to check with is already a merged node <br />
			 * ------------------------------------ ------------------------------------------------------------
			 */
			else {

				addMerged(mergeWithNode, sameVerticesList, addingVrep, addingPathway, mergeWithinSameGraph,
						addToSamePathway);

			}

		}

		return false;
	}

	/**
	 * Gets the node, which holds the old focus vertex and makes it into the new node
	 * 
	 */
	private void findAndCreateNewFocusNodeBasedOnOldFocusVertex() {

		ANodeElement newFocusNode = uniqueVertexMap.get(focusVertex);

		if (newFocusNode != null) {
			setFocusNode(newFocusNode);
			view.filterPathwayList(newFocusNode.getVertices());

		}

		for (PathwayVertex previousFocusVertex : previousFocusVertices) {
			ANodeElement wasPreviouslyFocusNode = uniqueVertexMap.get(previousFocusVertex);
			if (wasPreviouslyFocusNode != null && !wasPreviouslyFocusNode.equals(newFocusNode))
				wasPreviouslyFocusNode.setWasPreviouslyFocusNode(true);
		}
	}

	/**
	 * get the color of the pathway
	 * 
	 * @param pathway
	 * @return the pathway's color
	 */
	private final Color getColorOfPathway(PathwayGraph pathway) {
		Integer colorIndex = contextPathwayColorIndex.get(pathway);

		Color color = null;
		if (colorIndex != null)
			color = bubbleSetColors.get(colorIndex.intValue());

		return color;
	}

	private void resetEdges() {
		for (EdgeElement edge : this.edgeSet) {
			edge.setVisibility(EVisibility.HIDDEN);
			edge.setTimerDelay(DEFAULT_ADD_PATHWAY_DURATION);
		}
	}

	/**
	 * Set color of new pathway
	 * 
	 * @param pathwayToAdd
	 */
	private void setColorOfPathway(PathwayGraph pathwayToAdd) {

		if (contextPathwayColorIndex.get(pathwayToAdd) != null)
			return;

		Integer colorIndex = nextColorIndex++;

		contextPathwayColorIndex.put(pathwayToAdd, colorIndex);
	}

	/**
	 * set up bubble sets
	 */
	private final void setUpBubbleSet() {
		SetOutline setOutline = new BubbleSet(100, 20, 3, 10.0, 7.0, 0.5, 2.5, 15.0, 8);
		((BubbleSet) setOutline).useVirtualEdges(true);
		AbstractShapeGenerator shaper = new BSplineShapeGenerator(setOutline);
		this.bubblesetCanvas = new CanvasComponent(shaper);
		this.bubblesetCanvas.setDefaultView();
	}

}
