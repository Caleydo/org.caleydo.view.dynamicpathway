package org.caleydo.view.dynamicpathway.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.internal.NodeMergingException;
import org.caleydo.view.dynamicpathway.ui.DynamicPathwayGraphRepresentation;
import org.caleydo.view.dynamicpathway.ui.EdgeElement;
import org.caleydo.view.dynamicpathway.ui.NodeCompoundElement;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.caleydo.view.dynamicpathway.ui.NodeGeneElement;
import org.caleydo.view.dynamicpathway.ui.NodeGroupElement;
import org.jgrapht.graph.DefaultEdge;

public class GraphMergeUtil {
	
	
	
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
	public static void checkAndMergeNodes(PathwayGraph graphToAdd, PathwayVertexRep pathwayVertexRepToCheck,
			Map<PathwayVertex, NodeElement> vertexNodeMap, Set<NodeElement> nodeSetToAdd, Set<EdgeElement> edgeSetToAdd, boolean addToSameGraph, PathwayGraph combinedGraph, DynamicPathwayGraphRepresentation pathwayRep)
			throws NodeMergingException {
		
//		if(pathwayRep.DISPLAY_ONLY_VERTICES_WITH_EDGES && (graphToAdd.inDegreeOf(pathwayVertexRepToCheck) < 1) && (graphToAdd.outDegreeOf(pathwayVertexRepToCheck) < 1))
//			return;

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
		Map<NodeElement, List<PathwayVertex>> nodesWithSameVerticesMap = pathwayRep.getNodeElementsContainingSameVertices(
				vertexNodeMap, verticesToCheckList);

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
			NodeElement node = pathwayRep.addNewNodeElement(pathwayVertexRepToCheck, nonDuplicateVertexList, null);
			nodeSetToAdd.add(node);

			for (PathwayVertex vertex : nonDuplicateVertexList) {
				vertexNodeMap.put(vertex, node);
			}
		}

		/**
		 * merge with nodes that contain same vertices
		 */
		for (NodeElement nodeWithDuplicateVertices : nodesWithSameVerticesMap.keySet()) {
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
				NodeElement mergedNode = pathwayRep.addNewNodeElement(mergedVrep, sameVerticesList, vreps);
				nodeSetToAdd.add(mergedNode);

				/**
				 * if the duplicate vertices are all of the (not merged) node's vertices, it needs to be
				 * deleted, because the merged node took its purpose
				 */
				if (nodeWithDuplicateVertices.getVertices().size() == sameVerticesList.size()) {

					boolean containedNode = nodeSetToAdd.remove(nodeWithDuplicateVertices);					
					
					List<Pair<EdgeElement, Boolean>> edgesContainingThisNode = GraphMergeUtil.getEdgeWithThisNodeAsSourceOrTarget(edgeSetToAdd, nodeWithDuplicateVertices);
					for(Pair<EdgeElement, Boolean> edgePair : edgesContainingThisNode) {
						// node was edge source
						if(edgePair.getSecond()) 
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
							nodeWithDuplicateVertices.setDisplayedVertex(nodeWithDuplicateVerticesList.get(0));
						}
					}

					if (splitOfMergedVertexList.size() < 1) {
						throw new NodeMergingException("splitOfMergedVertexList didn't contain elements");
					}

					PathwayVertexRep mergedVrep = new PathwayVertexRep(splitOfMergedVertexList.get(0)
							.getHumanReadableName(), nodeVrep.getShapeType().name(), nodeVrep.getCenterX(),
							nodeVrep.getCenterY(), nodeVrep.getWidth(), nodeVrep.getHeight());

					mergedVrep.setPathway(combinedGraph);

					List<PathwayVertexRep> vrepsOfNode = nodeWithDuplicateVertices
							.getVrepsWithThisNodesVerticesList();
					if (vrepsOfNode == null) {
						throw new NodeMergingException("nodeWithDuplicateVertices didn't contain Vreps");
					}
					NodeElement mergedNode = pathwayRep.addNewNodeElement(mergedVrep, splitOfMergedVertexList,
							vrepsOfNode);

					for (PathwayVertex mergedVertex : splitOfMergedVertexList) {
						mergedVrep.addPathwayVertex(mergedVertex);
						vertexNodeMap.put(mergedVertex, mergedNode);
					}

					nodeSetToAdd.add(mergedNode);

				} else {

					nodeWithDuplicateVertices.addVrepWithThisNodesVerticesList(pathwayVertexRepToCheck);
				}

			}

		}

		return;
	}

	/**
	 * finds all edgeElement, which contain node either as source or as target node
	 * 
	 * @param edgeSet
	 *            set to look for
	 * 
	 * @param node
	 *            to look for
	 * @return List<Pair<EdgeElement, Boolean>> edgesContainingThisNode, when Boolean = true: node is source
	 *         node of this EdgeElement; is target node otherwise
	 */
	public static final List<Pair<EdgeElement, Boolean>> getEdgeWithThisNodeAsSourceOrTarget(
			Set<EdgeElement> edgeSet, NodeElement node) {

		List<Pair<EdgeElement, Boolean>> edgesContainingThisNode = new LinkedList<Pair<EdgeElement, Boolean>>();

		for (EdgeElement edge : edgeSet) {
			if (edge.getSourceNode().equals(node))
				edgesContainingThisNode.add(new Pair<EdgeElement, Boolean>(edge, true));
			if (edge.getTargetNode().equals(node))
				edgesContainingThisNode.add(new Pair<EdgeElement, Boolean>(edge, false));

		}

		return edgesContainingThisNode;
	}

	/**
	 * add given edge To given edge Set
	 * 
	 * @param edge
	 * @param pathway
	 * @param vertexNodeMap
	 * @param edgeSetToAdd
	 * @throws Exception
	 */
	public static void addEdgeToEdgeSet(DefaultEdge edge, PathwayGraph pathway,
			Map<PathwayVertex, NodeElement> vertexNodeMap, Set<EdgeElement> edgeSetToAdd) throws Exception {
		PathwayVertexRep srcVrep = pathway.getEdgeSource(edge);
		PathwayVertexRep targetVrep = pathway.getEdgeTarget(edge);

		List<NodeElement> srcNodes = new LinkedList<NodeElement>();
		List<NodeElement> targetNodes = new LinkedList<NodeElement>();

		for (PathwayVertex srcVertex : srcVrep.getPathwayVertices()) {
			if (!vertexNodeMap.containsKey(srcVertex) || vertexNodeMap.get(srcVertex) == null)
				throw new NodeMergingException("srcVertex(" + srcVertex + ") not in uniqueVertexMap");

			srcNodes.add(vertexNodeMap.get(srcVertex));
		}

		for (PathwayVertex targetVertex : targetVrep.getPathwayVertices()) {
			if (!vertexNodeMap.containsKey(targetVertex) || vertexNodeMap.get(targetVertex) == null)
				throw new NodeMergingException("targetVertex(" + targetVertex + ") not in uniqueVertexMap");

			targetNodes.add(vertexNodeMap.get(targetVertex));
		}

		for (NodeElement srcNode : srcNodes) {
			for (NodeElement targetNode : targetNodes) {
				if (srcNode == targetNode)
					continue;
				// throw new NodeMergingException("srcNode == targetNode");

				EdgeElement edgeEl = new EdgeElement(edge, srcNode, targetNode);
				edgeEl.setLayoutData(false);
				edgeSetToAdd.add(edgeEl);
			}
		}

	}

	/**
	 * return list without PathwayVertex with duplicate names
	 * 
	 * @param vertexListToFilter
	 *            list to filter
	 * @return filtered list
	 */
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


}
