/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.internal;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.caleydo.core.event.EventListenerManager;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.GLSizeRestrictiveFlowLayout;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.internal.serial.SerializedDynamicPathwayView;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayoutBuilder;
import org.caleydo.view.dynamicpathway.ranking.RankingElement;
import org.caleydo.view.dynamicpathway.ui.DynamicPathwayGraphRepresentation;
import org.caleydo.view.dynamicpathway.ui.MakeFocusPathwayEvent;
import org.caleydo.view.dynamicpathway.ui.RemoveDisplayedPathwayEvent;
import org.caleydo.view.entourage.SideWindow;
import org.caleydo.view.entourage.SlideInElement;
import org.caleydo.view.entourage.SlideInElement.ESlideInElementPosition;
import org.caleydo.view.entourage.ranking.PathwayFilters;

/**
 * view, which can represent different pathways combined into one pathway
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayView extends AGLElementView /* implements IEventBasedSelectionManagerUser */{
	public static final String VIEW_TYPE = "org.caleydo.view.dynamicpathway";
	public static final String VIEW_NAME = "DynamicPathway";

	private DynamicPathwayWindow activeWindow;
	private DynamicPathwayWindow rankingWindow;

	private RankingElement rankingElement;

	private DynamicPathwayGraphRepresentation currentPathwayElement;
	private GLFruchtermanReingoldLayout pathwayLayout;

	private GLElementContainer root = new GLElementContainer(new GLSizeRestrictiveFlowLayout(true, 1,
			GLPadding.ZERO));

	private AnimatedGLElementContainer baseContainer = new AnimatedGLElementContainer(
			new GLSizeRestrictiveFlowLayout(true, 10, GLPadding.ZERO));
	
	private ControllbarContainer controllBar;

	// private final DragAndDropController dndController = new DragAndDropController(this);

	// private EventBasedSelectionManager vertexSelectionManager;

	private PathwayFilters.CommonVertexFilter filter = null;

	public DynamicPathwayView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);

		createPathwayGraphView();

		createRankingSideBar();
		baseContainer.setSize(200, Float.NaN);
		// baseContainer.setRenderer(GLRenderers.fillRect(Color.RED));
		// currentPathwayElement.setLayoutData(1.0f);
		// currentPathwayElement.setRenderer(GLRenderers.fillRect(Color.GREEN));

		root.add(baseContainer);
		root.add(currentPathwayElement);
		GLElementContainer cont = new GLElementContainer(new GLSizeRestrictiveFlowLayout(false, 3,
				GLPadding.ZERO));
		cont.setSize(200, Float.NaN);
		this.controllBar = new ControllbarContainer(this);
		cont.add(controllBar);
		root.add(cont);

		// vertexSelectionManager = new EventBasedSelectionManager(this,
		// IDType.getIDType(EGeneIDTypes.PATHWAY_VERTEX_REP.name()));
		// vertexSelectionManager.registerEventListeners();
	}

	public EventListenerManager getEventListenerManager() {
		return eventListeners;
	}

	@Override
	public ASerializedView getSerializableRepresentation() {
		return new SerializedDynamicPathwayView();
	}

	@Override
	protected GLElement createRoot() {
		return root;
	}

	public void setActiveWindow(DynamicPathwayWindow activeWindow) {
		if (activeWindow != null && this.activeWindow != null && activeWindow != this.activeWindow) {
			this.activeWindow.setActive(false);
		}

		this.activeWindow = activeWindow;

	}

	/**
	 * if a pathway in the list was selected, it is added to the representation, so this pathway graph is
	 * drawn on the right side
	 * 
	 * @param pathway
	 *            the pathway which was selected
	 */
	public void addPathway(PathwayGraph pathway) {
		Boolean addKontextPathway = (currentPathwayElement.getFocusGraph() != null && (currentPathwayElement.getCurrentFilteringNode() != null)) ? true : false;
		
		currentPathwayElement.addPathwayRep(pathway, !addKontextPathway);
	}
	
	public void addPathwayToControllBar(PathwayGraph pathwayToAdd, boolean isFocusPathway) {
		try {
			controllBar.addPathwayTitle(pathwayToAdd, isFocusPathway);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * if the option display nodes with or without 0 degree (doesn't have nodes) was selected, this method is
	 * called
	 * 
	 * it repaint all displayed pathways, if the option has changed
	 * 
	 * @param addWithZeroDegreeVertices
	 */
	public void paintGraphWithOrWithoutZeroDegreeVertices(boolean addWithZeroDegreeVertices) {
		if (currentPathwayElement.isDisplayOnlyVerticesWithEdges() == addWithZeroDegreeVertices)
			return;

		currentPathwayElement.setDisplayOnlyVerticesWithEdges(addWithZeroDegreeVertices);
		
		List<PathwayGraph> kontextGraphs = new LinkedList<PathwayGraph>(currentPathwayElement.getContextGraphs());

		if (currentPathwayElement.getFocusGraph() != null)
			currentPathwayElement.addPathwayRep(currentPathwayElement.getFocusGraph(), true);

		if (kontextGraphs.size() > 0) {
			for (PathwayGraph kontextGraph : kontextGraphs)
				currentPathwayElement.addPathwayRep(kontextGraph, false);
		}

	}

	public void paintGraphWithOrWithoutDuplicateVertices(boolean addWithDuplicateVertices) {

		/**
		 * can't be called, when context graphs are already displayed -> can't merge graphs with duplicates
		 */
		if (currentPathwayElement.getContextGraphs().size() > 0) {
			System.out
					.println("Can't change this option, when kontext graphs are displayed, because merging graphs with duplicates is not possible");
			return;
		}
		
		currentPathwayElement.setRemoveDuplicateVertices(!addWithDuplicateVertices);
		
		if (currentPathwayElement.getFocusGraph() != null)
			currentPathwayElement.addPathwayRep(currentPathwayElement.getFocusGraph(), true);
	}
	
	@ListenTo
	public void onRemovePathway(RemoveDisplayedPathwayEvent removePathwayEvent) {
		removeGraph(removePathwayEvent.getPathway());
	}
	
	@ListenTo
	public void onMakeThisPathwayFocus(MakeFocusPathwayEvent makeFocusPathwayEvent) {
		
		PathwayGraph newFocusPathwayGraph = makeFocusPathwayEvent.getPathway();
		
		if(newFocusPathwayGraph == null || currentPathwayElement.getDynamicPathway().isFocusGraph(newFocusPathwayGraph))
			return;
		
		/** 
		 * get the old focus pathway & add it to the new context graphs
		 * 
		 * remove new focus pathway from old context pathways
		 */
		PathwayGraph oldFocusPathway = currentPathwayElement.getFocusGraph();
		System.out.println("Old Title: " + oldFocusPathway.getTitle());		
		List<PathwayGraph> newContextGraphs = new ArrayList<PathwayGraph>(currentPathwayElement.getContextGraphs());		
		newContextGraphs.remove(newFocusPathwayGraph);
		newContextGraphs.add(oldFocusPathway);
		
		currentPathwayElement.addPathwayRep(newFocusPathwayGraph, true);
		System.out.println("New Title: " + currentPathwayElement.getFocusGraph() + ", old Title: " + oldFocusPathway.getTitle());
		
		for(PathwayGraph contextGraph : newContextGraphs) {
			currentPathwayElement.addPathwayRep(contextGraph, false);
		}
		
	}
	
	public void removeGraph(PathwayGraph pathwayToRemove) {
		try {
//			PathwayGraph pathwayToRemove = currentPathwayElement.getDynamicPathway().getPathwayWithThisTitle(graphTitle);		
			
			// if the graph to remove is the focus graph, reset everything
			if(currentPathwayElement.getDynamicPathway().isFocusGraph(pathwayToRemove)) {
				currentPathwayElement.clearCanvasAndInfo();
				controllBar.removeFocusPathwayTitle(pathwayToRemove);
			} else {
				controllBar.removeContextPathwayTitle(pathwayToRemove);
				
				List<PathwayGraph> presentContextGraphs = new ArrayList<PathwayGraph>(currentPathwayElement.getContextGraphs());
				presentContextGraphs.remove(pathwayToRemove);
				
				currentPathwayElement.addPathwayRep(currentPathwayElement.getFocusGraph(), true);
				
				for(PathwayGraph contextGraph : presentContextGraphs) {
					currentPathwayElement.addPathwayRep(contextGraph, false);
				}
			}
			
			
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
	}

	/**
	 * check if this pathway is not already the main drawn pathway so it isn't drawn again
	 * 
	 * @param pathway
	 *            the pathway which should be checked
	 * @return true if it's the same, that is already drawn, false otherwise
	 */
	public boolean isGraphPresent(PathwayGraph pathway) {
		if (currentPathwayElement.getDynamicPathway().isPathwayPresent(pathway))
			return true;
		return false;
	}

	/**
	 * filter the pathway list on the left leaves pathways which contain the given PathwayVertexRep
	 * 
	 * called when a vertex is selected @see
	 * org.caleydo.view.dynamicpathway.ui.DynamicPathwayGraphRepresentation
	 * #setOrResetSelectedNode(org.caleydo.view.dynamicpathway.ui.NodeElement)
	 * 
	 * @param currentContextVertexRep
	 *            the currently selected node, after which the list is selected
	 */
	public void filterPathwayList(PathwayVertexRep currentContextVertexRep) {

		filter = new PathwayFilters.CommonVertexFilter(currentContextVertexRep, false);
		rankingElement.setFilter(filter);
		rankingElement.relayout();
	}

	public void unfilterPathwayList() {
		rankingElement.removeFilter(filter);
		rankingElement.relayout();
	}
	
	public PathwayGraph getCurrentFocusPathway() {
		return currentPathwayElement.getFocusGraph();
	}

	/**
	 * view for representing pathway graphs
	 */
	private void createPathwayGraphView() {
		pathwayLayout = new GLFruchtermanReingoldLayoutBuilder().repulsionMultiplier(-1.0)
				.attractionMultiplier(18.0).nodeBoundsExtension(4.0).buildLayout();

		currentPathwayElement = new DynamicPathwayGraphRepresentation(pathwayLayout, this);
		// currentPathwayElement.setLocation(200, 0);
	}

	/**
	 * view for side bar, which contains a list of representable pathways
	 */
	private void createRankingSideBar() {
		AnimatedGLElementContainer column = new AnimatedGLElementContainer(new GLSizeRestrictiveFlowLayout(
				false, 10, GLPadding.ZERO));

		column.add(baseContainer);

		rankingWindow = new DynamicPathwaySideWindow("Pathways", this, SideWindow.SLIDE_LEFT_OUT);
		rankingElement = new RankingElement(this);
		rankingWindow.setContent(rankingElement);
		// rankingWindow.setLocation(0, Float.NaN);
		// rankingWindow.setSize(200, Float.NaN);

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
		rankingElement.setWindow(rankingWindow);

		baseContainer.add(rankingWindow);
	}


}
