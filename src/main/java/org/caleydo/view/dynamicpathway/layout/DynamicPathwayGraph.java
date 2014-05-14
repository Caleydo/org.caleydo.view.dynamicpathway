package org.caleydo.view.dynamicpathway.layout;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

public class DynamicPathwayGraph {
	
	private PathwayGraph focusGraph;
	private Vector<PathwayGraph> kontextGraphs;
	private PathwayGraph combinedGraph;
	private Map<PathwayVertexRep, NodeElement> vertexNodeMap;
	private boolean setKontextGraph;
	
	
	
	public DynamicPathwayGraph() {
//		focusGraph = graph;
		kontextGraphs = new Vector<PathwayGraph>();		
		vertexNodeMap = new HashMap<PathwayVertexRep, NodeElement>();		
		
//		for (PathwayVertexRep vrep : graph.vertexSet()) {
//			if(vrep.getType() == EPathwayVertexType.gene) {
//				combinedGraph.addVertex(vrep);
//			}
//		}
//		for (DefaultEdge edge : graph.edgeSet()) {
//			PathwayVertexRep source = graph.getEdgeSource(edge);
//			PathwayVertexRep target = graph.getEdgeTarget(edge);
//			if(source.getType() == EPathwayVertexType.gene && target.getType() == EPathwayVertexType.gene) {
//				combinedGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
//			}
//		}

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
			if(vrep.getType() == EPathwayVertexType.gene) {
				combinedGraph.addVertex(vrep);
			}
		}
		for (DefaultEdge edge : graph.edgeSet()) {
			PathwayVertexRep source = graph.getEdgeSource(edge);
			PathwayVertexRep target = graph.getEdgeTarget(edge);
			if(source.getType() == EPathwayVertexType.gene && target.getType() == EPathwayVertexType.gene) {
				combinedGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
			}
		}
	}

}
