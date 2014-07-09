package org.caleydo.view.dynamicpathway.util;

import java.util.LinkedList;
import java.util.List;

import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class PathwayManagementUtil {
	
	/**
	 * returns a list of vertices which represented in vrep1 & vrep2
	 * used for merging pathways - so no vertex are added twice
	 * 
	 * @param vrep1
	 * @param vrep2
	 * @return
	 */
	public static List<PathwayVertex> getEquivalentVertices(PathwayVertexRep vrep1, PathwayVertexRep vrep2) {
		
		List<PathwayVertex> equivalentVertices = new LinkedList<PathwayVertex>();
		
		for(PathwayVertex vertexOfVrep1 : vrep1.getPathwayVertices()) {
			for(PathwayVertex vertexOfVrep2 : vrep2.getPathwayVertices()) {
				if(vertexOfVrep1 == vertexOfVrep2){
					equivalentVertices.add(vertexOfVrep1);
				}
			}
		}
		
		return equivalentVertices;
		
	}
	
	public static Boolean pathwayVertexRepListContainsVertex(List<PathwayVertexRep> pathwayVertexList, PathwayVertex vertex) {
		
		for(PathwayVertexRep vrep : pathwayVertexList) {
			if(vrep.getPathwayVertices().contains(vertex))
				return true;
		}
		
		return false;
		
	}

}
