package org.caleydo.view.dynamicpathway.layout;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.util.color.Color;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

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
	 * KontextGraph contains: PathwayGraph, to which it belong, the main vertex, list of represented vertices
	 * & edges
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

//	public Set<PathwayVertexRep> getCombinedVertexSet() {
//		return combinedGraph.vertexSet();
//	}
//
//	public Set<DefaultEdge> getCombinedEdgeSet() {
//		return combinedGraph.edgeSet();
//	}
//
//	public PathwayVertexRep getEdgeSource(DefaultEdge e) {
//		return combinedGraph.getEdgeSource(e);
//	}
//
//	public PathwayVertexRep getEdgeTarget(DefaultEdge e) {
//		return combinedGraph.getEdgeTarget(e);
//	}
	
	/** 
	 * checks if the current pathway is present
	 * 
	 * @param pathway the pathway to check
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
	 * @param title the title of the pathway to return
	 * @return the graph
	 * @throws Exception if there is no pathway with this title 
	 */
	public PathwayGraph getPathwayWithThisTitle(String title) throws Exception {
		if(focusPathway.getTitle().contentEquals(title))
			return focusPathway;
		
		for(PathwayGraph contextGraph : contextPathways) {
			if(contextGraph.getTitle().contentEquals(title))
				return contextGraph;
		}
				
		throw new Exception("INTERNAL ERROR: Pathway with this title (" + title + ") doesn't exist.");
	}

	// adds a new focus or kontext pathway, so they will be displayed
	public void addFocusOrKontextPathway(PathwayGraph pathway, Boolean addContextPathway,
			NodeElement currentSelectedNode) {

		if (!addContextPathway) {
			addFocusPathway(pathway);
		} else {
			contextPathways.add(pathway);
		}

	}
//
//	public float getFocusPathwayWidth() {
//		return focusPathway.getWidth();
//	}
//
//	public float getFocusPathwayHeight() {
//		return focusPathway.getHeight();
//	}

	public boolean isFocusPathwaySet() {
		if (focusPathway != null)
			return true;
		return false;
	}

	public PathwayGraph getFocusPathway() {
		return focusPathway;
	}

	public List<PathwayGraph> getContextPathways() {
		return contextPathways;
	}
	
	public void removeContextPathway(PathwayGraph contextPathwayToRemove) {
		contextPathways.remove(contextPathwayToRemove);
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

	private void addFocusPathway(PathwayGraph graph) {
		focusPathway = graph;
		contextPathways.clear();

		combinedGraph = new PathwayGraph(graph.getType(), "Combined Graph [Focus:" + graph.getName()+ "]", "Combined Graph [Focus:" + graph.getTitle()+ "]",
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


}
