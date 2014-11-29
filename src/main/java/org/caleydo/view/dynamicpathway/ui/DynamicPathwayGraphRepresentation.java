/*******************************************************************************
 * Caleydo - Visualization for Molecular Biology - http://caleydo.org
 * Copyright (c) The Caleydo Team. All rights reserved.
 * Licensed under the new BSD license, available at http://caleydo.org/license
 ******************************************************************************/
package org.caleydo.view.dynamicpathway.ui;

import gleem.linalg.Vec2f;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.caleydo.core.util.collection.MultiHashMap;
import org.caleydo.core.util.collection.Pair;
import org.caleydo.core.util.color.Color;
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

	private List<Color> contextPathwaysColors = Arrays.asList(Color.LIGHT_RED, Color.GREEN, Color.ORANGE,
			Color.CYAN);

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

	/**
	 * needed for node merging & edge representation
	 */
	Map<PathwayVertex, NodeElement> uniqueVertexMap;
	
	List<NodeElement> globallyMergedNodesList;

	Map<PathwayGraph, PathwayGraph> originalPathwaysOfSubpathwaysMap;

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

		this.originalPathwaysOfSubpathwaysMap = new HashMap<PathwayGraph, PathwayGraph>();
		
		this.globallyMergedNodesList = new LinkedList<NodeElement>();

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
	 * @param vrepSetToAdd
	 *            TODO
	 * @param allowDuplicateVertices
	 *            only allowed for unmerged focus pathways
	 * 
	 */
	public void addPathwayRep(PathwayGraph graph, Boolean isFocusPathway, Boolean clearOriginalSubwaysMap) {

		pathway.addFocusOrKontextPathway(graph, !isFocusPathway, currentSelectedNode);

		Color nodeColor = isFocusPathway ? Color.LIGHT_BLUE : Color.LIGHT_GRAY;
		if (!isFocusPathway && pathway.getContextPathways().size() <= contextPathwaysColors.size())
			nodeColor = contextPathwaysColors.get(pathway.getContextPathways().indexOf(graph));

		view.addPathwayToControllBar(graph, isFocusPathway, nodeColor);

		// if you want to add a new focus graph
		if (isFocusPathway) {

			// clears all from past selection
			clearCanvasAndInfo(clearOriginalSubwaysMap);

			/**
			 * if duplicate are allowed or not
			 */
			if (removeDuplicateVertices == false) {
				focusGraphWithDuplicateVertices = true;
				addGraphWithDuplicates(graph, nodeSet, edgeSet);
			} else {
				
				List<NodeElement> mergedNodesList = new LinkedList<NodeElement>();
				focusGraphWithDuplicateVertices = false;
				addPathwayWithoutDuplicates(graph, uniqueVertexMap, mergedNodesList, nodeSet, edgeSet, true,
						pathway.getCombinedGraph(), nodeColor);

				try {
					checkForDuplicateVertices(nodeSet);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} else {

			// if the focus graph was added with duplicates, it needs to be added without them
			if (focusGraphWithDuplicateVertices) {
				PathwayVertex oldFilteringVertex = currentFilteringNode.getDisplayedVertex();

				removeDuplicateVertices = true;
				addPathwayRep(pathway.getFocusPathway(), true, true);

				currentFilteringNode = uniqueVertexMap.get(oldFilteringVertex);
			}

			// TODO: save old filtering node
			if (currentFilteringNode != null)
				setOrResetFilteringNode(currentFilteringNode);

			List<NodeElement> mergedNodesList = new LinkedList<NodeElement>();
			
			addPathwayWithoutDuplicates(graph, uniqueVertexMap, mergedNodesList, nodeSet, edgeSet, false,
					pathway.getCombinedGraph(), nodeColor);

		}
		
		// TODO: find better solution
		for(NodeElement node : nodeSet) 
			node.setIsMerged(false);

		view.unfilterPathwayList();

	}

	/**
	 * clears all sets, view & maps, so it can be set again
	 * 
	 * @param clearOriginalPathwaysMap
	 *            true if all information needs to be reset
	 */
	public void clearCanvasAndInfo(Boolean clearOriginalPathwaysMap) {

		// clear all selection
		currentSelectedNode = null;
		currentFilteringNode = null;

		// clear all sets -> might be reset again
		nodeSet.clear();
		edgeSet.clear();
		uniqueVertexMap.clear();

		if (clearOriginalPathwaysMap)
			originalPathwaysOfSubpathwaysMap.clear();

		// clear the canvas
		clear();

		view.unfilterPathwayList();
	}

	/**
	 * add this new pathway, but remove all duplicate PathwayVertices
	 * 
	 * @param pathwayToAdd
	 *            the new pathway to add
	 * @param vertexNodeMap
	 *            needed to check the uniqueness of the vertices within all the displayed pathways
	 * @param nodeSetToAdd
	 *            all displayed nodes are add to this set -> needed for layout algorithm
	 * @param edgeSetToAdd
	 *            all displayed edges are add to this set -> needed for layout algorithm
	 * @param addMergedNodesToSamePathway
	 *            if <b>false<b/> the merged nodes are added part of the combined pathway (i.e. create vertex
	 *            uniqueness within 2 or more pathways)<br />
	 *            if <b>true<b/> they are part of the pathwayToAdd (i.e. create vertex uniqueness within 1
	 *            pathway itself)
	 * @param combinedPathway
	 *            the pathway to add the merged nodes to (if addMergedNodesToSamePathway is false)
	 * @param nodeColor
	 *            the color of the created nodes
	 */
	private void addPathwayWithoutDuplicates(PathwayGraph pathwayToAdd,
			Map<PathwayVertex, NodeElement> vertexNodeMap, List<NodeElement> localMergedNodesList, Set<NodeElement> nodeSetToAdd,
			Set<EdgeElement> edgeSetToAdd, boolean addMergedNodesToSamePathway, PathwayGraph combinedPathway,
			Color nodeColor) {

		// for (PathwayVertexRep vrep : newGraph.vertexSet()) {
		for (Iterator<PathwayVertexRep> vRepIterator = pathwayToAdd.vertexSet().iterator(); vRepIterator
				.hasNext();) {
			PathwayVertexRep vrep = vRepIterator.next();

			if (vrep.getType() == EPathwayVertexType.map)
				continue;

			try {
				checkAndMergeNodes(pathwayToAdd, vrep, vertexNodeMap, localMergedNodesList, nodeSetToAdd,
						addMergedNodesToSamePathway, combinedPathway, nodeColor);
			} catch (NodeMergingException e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}

		}

		for (DefaultEdge edge : pathwayToAdd.edgeSet()) {
			try {
				GraphMergeUtil.addEdgeToEdgeSet(edge, pathwayToAdd, vertexNodeMap, edgeSet, this);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				System.exit(-1);
			}
		}

	}

	/**
	 * method checks if pathwayVertexRepToCheck's vertices already exist in the uniqueVertexMap, if so the
	 * surrounding nodes are merged, if not a new node for pathwayVertexRepToCheck is added
	 * 
	 * @param pathwayToAdd
	 *            the graph that should be added without duplicates
	 * @param vRepToCheck
	 *            check its vertices for duplicates within the uniqueVertexMap
	 * @throws NodeMergingException
	 *             internal error - tool didn't behave as expected
	 */
	private void checkAndMergeNodes(PathwayGraph pathwayToAdd, PathwayVertexRep vRepToCheck,
			Map<PathwayVertex, NodeElement> vertexNodeMap, List<NodeElement> localMergedNodesList, Set<NodeElement> nodeSetToAdd,
			boolean addToSameGraph, PathwayGraph combinedPathway, Color nodeColor) throws NodeMergingException {

		if (displayOnlyVerticesWithEdges && (pathwayToAdd.inDegreeOf(vRepToCheck) < 1)
				&& (pathwayToAdd.outDegreeOf(vRepToCheck) < 1))
			return;

		List<PathwayVertex> verticesToCheckList = new ArrayList<PathwayVertex>(
				vRepToCheck.getPathwayVertices());


		if (verticesToCheckList.size() < 1) {
			System.err.println("-----------------------------------------------------------");
			System.err.println("Vrep (" + vRepToCheck + ") doesn't have vertices");
			System.err.println("-----------------------------------------------------------");
			return;
		}

		/**
		 * ------------------------------------------------------------------------------------------------
		 * STEP 1:
		 * Get map with all duplicate nodes 
		 * (i.e. NodeElement that contain same vertices as in the pathway to add)
		 * ------------------------------------------------------------------------------------------------
		 */
		Map<NodeElement, List<PathwayVertex>> nodesWithSameVerticesMap = getNodeElementsContainingSameVertices(
				vertexNodeMap, verticesToCheckList);
		
		/**
		 * ------------------------------------------------------------------------------------------------
		 * STEP 2:
		 * Get list of all non duplicate vertices of verticesToCheckList 
		 * by removing all duplicate vertices
		 * ------------------------------------------------------------------------------------------------
		 */
		List<PathwayVertex> alreadyExistingPathwayVertexList = new ArrayList<PathwayVertex>();
		for (NodeElement nodeContainingDuplicateVertices : nodesWithSameVerticesMap.keySet())
			alreadyExistingPathwayVertexList.addAll(nodesWithSameVerticesMap
					.get(nodeContainingDuplicateVertices));
		List<PathwayVertex> nonDuplicateVertexList = new ArrayList<PathwayVertex>(verticesToCheckList);
		nonDuplicateVertexList.removeAll(alreadyExistingPathwayVertexList);

		if (nonDuplicateVertexList.size() > 0) {
			NodeElement node = GraphMergeUtil.createNewNodeElement(vRepToCheck,
					nonDuplicateVertexList, null, localMergedNodesList, this, nodeColor);
			nodeSetToAdd.add(node);
			add(node);

			for (PathwayVertex vertex : nonDuplicateVertexList) {
				vertexNodeMap.put(vertex, node);
			}
		}

		/**
		 * ------------------------------------------------------------------------------------------------
		 * STEP 3:
		 * Merge nodes that contain same vertices
		 * ------------------------------------------------------------------------------------------------
		 */
		for (Iterator<NodeElement> nodeWithDuplicateVerticesIter = nodesWithSameVerticesMap.keySet()
				.iterator(); nodeWithDuplicateVerticesIter.hasNext();) {
			NodeElement nodeWithDuplicateVertices = nodeWithDuplicateVerticesIter.next();

			List<PathwayVertex> sameVerticesList = nodesWithSameVerticesMap.get(nodeWithDuplicateVertices);
			PathwayVertexRep nodeVrep = nodeWithDuplicateVertices.getVertexRep();

			if (sameVerticesList.size() < 1) {
				throw new NodeMergingException("Node(" + nodeWithDuplicateVertices.getLabel()
						+ ") was added to nodesWithSameVerticesMap, but didn't contain same vertices");
			}

			/**
			 * ------------------------------------------------------------------------------------------------
			 * STEP 3.1a:
			 * If this node is not a merged one, it will be deleted & replaced by a merged node
			 * ------------------------------------------------------------------------------------------------
			 */
			if (!nodeWithDuplicateVertices.isMerged()) {
				
				boolean mergeWithinSameGraph = (vRepToCheck.getPathway().equals(nodeVrep.getPathway())) ? true : false;
				
				/**
				 * STEP 3.1a.1:
				 * Create a merged node element & remove the duplicate vertices from the existing node 
				 * (or the delete the existing node)
				 * ---------------------------------------------------------------------------------------------
				 */
				PathwayVertexRep mergedVrep = new PathwayVertexRep(sameVerticesList.get(0)
						.getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
						nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());
				/**
				 * get all common vertices between the two nodes
				 */
				for (PathwayVertex mergedVertex : sameVerticesList)
					mergedVrep.addPathwayVertex(mergedVertex);
//				if (mergeWithinSameGraph)
				if (addToSameGraph || mergeWithinSameGraph)
					mergedVrep.setPathway(pathwayToAdd);
				else 
					mergedVrep.setPathway(combinedPathway);
				
				combinedPathway.addVertex(mergedVrep);
				List<PathwayVertexRep> vreps = new LinkedList<PathwayVertexRep>();
				vreps.add(vRepToCheck);
				//TODO: add all vertices!!
				vreps.add(nodeWithDuplicateVertices.getVertexRep());
				
				List<PathwayVertexRep> vrepList = nodeWithDuplicateVertices.getVrepsWithThisNodesVerticesList();
				if(vrepList.size() > 0) {
					for(PathwayVertexRep vrep : vrepList) {
						if(!vreps.contains(vrep))
							vreps.add(vrep);
					}
				}
				Color mergedNodeColor;
				if(mergeWithinSameGraph)
					mergedNodeColor = nodeColor;
				else
					mergedNodeColor = determineMixedColor(nodeColor, nodeWithDuplicateVertices.getColor());
				
				NodeElement mergedNode = GraphMergeUtil.createNewNodeElement(mergedVrep, sameVerticesList,
						vreps, localMergedNodesList, this, mergedNodeColor);
				if(!addToSameGraph && mergeWithinSameGraph)
					mergedNode.setIsMerged(false);
				
				nodeSetToAdd.add(mergedNode);
				add(mergedNode);

				/**
				 * STEP 3.1a.2a:
				 * if the duplicate vertices are all of the (not merged) node's vertices, it needs to be
				 * deleted, because the new merged node took its purpose
				 * ---------------------------------------------------------------------------------------------
				 */
				if (nodeWithDuplicateVertices.getVertices().size() == sameVerticesList.size()) {
					
					boolean containedNode = nodeSetToAdd.remove(nodeWithDuplicateVertices);
					if (containedNode)
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

				} 
				
				/**
				 * STEP 3.1a.2b:
				 * remove all duplicate vertices of the existing node, since they are already in the new merged node
				 * ---------------------------------------------------------------------------------------------
				 */
				else {
					boolean containedNodes = nodeWithDuplicateVertices
							.removeMultipleVertices(sameVerticesList);

					if (containedNodes == false)
						throw new NodeMergingException("nodeWithDuplicateVertices("
								+ nodeWithDuplicateVertices
								+ ") didn't contain at leat one of sameVerticesList");
				}

				/**
				 * STEP 3.1a.3:
				 * add new vertices, node elements, replace old node (if it existed) with new merged
				 * ---------------------------------------------------------------------------------------------
				 */
				for (PathwayVertex sameVertex : sameVerticesList) {
					vertexNodeMap.put(sameVertex, mergedNode);
				}

			} 
			
			/**
			 * ------------------------------------------------------------------------------------------------
			 * STEP 3.1b:
			 * If the node to check with is already a merged node
			 * ------------------------------------------------------------------------------------------------
			 */
			else {
				List<PathwayVertex> nodeWithDuplicateVerticesList = nodeWithDuplicateVertices.getVertices();
				/**
				 * STEP 3.1b.1a:
				 * If the node to check with contains also vertices than are not common: <br />
				 * 1. Make new node for all these non duplicate vertices & remove them from the existing node <br />
				 * 2. link vreps of
				 * nodeWithDuplicateVertices.vreps to it & 
				 * 3. link current vrep to nodeWithDuplicateVertices
				 * ---------------------------------------------------------------------------------------------
				 */
				if (nodeWithDuplicateVerticesList.size() != sameVerticesList.size()) {

					List<PathwayVertex> nonDuplicateVerticesOfExistingNode = new LinkedList<PathwayVertex>();

					/**
					 * get all vertices that are not in sameVerticesList
					 */
					for (Iterator<PathwayVertex> nodeVerticesIter = nodeWithDuplicateVerticesList.iterator(); nodeVerticesIter
							.hasNext();) {
						PathwayVertex nodeVertex = nodeVerticesIter.next();

						if (!sameVerticesList.contains(nodeVertex)) {
							nonDuplicateVerticesOfExistingNode.add(nodeVertex);
							nodeWithDuplicateVerticesList.remove(nodeVertex);
							nodeWithDuplicateVertices
									.setDisplayedVertex(nodeWithDuplicateVerticesList.get(0));
						}
					}

					if (nonDuplicateVerticesOfExistingNode.size() < 1) {
						throw new NodeMergingException("splitOfMergedVertexList didn't contain elements");
					}

					PathwayVertexRep vrepOfNonDuplicateVertices = new PathwayVertexRep(nonDuplicateVerticesOfExistingNode.get(0)
							.getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
							nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());

					vrepOfNonDuplicateVertices.setPathway(nodeVrep.getPathway());

					List<PathwayVertexRep> vrepsOfNode = nodeWithDuplicateVertices
							.getVrepsWithThisNodesVerticesList();
					if (vrepsOfNode == null) {
						throw new NodeMergingException("nodeWithDuplicateVertices didn't contain Vreps");
					}
//					Color mergedColor = determineMixedColor(nodeColor, nodeWithDuplicateVertices.getColor());
					NodeElement newNodeForNonDuplicateVertices = GraphMergeUtil.createNewNodeElement(vrepOfNonDuplicateVertices,
							nonDuplicateVerticesOfExistingNode, vrepsOfNode,localMergedNodesList, this, nodeWithDuplicateVertices.getColor());
					
					for (PathwayVertex mergedVertex : nonDuplicateVerticesOfExistingNode) {
						vrepOfNonDuplicateVertices.addPathwayVertex(mergedVertex);
						vertexNodeMap.put(mergedVertex, newNodeForNonDuplicateVertices);
					}

					nodeSetToAdd.add(newNodeForNonDuplicateVertices);
					add(newNodeForNonDuplicateVertices);
					
//					nodeWithDuplicateVertices.addVrepWithThisNodesVerticesList(pathwayVertexRepToCheck);

				}
//				else {
				
					/**
					 * new vrep is added to the merged node
					 */
					nodeWithDuplicateVertices.addVrepWithThisNodesVerticesList(vRepToCheck);
					if (!addToSameGraph)
						nodeWithDuplicateVertices.getVertexRep().setPathway(combinedPathway);
//				}

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
	 * @param vrepSetToAdd
	 *            TODO
	 */
	private void addGraphWithDuplicates(PathwayGraph newGraph, Set<NodeElement> nodeSet,
			Set<EdgeElement> edgeSet) {

		Map<PathwayVertexRep, NodeElement> vrepToNodeElementMap = new HashMap<PathwayVertexRep, NodeElement>();

		for (PathwayVertexRep vrep : newGraph.vertexSet()) {
			if (vrep.getType() == EPathwayVertexType.map /** || vrep.getPathwayVertices().size() == 0 **/
			)
				continue;

			/**
			 * ignore 0° vertices, if the option was selected
			 */
			if (displayOnlyVerticesWithEdges && (newGraph.inDegreeOf(vrep) < 1)
					&& (newGraph.outDegreeOf(vrep) < 1))
				continue;

			NodeElement nodeElement = GraphMergeUtil.createNewNodeElement(vrep, vrep.getPathwayVertices(), null,
					globallyMergedNodesList, this, Color.LIGHT_BLUE);

			if (nodeElement == null) {
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

			if (sourceNode == null || targetNode == null) {
				System.err.println("Source (" + sourceNode + ") or Target Node (" + targetNode
						+ ") of edge (" + edge + ") not in map");
				continue;
			}

			EdgeElement edgeElement = new EdgeElement(edge, sourceNode, targetNode);

			edgeSet.add(edgeElement);
			add(edgeElement);
		}
	}

	/**
	 * debug method to check for uniqueness of vertices
	 * 
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

	private Color determineMixedColor(Color color1, Color color2) {
		float[] rgbColor1 = color1.getRGB();
		float[] rgbColor2 = color2.getRGB();

		float newR = (float) ((rgbColor1[0] + rgbColor2[0]) / 2.0);
		float newG = (float) ((rgbColor1[1] + rgbColor2[1]) / 2.0);
		float newB = (float) ((rgbColor1[2] + rgbColor2[2]) / 2.0);

		return (new Color(newR, newG, newB));
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
		return pathway.getFocusPathway();
	}

	public List<PathwayGraph> getContextGraphs() {
		return pathway.getContextPathways();
	}

	/**
	 * get the original pathway
	 * 
	 * @param subPathway
	 * @return
	 */
	public PathwayGraph getOriginalPathwaysOfSubpathway(PathwayGraph subPathway) {
		return originalPathwaysOfSubpathwaysMap.get(subPathway);
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
	 * removes a pair from the map
	 * 
	 * @param subPathway
	 */
	public void removeOriginalPathwayAndSubpathwayOfMap(PathwayGraph subPathway) {
		this.originalPathwaysOfSubpathwaysMap.remove(subPathway);
	}

	/**
	 * checks whether the given pathway is a sub pathway (limited to a certain node range) or a full pathway
	 * graph
	 * 
	 * @param subPathway
	 *            the pathway to check
	 * @return true if the given pathway is a sub pathway, false otherwise
	 */
	public boolean isSubPathway(PathwayGraph subPathway) {
		return this.originalPathwaysOfSubpathwaysMap.containsKey(subPathway);
	}

	// public void clearOriginalPathwaysOfSubpathwaysMap() {
	// this.originalPathwaysOfSubpathwaysMap.clear();
	// }
}
