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
	
	PathwayGraph focusGraph;
	Vector<PathwayGraph> kontextGraphs;
	PathwayGraph combinedGraph;
	Map<PathwayVertexRep, NodeElement> vertexNodeMap;
	
	public DynamicPathwayGraph(PathwayGraph graph) {
		focusGraph = graph;
		kontextGraphs = new Vector<PathwayGraph>();	
		combinedGraph = new PathwayGraph(graph.getType(), graph.getName(), graph.getTitle(), graph.getImage(),
				graph.getExternalLink());		
		vertexNodeMap = new HashMap<PathwayVertexRep, NodeElement>();
		
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
	

}
