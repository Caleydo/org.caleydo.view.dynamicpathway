package org.caleydo.view.dynamicpathway.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.internal.NodeMergingException;
import org.caleydo.view.dynamicpathway.ui.EdgeElement;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

public class GraphMergeUtil {

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
	 * @param vertexListToFilter list to filter
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
