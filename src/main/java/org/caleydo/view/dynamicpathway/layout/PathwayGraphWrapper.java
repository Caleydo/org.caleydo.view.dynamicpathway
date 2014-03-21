package org.caleydo.view.dynamicpathway.layout;

import gleem.linalg.Vec2f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.jgrapht.graph.DefaultEdge;

public class PathwayGraphWrapper  {

	PathwayGraph graph;
	Map<PathwayVertexRep, Vec2f> distances;
	
	public PathwayGraphWrapper() {
		distances = new HashMap<PathwayVertexRep, Vec2f>();
	}
	
	public void addGraph(PathwayGraph pathwayGraph) {
		graph = pathwayGraph;
	}
	
	public void setDistance(PathwayVertexRep vrep, Vec2f distance) {
		distances.put(vrep, distance);
	}
	
	public Vec2f getDistance(PathwayVertexRep vrep) {
		return distances.get(vrep);
	}
	
	// methods forwarded to PathwayGraph
	public Set<PathwayVertexRep> vertexSet() {
		return graph.vertexSet();
	}
	
	public Set<DefaultEdge> edgeSet() {
		return graph.edgeSet();
	}


}
