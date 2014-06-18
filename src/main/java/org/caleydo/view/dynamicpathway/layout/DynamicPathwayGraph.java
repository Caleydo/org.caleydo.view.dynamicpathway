package org.caleydo.view.dynamicpathway.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.caleydo.view.dynamicpathway.ui.NodeGeneElement;
import org.jgrapht.graph.DefaultEdge;

/**
 * contains all informations for the different graphs, such
 * as the focusGraph & all kontextGraphs
 * 
 * @author Christiane Schwarzl
 *
 */
public class DynamicPathwayGraph {
	
	/** 
	 * the actual focus pathway graph,
	 * which is completely represented
	 */
	private PathwayGraph focusGraph;
	
	/**
	 * the kontext graph, which may not be 
	 * be fully represented
	 * TODO: change vector to : vector<KontextGraph>
	 * KontextGraph contains: PathwayGraph, to which it belong, the main vertex, list of represented vertices & edges
	 */
	private Vector<PathwayGraph> kontextGraphs;
	
	/**
	 * needed for searching all currently represented vertices
	 */
	private PathwayGraph combinedGraph;
	
	/**
	 * get source/target vertex of edge returns a PathwayVertexRep, but
	 * we need a NodeElement, which is a container for PathwayVertexRep
	 */
	private Map<PathwayVertexRep, NodeElement> vertexNodeMap;
	
	/**
	 * so we can extinguish if a focus or kontextGraph should be added, deleted
	 * or was chosen
	 */
	private boolean setKontextGraph;

	
	public DynamicPathwayGraph() {
		
		kontextGraphs = new Vector<PathwayGraph>();		
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
	
	public boolean isGraphPresented(PathwayGraph pathway) {
		if(isFocusGraph(pathway))
			return true;
		
		if(isKontextGraph(pathway))
			return true;
		
		return false;
	}
	
	private boolean isFocusGraph(PathwayGraph pathway) {
		if(pathway == focusGraph)
			return true;
		return false;
	}
	
	private boolean isKontextGraph(PathwayGraph pathway) {
		for(PathwayGraph kontextPathway : kontextGraphs) {
			if(pathway == kontextPathway)
				return true;
		}
		return false;
	}
	
	

	// adds a new focus or kontext pathway, so they will be displayed
	public void addFocusOrKontextPathway(PathwayGraph pathway) {
		
		if(!setKontextGraph) {
			addFocusPathway(pathway);
		}
		else {
			kontextGraphs.add(pathway);
		}

//		if(!isFocusGraphSet()) {
//			assert(kontextGraphs.size() == 0);
//			
//			addFocusPathway(pathway);
//
//		}
//		else {
//			if(!isKontextGraph(pathway)) {
//				kontextGraphs.add(pathway);
//			}
//		}
	}
	
	public float getFocusPathwayWidth() {
		return focusGraph.getWidth();
	}
	
	public float getFocusPathwayHeight() {
		return focusGraph.getHeight();
	}
	
	private boolean isFocusGraphSet() {
		if(focusGraph != null)
			return true;
		return false;
	}
	
	private void addFocusPathway(PathwayGraph graph) {
		focusGraph = graph;
		
		combinedGraph = new PathwayGraph(graph.getType(), graph.getName(), graph.getTitle(), graph.getImage(),
				graph.getExternalLink());	
		
		for (PathwayVertexRep vrep : graph.vertexSet()) {
			if(vrep.getType() != EPathwayVertexType.map ) {
//				if(vrep.getType() != EPathwayVertexType.gene )
//					System.out.println(vrep.getShortName() + ": " + vrep.getType().toString());
				
				combinedGraph.addVertex(vrep);
			}
		}
		for (DefaultEdge edge : graph.edgeSet()) {
			PathwayVertexRep source = graph.getEdgeSource(edge);
			PathwayVertexRep target = graph.getEdgeTarget(edge);
//			if(source.getType() == EPathwayVertexType.gene && target.getType() == EPathwayVertexType.gene) {
				combinedGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
//			}
		}
	}

}
