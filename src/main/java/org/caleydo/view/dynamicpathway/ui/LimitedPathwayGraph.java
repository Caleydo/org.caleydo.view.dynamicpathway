package org.caleydo.view.dynamicpathway.ui;

import java.io.File;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.manager.EPathwayDatabaseType;

public class LimitedPathwayGraph extends PathwayGraph {
	

	private static final long serialVersionUID = 1L;
	
	private final PathwayGraph correspondingFullPathway;
	private final PathwayVertex focusVertex;

	public LimitedPathwayGraph(PathwayGraph correspondingFullPathway, PathwayVertex focusVertex, EPathwayDatabaseType type, String name, String title, File image, String link) {
		super(type, name, title, image, link);
		// TODO Auto-generated constructor stub
		this.correspondingFullPathway = correspondingFullPathway;
		this.focusVertex = focusVertex;
	}


	public PathwayGraph getCorrespondingFullPathway() {
		return correspondingFullPathway;
	}

	public PathwayVertex getFocusVertex() {
		return focusVertex;
	}

}
