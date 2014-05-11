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
	
	private static final int MAX_ITERATIONS = 5;
	private static final int MAX_TEMPERATURE = 100;
	private static final float STEP = 0.1f;
	
	private float temperature;
	private float space;
	private float cooldown;
	private float area;
	private float width;
	private float height;


	public GLDynamicPathwayLayout() {
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h,
			IGLLayoutElement parent, int deltaTimeMs) {
		// TODO Auto-generated method stub
//		Map<DefaultEdge, Pair<PathwayVertexRep, PathwayVertexRep>> edges = graph.getCombinedEdges();
		
		parent.setBounds(0, 0, w, h);
		
		width = w;
		height = h;
		
		temperature = width/10;
		area = width*height;
		space = (float)Math.sqrt(area/children.size());	
		cooldown = temperature/MAX_ITERATIONS;

		
		GLElementContainer verticeContainer = (GLElementContainer)parent.asElement();
		DynamicPathwayElement pathwayElement = (DynamicPathwayElement)verticeContainer.getParent();
		DynamicPathwayGraph graph = pathwayElement.getDynamicPathway();
		Set<DefaultEdge> edgeSet = pathwayElement.getDynamicPathway().getCombinedEdgeSet();
		
//		float scaleX = width/graph.getFocusPathwayWidth();
//		float scaleY = height/graph.getFocusPathwayHeight();
//
//		for(IGLLayoutElement child : children) {
//			NodeElement node = (NodeElement)child.asElement();
//			node.setCenter(node.getCenterX()*scaleX, node.getCenterY()*scaleY);
//		}
		
		
		int i = 1;
		while(i<=MAX_ITERATIONS) {
			
			for(IGLLayoutElement child : children) {
				
				NodeElement node = (NodeElement)child.asElement();
				
				node.setDisplacement(0.0f, 0.0f);
				calcRepulsiveForces(children, node);
				
			}
			
			for(DefaultEdge edge : edgeSet) {
				calcAttractiveForces(graph, edge);
			}
			
			
			for(IGLLayoutElement child : children) {
				NodeElement node = (NodeElement)child.asElement();
				
				calcNewVertexPositions(node, child);
			}
			
			coolDownTemp();
			
			i++;
			
		}
		
		return false;
	}
	
	private void calcRepulsiveForces(List<? extends IGLLayoutElement> children, NodeElement currentNode) {
		float xDisplacement = currentNode.getDisplacementX();
		float yDisplacement = currentNode.getDisplacementY();
		
		for(IGLLayoutElement otherChild : children) {
			NodeElement otherNode = (NodeElement)otherChild.asElement();
			
			if(otherNode.getVertex().getID() != currentNode.getVertex().getID()) {
				float xDistance = currentNode.getCenterX() - otherNode.getCenterX();
				float yDistance = currentNode.getCenterY() - otherNode.getCenterY();
				
				float otherNodeXDisplacement = otherNode.getDisplacementX();
				float otherNodeYDisplacement = otherNode.getDisplacementY();
				
				float distance = calcDistance(xDistance,yDistance);
				float repulsiveForce = space*space/distance;
				
				if(distance > 0) {					
					xDisplacement += (xDistance/distance) * repulsiveForce;
					yDisplacement += (yDistance/distance) * repulsiveForce;	
					currentNode.setDisplacement(xDisplacement, yDisplacement);
				}				
			}			
		}
		
		
	}
	
	private void calcAttractiveForces(DynamicPathwayGraph graph, DefaultEdge currentEdge) {
		NodeElement sourceNode = graph.getNodeOfVertex(graph.getEdgeSource(currentEdge));
		NodeElement targetNode = graph.getNodeOfVertex(graph.getEdgeTarget(currentEdge));
		
		float xDisplacementSource = sourceNode.getDisplacementX();
		float yDisplacementSource = sourceNode.getDisplacementY();
		float xDisplacementTarget = targetNode.getDisplacementX();
		float yDisplacementTarget = targetNode.getDisplacementY();
		
		float xDistance = sourceNode.getCenterX() - targetNode.getCenterX();
		float yDistance = sourceNode.getCenterY() - targetNode.getCenterY();
		
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
	
	private void calcNewVertexPositions(NodeElement currentNode, IGLLayoutElement child) {
		float xPosition = currentNode.getCenterX();
		float yPosition = currentNode.getCenterY();
		float xDisplacement = currentNode.getDisplacementX();
		float yDisplacement = currentNode.getDisplacementY();	
		float displacementDistance = calcDistance(xDisplacement, yDisplacement);
		
		float maxDisplacementLimit = Math.min(displacementDistance, temperature);
		
		if(maxDisplacementLimit > 0) {
			xPosition += (xDisplacement/displacementDistance)*maxDisplacementLimit;
			yPosition += (yDisplacement/displacementDistance)*maxDisplacementLimit;
		}


		float vertexWidth = currentNode.getVertex().getWidth();
		float vertexHeight = currentNode.getVertex().getHeight();
		
		float newXPos = xPosition-vertexWidth/2;
		float newYPos = yPosition-vertexHeight/2;
		
		
//		if(xPosition+vertexWidth >= this.width)
//			newXPos = width-vertexWidth/2-20;
//		if(yPosition+vertexHeight >= this.height)
//			newYPos = height-vertexHeight/2-20;
		
		child.setBounds(newXPos, newYPos, vertexWidth, vertexHeight);
		currentNode.setCenter(newXPos, newYPos);
//		currentNode.setCoords((short)newXPos, (short)newYPos, currentNode.getVertex().getWidth(), currentNode.getVertex().getHeight());
		
		
	}
	
	
	private float calcDistance(float x, float y) {
		return (float)Math.sqrt(x * x + y * y);
	}
	
	private void coolDownTemp() {
		temperature -= cooldown;
		if(temperature < 0)
			temperature = 0;
	}
	

}
