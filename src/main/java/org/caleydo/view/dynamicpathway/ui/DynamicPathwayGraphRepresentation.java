/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.caleydo.core.data.selection.EventBasedSelectionManager;
import org.caleydo.core.data.selection.IEventBasedSelectionManagerUser;
import org.caleydo.core.data.selection.SelectionType;
import org.caleydo.core.event.EventListenerManager.ListenTo;
import org.caleydo.core.id.IDType;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.genetic.EGeneIDTypes;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexGroupRep;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwayView;
import org.caleydo.view.dynamicpathway.internal.NodeMergingException;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutGraph;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.caleydo.view.dynamicpathway.util.GraphMergeUtil;
import org.jgrapht.graph.DefaultEdge;

/**
 * Container, which is defined by the graph layout {@link GLFruchtermanReingoldLayout} contains the renderable
 * Elements
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayGraphRepresentation extends AnimatedGLElementContainer implements IFRLayoutGraph,
		IEventBasedSelectionManagerUser {

	private boolean displayOnlyVerticesWithEdges = true;
	private boolean removeDuplicateVertices = true;
	private boolean focusGraphWithDuplicateVertices = false;

	private static final String MERGE_LOG_FILE_PATH = "/home/chaoscause/Documents/Sem_6/Bakk/Project/logs/mergeLog.log";

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

	Map<PathwayVertex, NodeElement> uniqueVertexMap;

	Logger mergeLogger = Logger.getLogger("MergeLog");

	public DynamicPathwayGraphRepresentation(GLFruchtermanReingoldLayout layout, DynamicPathwayView view) {

		this.pathway = new DynamicPathwayGraph();

		this.nodeSet = new HashSet<NodeElement>();
		this.edgeSet = new HashSet<EdgeElement>();

		this.view = view;

		this.vertexSelectionManager = new EventBasedSelectionManager(this,
				IDType.getIDType(EGeneIDTypes.PATHWAY_VERTEX.name()));
		this.vertexSelectionManager.registerEventListeners();

		this.uniqueVertexMap = new HashMap<PathwayVertex, NodeElement>();

		setLayout(layout);
		
		setDefaultDuration(1500);

	}

	/**
	 * 
	 * @param graph
	 *            if a new pathway was added, a new combined (focus + parts of kontext pathways) pathway is
	 *            created
	 * @param isFocusPathway
	 *            true if a kontext pathway should be added, false if a focus pathway should be added, if
	 *            null, it is defined later
	 * @param allowDuplicateVertices
	 *            only allowed for unmerged focus pathways
	 * 
	 */
	public void addPathwayRep(PathwayGraph graph, Boolean isFocusPathway) {

		/**
		 * if a node is selected & another pathway was selected, this has to be a kontextpathway
		 */
//		Boolean addKontextPathway;
////		if (isFocusPathway == null)
////			addKontextPathway = (pathway.isFocusGraphSet() && (currentFilteringNode != null)) ? true : false;
////		else
//			addKontextPathway = !isFocusPathway;

		pathway.addFocusOrKontextPathway(graph, !isFocusPathway, currentSelectedNode);
		
		view.addPathwayToControllBar(graph, isFocusPathway);

		// if you want to add a new focus graph
		if (isFocusPathway) {
			
			// clears all from past selection
			clearCanvasAndInfo();		
			

			/** 
			 * if duplicate are allowed or not
			 */
			if (removeDuplicateVertices == false) {
				focusGraphWithDuplicateVertices = true;
				addGraphWithDuplicates(graph, nodeSet, edgeSet);
			} else {
				focusGraphWithDuplicateVertices = false;
				addGraphWithoutDuplicates(graph, uniqueVertexMap, nodeSet, edgeSet, true,
						pathway.getCombinedGraph());

				try {
					checkForDuplicateVertices(nodeSet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {
			
			// if the focus graph was added with duplicates, it needs to be added without them
			if(focusGraphWithDuplicateVertices) {
				PathwayVertex oldFilteringVertex = currentFilteringNode.getDisplayedVertex();
				
				removeDuplicateVertices = true;
				addPathwayRep(pathway.getFocusGraph(), true);
				
				currentFilteringNode = uniqueVertexMap.get(oldFilteringVertex);
			}
			
//			clear();
			
			//TODO: save old filtering node
			if(currentFilteringNode != null)
				setOrResetFilteringNode(currentFilteringNode);

			addGraphWithoutDuplicates(graph, uniqueVertexMap, nodeSet, edgeSet, false,
					pathway.getCombinedGraph());

		}

	}
	
	/**
	 * clears all sets, view & maps, so it can be set again
	 */
	public void clearCanvasAndInfo() {
		// clear all selection
		currentSelectedNode = null;
		currentFilteringNode = null;	

		// clear all sets -> might be reset again
		nodeSet.clear();
		edgeSet.clear();
		uniqueVertexMap.clear();

		// clear the canvas
		clear();	
		
		//TODO: doesn't work for removing the focus pathway
		// unfilter the pathway list
		view.unfilterPathwayList();
	}

	/**
	 * add this new graph, but remove all duplicate PathwayVertices
	 * 
	 * @param newGraph
	 */
	private void addGraphWithoutDuplicates(PathwayGraph newGraph,
			Map<PathwayVertex, NodeElement> vertexNodeMap, Set<NodeElement> nodeSetToAdd,
			Set<EdgeElement> edgeSetToAdd, boolean addToSameGraph, PathwayGraph combinedGraph) {

		for (PathwayVertexRep vrep : newGraph.vertexSet()) {
			if (vrep.getType() == EPathwayVertexType.map)
				continue;

			try {
				checkAndMergeNodes(newGraph, vrep, vertexNodeMap, nodeSetToAdd, addToSameGraph, combinedGraph);
				// GraphMergeUtil.checkAndMergeNodes(newGraph, vrep, vertexNodeMap, nodeSetToAdd,
				// edgeSetToAdd, addToSameGraph, combinedGraph, this);
			} catch (NodeMergingException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}

		}

		for (DefaultEdge edge : newGraph.edgeSet()) {
			try {
				GraphMergeUtil.addEdgeToEdgeSet(edge, newGraph, vertexNodeMap, edgeSet, this);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}

//		for (NodeElement node : nodeSetToAdd)
//			add(node);

//		for (EdgeElement edge : edgeSet)
//			add(edge);
	}

	/**
	 * method checks if pathwayVertexRepToCheck's vertices already exist in the uniqueVertexMap, if so the
	 * surrounding nodes are merged, if not a new node for pathwayVertexRepToCheck is added
	 * 
	 * @param graphToAdd
	 *            the graph that should be added without duplicates
	 * @param pathwayVertexRepToCheck
	 *            check its vertices for duplicates within the uniqueVertexMap
	 * @throws NodeMergingException
	 *             internal error - tool didn't behave as expected
	 */
	private void checkAndMergeNodes(PathwayGraph graphToAdd, PathwayVertexRep pathwayVertexRepToCheck,
			Map<PathwayVertex, NodeElement> vertexNodeMap, Set<NodeElement> nodeSetToAdd,
			boolean addToSameGraph, PathwayGraph combinedGraph) throws NodeMergingException {

		if (displayOnlyVerticesWithEdges && (graphToAdd.inDegreeOf(pathwayVertexRepToCheck) < 1)
				&& (graphToAdd.outDegreeOf(pathwayVertexRepToCheck) < 1))
			return;

		List<PathwayVertex> verticesToCheckList = new ArrayList<PathwayVertex>(
				pathwayVertexRepToCheck.getPathwayVertices());

		// printUniqueVertexMap();

		if (verticesToCheckList.size() < 1) {
			System.err.println("-----------------------------------------------------------");
			System.err.println("Vrep (" + pathwayVertexRepToCheck + ") doesn't have vertices");
			System.err.println("-----------------------------------------------------------");
			return;
		}

		/**
		 * get map with all duplicate nodes
		 */
		Map<NodeElement, List<PathwayVertex>> nodesWithSameVerticesMap = getNodeElementsContainingSameVertices(
				vertexNodeMap, verticesToCheckList);
		// if (nodesWithSameVerticesMap.size() > 0)
		// mergeLogger.info("nodesWithSameVerticesMap for " + pathwayVertexRepToCheck.getShortName()
		// + ": \n" + nodesWithSameVerticesMap + "\n--------------------------------------");

		/**
		 * get list of all non duplicate vertices of verticesToCheckList by removing all duplicate vertices
		 */
		List<PathwayVertex> alreadyExistingPathwayVertexList = new ArrayList<PathwayVertex>();
		for (NodeElement nodeContainingDuplicateVertices : nodesWithSameVerticesMap.keySet())
			alreadyExistingPathwayVertexList.addAll(nodesWithSameVerticesMap
					.get(nodeContainingDuplicateVertices));
		List<PathwayVertex> nonDuplicateVertexList = new ArrayList<PathwayVertex>(verticesToCheckList);
		nonDuplicateVertexList.removeAll(alreadyExistingPathwayVertexList);

		if (nonDuplicateVertexList.size() > 0) {
			NodeElement node = GraphMergeUtil.addNewNodeElement(pathwayVertexRepToCheck, nonDuplicateVertexList, null, this);
			nodeSetToAdd.add(node);
			add(node);

			for (PathwayVertex vertex : nonDuplicateVertexList) {
				vertexNodeMap.put(vertex, node);
			}
		}

		/**
		 * merge with nodes that contain same vertices
		 */
//		for (NodeElement nodeWithDuplicateVertices : nodesWithSameVerticesMap.keySet()) {
		for (Iterator<NodeElement> nodeWithDuplicateVerticesIter = nodesWithSameVerticesMap.keySet().iterator(); nodeWithDuplicateVerticesIter
					.hasNext();) {
			NodeElement nodeWithDuplicateVertices = nodeWithDuplicateVerticesIter.next();
			
			List<PathwayVertex> sameVerticesList = nodesWithSameVerticesMap.get(nodeWithDuplicateVertices);
			PathwayVertexRep nodeVrep = nodeWithDuplicateVertices.getVertexRep();

			if (sameVerticesList.size() < 1) {
				throw new NodeMergingException("Node(" + nodeWithDuplicateVertices.getLabel()
						+ ") was added to nodesWithSameVerticesMap, but didn't contain same vertices");
			}

			/**
			 * if this element is not a merged one, it will be deleted
			 */
			if (!nodeWithDuplicateVertices.isMerged()) {
				PathwayVertexRep mergedVrep = new PathwayVertexRep(sameVerticesList.get(0)
						.getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
						nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());
				for (PathwayVertex mergedVertex : sameVerticesList)
					mergedVrep.addPathwayVertex(mergedVertex);

				if (addToSameGraph)
					mergedVrep.setPathway(graphToAdd);
				else
					mergedVrep.setPathway(combinedGraph);
				combinedGraph.addVertex(mergedVrep);
				List<PathwayVertexRep> vreps = new LinkedList<PathwayVertexRep>();
				vreps.add(pathwayVertexRepToCheck);
				vreps.add(nodeWithDuplicateVertices.getVertexRep());
				NodeElement mergedNode = GraphMergeUtil.addNewNodeElement(mergedVrep, sameVerticesList, vreps, this);
				nodeSetToAdd.add(mergedNode);
				add(mergedNode);

				/**
				 * if the duplicate vertices are all of the (not merged) node's vertices, it needs to be
				 * deleted, because the merged node took its purpose
				 */
				if (nodeWithDuplicateVertices.getVertices().size() == sameVerticesList.size()) {

					boolean containedNode = nodeSetToAdd.remove(nodeWithDuplicateVertices);
					if(containedNode)
						remove(nodeWithDuplicateVertices);

					List<Pair<EdgeElement, Boolean>> edgesContainingThisNode = GraphMergeUtil
							.getEdgeWithThisNodeAsSourceOrTarget(edgeSet, nodeWithDuplicateVertices);
					for (Pair<EdgeElement, Boolean> edgePair : edgesContainingThisNode) {
						// node was edge source
						if (edgePair.getSecond())
							edgePair.getFirst().setSourceNode(mergedNode);
						else
							edgePair.getFirst().setTargetNode(mergedNode);
					}

					if (containedNode == false)
						throw new NodeMergingException("nodeSet didn't contain node("
								+ nodeWithDuplicateVertices + ") to remove");

				} else {
					boolean containedNodes = nodeWithDuplicateVertices
							.removeMultipleVertices(sameVerticesList);

					if (containedNodes == false)
						throw new NodeMergingException("nodeWithDuplicateVertices("
								+ nodeWithDuplicateVertices
								+ ") didn't contain at leat one of sameVerticesList");
				}

				/**
				 * add new vertices, node elements, replace old node with new merged
				 */
				for (PathwayVertex sameVertex : sameVerticesList) {
					vertexNodeMap.put(sameVertex, mergedNode);
				}

			} // if the node element to check with is already a merged node
			else {
				List<PathwayVertex> nodeWithDuplicateVerticesList = nodeWithDuplicateVertices.getVertices();
				/**
				 * if sameVerticesList is subset of nodeWithDuplicateVertices.getVertices(), make new node for
				 * all vertices, which are not in sameVerticesList, link vreps of
				 * nodeWithDuplicateVertices.vreps to it & link current vrep to nodeWithDuplicateVertices
				 */
				if (nodeWithDuplicateVerticesList.size() != sameVerticesList.size()) {

					List<PathwayVertex> splitOfMergedVertexList = new LinkedList<PathwayVertex>();

					/**
					 * get all vertices that are not in sameVerticesList
					 */
					for (Iterator<PathwayVertex> nodeVerticesIter = nodeWithDuplicateVerticesList.iterator(); nodeVerticesIter
							.hasNext();) {
						PathwayVertex nodeVertex = nodeVerticesIter.next();

						if (!sameVerticesList.contains(nodeVertex)) {
							splitOfMergedVertexList.add(nodeVertex);
							nodeWithDuplicateVerticesList.remove(nodeVertex);
							nodeWithDuplicateVertices
									.setDisplayedVertex(nodeWithDuplicateVerticesList.get(0));
						}
					}

					if (splitOfMergedVertexList.size() < 1) {
						throw new NodeMergingException("splitOfMergedVertexList didn't contain elements");
					}

					PathwayVertexRep mergedVrep = new PathwayVertexRep(splitOfMergedVertexList.get(0)
							.getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
							nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());

					mergedVrep.setPathway(pathway.getCombinedGraph());

					List<PathwayVertexRep> vrepsOfNode = nodeWithDuplicateVertices
							.getVrepsWithThisNodesVerticesList();
					if (vrepsOfNode == null) {
						throw new NodeMergingException("nodeWithDuplicateVertices didn't contain Vreps");
					}
					NodeElement mergedNode = GraphMergeUtil.addNewNodeElement(mergedVrep, splitOfMergedVertexList,
							vrepsOfNode, this);

					for (PathwayVertex mergedVertex : splitOfMergedVertexList) {
						mergedVrep.addPathwayVertex(mergedVertex);
						vertexNodeMap.put(mergedVertex, mergedNode);
					}

					nodeSetToAdd.add(mergedNode);
					add(mergedNode);

				} else {

					nodeWithDuplicateVertices.addVrepWithThisNodesVerticesList(pathwayVertexRepToCheck);
				}

			}

		}

		return;
	}

	/**
	 * checks which vertices of verticesToCheckList already exist in {@link #uniqueVertexMap}
	 * 
	 * @param verticesToCheckList
	 *            check if these vertices already exist
	 * @return Map<NodeElement, List<PathwayVertex>>: NodeElement - this NodeElement has vertices duplicate to
	 *         some in verticesToCheckList, List &lt;PathwayVertex&gt;: all vertices of NodeElement, that are
	 *         also in verticesToCheckList
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

	/**
	 * represent graph biologically correct, with duplicates <br />
	 * used when only one graph is displayed
	 * 
	 * @param newGraph
	 */
	private void addGraphWithDuplicates(PathwayGraph newGraph, Set<NodeElement> nodeSet,
			Set<EdgeElement> edgeSet) {
		
		Map<PathwayVertexRep, NodeElement> vrepToNodeElementMap = new HashMap<PathwayVertexRep, NodeElement>();

		for (PathwayVertexRep vrep : newGraph.vertexSet()) {
			if (vrep.getType() == EPathwayVertexType.map || vrep.getPathwayVertices().size() == 0)
				continue;
			
			/**
			 * ignore 0° vertices, if the option was selected
			 */
			if (displayOnlyVerticesWithEdges && (newGraph.inDegreeOf(vrep) < 1)
					&& (newGraph.outDegreeOf(vrep) < 1))
				continue;

			NodeElement nodeElement = GraphMergeUtil.addNewNodeElement(vrep, vrep.getPathwayVertices(), null, this);
			
			if(nodeElement == null) {
				System.err.println("Node creation of vrep " + vrep + "failed");
				continue;
			}
			
			nodeSet.add(nodeElement);
			vrepToNodeElementMap.put(vrep, nodeElement);
			add(nodeElement);
		}

		for (DefaultEdge edge : newGraph.edgeSet()) {
			NodeElement sourceNode = vrepToNodeElementMap.get(newGraph.getEdgeSource(edge));
			NodeElement targetNode = vrepToNodeElementMap.get(newGraph.getEdgeTarget(edge));
			
			if(sourceNode == null || targetNode == null) {
				System.err.println("Source ("+sourceNode+") or Target Node ("+targetNode+") of edge ("+edge+") not in map");
				continue;
			}

			EdgeElement edgeElement = new EdgeElement(edge, sourceNode, targetNode);

			edgeSet.add(edgeElement);
			add(edgeElement);
		}
	}

	
	/**
	 * debug method to check for uniqueness of vertices
	 * @param nodeSetToCheck
	 * @throws Exception
	 */
	private void checkForDuplicateVertices(Set<NodeElement> nodeSetToCheck) throws Exception {
		for (NodeElement nodeToCheck : nodeSetToCheck) {
			List<PathwayVertex> verticesToCheck = nodeToCheck.getVertices();

			if (nodeToCheck.getType() != EPathwayVertexType.gene)
				continue;

			for (PathwayVertex vertexToCheck : verticesToCheck) {

				for (NodeElement nodeToCheckAgainst : nodeSetToCheck) {
					if (nodeToCheck.equals(nodeToCheckAgainst))
						continue;

					for (PathwayVertex vertexToCheckAgainst : nodeToCheckAgainst.getVertices()) {
						if (vertexToCheck.getHumanReadableName().contentEquals(
								vertexToCheckAgainst.getHumanReadableName()))
							throw new Exception("Node (" + nodeToCheck.getLabel()
									+ ") contained same vertex (" + vertexToCheck.getHumanReadableName()
									+ ") as node (" + nodeToCheckAgainst.getLabel() + ")");
					}
				}

			}
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
		Set<IFRLayoutEdge> interfaceSet = new HashSet<IFRLayoutEdge>();

		for (EdgeElement edge : edgeSet) {
			interfaceSet.add((IFRLayoutEdge) edge);
		}

		return interfaceSet;
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

	public boolean isDisplayOnlyVerticesWithEdges() {
		return displayOnlyVerticesWithEdges;
	}

	public void setDisplayOnlyVerticesWithEdges(boolean displayOnlyVerticesWithEdges) {
		this.displayOnlyVerticesWithEdges = displayOnlyVerticesWithEdges;
	}


	public boolean isRemoveDuplicateVertices() {
		return removeDuplicateVertices;
	}

	public void setRemoveDuplicateVertices(boolean removeUniqueVertices) {
		this.removeDuplicateVertices = removeUniqueVertices;
	}
	
	public PathwayGraph getFocusGraph() {
		return pathway.getFocusGraph();
	}

	public List<PathwayGraph> getKontextGraphs() {
		return pathway.getKontextGraphs();
	}
}
