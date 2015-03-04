package org.caleydo.view.dynamicpathway.layout;

import java.util.LinkedList;
import java.util.List;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

/**
 * contains all informations for the different graphs, such as the focusGraph & all kontextGraphs
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayGraph {

	private static final String COMBINED_GRAPH_NAME = "Combined Graph";

	/**
	 * the actual focus pathway graph, which is completely represented
	 */
	private PathwayGraph focusPathway;

	/**
	 * the kontext graph, which may not be be fully represented TODO: change vector to : vector<KontextGraph>
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
	 * @return true if the
	 */
	public boolean isPathwayPresent(PathwayGraph pathway) {
		if (isFocusGraph(pathway) || isContextGraph(pathway))
			return true;

		return false;
	}

	/**
	 * returns the pathway with this title, if it exists
	 * 
	 * @param title
	 *            the title of the pathway to return
	 * @return the graph
	 * @throws Exception
	 *             if there is no pathway with this title
	 */
	public PathwayGraph getPathwayWithThisTitle(String title) throws Exception {
		if (focusPathway.getTitle().contentEquals(title))
			return focusPathway;

		for (PathwayGraph contextGraph : contextPathways) {
			if (contextGraph.getTitle().contentEquals(title))
				return contextGraph;
		}

		throw new Exception("INTERNAL ERROR: Pathway with this title (" + title + ") doesn't exist.");
	}

	// adds a new focus or kontext pathway, so they will be displayed
	public void addFocusOrKontextPathway(PathwayGraph pathway, Boolean addContextPathway) {

		if (!addContextPathway) {
			addFocusPathway(pathway);
		} else {
			contextPathways.add(pathway);
		}

	}

	public boolean isFocusPathwaySet() {
		if (focusPathway != null)
			return true;
		return false;
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

	// ----------------------------------------------------

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
