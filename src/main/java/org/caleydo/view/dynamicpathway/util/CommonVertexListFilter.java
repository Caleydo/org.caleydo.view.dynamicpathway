package org.caleydo.view.dynamicpathway.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.datadomain.pathway.manager.PathwayManager;
import org.caleydo.view.entourage.ranking.IPathwayFilter;

public class CommonVertexListFilter implements IPathwayFilter {

	private Set<PathwayGraph> pathways = new HashSet<>();

	public CommonVertexListFilter(List<PathwayVertex> vertices, String resourceName) {
		
		for (PathwayGraph pathway : PathwayManager.get().getAllItems()) {
			// Just kegg
			String name = pathway.getType().getName();
			if (!name.equalsIgnoreCase(resourceName))
				continue;

			PathwayVertexRep vrep = PathwayUtil.pathwayContainsVertices(vertices, pathway);
			if (vrep != null) {
				pathways.add(pathway);
			}
		}
	}

	@Override
	public boolean showPathway(PathwayGraph pathway) {
		return pathways.contains(pathway);
	}
}
