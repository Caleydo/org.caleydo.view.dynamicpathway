package org.caleydo.view.dynamicpathway.layout;

import java.util.List;
import java.util.Set;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.ui.DynamicPathwayElement;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

public class GLDynamicPathwayLayout implements IGLLayout2 {
	
	private static final int MAX_ITERATIONS = 1;
	private static final int MAX_TEMPERATURE = 400;
	private static final float STEP = 0.1f;

	public GLDynamicPathwayLayout() {
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h,
			IGLLayoutElement parent, int deltaTimeMs) {
		// TODO Auto-generated method stub
//		Map<DefaultEdge, Pair<PathwayVertexRep, PathwayVertexRep>> edges = graph.getCombinedEdges();
		
		float temperature = MAX_TEMPERATURE;
		double space = (w*h)/children.size();
		float rootSpace = (float)Math.sqrt(space);	
		
		GLElementContainer verticeContainer = (GLElementContainer)parent.asElement();
		DynamicPathwayElement pathwayElement = (DynamicPathwayElement)verticeContainer.getParent();
		DynamicPathwayGraph graph = pathwayElement.getDynamicPathway();
		Set<DefaultEdge> edgeSet = pathwayElement.getDynamicPathway().getCombinedEdgeSet();
		//		
		for(int i = 1; i <= MAX_ITERATIONS; i++) {
			
			for(IGLLayoutElement child : children) {
				child.setBounds(0, 0, w, h);
				
				NodeElement node = (NodeElement)child.asElement();
				
				if(node.getCenterX() != 0 || node.getCenterY() != 0) {
					node.setDisplacement(0.0f, 0.0f);
					calcRepulsiveForces(children, node, rootSpace);
				}
				
			}
			
			for(DefaultEdge edge : edgeSet) {
				calcAttractiveForces(graph, rootSpace, edge);
			}
			
			for(IGLLayoutElement child : children) {
				NodeElement node = (NodeElement)child.asElement();
				
				calcNewVertexPositions(temperature, node, child);
			}
			
			
			
		}
		
		return false;
	}
	
	private void calcRepulsiveForces(List<? extends IGLLayoutElement> children, NodeElement currentNode, float space) {
		float xDisplacement = currentNode.getDisplacementX();
		float yDisplacement = currentNode.getDisplacementY();
		
		for(IGLLayoutElement otherChild : children) {
			NodeElement otherNode = (NodeElement)otherChild.asElement();
			
			if(otherNode.getVertex().getID() != currentNode.getVertex().getID()) {
				float xDistance = otherNode.getCenterX() - currentNode.getCenterX();
				float yDistance = otherNode.getCenterY() - currentNode.getCenterY();
				
				float distance = calcDistance(xDistance,yDistance);
				float repulsiveForce = space*space/distance;
				
				if(distance > 0) {					
					xDisplacement += (xDistance/distance) * repulsiveForce;
					yDisplacement += (yDistance/distance) * repulsiveForce;		
				}				
			}			
		}
		
		currentNode.setDisplacement(xDisplacement, yDisplacement);
	}
	
	private void calcAttractiveForces(DynamicPathwayGraph graph, float space, DefaultEdge currentEdge) {
		NodeElement sourceNode = graph.getNodeOfVertex(graph.getEdgeSource(currentEdge));
		NodeElement targetNode = graph.getNodeOfVertex(graph.getEdgeTarget(currentEdge));
		
		float xDisplacementSource = sourceNode.getDisplacementX();
		float yDisplacementSource = sourceNode.getDisplacementY();
		float xDisplacementTarget = targetNode.getDisplacementX();
		float yDisplacementTarget = targetNode.getDisplacementY();
		
		float xDistance = sourceNode.getCenterX()-targetNode.getCenterX();
		float yDistance = sourceNode.getCenterY()-targetNode.getCenterY();
		
		float distance = calcDistance(xDistance, yDistance);
		float attractiveForce = distance*distance/space;
		
		if(distance > 0) {	
			xDisplacementSource -= xDistance/distance * attractiveForce;
			yDisplacementSource -= yDistance/distance * attractiveForce;
			xDisplacementTarget += xDistance/distance * attractiveForce;
			yDisplacementTarget += yDistance/distance * attractiveForce;
		}		
		sourceNode.setDisplacement(xDisplacementSource, yDisplacementSource);
		targetNode.setDisplacement(xDisplacementTarget, yDisplacementTarget);		
	}
	
	private void calcNewVertexPositions(float temperature, NodeElement currentNode, IGLLayoutElement child) {
		float xPosition = currentNode.getCenterX();
		float yPosition = currentNode.getCenterY();
		float xDisplacement = currentNode.getDisplacementX();
		float yDisplacement = currentNode.getDisplacementY();	
		float displacementDistance = calcDistance(xDisplacement, yDisplacement);
		
		float maxDisplacementLimit = Math.min(displacementDistance, temperature);
		
		xPosition += xDisplacement/displacementDistance*maxDisplacementLimit;
		yPosition += yDisplacement/displacementDistance*maxDisplacementLimit;
		
//		child.setBounds(xPosition, yPosition, currentNode.getVertex().getWidth(), currentNode.getVertex().getHeight());
//		currentNode.setCoords((short)0, (short)0, currentNode.getVertex().getWidth(), currentNode.getVertex().getHeight());
		
		currentNode.setCoords((short)xPosition, (short)yPosition, currentNode.getVertex().getWidth(), currentNode.getVertex().getHeight());
	}
	
	
	private float calcDistance(float x, float y) {
		return (float)Math.sqrt(x * x + y * y);
	}
	

}
