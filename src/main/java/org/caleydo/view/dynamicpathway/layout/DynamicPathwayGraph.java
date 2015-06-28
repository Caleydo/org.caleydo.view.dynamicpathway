package org.caleydo.view.dynamicpathway.layout;

import java.util.LinkedList;
import java.util.List;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.jgrapht.graph.DefaultEdge;

/**
 * contains all informations for the different graphs, such as the Focus Pathway & the Context Pathways
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayGraph {

	/**
	 * the actual focus pathway graph, which is completely represented
	 */
	private PathwayGraph focusPathway;

	/**
	 * the context graph, which may not be be fully represented
	 * KontextGraph contains: PathwayGraph, to which it belong, the main vertex, list of represented vertices & edges
	 */
	private List<PathwayGraph> contextPathways;

	/**
	 * needed for searching all currently represented vertices
	 */
	private PathwayGraph combinedGraph;

	public DynamicPathwayGraph() {

		contextPathways = new LinkedList<PathwayGraph>();

	}

	public PathwayGraph getCombinedGraph() {
		return combinedGraph;
	}

	/**
	 * checks if the current pathway is present
	 * 
	 * @param pathway
	 *            the pathway to check
	 * 
	 * @return true if the pathway is present
	 */
	public boolean isPathwayPresent(PathwayGraph pathway) {
		if (isFocusGraph(pathway) || isContextGraph(pathway))
			return true;

		return false;
	}


	/**
	 * adds a new focus or context pathway, so they will be displayed
	 * 
	 * @param pathway
	 * @param addContextPathway
	 */
	public void addFocusOrContextPathway(PathwayGraph pathway, Boolean addContextPathway) {

		if (!addContextPathway) {
			addFocusPathway(pathway);
		} else {
			contextPathways.add(pathway);
		}

	}

	public PathwayGraph getFocusPathway() {
		return focusPathway;
	}

	public void setFocusPathway(PathwayGraph newFocusPathway) {
		this.focusPathway = newFocusPathway;
	}

	public List<PathwayGraph> getContextPathways() {
		return contextPathways;
	}

	public Boolean removeContextPathway(PathwayGraph contextPathwayToRemove) {
		return contextPathways.remove(contextPathwayToRemove);
	}

	public void removeAllPathways() {
		focusPathway = null;
		contextPathways.clear();
		combinedGraph = null;
	}


	public boolean isFocusGraph(PathwayGraph pathway) {
		if (pathway.equals(focusPathway))
			return true;
		return false;
	}

	public boolean isContextGraph(PathwayGraph pathway) {
		for (PathwayGraph contextPathway : contextPathways) {
			if (pathway.equals(contextPathway))
				return true;
		}
		return false;
	}

	private void addFocusPathway(PathwayGraph newFocusPathway) {
		focusPathway = newFocusPathway;
		contextPathways.clear();

		combinedGraph = new PathwayGraph(newFocusPathway.getType(), "Combined Graph [Focus:"
				+ newFocusPathway.getName() + "]", "Combined Graph [Focus:" + newFocusPathway.getTitle() + "]",
				newFocusPathway.getImage(), newFocusPathway.getExternalLink());

		for (PathwayVertexRep vrep : newFocusPathway.vertexSet()) {

			combinedGraph.addVertex(vrep);

		}
		for (DefaultEdge edge : newFocusPathway.edgeSet()) {

			combinedGraph.addEdge(newFocusPathway.getEdgeSource(edge), newFocusPathway.getEdgeTarget(edge), edge);
		}
	}

}
