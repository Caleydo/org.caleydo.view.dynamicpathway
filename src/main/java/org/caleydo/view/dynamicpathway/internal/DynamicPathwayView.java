/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.internal;

import org.caleydo.core.event.EventListenerManager;
import org.caleydo.core.serialize.ASerializedView;
import org.caleydo.core.view.opengl.canvas.IGLCanvas;
import org.caleydo.core.view.opengl.layout2.AGLElementView;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.GLSizeRestrictiveFlowLayout;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.internal.serial.SerializedDynamicPathwayView;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayoutBuilder;
import org.caleydo.view.dynamicpathway.ranking.RankingElement;
import org.caleydo.view.dynamicpathway.ui.DynamicPathwayGraphRepresentation;
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

	private GLElementContainer root = new GLElementContainer(GLLayouts.LAYERS);

	private AnimatedGLElementContainer baseContainer = new AnimatedGLElementContainer(
			new GLSizeRestrictiveFlowLayout(true, 10, GLPadding.ZERO));

//	private final DragAndDropController dndController = new DragAndDropController(this);

	// private EventBasedSelectionManager vertexSelectionManager;

	private PathwayFilters.CommonVertexFilter filter = null;

	public DynamicPathwayView(IGLCanvas glCanvas) {
		super(glCanvas, VIEW_TYPE, VIEW_NAME);

		createPathwayGraphView();

		createRankingSideBar();

		root.add(baseContainer);
		root.add(currentPathwayElement);

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
//
//	public DragAndDropController getDndController() {
//		return dndController;
//	}

	// TODO: inlude Vertex highlighting
	// @Override
	// public void notifyOfSelectionChange(EventBasedSelectionManager selectionManager) {
	// // TODO Auto-generated method stub
	//
	// }

	/**
	 * if a pathway in the list was selected, it is added to the representation, so this pathway graph is
	 * drawn on the right side
	 * 
	 * @param pathway
	 *            the pathway which was selected
	 */
	public void addPathway(PathwayGraph pathway) {
		this.currentPathwayElement.addPathwayRep(pathway);
	}

	/**
	 * check if this pathway is not already the main drawn pathway
	 * so it isn't drawn again
	 * 
	 * @param pathway
	 *            the pathway which should be checked
	 * @return true if it's the same, that is already drawn, false otherwise
	 */
	public boolean isGraphPresented(PathwayGraph pathway) {
		if (currentPathwayElement.getDynamicPathway().isGraphPresented(pathway))
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

	/**
	 * view for representing pathway graphs
	 */
	private void createPathwayGraphView() {
		pathwayLayout = new GLFruchtermanReingoldLayoutBuilder().repulsionMultiplier(-1.0)
				.attractionMultiplier(18.0).nodeBoundsExtension(4.0).buildLayout();

		currentPathwayElement = new DynamicPathwayGraphRepresentation(pathwayLayout, this);
		currentPathwayElement.setLocation(200, 0);
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
		rankingWindow.setLocation(0, Float.NaN);
		rankingWindow.setSize(200, Float.NaN);

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
