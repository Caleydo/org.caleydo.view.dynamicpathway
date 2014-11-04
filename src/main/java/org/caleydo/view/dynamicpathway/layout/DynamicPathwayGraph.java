package org.caleydo.view.dynamicpathway.layout;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

/**
 * contains all informations for the different graphs, such as the focusGraph & all kontextGraphs
 * 
 * @author Christiane Schwarzl
 * 
 */
public class DynamicPathwayGraph {

	private static final Boolean DISPLAY_ONLY_VERTICES_WITH_EDGES = true;
	private static final String COMBINED_GRAPH_NAME = "Combined Graph";

	/**
	 * the actual focus pathway graph, which is completely represented
	 */
	private PathwayGraph focusGraph;

	/**
	 * the kontext graph, which may not be be fully represented TODO: change vector to : vector<KontextGraph>
	 * KontextGraph contains: PathwayGraph, to which it belong, the main vertex, list of represented vertices
	 * & edges
	 */
	private List<PathwayGraph> kontextGraphs;

	/**
	 * needed for searching all currently represented vertices
	 */
	private PathwayGraph combinedGraph;

	/**
	 * get source/target vertex of edge returns a PathwayVertexRep, but we need a NodeElement, which is a
	 * container for PathwayVertexRep
	 */
	private Map<PathwayVertexRep, NodeElement> vertexNodeMap;
	

	public DynamicPathwayGraph() {

		kontextGraphs = new LinkedList<PathwayGraph>();
		vertexNodeMap = new HashMap<PathwayVertexRep, NodeElement>();

	}

	public PathwayGraph getCombinedGraph() {
		return combinedGraph;
	}

	public Set<PathwayVertexRep> getCombinedVertexSet() {
		return combinedGraph.vertexSet();
	}

	public Set<DefaultEdge> getCombinedEdgeSet() {
		return combinedGraph.edgeSet();
	}

	public PathwayVertexRep getEdgeSource(DefaultEdge e) {
		return combinedGraph.getEdgeSource(e);
	}

	public PathwayVertexRep getEdgeTarget(DefaultEdge e) {
		return combinedGraph.getEdgeTarget(e);
	}

	public void addVertexNodeMapEntry(PathwayVertexRep vrep, NodeElement node) {
		vertexNodeMap.put(vrep, node);
	}

	public NodeElement getNodeOfVertex(PathwayVertexRep vrep) {
		return vertexNodeMap.get(vrep);
	}

	/** 
	 * checks if the current pathway is present
	 * 
	 * @param pathway the pathway to check
	 * 
	 * @return true if the 
	 */
	public boolean isPathwayPresent(PathwayGraph pathway) {
		if (isFocusGraph(pathway) || isKontextGraph(pathway))
			return true;

		return false;
	}
	
	/**
	 * returns the pathway with this title, if it exists
	 * 
	 * @param title the title of the pathway to return
	 * @return the graph
	 * @throws Exception if there is no pathway with this title 
	 */
	public PathwayGraph getPathwayWithThisTitle(String title) throws Exception {
		if(focusGraph.getTitle().contentEquals(title))
			return focusGraph;
		
		for(PathwayGraph kontextGraph : kontextGraphs) {
			if(kontextGraph.getTitle().contentEquals(title))
				return kontextGraph;
		}
				
		throw new Exception("INTERNAL ERROR: Pathway with this title (" + title + ") doesn't exist.");
	}

	// adds a new focus or kontext pathway, so they will be displayed
	public void addFocusOrKontextPathway(PathwayGraph pathway, Boolean addKontextPathway,
			NodeElement currentSelectedNode) {

		if (!addKontextPathway) {
			addFocusPathway(pathway);
		} else {
			kontextGraphs.add(pathway);
//			addKontextGraph(pathway, currentSelectedNode);
		}

	}

	public float getFocusPathwayWidth() {
		return focusGraph.getWidth();
	}

	public float getFocusPathwayHeight() {
		return focusGraph.getHeight();
	}

	public boolean isFocusGraphSet() {
		if (focusGraph != null)
			return true;
		return false;
	}

	public PathwayGraph getFocusGraph() {
		return focusGraph;
	}

	public List<PathwayGraph> getKontextGraphs() {
		return kontextGraphs;
	}

	// ----------------------------------------------------

	public boolean isFocusGraph(PathwayGraph pathway) {
		if (pathway == focusGraph)
			return true;
		return false;
	}

	public boolean isKontextGraph(PathwayGraph pathway) {
		for (PathwayGraph kontextPathway : kontextGraphs) {
			if (pathway == kontextPathway)
				return true;
		}
		return false;
	}

	private void addFocusPathway(PathwayGraph graph) {
		focusGraph = graph;
		kontextGraphs.clear();

		combinedGraph = new PathwayGraph(graph.getType(), graph.getName(), graph.getTitle(),
				graph.getImage(), graph.getExternalLink());

		for (PathwayVertexRep vrep : graph.vertexSet()) {
			
			/**
			 * map is the type, which display the current pathway's name this should be layoutet
			 * 
			 * user can choose if only vertices with edges should be displayed, so that the workspace is not
			 * so cluttered
			 * TODO: implement user interaction
			 */
//			if (vrep.getType() != EPathwayVertexType.map) {
//				if (DISPLAY_ONLY_VERTICES_WITH_EDGES) {
//					if(graph.inDegreeOf(vrep) > 0 && graph.outDegreeOf(vrep) > 0)
//						combinedGraph.addVertex(vrep);
//				}
//				else
					combinedGraph.addVertex(vrep);
					
//			}
		}
		for (DefaultEdge edge : graph.edgeSet()) {
			
			combinedGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
		}
	}

	private void addKontextGraph(PathwayGraph pathway, NodeElement currentSelectedNode) {
		kontextGraphs.add(pathway);
		// Vector<PathwayVertexRep> vrepsToIgnore = new Vector<PathwayVertexRep>();

		Map<PathwayVertexRep, PathwayVertexRep> equivalVertexMap = new HashMap<PathwayVertexRep, PathwayVertexRep>();

		/**
		 * if one of the vreps of the new kontext pathway to add, is already in the displayed graph, it is
		 * saved in the equivalVertexMap, which contains all vertices which should be ignored, to avoid
		 * duplicates
		 */
		for (PathwayVertexRep vrepToAdd : pathway.vertexSet()) {
			for (PathwayVertexRep alreadyDisplayedVrep : combinedGraph.vertexSet()) {
				if (PathwayManager.get().areVerticesEquivalent(alreadyDisplayedVrep, vrepToAdd)) {

					// if(equivalVertexMap.get(vrepToAdd) != null)
					equivalVertexMap.put(vrepToAdd, alreadyDisplayedVrep);

				}
			}
		}

		for (PathwayVertexRep vrep : pathway.vertexSet()) {

			/**
			 * map is the type, which display the current pathway's name this should be layoutet - TODO:
			 * display map
			 */
			if (vrep.getType() != EPathwayVertexType.map) {

				if (DISPLAY_ONLY_VERTICES_WITH_EDGES
						&& !(pathway.inDegreeOf(vrep) == 0 && pathway.outDegreeOf(vrep) == 0)) {
					/**
					 * if the vertex to add is not already displayed
					 */
					if (equivalVertexMap.get(vrep) == null) {
						combinedGraph.addVertex(vrep);
					}
				}

			}
		}

		for (DefaultEdge edge : pathway.edgeSet()) {
			PathwayVertexRep source = pathway.getEdgeSource(edge);
			PathwayVertexRep target = pathway.getEdgeTarget(edge);

			/**
			 * if source or target are already displayed with an equivalent vertex, change edge
			 */
			if (equivalVertexMap.containsKey(source)) {
				source = equivalVertexMap.get(source);
			}
			if (equivalVertexMap.containsKey(target)) {
				target = equivalVertexMap.get(target);
			}

			/**
			 * if the edged is already displayed, don't add it
			 */
			// Set<DefaultEdge> equivalentEdges = combinedGraph.getAllEdges(source, target);
			//
			// if(equivalentEdges == null || equivalentEdges.size() == 0)
			// continue;

			if (source.getType() != EPathwayVertexType.map && target.getType() != EPathwayVertexType.map)
				combinedGraph.addEdge(source, target, edge);
		}

	}

}
