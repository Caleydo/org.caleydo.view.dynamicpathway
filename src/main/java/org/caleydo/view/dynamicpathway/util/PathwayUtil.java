package org.caleydo.view.dynamicpathway.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class PathwayUtil {
	
	public static final PathwayVertexRep pathwayContainsVertex(PathwayVertex vertextoCheck, PathwayGraph pathway) {

		Set<PathwayVertexRep> vreps = pathway.vertexSet();

		for (PathwayVertexRep vrep : vreps) {
			List<PathwayVertex> vertices = vrep.getPathwayVertices();
			if (vertices.contains(vertextoCheck))
				return vrep;
		}
		return null;
	}

	public static final PathwayVertexRep pathwayContainsVertices(List<PathwayVertex> verticestoCheck,
			PathwayGraph pathway) {

		Set<PathwayVertexRep> vreps = pathway.vertexSet();

		for (PathwayVertexRep vrep : vreps) {
			List<PathwayVertex> vertices = vrep.getPathwayVertices();
			for (PathwayVertex vertex : vertices) {
				if (verticestoCheck.contains(vertex))
					return vrep;
			}

		}
		return null;
	}
	
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

}
