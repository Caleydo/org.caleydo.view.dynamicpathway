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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
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
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.genetic.EGeneIDTypes;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexShape;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwayView;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayGraph;
import org.caleydo.view.dynamicpathway.layout.GLFruchtermanReingoldLayout;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutEdge;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutGraph;
import org.caleydo.view.dynamicpathway.layout.IFRLayoutNode;
import org.jgrapht.graph.DefaultEdge;

import com.google.common.collect.Lists;
import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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

	private Map<PathwayVertex, List<NodeElement>> preventDuplicatesMap;

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

		this.preventDuplicatesMap = new HashMap<PathwayVertex, List<NodeElement>>();

		this.uniqueVertexMap = new HashMap<PathwayVertex, NodeElement>();

		setLayout(layout);

		setLoggerToWriteFile(mergeLogger, MERGE_LOG_FILE_PATH);

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

		// if you want to add a new focus graph
		if (!addKontextPathway) {
			currentSelectedNode = null;
			currentFilteringNode = null;
			view.unfilterPathwayList();

			nodeSet.clear();
			edgeSet.clear();
			preventDuplicatesMap.clear();
			uniqueVertexMap.clear();

			clear();

			// for (PathwayVertexRep vrep : pathway.getCombinedVertexSet()) {
			// if (DISPLAY_ONLY_VERTICES_WITH_EDGES) {
			// if (pathway.getCombinedGraph().inDegreeOf(vrep) <= 0
			// && pathway.getCombinedGraph().outDegreeOf(vrep) <= 0) {
			// System.out.println("ignoring: " + vrep.getShortName());
			// continue;
			// }

			addGraphWithoutDuplicates(graph);
			// addGraphWithDuplicates(graph);

			try {
				checkForDuplicateVertices();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			setOrResetFilteringNode(currentFilteringNode);
			// for(PathwayVertexRep vrep : graph.vertexSet()) {
			// if (DISPLAY_ONLY_VERTICES_WITH_EDGES) {
			// if (graph.inDegreeOf(vrep) <= 0
			// && graph.outDegreeOf(vrep) <= 0) {
			// System.out.println("ignoring: " + vrep.getShortName());
			// continue;
			// }
			// }

			int index = pathway.getKontextGraphs().indexOf(graph);

			assert (index != -1);

			addGraphWithoutDuplicatesOld(pathway.getKontextGraphs().get(index));

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

	private NodeElement addNewNodeElement(PathwayVertexRep vrep, List<PathwayVertex> pathwayVertices,
			List<PathwayVertexRep> vrepsWithThisNodesVertices) {
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
		 * if this node contains vertices from 2 or more PathwayVertexReps, i.e. it's a merged node
		 */
		if (pathwayVertices != null) {

			node.setVertices(pathwayVertices);

			if (vrepsWithThisNodesVertices != null) {
				node.setVrepsWithThisNodesVerticesList(vrepsWithThisNodesVertices);
				node.setIsMerged(true);
			}
		}

		/**
		 * needed for the edge, because edges just get you the vertexRep of the source & target vertices, but
		 * not the element, which contain the new position
		 */
		pathway.addVertexNodeMapEntry(vrep, node);

		return node;

	}

	/**
	 * add this new graph, but remove all duplicate PathwayVertices
	 * 
	 * @param newGraph
	 */
	private void addGraphWithoutDuplicates(PathwayGraph newGraph) {
		/**
		 * contains information, which PathwayVertex belongs to which NodeElement
		 */
		// Map<PathwayVertex, NodeElement> uniqueVertexMap = new HashMap<PathwayVertex, NodeElement>();

		/**
		 * needed for creating edges correctly (only contain information about original PathwayVertexRep - not
		 * merged; contains information, where to redirect edges
		 */
		Map<PathwayVertexRep, List<NodeElement>> vrepNodeMap = new HashMap<PathwayVertexRep, List<NodeElement>>();

		for (PathwayVertexRep vrep : newGraph.vertexSet()) {
			if (vrep.getType() == EPathwayVertexType.map)
				continue;

			vrepNodeMap = checkAndMergeNodes(newGraph, vrep, vrepNodeMap);

		}

		for (DefaultEdge edge : newGraph.edgeSet()) {
			try {
				addEdgeToEdgeSet(edge, newGraph, vrepNodeMap);
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}
		}

		mergeLogger.info("vrepNodeMap: " + vrepNodeMap.toString());

		for (NodeElement node : nodeSet)
			add(node);

		for (EdgeElement edge : edgeSet)
			add(edge);
	}

	private void addEdgeToEdgeSet(DefaultEdge edge, PathwayGraph pathway,
			Map<PathwayVertexRep, List<NodeElement>> vrepNodeMap) throws Exception {
		PathwayVertexRep srcVrep = pathway.getEdgeSource(edge);
		PathwayVertexRep targetVrep = pathway.getEdgeTarget(edge);
		
		System.out.println("Src vrep type: " + srcVrep.getType() + " target vrep type: " + targetVrep.getType());

		if(srcVrep.getParent() != null)
			System.out.println("src vrep parent: " + srcVrep.getParent());
		
		if(targetVrep.getParent() != null)
			System.out.println("target vrep parent: " + targetVrep.getParent());
		
		if(!vrepNodeMap.containsKey(srcVrep))
			System.out.println("vrepNodeMap does not contain srcVrep " + srcVrep.getName());
		
		if(!vrepNodeMap.containsKey(targetVrep))
			System.out.println("vrepNodeMap does not contain targetVrep " + targetVrep.getName());
		

		List<NodeElement> srcNodes = vrepNodeMap.get(srcVrep);
		List<NodeElement> targetNodes = vrepNodeMap.get(targetVrep);

		if (srcNodes == null) {
			System.out.println("fasel");
			throw new Exception("no src nodes to redirect. Edge " + edge + " Target nodes: " + targetNodes);
		}
		if (targetNodes == null) {
			System.out.println("bla");
			throw new Exception("no target nodes to redirect. Edge " + edge + " Src nodes: " + srcNodes);
		}

		for (NodeElement srcNode : srcNodes) {
			for (NodeElement targetNode : targetNodes) {
				EdgeElement edgeElement = new EdgeElement(edge, srcNode, targetNode);
				edgeSet.add(edgeElement);
			}
		}
	}

	/**
	 * method checks if pathwayVertexRepToCheck's vertices already exist in the uniqueVertexMap, if so the
	 * surrounding nodes are merged, if not a new node for pathwayVertexRepToCheck is added
	 * 
	 * @param graphToAdd
	 *            the graph that should be added without duplicates
	 * @param pathwayVertexRepToCheck
	 *            check its vertices for duplicates within the uniqueVertexMap
	 * @return map that contains information, where to redirect edges; needed for creating edges correctly
	 *         (only contain information about original PathwayVertexRep - not merged;
	 */
	private Map<PathwayVertexRep, List<NodeElement>> checkAndMergeNodes(PathwayGraph graphToAdd,
			PathwayVertexRep pathwayVertexRepToCheck, Map<PathwayVertexRep, List<NodeElement>> vrepNodeMap) {

		 List<PathwayVertex> verticesToCheckList = pathwayVertexRepToCheck.getPathwayVertices();
		/**
		 * just for debugging: remove vertices with duplicate names
		 */
//		List<PathwayVertex> verticesToCheckList = filterVerticesByName(pathwayVertexRepToCheck
//				.getPathwayVertices());

		String debug = "filtered verticesToCheckList: \n[";
		for (PathwayVertex printVertex : verticesToCheckList)
			debug += printVertex.getHumanReadableName() + ", ";
		debug += "]\n -----------------------------------------";
		mergeLogger.info(debug);

		/**
		 * get map with all duplicate nodes
		 */
		Map<NodeElement, List<PathwayVertex>> nodesWithSameVerticesMap = getNodeElementsContainingSameVertices(verticesToCheckList);
		if (nodesWithSameVerticesMap.size() > 0)
			mergeLogger.info("nodesWithSameVerticesMap for " + pathwayVertexRepToCheck.getShortName()
					+ ": \n" + nodesWithSameVerticesMap + "\n--------------------------------------");

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
			NodeElement node = addNewNodeElement(pathwayVertexRepToCheck, nonDuplicateVertexList, null);
			nodeSet.add(node);

			for (PathwayVertex vertex : nonDuplicateVertexList) {
				uniqueVertexMap.put(vertex, node);
			}

			addNodeToVrepNodeMap(vrepNodeMap, pathwayVertexRepToCheck, node);
		}

		/**
		 * merge with nodes that contain same vertices
		 */
		for (NodeElement nodeWithDuplicateVertices : nodesWithSameVerticesMap.keySet()) {
			List<PathwayVertex> sameVerticesList = nodesWithSameVerticesMap.get(nodeWithDuplicateVertices);
			PathwayVertexRep nodeVrep = nodeWithDuplicateVertices.getVertexRep();

			assert (sameVerticesList.size() > 0);

			/**
			 * if this element is not a merged one, it will be deleted
			 */
			if (!nodeWithDuplicateVertices.isMerged()) {
				PathwayVertexRep mergedVrep = new PathwayVertexRep(sameVerticesList.get(0)
						.getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
						nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());
				for (PathwayVertex mergedVertex : sameVerticesList)
					mergedVrep.addPathwayVertex(mergedVertex);
				// mergedVrep.setPathway(graphToAdd);
				mergedVrep.setPathway(pathway.getCombinedGraph());
				pathway.getCombinedGraph().addVertex(mergedVrep);
				List<PathwayVertexRep> vreps = new LinkedList<PathwayVertexRep>();
				vreps.add(pathwayVertexRepToCheck);
				vreps.add(nodeWithDuplicateVertices.getVertexRep());
				NodeElement mergedNode = addNewNodeElement(mergedVrep, sameVerticesList, vreps);
				nodeSet.add(mergedNode);

				/**
				 * if the duplicate vertices are all of the (not merged) node's vertices, it needs to be
				 * deleted, because the merged node took its purpose
				 */
				if (nodeWithDuplicateVertices.getVertices().size() == sameVerticesList.size()) {
					List<NodeElement> edgeRedirectionList = vrepNodeMap.get(nodeVrep);
					boolean containedNode = edgeRedirectionList.remove(nodeWithDuplicateVertices);
					assert (containedNode == true);

					containedNode = nodeSet.remove(nodeWithDuplicateVertices);
					assert (containedNode == true);
				} else {
					boolean containedNodes = nodeWithDuplicateVertices
							.removeMultipleVertices(sameVerticesList);
					assert (containedNodes == true);
				}

				/**
				 * add new vertices, node elements, replace old node with new merged
				 */
				for (PathwayVertex sameVertex : sameVerticesList) {
					uniqueVertexMap.put(sameVertex, mergedNode);
				}
				vrepNodeMap.get(nodeVrep).add(mergedNode);
				List<NodeElement> mergedNodes = new LinkedList<NodeElement>();
				mergedNodes.add(mergedNode);
				vrepNodeMap.put(mergedVrep, mergedNodes);

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
						}
					}

					assert (splitOfMergedVertexList.size() > 0);

					PathwayVertexRep mergedVrep = new PathwayVertexRep(splitOfMergedVertexList.get(0)
							.getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
							nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());

					// mergedVrep.setPathway(graphToAdd);
					mergedVrep.setPathway(pathway.getCombinedGraph());

					List<PathwayVertexRep> vrepsOfNode = nodeWithDuplicateVertices
							.getVrepsWithThisNodesVerticesList();
					assert (vrepsOfNode != null);
					NodeElement mergedNode = addNewNodeElement(mergedVrep, splitOfMergedVertexList,
							vrepsOfNode);

					for (PathwayVertex mergedVertex : splitOfMergedVertexList) {
						mergedVrep.addPathwayVertex(mergedVertex);
						uniqueVertexMap.put(mergedVertex, mergedNode);
					}

					nodeSet.add(mergedNode);

					// for(PathwayVertexRep vrepOfNode : vrepsOfNode)
					// vrepNodeMap.get(vrepOfNode).add(mergedNode);
					for (PathwayVertexRep vrepOfNode : vrepsOfNode)
						addNodeToVrepNodeMap(vrepNodeMap, vrepOfNode, mergedNode);

				} else {
					/**
					 * all of node's vertices are a subset of vrep, so just add it to the vrep-node entry
					 */
					addNodeToVrepNodeMap(vrepNodeMap, pathwayVertexRepToCheck, nodeWithDuplicateVertices);
					nodeWithDuplicateVertices.addVrepWithThisNodesVerticesList(pathwayVertexRepToCheck);
				}

			}

		}

		return vrepNodeMap;
	}

	private List<PathwayVertex> filterVerticesByName(List<PathwayVertex> vertexListToFilter) {
		List<PathwayVertex> filteredList = new LinkedList<PathwayVertex>();

		for (PathwayVertex vertexToCheck : vertexListToFilter) {

			boolean alreadyContainsName = false;
			for (PathwayVertex checkedVertex : filteredList) {
				String checkWith = checkedVertex.getHumanReadableName();
				String check = vertexToCheck.getHumanReadableName();
				if (checkWith.contentEquals(check)) {
					alreadyContainsName = true;
					break;
				}
			}

			if (!alreadyContainsName) {
				filteredList.add(vertexToCheck);
			}
		}

		return filteredList;
	}

	/**
	 * adds value to map: <br />
	 * if key wasn't yet inserted: create new list, add value to list, put key + list to map <br/>
	 * otherwise get list from map with key & add new value
	 * 
	 * @param vrepNodeMap
	 * @param vrep
	 * @param node
	 */
	private void addNodeToVrepNodeMap(Map<PathwayVertexRep, List<NodeElement>> vrepNodeMap,
			PathwayVertexRep vrep, NodeElement node) {
		if (!vrepNodeMap.containsKey(vrep)) {
			List<NodeElement> nodes = new ArrayList<NodeElement>();
			nodes.add(node);
			vrepNodeMap.put(vrep, nodes);
		} else {
			vrepNodeMap.get(vrep).add(node);
		}

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
	private Map<NodeElement, List<PathwayVertex>> getNodeElementsContainingSameVertices(
			List<PathwayVertex> verticesToCheckList) {
		Map<NodeElement, List<PathwayVertex>> equivalentVerticesMap = new HashMap<NodeElement, List<PathwayVertex>>();

		for (PathwayVertex vertexToCheck : verticesToCheckList) {
			if (uniqueVertexMap.containsKey(vertexToCheck)) {
				NodeElement existingNodeElement = uniqueVertexMap.get(vertexToCheck);
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

	// insert all PathwayVertex into
	private void addGraphWithoutDuplicatesOld(PathwayGraph newGraph) {

		/**
		 * storing the new NodeElements (which contain the merged elements) in a list & adding the afterwards
		 * to the nodeSet list adding them while iterating over the list & deleting elements, would be too
		 * much of a mess
		 */
		Map<PathwayVertex, NodeElement> newNodeElements = new HashMap<PathwayVertex, NodeElement>();
		Map<NodeElement, NodeElement> removedNodeSubstitutedByNewNodeMap = new HashMap<NodeElement, NodeElement>();

		// for (Iterator<NodeElement> nodeSetIterator = nodeSet.iterator(); nodeSetIterator.hasNext();) {
		// for (PathwayVertexRep vrepOfGraph : graphToSaveNewElementsTo.vertexSet()) {
		for (Iterator<PathwayVertexRep> vrepIterator = newGraph.vertexSet().iterator(); vrepIterator
				.hasNext();) {
			PathwayVertexRep vrepOfGraph = vrepIterator.next();
			// System.out.println(vrepOfGraph.getName() + " type: " + vrepOfGraph.getType());
			if (vrepOfGraph.getType() == EPathwayVertexType.map)
				continue;

			boolean removeCurrentNodeFromNodeSet = false;
			boolean addedMergedNode = false;
			PathwayVertex vertexForAddedMergedNode = null;
			NodeElement mergedNode = null;
			NodeElement substitutedByNode = null;

			NodeElement node = addNewNodeElement(vrepOfGraph, null, null);
			// NodeElement node = nodeSetIterator.next();

			System.out.println(node.getLabel());

			for (Iterator<PathwayVertex> vertexOfNodeIterator = node.getVertices().iterator(); vertexOfNodeIterator
					.hasNext();) {

				PathwayVertex vertexOfNode = vertexOfNodeIterator.next();
				PathwayVertexRep vrepOfNode = node.getVertexRep();

				// if this is the first (and maybe only) time, this PathwayVertex is in the Graph,
				// make a new entry
				if (preventDuplicatesMap.get(vertexOfNode) == null) {
					List<NodeElement> referencesToThisString = new LinkedList<NodeElement>();
					referencesToThisString.add(node);

					preventDuplicatesMap.put(vertexOfNode, referencesToThisString);
					removeCurrentNodeFromNodeSet = false;
					substitutedByNode = null;
					addedMergedNode = false;
					vertexForAddedMergedNode = null;
					mergedNode = null;
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
						newMergedVrep.setPathway(pathway.getCombinedGraph());
						pathway.getCombinedGraph().addVertex(newMergedVrep);

						NodeElement newMergedNode = addNewNodeElement(newMergedVrep, null, null);
						newNodeElements.put(vertexOfNode, newMergedNode);

						// add new node to map of all nodes
						preventDuplicatesMap.get(vertexOfNode).add(newMergedNode);
						preventDuplicatesMap.get(vertexOfNode).add(node);

						nodeSet.add(newMergedNode);
						addedMergedNode = true;
						vertexForAddedMergedNode = vertexOfNode;
						mergedNode = newMergedNode;
					}

					// if this was the last vertex of this node -> delete node (merged graph node is its
					// replacement)
					if (node.getVertices().size() <= 1) {
						removeCurrentNodeFromNodeSet = true;
						substitutedByNode = newNodeElements.get(vertexOfNode);

						// remove node from list
						if (substitutedByNode == null)
							throw new NullPointerException("substitudedbyNode is null; vrep: " + vrepOfNode);
					} else {
						removeCurrentNodeFromNodeSet = false;
						substitutedByNode = null;
					}

					vertexOfNodeIterator.remove();

				}

			}

			if (removeCurrentNodeFromNodeSet) {
				removedNodeSubstitutedByNewNodeMap.put(node, substitutedByNode);
				preventDuplicatesMap.get(substitutedByNode.getVertices().get(0)).remove(node);
				// nodeSetIterator.remove();
			} else if (addedMergedNode) {
				// not sure if correct
				NodeElement first = preventDuplicatesMap.get(vertexForAddedMergedNode).get(0);
				if (nodeSet.contains(first)) {
					removedNodeSubstitutedByNewNodeMap.put(first, mergedNode);
					preventDuplicatesMap.get(vertexForAddedMergedNode).remove(first);
					nodeSet.remove(first);
				}
				System.out.println("wahhh!");
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
			newGraph.addVertex(newNodeElement.getVertexRep());

			nodeSet.add(newNodeElement);
			// add(newNodeElement);
		}

		for (DefaultEdge edge : newGraph.edgeSet()) {
			PathwayVertexRep srcVrep = newGraph.getEdgeSource(edge);
			PathwayVertexRep targetVrep = newGraph.getEdgeTarget(edge);

			assert (srcVrep != null);
			assert (targetVrep != null);

			NodeElement srcNode = pathway.getNodeOfVertex(srcVrep);

			// if this node is to be replaced by a new merged node
			if (removedNodeSubstitutedByNewNodeMap.containsKey(srcNode)) {
				assert (nodeSet.contains(srcNode) == false);
				srcNode = removedNodeSubstitutedByNewNodeMap.get(srcNode);
				assert (nodeSet.contains(srcNode));
				assert (srcNode != null);
			}
			NodeElement targetNode = pathway.getNodeOfVertex(targetVrep);
			NodeElement oldTarget = targetNode;
			if (removedNodeSubstitutedByNewNodeMap.containsKey(oldTarget)) {
				assert (nodeSet.contains(targetNode) == false);
				targetNode = removedNodeSubstitutedByNewNodeMap.get(oldTarget);
				assert (nodeSet.contains(targetNode));
				assert (targetNode != null);
			}

			EdgeElement edgeElement = new EdgeElement(edge, srcNode, targetNode);

			/**
			 * so the layouting algorithm can extinguish, if it's a node or an edge
			 */
			edgeElement.setLayoutData(false);

			/**
			 * needed for the layouting algorithm
			 */
			edgeSet.add(edgeElement);
			//
			// add(edgeElement);

		}

		for (NodeElement node : nodeSet) {
			add(node);
		}

		for (EdgeElement edge : edgeSet) {
			add(edge);
		}

	}

	/**
	 * represent graph biologically correct, with duplicates <br />
	 * used when only one graph is displayed
	 * 
	 * @param newGraph
	 */
	private void addGraphWithDuplicates(PathwayGraph newGraph) {
		for (PathwayVertexRep vrep : newGraph.vertexSet()) {
			NodeElement nodeElement = addNewNodeElement(vrep, null, null);
			nodeSet.add(nodeElement);
			add(nodeElement);
		}

		for (DefaultEdge edge : newGraph.edgeSet()) {
			NodeElement sourceNode = pathway.getNodeOfVertex(newGraph.getEdgeSource(edge));
			NodeElement targetNode = pathway.getNodeOfVertex(newGraph.getEdgeTarget(edge));

			EdgeElement edgeElement = new EdgeElement(edge, sourceNode, targetNode);

			edgeSet.add(edgeElement);
			add(edgeElement);
		}
	}

	private void checkForDuplicateVertices() throws Exception {
		for (NodeElement nodeToCheck : nodeSet) {
			List<PathwayVertex> verticesToCheck = nodeToCheck.getVertices();

			if (nodeToCheck.getType() != EPathwayVertexType.gene)
				continue;

			for (PathwayVertex vertexToCheck : verticesToCheck) {

				for (NodeElement nodeToCheckAgainst : nodeSet) {
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

	/**
	 * set up a logger to log into a file, additionally to the console
	 * 
	 * @param log
	 *            to redirect
	 * @param filePath
	 *            file to write to
	 */
	private void setLoggerToWriteFile(Logger logger, String filePath) {
		FileHandler fh;

		try {
			fh = new FileHandler(filePath);
			logger.addHandler(fh);

			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);

		} catch (SecurityException | IOException e) {
			e.printStackTrace();
		}
	}

}
