package org.caleydo.view.dynamicpathway.layout;

import gleem.linalg.Vec2f;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

public class ForceDirectedFRGraphLayout {
	
	private static ForceDirectedFRGraphLayout instance = null;
	private static final int MAX_ITERATIONS = 1;
	
	private ForceDirectedFRGraphLayout() {}
	
	public static ForceDirectedFRGraphLayout getInstance() {
		if(instance == null) {
			synchronized (ForceDirectedFRGraphLayout.class) {
				if (instance == null)
					instance = new ForceDirectedFRGraphLayout();
			}
		}
			
		
		return instance;
	}
	
	public void layout(PathwayGraphWrapper graph,Rectangle2D area) {
		Set<PathwayVertexRep> vertices = graph.vertexSet();
		Set<DefaultEdge> edges = graph.edgeSet();
		double space = (area.getWidth()*area.getHeight())/vertices.size();
		float root_space = (float)Math.sqrt(space);
		
		
		for(int i = 1; i <= MAX_ITERATIONS; i++) {
			
			// calculate repulsive forces
			for(PathwayVertexRep vrep : vertices) {
				//System.out.println("Vertice Label: " + vrep.getLabel());
				//System.out.println("Vertices: " + vrep.toString());
				if(vrep.getCenterX() != 0 || vrep.getCenterY() != 0) {
					graph.setDistance(vrep, new Vec2f());
					calcRepulsiveForces(graph, vertices, vrep, root_space);
				}				
			}
			
			//calculate attrative forces			
			for(DefaultEdge edge : edges) {
				DefaultEdgeWrapper edgeWrapper = new DefaultEdgeWrapper(edge);
				System.out.println("edgeWrapper: " + edgeWrapper.getSource());

//				
//				System.out.println("Vertices: " + vertices.iterator().next().toString());
//				System.out.println("Vertex short name: " + vertices.iterator().next().getShortName());
//				System.out.println("Vertex name: " + vertices.iterator().next().getName());
				System.out.println("Edges: " + edge.toString());
				
				
			}
		}
		
		int a = 0;
		
	}
	
	private void calcRepulsiveForces(PathwayGraphWrapper graph, Set<PathwayVertexRep> vertices, PathwayVertexRep currentVertex, float space) {
		float xVertexDisp = graph.getDistance(currentVertex).x();
		float yVertexDisp = graph.getDistance(currentVertex).y();
		for(PathwayVertexRep vrep : vertices) {
			if(vrep.getID() != currentVertex.getID()) {
				float xDistance = vrep.getCenterX() - currentVertex.getCenterX();
				float yDistance = vrep.getCenterY() - currentVertex.getCenterY();
				float distance = (float) Math.sqrt(xDistance * xDistance + yDistance * yDistance);
				
				if(distance > 0) {
					float repulsiveForce = space*space/distance;
					xVertexDisp += xDistance/distance * repulsiveForce;
					yVertexDisp += yDistance/distance * repulsiveForce;
				}
			}
		}
		System.out.println("before calc disparity: " + graph.getDistance(currentVertex));
		graph.setDistance(currentVertex, new Vec2f(xVertexDisp,yVertexDisp));
		System.out.println("after calc disparity: " + graph.getDistance(currentVertex));
	}
	
	private void calcAttractiveForces() {
		
		
	}
	


}
