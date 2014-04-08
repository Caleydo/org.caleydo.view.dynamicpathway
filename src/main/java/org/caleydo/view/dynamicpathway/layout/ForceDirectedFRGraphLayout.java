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
	private static final int MAX_TEMPERATURE = 1000;
	private static final float STEP = 0.1f;
	
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
	
	public void layout(PathwayGraphWrapper graph, Rectangle2D area) {
		float temperature = MAX_TEMPERATURE;
		Set<PathwayVertexRep> vertices = graph.focusGraphVertexSet();
		Set<DefaultEdge> edges = graph.focusGraphEdgeSet();
		double space = (area.getWidth()*area.getHeight())/vertices.size();
		float rootSpace = (float)Math.sqrt(space);		
		
		for(int i = 1; i <= MAX_ITERATIONS; i++) {
			
			// calculate repulsive forces
			for(PathwayVertexRep vrep : vertices) {
				if(vrep.getCenterX() != 0 || vrep.getCenterY() != 0) {
					graph.setDisplacement(vrep, new Vec2f());
					calcRepulsiveForces(graph, vertices, vrep, rootSpace);
				}				
			}
			
			//calculate attractive forces			
			for(DefaultEdge edge : edges) {			
				calcAttractiveForces(graph, rootSpace, edge);			
			}
			
			for(PathwayVertexRep vrep : vertices) {
				calcNewVertexPositions(graph, area, temperature, vrep);
			}
			
			temperature = coolTemperature(temperature, i);
			
		}
		
		int debug;
		
	}
	
	private void calcRepulsiveForces(PathwayGraphWrapper graph, Set<PathwayVertexRep> vertices, PathwayVertexRep currentVertex, float space) {
		float xDisparity = graph.getDisplacement(currentVertex).x();
		float yDisparity = graph.getDisplacement(currentVertex).y();
		for(PathwayVertexRep vrep : vertices) {
			if(vrep.getID() != currentVertex.getID()) {
				float xDistance = vrep.getCenterX() - currentVertex.getCenterX();
				float yDistance = vrep.getCenterY() - currentVertex.getCenterY();
				
				float distance = calcDistance(xDistance,yDistance);
				float repulsiveForce = space*space/distance;
				
				if(distance > 0) {					
					xDisparity += xDistance/distance * repulsiveForce;
					yDisparity += yDistance/distance * repulsiveForce;
					
					
				}
			}
		}
		
		graph.setDisplacement(currentVertex, new Vec2f(xDisparity,yDisparity));

	}
	
	private void calcAttractiveForces(PathwayGraphWrapper graph, float space, DefaultEdge currentEdge) {
		PathwayVertexRep sourceVertex = graph.getEdgeSource(currentEdge);
		PathwayVertexRep targetVertex = graph.getEdgeTarget(currentEdge);
		
		float xDisparitySource = graph.getDisplacement(sourceVertex).x();
		float yDisparitySource = graph.getDisplacement(sourceVertex).y();
		float xDisparityTarget = graph.getDisplacement(targetVertex).x();
		float yDisparityTarget = graph.getDisplacement(targetVertex).y();
		
		float xDistance = sourceVertex.getCenterX()-targetVertex.getCenterX();
		float yDistance = sourceVertex.getCenterY()-targetVertex.getCenterY();
		
		float distance = calcDistance(xDistance, yDistance);
		float attractiveForce = distance*distance/space;
		
		if(distance > 0) {	
			xDisparitySource -= xDistance/distance * attractiveForce;
			yDisparitySource -= yDistance/distance * attractiveForce;
			xDisparityTarget += xDistance/distance * attractiveForce;
			yDisparityTarget += yDistance/distance * attractiveForce;
		}		
		graph.setDisplacement(sourceVertex, new Vec2f(xDisparitySource,yDisparitySource));
		graph.setDisplacement(sourceVertex, new Vec2f(xDisparityTarget,yDisparityTarget));
	}
	
	private void calcNewVertexPositions(PathwayGraphWrapper graph, Rectangle2D area, float temperature, PathwayVertexRep currentVertex) {
		float xPosition = currentVertex.getCenterX();
		float yPosition = currentVertex.getCenterY();
		float xDisplacement = graph.getDisplacement(currentVertex).x();
		float yDisplacement = graph.getDisplacement(currentVertex).y();
		float displacementDistance = calcDistance(xDisplacement, yDisplacement);
		
		float maxDisplacementLimit = Math.min(displacementDistance, temperature);
		
		xPosition += xDisplacement/displacementDistance*maxDisplacementLimit;
		yPosition += yDisplacement/displacementDistance*maxDisplacementLimit;
		
//		double areaResrictedXPosition = Math.min(area.getWidth()/2, Math.max(-area.getWidth()/2, xPosition));
//		double areaResrictedYPosition = Math.min(area.getHeight()/2, Math.max(-area.getHeight()/2, yPosition));
		
		graph.setVertexPosition(currentVertex, xPosition, yPosition);
	}
	
	
	private float calcDistance(float x, float y) {
		return (float)Math.sqrt(x * x + y * y);
	}
	
	private float coolTemperature(float temp, int iteration) {
		return temp / 10;
	}
	


}
