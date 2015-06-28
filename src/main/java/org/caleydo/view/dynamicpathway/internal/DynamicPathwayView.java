/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.internal;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.event.EventListenerManager;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.GLSizeRestrictiveFlowLayout;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.events.ChangeBubbleSetVisibilityEvent;
import org.caleydo.view.dynamicpathway.events.ChangeFocusNodeEvent;
import org.caleydo.view.dynamicpathway.events.ChangeVertexEnvironmentEvent;
import org.caleydo.view.dynamicpathway.events.ClearCanvasEvent;
import org.caleydo.view.dynamicpathway.events.DuplicateVerticesSettingChangeEvent;
import org.caleydo.view.dynamicpathway.events.FilterPathwayEvent;
import org.caleydo.view.dynamicpathway.events.MakeFocusPathwayEvent;
import org.caleydo.view.dynamicpathway.events.RemoveDisplayedPathwayEvent;
import org.caleydo.view.dynamicpathway.events.ZeroDegreeNodesSettingChangeEvent;
import org.caleydo.view.dynamicpathway.internal.serial.SerializedDynamicPathwayView;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayoutBuilder;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayRankingElement;
import org.caleydo.view.dynamicpathway.ui.ANodeElement;
import org.caleydo.view.dynamicpathway.ui.DynamicPathwaysCanvas;
import org.caleydo.view.dynamicpathway.util.CommonVertexListFilter;
import org.caleydo.view.dynamicpathway.util.PathwayUtil;
import org.caleydo.view.entourage.SideWindow;
import org.caleydo.view.entourage.SlideInElement;
import org.caleydo.view.entourage.SlideInElement.ESlideInElementPosition;
import org.jgrapht.graph.DefaultEdge;

/**
 * Main view of the project, containing the sub-views: PathwayList, PathwayCanvas & ControlBar
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayView extends AGLElementView {

	public static final String VIEW_TYPE = "org.caleydo.view.dynamicpathway";
	public static final String VIEW_NAME = "DynamicPathway";

	public static final Color FOCUS_PATHWAY_COLOR = Color.GRAY;

	private static final String PATHWAY_PARTLY_IDENTIFIER = " [P]";

	private static final String PATHWAY_RESOURCE = "KEGG";

	// root view container
	private GLElementContainer viewContainer = new GLElementContainer(new GLSizeRestrictiveFlowLayout(true, 1,
			GLPadding.ZERO));

	private DynamicPathwayWindow activeWindow;
	private DynamicPathwayWindow rankingWindow;

	// viewContainer children:
	// ===========================
	// pathway list
	private AnimatedGLElementContainer pathwayListContainer = new AnimatedGLElementContainer(
			new GLSizeRestrictiveFlowLayout(true, 10, GLPadding.ZERO));

	private DynamicPathwayRankingElement rankingElement;

	private CommonVertexListFilter pathwayListFilter = null;

	// pathway canvas
	private DynamicPathwaysCanvas pathwayCanvasContainer;

	private GLFruchtermanReingoldLayout pathwayLayout;

	// control bar
	private ControlbarContainer controlBar;

	public DynamicPathwayView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);

		createPathwayGraphView();

		createRankingSideBar();
		pathwayListContainer.setSize(200, Float.NaN);

		viewContainer.add(pathwayListContainer);
		viewContainer.add(pathwayCanvasContainer);
		GLElementContainer controlBarContainer = new GLElementContainer(new GLSizeRestrictiveFlowLayout(false, 3,
				GLPadding.ZERO));
		controlBarContainer.setSize(200, Float.NaN);
		this.controlBar = new ControlbarContainer(this);
		controlBarContainer.add(controlBar);
		viewContainer.add(controlBarContainer);

	}

	/**
	 * if a pathway in the list was selected, it is added to the representation, so this pathway graph is drawn on the
	 * right side
	 * 
	 * @param pathwayToAdd
	 *            the pathway which was selected
	 */
	public void addPathway(PathwayGraph pathwayToAdd) {

		boolean addContextPathway = (pathwayCanvasContainer.getFocusPathway() != null) ? true : false;

		try {
			int envSize = controlBar.getNodeEnvironmentSize();
			if (addContextPathway && envSize > 0) {

				PathwayGraph subPathway;
				if (pathwayCanvasContainer.getFocusNode() == null)
					subPathway = null;
				else
					subPathway = createPathwayWithFocusVertexAndHisEnvironment(pathwayToAdd, envSize);

				if (subPathway != null && subPathway.vertexSet().size() > 0)
					pathwayCanvasContainer.addPathwayToCanvas(subPathway, !addContextPathway, true, true);
				else {
					addPathwayToControlBar(pathwayToAdd, false, Color.LIGHT_GRAY);
					pathwayCanvasContainer.getDynamicPathway().addFocusOrContextPathway(pathwayToAdd, true);
					System.out.println("Pathway didn't contain the focus vrep");
				}
			} else {
				pathwayCanvasContainer.addPathwayToCanvas(pathwayToAdd, !addContextPathway, false, true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * add a pathway title to the control bar
	 * 
	 * @param pathwayToAdd
	 *            the pathway to add
	 * @param isFocusPathway
	 *            whether it's the Focus Pathway or a Context Pathway
	 * @param titleColor
	 *            the pathway's bubble set's color
	 */
	public void addPathwayToControlBar(PathwayGraph pathwayToAdd, boolean isFocusPathway, Color titleColor) {
		try {
			controlBar.addPathwayTitle(pathwayToAdd, isFocusPathway, titleColor);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * filter the pathway list on the left leaves pathways which contain at least one of the given verticesToFilterBy
	 * 
	 * called when a vertex is selected @see org.caleydo.view.dynamicpathway.ui.DynamicPathwayGraphRepresentation
	 * #setOrResetSelectedNode(org.caleydo.view.dynamicpathway.ui.NodeElement)
	 * 
	 */
	public void filterPathwayList(List<PathwayVertex> verticesToFilterBy) {

		pathwayListFilter = new CommonVertexListFilter(verticesToFilterBy, PATHWAY_RESOURCE);
		rankingElement.setFilter(pathwayListFilter);
		rankingElement.relayout();
	}

	public PathwayGraph getCurrentFocusPathway() {
		return pathwayCanvasContainer.getFocusPathway();
	}

	public EventListenerManager getEventListenerManager() {
		return eventListeners;
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		return new SerializedDynamicPathwayView();
	}

	/**
	 * check if this pathway is not already the main drawn pathway so it isn't drawn again
	 * 
	 * @param pathway
	 *            the pathway which should be checked
	 * @return true if it's the same, that is already drawn, false otherwise
	 */
	public boolean isPathwayPresent(PathwayGraph pathway) {
		return pathwayCanvasContainer.isPathwayPresent(pathway);

	}

	@ListenTo
	public void onChangeBubbleSetVisibilityOfPathway(ChangeBubbleSetVisibilityEvent changeBubbleSetVisibilityEvent) {
		pathwayCanvasContainer.enableOrDisableBubbleSetOfPathway(
				changeBubbleSetVisibilityEvent.getPathwayWithVisibilityToChange(),
				changeBubbleSetVisibilityEvent.getNewBubbleSetVisibilityStatus());
	}

	/**
	 * listens if the path should be filtered or not used for selecting a kontext pathway, which contains the requested
	 * vertex
	 * 
	 * @param event
	 * @throws Exception
	 */
	@ListenTo
	public void onChangeFocusNode(ChangeFocusNodeEvent event) throws Exception {
		ANodeElement newFocusNode = event.getNodeElementToFilterBy();

		if (newFocusNode == null)
			return;

		filterPathwayList(newFocusNode.getVertices());

		/**
		 * if the new focus node is part of the focus pathway, just the current focus node needs to be changed.
		 * Otherwise the focus pathway is exchanged with the new focus node's context pathway
		 */
		if (newFocusNode.getPathways().contains(pathwayCanvasContainer.getFocusPathway())) {

			pathwayCanvasContainer.setFocusNode(newFocusNode);

			if (pathwayCanvasContainer.getContextPathways().size() > 0 && controlBar.getNodeEnvironmentSize() >= 0) {
				readdPresentPathways();
			}
		} else {
			PathwayGraph oldFocusPathway = pathwayCanvasContainer.getFocusPathway();
			PathwayGraph newFocusPathway = newFocusNode.getPathways().get(0);

			List<PathwayGraph> contextPathways = pathwayCanvasContainer.getContextPathways();
			contextPathways.remove(newFocusPathway);
			if (controlBar.getNodeEnvironmentSize() >= 0)
				newFocusPathway = pathwayCanvasContainer.getOriginalPathwaysOfSubpathway(newFocusPathway);
			contextPathways.add(oldFocusPathway);
			pathwayCanvasContainer.setFocusPathway(newFocusPathway);
			pathwayCanvasContainer.setFocusNode(newFocusNode);
			readdPresentPathways();
		}
	}

	@ListenTo
	public void onChangeVertexEnvironmentSize(ChangeVertexEnvironmentEvent event) {

		if (pathwayCanvasContainer.getFocusPathway() == null)
			return;

		readdPresentPathways();
	}

	@ListenTo
	public void onClearCancas(ClearCanvasEvent clearCanvasEvent) {
		PathwayGraph focusPathway = pathwayCanvasContainer.getFocusPathway();
		if (focusPathway != null)
			removePathway(focusPathway);
	}

	@ListenTo
	public void onDuplicateVerticesSettingChange(DuplicateVerticesSettingChangeEvent duplicateVerticesEvent) {

		boolean addWithDuplicateVertices = duplicateVerticesEvent.allowDuplicateVertices();
		/**
		 * can't be called, when context graphs are already displayed -> can't merge graphs with duplicates
		 */
		if (pathwayCanvasContainer.getContextPathways().size() > 0) {
			System.out
					.println("Can't change this option, when context graphs are displayed, because merging graphs with duplicates is not possible");
			return;
		}

		pathwayCanvasContainer.setRemoveDuplicateVertices(!addWithDuplicateVertices);

		PathwayGraph focusPathway = pathwayCanvasContainer.getFocusPathway();
		if (focusPathway != null) {
			readdFocusPathway(true, true);
		}
	}

	@ListenTo
	public void onFilterPathwayList(FilterPathwayEvent filterPathway) {
		ANodeElement filterNode = filterPathway.getNodeElementToFilterBy();

		if (filterNode != null) {
			pathwayCanvasContainer.setOrResetSelectedNode(filterNode);
			filterPathwayList(filterNode.getVertices());

		}

	}

	@ListenTo
	public void onMakeThisPathwayFocus(MakeFocusPathwayEvent makeFocusPathwayEvent) throws Exception {

		PathwayGraph newFocusPathwayGraph = makeFocusPathwayEvent.getPathway();

		PathwayGraph pathwayToAdd = null;
		if (newFocusPathwayGraph == null
				|| pathwayCanvasContainer.getDynamicPathway().isFocusGraph(newFocusPathwayGraph))
			return;

		// if the context pathway was a sub pathway, get it's original full version & make this the new focus pathway
		if (pathwayCanvasContainer.isSubPathway(newFocusPathwayGraph) == true) {
			// removePathway(newFocusPathwayGraph);
			pathwayToAdd = pathwayCanvasContainer.getOriginalPathwaysOfSubpathway(newFocusPathwayGraph);
			pathwayCanvasContainer.removeOriginalPathwayAndSubpathwayOfMap(newFocusPathwayGraph);
		} else
			pathwayToAdd = newFocusPathwayGraph;

		System.out.println("Is sub pathway? " + pathwayCanvasContainer.isSubPathway(newFocusPathwayGraph));

		/**
		 * get the old focus pathway & add it to the new context graphs
		 * 
		 * remove new focus pathway from old context pathways
		 */
		PathwayGraph oldFocusPathway = pathwayCanvasContainer.getFocusPathway();
		System.out.println("Old Title: " + oldFocusPathway.getTitle());
		List<PathwayGraph> newContextGraphs = new ArrayList<PathwayGraph>(pathwayCanvasContainer.getContextPathways());
		newContextGraphs.remove(newFocusPathwayGraph);

		if (controlBar.getNodeEnvironmentSize() > 0) {
			PathwayGraph subPathway = createPathwayWithFocusVertexAndHisEnvironment(oldFocusPathway,
					controlBar.getNodeEnvironmentSize());
			newContextGraphs.add(subPathway);
		} else {
			newContextGraphs.add(oldFocusPathway);
		}

		pathwayCanvasContainer.addPathwayToCanvas(pathwayToAdd, true, false, true);
		System.out.println("New Title: " + pathwayCanvasContainer.getFocusPathway() + ", old Title: "
				+ oldFocusPathway.getTitle());

		for (PathwayGraph contextGraph : newContextGraphs) {
			pathwayCanvasContainer.addPathwayToCanvas(contextGraph, false, false, false);
		}

	}

	/**
	 * if the option display nodes with or without 0 degree (doesn't have nodes) was selected, this method is called
	 * 
	 * it repaint all displayed pathways, if the option has changed
	 * 
	 * @param addWithZeroDegreeVertices
	 * @throws Exception
	 */
	@ListenTo
	public void onPaintGraphWithOrWithoutZeroDegreeVertices(ZeroDegreeNodesSettingChangeEvent zeroDegreeEvent) {
		boolean addWithZeroDegreeVertices = zeroDegreeEvent.allowZeroDegreeNodes();

		if (pathwayCanvasContainer.isDisplayOnlyVerticesWithEdges() == addWithZeroDegreeVertices)
			return;

		pathwayCanvasContainer.setDisplayOnlyVerticesWithEdges(addWithZeroDegreeVertices);

		List<PathwayGraph> contextPathways = new LinkedList<PathwayGraph>(pathwayCanvasContainer.getContextPathways());

		Boolean hasContextPathways = (contextPathways.size() > 0) ? true : false;

		PathwayGraph focusPathway = pathwayCanvasContainer.getFocusPathway();
		if (focusPathway != null) {
			readdFocusPathway(!hasContextPathways, true);
		}

		if (hasContextPathways) {
			for (PathwayGraph contextGraph : contextPathways)
				addContextPathway(contextGraph);

			// dynamicGraphCanvas.addPathwayToCanvas(contextGraph, false, false, true);

		}

	}

	@ListenTo
	public void onRemovePathway(RemoveDisplayedPathwayEvent removePathwayEvent) {
		removePathway(removePathwayEvent.getPathway());
	}

	/**
	 * remove a pathway from the canvas
	 * 
	 * @param pathwayToRemove
	 */
	public void removePathway(PathwayGraph pathwayToRemove) {
		try {

			// TODO: isFocusGraph(pathwayToRemove) was false even though focus pw was added!!!
			// if the graph to remove is the focus graph, reset everything
			if (pathwayCanvasContainer.getDynamicPathway().isFocusGraph(pathwayToRemove)) {
				pathwayCanvasContainer.clearCanvasAndInfo(true, false);
				pathwayCanvasContainer.getDynamicPathway().removeAllPathways();
				controlBar.removeFocusPathwayTitle(pathwayToRemove);
			} else {
				PathwayGraph focusPathway = pathwayCanvasContainer.getFocusPathway();
				boolean displayBubbleSetOfFocusPathway = pathwayCanvasContainer.isBubbleSetDisplayed(focusPathway);
				pathwayCanvasContainer.getDynamicPathway().removeContextPathway(pathwayToRemove);
				pathwayCanvasContainer.removePathwayFromContextPathwayColorIndexMapAndBubbleSetList(pathwayToRemove);
				controlBar.removeContextPathwayTitle(pathwayToRemove);

				List<PathwayGraph> presentContextPathways = new ArrayList<PathwayGraph>(
						pathwayCanvasContainer.getContextPathways());
				presentContextPathways.remove(pathwayToRemove);

				// if (dynamicGraphCanvas.isSubPathway(pathwayToRemove))
				// dynamicGraphCanvas.getOriginalPathwaysOfSubpathway(subPathway)
				System.out.println(pathwayCanvasContainer.removeOriginalPathwayAndSubpathwayOfMap(pathwayToRemove));

				pathwayCanvasContainer.addPathwayToCanvas(focusPathway, true, false, true);
				if (displayBubbleSetOfFocusPathway)
					pathwayCanvasContainer.displayBubbleSetOfPathway(focusPathway);

				for (PathwayGraph contextPathway : presentContextPathways) {
					PathwayGraph pathwayToAdd = pathwayCanvasContainer
							.removeOriginalPathwayAndSubpathwayOfMap(contextPathway);
					if (pathwayToAdd == null)
						pathwayToAdd = contextPathway;
					addContextPathway(pathwayToAdd);
					// dynamicGraphCanvas.addPathwayToCanvas(contextGraph, false, false, true);
				}

				relayout();

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}

	}

	/**
	 * set new window as active window
	 * 
	 * @param activeWindow
	 *            new active window
	 */
	public void setActiveWindow(DynamicPathwayWindow activeWindow) {
		if (activeWindow != null && this.activeWindow != null && activeWindow != this.activeWindow) {
			this.activeWindow.setActive(false);
		}

		this.activeWindow = activeWindow;

	}

	/**
	 * removes the filter from the pathway ranking bar on the left side, i.e. all pathways are shown again
	 */
	public void unfilterPathwayList() {
		rankingElement.removeFilter(pathwayListFilter);
		rankingElement.relayout();
	}

	@Override
	protected GLElement createRoot() {
		return viewContainer;
	}

	/**
	 * add context pathway to views
	 * 
	 * @param contextPathway
	 *            pathway to add
	 */
	private void addContextPathway(PathwayGraph contextPathway) {

		try {

			int envSize = controlBar.getNodeEnvironmentSize();
			if (envSize > 0) {

				PathwayGraph subPathway;
				if (pathwayCanvasContainer.getFocusNode() == null)
					subPathway = null;
				else
					subPathway = createPathwayWithFocusVertexAndHisEnvironment(contextPathway, envSize);

				if (subPathway != null && subPathway.vertexSet().size() > 0)
					pathwayCanvasContainer.addPathwayToCanvas(subPathway, false, true, true);
				else {
					addPathwayToControlBar(contextPathway, false, Color.LIGHT_GRAY);
					pathwayCanvasContainer.getDynamicPathway().addFocusOrContextPathway(contextPathway, true);
					System.out.println("Pathway didn't contain the focus vrep");
				}
			} else {
				pathwayCanvasContainer.addPathwayToCanvas(contextPathway, false, false, true);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * view for representing pathway graphs
	 */
	private void createPathwayGraphView() {
		pathwayLayout = new GLFruchtermanReingoldLayoutBuilder().repulsionMultiplier(-1.0).attractionMultiplier(18.0)
				.buildLayout();

		pathwayCanvasContainer = new DynamicPathwaysCanvas(pathwayLayout, this);
		// currentPathwayElement.setLocation(200, 0);
	}

	/**
	 * If pathways are only added partly (i.e. if a valid vertex environment size was defined) this method finds all
	 * vertices with node environment's, e.g. 4, distance to the focus node
	 * 
	 * @param vertexEnvironmentSize
	 * @return
	 * @throws Exception
	 */
	private PathwayGraph createPathwayWithFocusVertexAndHisEnvironment(PathwayGraph pathwayToAdd,
			int vertexEnvironmentSize) throws Exception {

		/**
		 * ----------------------------------------------------------------------- <br/>
		 * STEP 1: find the FOCUS VERTEX REPRESENATION
		 * -----------------------------------------------------------------------
		 */
		ANodeElement currentFilteringNode = pathwayCanvasContainer.getFocusNode();

		if (currentFilteringNode == null)
			return null;

		PathwayVertex currentFilteringVertex = currentFilteringNode.getDisplayedVertex();
		List<PathwayVertex> focusVertices = currentFilteringNode.getVertices();
		if (currentFilteringNode == null)
			return null;

		PathwayVertexRep currentFilteringVRep; // = currentFilteringNode.getVertexRep();

		Set<DefaultEdge> edgesOfThisNode = null;
		PathwayVertexRep alternativeVrepFromPathway = null;
		PathwayVertex focusVertex = null;

		currentFilteringVRep = PathwayUtil.pathwayContainsVertex(currentFilteringVertex, pathwayToAdd);
		if (currentFilteringVRep == null) {
			currentFilteringVRep = PathwayUtil.pathwayContainsVertices(focusVertices, pathwayToAdd);
			if (currentFilteringVRep == null) {
				System.out
						.println("createPathwayWithFocusVertexAndHisEnvironment: vertices not found. Focus vertices: "
								+ focusVertices);
				return null;
			}
		}

		edgesOfThisNode = pathwayToAdd.edgesOf(currentFilteringVRep);
		focusVertex = currentFilteringNode.getVertices().get(0);

		if (edgesOfThisNode == null || focusVertex == null)
			return null;

		String title = pathwayToAdd.getTitle().endsWith(PATHWAY_PARTLY_IDENTIFIER) ? pathwayToAdd.getTitle()
				: pathwayToAdd.getTitle() + PATHWAY_PARTLY_IDENTIFIER;

		PathwayGraph subPathway = new PathwayGraph(pathwayToAdd.getType(), pathwayToAdd.getName(), title,
				pathwayToAdd.getImage(), pathwayToAdd.getExternalLink());

		subPathway.addVertex(currentFilteringVRep);

		PathwayVertexRep vrepToCheckWith = currentFilteringVRep;
		if (alternativeVrepFromPathway != null) {
			subPathway.addVertex(alternativeVrepFromPathway);
			vrepToCheckWith = alternativeVrepFromPathway;
		}

		/**
		 * ----------------------------------------------------------------------- <br />
		 * STEP 2: find the nodes of the next level
		 * -----------------------------------------------------------------------
		 */
		Set<PathwayVertexRep> vrepsOfCurrentLevel = new HashSet<PathwayVertexRep>();
		Set<PathwayVertexRep> vrepsOfNextLevel = new HashSet<PathwayVertexRep>();

		for (DefaultEdge edgeOfCurrentFilteringVrep : edgesOfThisNode) {
			PathwayVertexRep sourceVrep = pathwayToAdd.getEdgeSource(edgeOfCurrentFilteringVrep);
			PathwayVertexRep targetVrep = pathwayToAdd.getEdgeTarget(edgeOfCurrentFilteringVrep);

			// if the main node is the target node, the other node is the source node and vice versa
			if (vrepToCheckWith.equals(targetVrep)) {
				subPathway.addVertex(sourceVrep);
				subPathway.addEdge(sourceVrep, vrepToCheckWith, edgeOfCurrentFilteringVrep);
				vrepsOfNextLevel.add(sourceVrep);
			} else if (vrepToCheckWith.equals(sourceVrep)) {
				subPathway.addVertex(targetVrep);
				subPathway.addEdge(vrepToCheckWith, targetVrep, edgeOfCurrentFilteringVrep);
				vrepsOfNextLevel.add(targetVrep);
			} else
				throw new Exception("getVertexEnvironment: vrepToCheckWith was neither source nor target");

		}

		for (int i = 1; i < (vertexEnvironmentSize - 1); i++) {
			vrepsOfCurrentLevel.clear();
			vrepsOfCurrentLevel.addAll(vrepsOfNextLevel);
			vrepsOfNextLevel.clear();

			for (PathwayVertexRep vrepOfCurrentLevel : vrepsOfCurrentLevel) {
				// get the edges of the node
				edgesOfThisNode = pathwayToAdd.edgesOf(vrepOfCurrentLevel);

				// go throw all edges
				for (DefaultEdge edgeOfCurrentFilteringVrep : edgesOfThisNode) {

					// if the edge was already added, go on with the next edge
					if (subPathway.containsEdge(edgeOfCurrentFilteringVrep))
						continue;

					PathwayVertexRep sourceVrep = pathwayToAdd.getEdgeSource(edgeOfCurrentFilteringVrep);
					PathwayVertexRep targetVrep = pathwayToAdd.getEdgeTarget(edgeOfCurrentFilteringVrep);

					// if the main node is the target node, the other node is the source node and vice versa
					if (vrepOfCurrentLevel.equals(targetVrep)) {
						subPathway.addVertex(sourceVrep);
						subPathway.addEdge(sourceVrep, vrepOfCurrentLevel, edgeOfCurrentFilteringVrep);
						vrepsOfNextLevel.add(sourceVrep);
					} else if (vrepOfCurrentLevel.equals(sourceVrep)) {
						subPathway.addVertex(targetVrep);
						subPathway.addEdge(vrepOfCurrentLevel, targetVrep, edgeOfCurrentFilteringVrep);
						vrepsOfNextLevel.add(targetVrep);
					} else
						throw new Exception("getVertexEnvironment: vrepToCheckWith was neither source nor target");
				}
			}

		}

		pathwayCanvasContainer.addOriginalPathwayAndSubpathwayToMap(subPathway, pathwayToAdd);

		return subPathway;
	}

	/**
	 * view for side bar, which contains a list of representable pathways
	 */
	private void createRankingSideBar() {
		AnimatedGLElementContainer column = new AnimatedGLElementContainer(new GLSizeRestrictiveFlowLayout(false, 10,
				GLPadding.ZERO));

		column.add(pathwayListContainer);

		rankingWindow = new DynamicPathwaySideWindow("Pathways", this, SideWindow.SLIDE_LEFT_OUT);
		rankingElement = new DynamicPathwayRankingElement(this);
		rankingWindow.setContent(rankingElement);

		SlideInElement slideInElement = new SlideInElement(rankingWindow, ESlideInElementPosition.RIGHT);
		slideInElement.setCallBack(new ISelectionCallback() {
			@Override
			public void onSelectionChanged(GLButton button, boolean selected) {
				AnimatedGLElementContainer anim = (AnimatedGLElementContainer) rankingWindow.getParent();
				if (selected) {
					anim.resizeChild(rankingWindow, 200, Float.NaN);

				} else {
					anim.resizeChild(rankingWindow, 1, Float.NaN);
				}

			}
		});

		rankingWindow.addSlideInElement(slideInElement);
		rankingWindow.setShowCloseButton(false);

		// add reset button
		GLButton clearPathwayFiltersButton = new GLButton(EButtonMode.BUTTON);
		rankingWindow.getTitleBar().add(rankingWindow.getTitleBar().size() - 1, clearPathwayFiltersButton);
		clearPathwayFiltersButton.setSize(16, 16);
		clearPathwayFiltersButton.setRenderer(GLRenderers.fillImage("resources/icons/filter_clear.png"));

		clearPathwayFiltersButton.onPick(new APickingListener() {
			@Override
			protected void clicked(Pick pick) {
				// rankingElement.setFilter(PathwayFilters.NONE);
				// rankingElement.setRanking(null);
				pathwayCanvasContainer.removeFocusNode();
				unfilterPathwayList();
			}
		});
		clearPathwayFiltersButton.setTooltip("Clear Filters from Pathway List and Reset Focus Node");

		rankingElement.setWindow(rankingWindow);

		pathwayListContainer.add(rankingWindow);
	}

	/**
	 * re-add the current focus pathway
	 * 
	 * @param clearOriginalPathwaysMap
	 *            whether this original pathways should be removed or not
	 * @param keepOldFocusNode
	 *            whether the old focus node should be kept or not
	 */
	private void readdFocusPathway(boolean clearOriginalPathwaysMap, boolean keepOldFocusNode) {
		PathwayGraph focusPathway = pathwayCanvasContainer.getFocusPathway();
		boolean displayBubbleSet = pathwayCanvasContainer.isBubbleSetDisplayed(focusPathway);
		pathwayCanvasContainer.addPathwayToCanvas(focusPathway, true, clearOriginalPathwaysMap, keepOldFocusNode);
		if (displayBubbleSet)
			pathwayCanvasContainer.displayBubbleSetOfPathway(focusPathway);

		controlBar.setDisplayFocusPathwayBubbleSetCheckBox(displayBubbleSet);

	}

	/**
	 * re-add all present pathways
	 */
	private void readdPresentPathways() {
		List<PathwayGraph> contextPathways = new LinkedList<PathwayGraph>(pathwayCanvasContainer.getContextPathways());

		readdFocusPathway(false, true);

		for (PathwayGraph contextPathway : contextPathways) {
			try {

				PathwayGraph pathwayToAdd;

				/**
				 * if the contextGraph was a subpathway, get the full pathway, else just use the contextPathway
				 */
				// PathwayGraph fullPathway = dynamicGraphCanvas.getOriginalPathwaysOfSubpathway(contextPathway);
				PathwayGraph fullPathway = pathwayCanvasContainer
						.removeOriginalPathwayAndSubpathwayOfMap(contextPathway);
				System.out.println("ContextPw: " + contextPathway + " fullPW: " + fullPathway + " envSize: "
						+ controlBar.getNodeEnvironmentSize());
				if (fullPathway == null)
					fullPathway = contextPathway;
				if (controlBar.getNodeEnvironmentSize() < 1)
					pathwayToAdd = fullPathway;
				else
					pathwayToAdd = createPathwayWithFocusVertexAndHisEnvironment(fullPathway,
							controlBar.getNodeEnvironmentSize());

				if (pathwayToAdd != null) {
					pathwayCanvasContainer.addPathwayToCanvas(pathwayToAdd, false, false, true);
				} else {
					addPathwayToControlBar(contextPathway, false, Color.LIGHT_GRAY);
					pathwayCanvasContainer.getDynamicPathway().addFocusOrContextPathway(contextPathway, true);
					System.out.println("Pathway didn't contain focus node");
				}
			} catch (Exception e) {
				e.printStackTrace();
				System.exit(-1);
			}

		}
	}

}
