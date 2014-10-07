package org.caleydo.view.dynamicpathway.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.caleydo.core.util.collection.Pair;
import org.caleydo.view.dynamicpathway.ui.EdgeElement;
import org.caleydo.view.dynamicpathway.ui.NodeElement;

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

}
