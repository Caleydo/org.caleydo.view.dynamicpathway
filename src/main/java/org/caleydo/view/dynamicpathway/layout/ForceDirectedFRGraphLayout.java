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
		float root_space = (float)java.lang.Math.sqrt(space);
		
		
		for(int i = 1; i <= MAX_ITERATIONS; i++) {
			
			// calculate repulsive forces
			for(PathwayVertexRep vrep : vertices) {
				graph.setDistance(vrep, new Vec2f());
				calcDisparity(graph, vertices, vrep, root_space);
				
			}
		}
		
	}
	
	private void calcDisparity(PathwayGraphWrapper graph, Set<PathwayVertexRep> vertices, PathwayVertexRep currentVertex, float space) {
		Vec2f difference = new Vec2f();
		for(PathwayVertexRep vrep : vertices) {
			if(vrep.getID() != currentVertex.getID()) {
				difference.setX(vrep.getCenterX() - currentVertex.getCenterX());
				difference.setY(vrep.getCenterY() - currentVertex.getCenterY());
				
				Vec2f tmp = new Vec2f();				
				tmp = difference.times(1/difference.length()).times(attrativeForceFunc(difference.length(), space));
				//Vec2f tmp = graph.getDistance(vrep) + (difference/difference.length());
				//graph.setDistance(vrep, graph.getDistance(vrep).add(b));
				
				System.out.println("before calc disparity: " + graph.getDistance(currentVertex));
				graph.getDistance(currentVertex).add(tmp);
				System.out.println("after calc disparity: " + graph.getDistance(currentVertex));
				
			}
				
		}
	}
	
	private float attrativeForceFunc(float position, float space) {
		return (float)((java.lang.Math.pow(position, 2))/space);
	}
	
	private float repulsiveForceFunc(float position, float space) {
		return (float)((java.lang.Math.pow(space, 2))/position);
	}
	
//	private void setNodePosition(NodeElement node, Point2D position) {
//		
//	}

}
