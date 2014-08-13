/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.IEventBasedSelectionManagerUser;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.genetic.EGeneIDTypes;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexShape;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwayView;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutGraph;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.collect.Lists;

/**
 * Container, which is defined by the graph layout {@link GLFruchtermanReingoldLayout} contains the renderable
 * Elements
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayGraphRepresentation extends AnimatedGLElementContainer implements IFRLayoutGraph,
		IEventBasedSelectionManagerUser {

	private static final boolean DISPLAY_ONLY_VERTICES_WITH_EDGES = false;

	/**
	 * contains focus & kontextpathway informations
	 */
	private DynamicPathwayGraph pathway;

	/**
	 * contains nodes & edges used for defining and rendering the layout
	 */
	private Set<NodeElement> nodeSet;
	private Set<IFRLayoutEdge> edgeSet;

	/**
	 * the currently selected node
	 */
	private NodeElement currentSelectedNode;

	/**
	 * is null if no node was selected, otherwise it is a reference to the currently selected node -> needed
	 * for merging
	 */
	private NodeElement currentFilteringNode;

	/**
	 * the view that hold the pathway list & the pathway representation
	 */
	private DynamicPathwayView view;

	/**
	 * informs other that (and which) vertex (nodeElement) was selected
	 */
	private EventBasedSelectionManager vertexSelectionManager;

	private Map<PathwayVertex, List<NodeElement>> preventDuplicatesMap;

	public DynamicPathwayGraphRepresentation(GLFruchtermanReingoldLayout layout, DynamicPathwayView view) {

		this.pathway = new DynamicPathwayGraph();

		this.nodeSet = new HashSet<NodeElement>();
		this.edgeSet = new HashSet<IFRLayoutEdge>();

		this.view = view;

		this.vertexSelectionManager = new EventBasedSelectionManager(this,
				IDType.getIDType(EGeneIDTypes.PATHWAY_VERTEX.name()));
		this.vertexSelectionManager.registerEventListeners();

		this.preventDuplicatesMap = new HashMap<PathwayVertex, List<NodeElement>>();

		setLayout(layout);

	}

	/**
	 * 
	 * @param graph
	 *            if a new pathway was added, a new combined (focus + parts of kontext pathways) pathway is
	 *            created
	 * 
	 */
	public void addPathwayRep(PathwayGraph graph) {

		/**
		 * if a node is selected & another pathway was selected, this has to be a kontextpathway
		 */
		Boolean addKontextPathway = (pathway.isFocusGraphSet() && (currentFilteringNode != null)) ? true
				: false;

		pathway.addFocusOrKontextPathway(graph, addKontextPathway, currentSelectedNode);
		if (!addKontextPathway) {
			currentSelectedNode = null;
			currentFilteringNode = null;
			view.unfilterPathwayList();

			nodeSet.clear();
			edgeSet.clear();
			preventDuplicatesMap.clear();

			clear();

			// for (PathwayVertexRep vrep : pathway.getCombinedVertexSet()) {
			// if (DISPLAY_ONLY_VERTICES_WITH_EDGES) {
			// if (pathway.getCombinedGraph().inDegreeOf(vrep) <= 0
			// && pathway.getCombinedGraph().outDegreeOf(vrep) <= 0) {
			// System.out.println("ignoring: " + vrep.getShortName());
			// continue;
			// }
			// }
			//
			// NodeElement node = addNewNodeElement(vrep);
			//
			// /**
			// * needed for the layouting algorithm
			// */
			// // nodeSet.add(node);
			//
			// }

			removeDuplicateNodeElements(graph);
		} else {
			// for(PathwayVertexRep vrep : graph.vertexSet()) {
			// if (DISPLAY_ONLY_VERTICES_WITH_EDGES) {
			// if (graph.inDegreeOf(vrep) <= 0
			// && graph.outDegreeOf(vrep) <= 0) {
			// System.out.println("ignoring: " + vrep.getShortName());
			// continue;
			// }
			// }
			//
			// NodeElement node = addNewNodeElement(vrep);
			//
			// /**
			// * needed for the layouting algorithm
			// */
			// // nodeSet.add(node);
			//
			// }

			int index = pathway.getKontextGraphs().indexOf(graph);

			assert (index != -1);

			 removeDuplicateNodeElements(pathway.getKontextGraphs().get(index));
		}

		// for (DefaultEdge e : pathway.getCombinedEdgeSet()) {
		// PathwayVertexRep vrepSource = pathway.getEdgeSource(e);
		// PathwayVertexRep vrepTarget = pathway.getEdgeTarget(e);
		//
		// if (vrepSource == null || vrepTarget == null)
		// continue;
		//
		// NodeElement nodeSource = pathway.getNodeOfVertex(vrepSource);
		// NodeElement nodeTarget = pathway.getNodeOfVertex(vrepTarget);
		//
		// if (nodeSource == null || nodeTarget == null)
		// continue;
		//
		// EdgeElement edgeElement = new EdgeElement(e, nodeSource, nodeTarget);
		//
		// /**
		// * so the layouting algorithm can extinguish, if it's a node or an edge
		// */
		// edgeElement.setLayoutData(false);
		//
		// /**
		// * needed for the layouting algorithm
		// */
		// edgeSet.add((IFRLayoutEdge) edgeElement);
		//
		// add(edgeElement);
		// }

	}

	private NodeElement addNewNodeElement(PathwayVertexRep vrep) {
		/**
		 * create node of correct type to vertex rep -> different shapes
		 */
		NodeElement node;

		if (vrep.getType() == EPathwayVertexType.compound) {
			node = new NodeCompoundElement(vrep, this);
		} else if (vrep.getType() == EPathwayVertexType.group) {
			node = new NodeGroupElement(vrep, this);
		} else {
			node = new NodeGeneElement(vrep, this);

		}

		/**
		 * so the layouting algorithm can extinguish, if it's a node or an edge
		 */
		node.setLayoutData(true);

		/**
		 * needed for the edge, because edges just get you the vertexRep of the source & target vertices, but
		 * not the element, which contain the new position
		 */
		pathway.addVertexNodeMapEntry(vrep, node);

		return node;

	}

	// insert all PathwayVertex into
	private void removeDuplicateNodeElements(PathwayGraph graphToSaveNewElementsTo) {

		/**
		 * storing the new NodeElements (which contain the merged elements) in a list & adding the afterwards
		 * to the nodeSet list adding them while iterating over the list & deleting elements, would be too
		 * much of a mess
		 */
		Map<PathwayVertex, NodeElement> newNodeElements = new HashMap<PathwayVertex, NodeElement>();
		Map<NodeElement, NodeElement> removedNodeSubstitutedByNewNodeMap = new HashMap<NodeElement, NodeElement>();

		// for (Iterator<NodeElement> nodeSetIterator = nodeSet.iterator(); nodeSetIterator.hasNext();) {
		// for (PathwayVertexRep vrepOfGraph : graphToSaveNewElementsTo.vertexSet()) {
		for (Iterator<PathwayVertexRep> vrepIterator = graphToSaveNewElementsTo.vertexSet().iterator(); vrepIterator
				.hasNext();) {
			PathwayVertexRep vrepOfGraph = vrepIterator.next();
			// System.out.println(vrepOfGraph.getName() + " type: " + vrepOfGraph.getType());
			if (vrepOfGraph.getType() == EPathwayVertexType.map) 
				continue;

				boolean removeCurrentNodeFromNodeSet = false;
				NodeElement substitutedByNode = null;

				NodeElement node = addNewNodeElement(vrepOfGraph);
				// NodeElement node = nodeSetIterator.next();

				for (Iterator<PathwayVertex> vrepOfNodeIterator = node.getVertices().iterator(); vrepOfNodeIterator
						.hasNext();) {

					PathwayVertex vertexOfNode = vrepOfNodeIterator.next();
					PathwayVertexRep vrepOfNode = node.getVertexRep();

					// if this is the first (and maybe only) time, this PathwayVertex is in the PathwayVertex,
					// make a new entry
					if (preventDuplicatesMap.get(vertexOfNode) == null) {
						List<NodeElement> referencesToThisString = new LinkedList<NodeElement>();
						referencesToThisString.add(node);

						preventDuplicatesMap.put(vertexOfNode, referencesToThisString);
						removeCurrentNodeFromNodeSet = false;
						substitutedByNode = null;
					}
					// make new NodeElement
					else {
						assert (preventDuplicatesMap.get(vertexOfNode).size() >= 1);

						// if there was no merged node created yet
						if (newNodeElements.get(vertexOfNode) == null) {
							String shapeType = vrepOfNode.getShapeType().name();
							PathwayVertexRep newMergedVrep = new PathwayVertexRep(
									vertexOfNode.getHumanReadableName(), vrepOfNode.getShapeType().name(),
									vrepOfNode.getCenterX(), vrepOfNode.getCenterY(), vrepOfNode.getWidth(),
									vrepOfNode.getHeight());
							newMergedVrep.addPathwayVertex(vertexOfNode);
							newMergedVrep.setPathway(graphToSaveNewElementsTo);
							
							
							

							NodeElement newMergedNode = addNewNodeElement(newMergedVrep);
							newNodeElements.put(vertexOfNode, newMergedNode);
						}

						// if this was the last vertex of this node -> delete node (merged graph node is its
						// replacement)
						if (node.getVertices().size() <= 1) {
							removeCurrentNodeFromNodeSet = true;
							substitutedByNode = newNodeElements.get(vrepOfNode);
							assert (substitutedByNode != null);
						} else {
							removeCurrentNodeFromNodeSet = false;
							substitutedByNode = null;
						}

						vrepOfNodeIterator.remove();

					}

				}

				if (removeCurrentNodeFromNodeSet) {
					removedNodeSubstitutedByNewNodeMap.put(node, substitutedByNode);
					// nodeSetIterator.remove();
				} else {
					nodeSet.add(node);
					// add(node);
				}
		}

		/**
		 * add all new merged node elements to the nodeset & the rendered children set
		 */
		for (Map.Entry<PathwayVertex, NodeElement> newNodeElementEntry : newNodeElements.entrySet()) {
			
			NodeElement newNodeElement = newNodeElementEntry.getValue();
			graphToSaveNewElementsTo.addVertex(newNodeElement.getVertexRep());

			nodeSet.add(newNodeElement);
			// add(newNodeElement);
		}

		 for(NodeElement node : nodeSet) {
		 add(node);
		 }

	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		super.renderImpl(g, w, h);
	}

	@Override
	public Vec2f getMinSize() {
		return new Vec2f(100, 100);
	}

	public DynamicPathwayGraph getDynamicPathway() {
		return pathway;
	}

	@Override
	public Set<IFRLayoutNode> getNodeSet() {
		Set<IFRLayoutNode> interfaceSet = new HashSet<IFRLayoutNode>();

		for (NodeElement node : nodeSet) {
			interfaceSet.add((IFRLayoutNode) node);
		}

		return interfaceSet;
	}

	@Override
	public Set<IFRLayoutEdge> getEdgeSet() {
		return this.edgeSet;
	}

	/**
	 * if a node (wrapper for PathwayVertexRep) is selected, it is highlighted and the pathway list on the
	 * left is filtered by pathways, which contain this element
	 * 
	 * @param newSelectedNode
	 */
	public void setOrResetSelectedNode(NodeElement newSelectedNode) {
		/**
		 * if nothing was selected, just set the new node
		 */
		if (currentSelectedNode == null) {
			currentSelectedNode = newSelectedNode;
			currentSelectedNode.setIsNodeSelected(true);
		}

		/**
		 * if another node was selected before, deselect it and selected the new node
		 */
		else {
			currentSelectedNode.setIsNodeSelected(false);
			currentSelectedNode = newSelectedNode;
			currentSelectedNode.setIsNodeSelected(true);

		}

	}

	/**
	 * if a node (wrapper for PathwayVertexRep) is selected via the filtering command, it is highlighted and
	 * the pathway list on the left is filtered by pathways, which contain this element
	 * 
	 * @param newFilteringNode
	 *            the node, which the pathway list should be filtered with
	 */
	public void setOrResetFilteringNode(NodeElement newFilteringNode) {
		/**
		 * if nothing was selected, just set the new node
		 */
		if (currentFilteringNode == null) {
			currentFilteringNode = newFilteringNode;
			currentFilteringNode.setIsThisNodeUsedForFiltering(true);
		}

		else if (currentFilteringNode == newFilteringNode) {
			currentFilteringNode.setIsThisNodeUsedForFiltering(false);
			currentFilteringNode = null;
		}

		/**
		 * if another node was selected before, deselect it and selected the new node
		 */
		else {
			currentFilteringNode.setIsThisNodeUsedForFiltering(false);
			currentFilteringNode = newFilteringNode;
			currentFilteringNode.setIsThisNodeUsedForFiltering(true);

		}

	}

	public void filterPathwayList() {
		/**
		 * a new filter was added
		 */
		if (currentFilteringNode != null) {
			view.filterPathwayList(currentFilteringNode.getVertexRep());
		}
		// else {
		// view.unfilterPathwayList();
		// currentFilteringNode = null;
		// }

	}

	/**
	 * listens if the path should be filtered or not used for selecting a kontext pathway, which contains the
	 * requested vertex
	 * 
	 * @param event
	 */
	@ListenTo
	public void onFilterPathwayListByNodeElement(FilterPathwayListByVertexEvent event) {
		filterPathwayList();
	}

	public DynamicPathwayView getView() {
		return view;
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

	@Override
	public void notifyOfSelectionChange(EventBasedSelectionManager selectionManager) {
		repaint();

	}

	@Override
	protected void takeDown() {
		vertexSelectionManager.unregisterEventListeners();
		vertexSelectionManager = null;
		super.takeDown();
	}

	public NodeElement getCurrentFilteringNode() {
		return currentFilteringNode;
	}

	public void setCurrentFilteringNode(NodeElement currentFilteringNode) {
		this.currentFilteringNode = currentFilteringNode;
	}

}
