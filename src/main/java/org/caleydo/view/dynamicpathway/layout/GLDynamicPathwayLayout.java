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
	
	private static final int MAX_ITERATIONS = 700;
	private static final int MAX_TEMPERATURE = 100;
	
	private double temperature;
	private int maxIterations;
	private double globalEdgeLength;
	private double cooldown;
	private double area;
	private double width;
	private double height;
	private boolean temperatureAndCooldownSetByUser;


	public GLDynamicPathwayLayout() {
		this.maxIterations = MAX_ITERATIONS;
		this.temperatureAndCooldownSetByUser = false;
	}
	
	public GLDynamicPathwayLayout(int maxIterations, double temperature, double cooldown) {
		this.maxIterations = maxIterations;
		this.temperature = temperature;
		this.cooldown = cooldown;
		
		this.temperatureAndCooldownSetByUser = true;	
	}

	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h,
			IGLLayoutElement parent, int deltaTimeMs) {
		// TODO Auto-generated method stub
//		Map<DefaultEdge, Pair<PathwayVertexRep, PathwayVertexRep>> edges = graph.getCombinedEdges();
		
		parent.setBounds(0, 0, w, h);
		
		width = w;
		height = h;
		
		if(!temperatureAndCooldownSetByUser) {
			temperature = width/10;
			cooldown = temperature/maxIterations;
		}
		
		area = width*height;
		globalEdgeLength = (float)Math.sqrt(area/children.size());	
		
		
		GLElementContainer verticeContainer = (GLElementContainer)parent.asElement();
		DynamicPathwayElement pathwayElement = (DynamicPathwayElement)verticeContainer.getParent();
		DynamicPathwayGraph graph = pathwayElement.getDynamicPathway();
		Set<DefaultEdge> edgeSet = pathwayElement.getDynamicPathway().getCombinedEdgeSet();

		for(int i = 1; i <= maxIterations; i++) {
			
			
			/**
			 * reset Displacement for the current iteration
			 * otherwise it would get too big and the nodes would
			 * be out of bounds
			 */
			for(IGLLayoutElement child : children) {
				NodeElement node = (NodeElement)child.asElement();
				node.setDisplacement(0.0f, 0.0f);
			}
			
			for(IGLLayoutElement child : children) {
				
				NodeElement node = (NodeElement)child.asElement();
				
//				node.setDisplacement(0.0f, 0.0f);
				calcRepulsiveForces(children, node);
				
			}
			/**
			 * calculate 
			 */
			for(DefaultEdge edge : edgeSet) {
				calcAttractiveForces(graph, edge);
			}
			
			
			for(IGLLayoutElement child : children) {
				NodeElement node = (NodeElement)child.asElement();
				
				calcNewVertexPositions(node, child);
			}
			
			coolDownTemp();
			
		}
		
		return false;
	}
	
	/**
	 * 
	 * @param children renderable children objects of the container, which contain the actual Nodes
	 * @param currentNode node for which the displacement is calculated
	 * 
	 * calculate the repulsive forces between all nodes, afterwards the displacement 
	 * of the node is accordingly set
	 */
	private void calcRepulsiveForces(List<? extends IGLLayoutElement> children, NodeElement currentNode) {
		double xDisplacement = currentNode.getDisplacementX();
		double yDisplacement = currentNode.getDisplacementY();
		
		for(IGLLayoutElement otherChild : children) {
			NodeElement otherNode = (NodeElement)otherChild.asElement();
			
			if(otherNode.getVertex().getID() != currentNode.getVertex().getID()) {
				double xDistance = currentNode.getCenterX() - otherNode.getCenterX();
				double yDistance = currentNode.getCenterY() - otherNode.getCenterY();
				
				double distance = calcDistance(xDistance,yDistance);
				double repulsiveForce = globalEdgeLength*globalEdgeLength/(distance*distance);
				
				if(distance > 0) {					
					xDisplacement += (xDistance/distance) * repulsiveForce;
					yDisplacement += (yDistance/distance) * repulsiveForce;	
					currentNode.setDisplacement(xDisplacement, yDisplacement);
				}				
			}			
		}
		
		
	}
	
	/**
	 * 
	 * @param graph needed to get the source & target nodes of the edge
	 * @param currentEdge edge, which gives us the nodes, which's displacement will be recalculated (to be closer)
	 */
	private void calcAttractiveForces(DynamicPathwayGraph graph, DefaultEdge currentEdge) {
		NodeElement sourceNode = graph.getNodeOfVertex(graph.getEdgeSource(currentEdge));
		NodeElement targetNode = graph.getNodeOfVertex(graph.getEdgeTarget(currentEdge));
		
		double xDisplacementSource = sourceNode.getDisplacementX();
		double yDisplacementSource = sourceNode.getDisplacementY();
		double xDisplacementTarget = targetNode.getDisplacementX();
		double yDisplacementTarget = targetNode.getDisplacementY();
		
		double xDistance = sourceNode.getCenterX() - targetNode.getCenterX();
		double yDistance = sourceNode.getCenterY() - targetNode.getCenterY();
		
		double distance = calcDistance(xDistance, yDistance);
		double attractiveForce = distance*distance/globalEdgeLength;
		
		if(distance > 0) {	
			xDisplacementSource -= xDistance/distance * attractiveForce;
			yDisplacementSource -= yDistance/distance * attractiveForce;
			xDisplacementTarget += xDistance/distance * attractiveForce;
			yDisplacementTarget += yDistance/distance * attractiveForce;
		}		
		sourceNode.setDisplacement(xDisplacementSource, yDisplacementSource);
		targetNode.setDisplacement(xDisplacementTarget, yDisplacementTarget);		
	}
	
	/**
	 * 
	 * @param currentNode node, which's position should be changed
	 * @param child used to set the drawing bounds for the rendered object
	 * 
	 * the object's position is not really changed, but its drawing bounds
	 */
	private void calcNewVertexPositions(NodeElement currentNode, IGLLayoutElement child) {
		double xPosition = currentNode.getCenterX();
		double yPosition = currentNode.getCenterY();
		double xDisplacement = currentNode.getDisplacementX();
		double yDisplacement = currentNode.getDisplacementY();	
		double displacementDistance = calcDistance(xDisplacement, yDisplacement);
		
		double maxDisplacementLimit = Math.min(displacementDistance, temperature);
		
		if(maxDisplacementLimit > 0) {
			xPosition += (xDisplacement/displacementDistance)*maxDisplacementLimit;
			yPosition += (yDisplacement/displacementDistance)*maxDisplacementLimit;
		}


		double vertexWidth = currentNode.getVertex().getWidth();
		double vertexHeight = currentNode.getVertex().getHeight();
		

		
		
//		if(xPosition+vertexWidth >= this.width)
//			newXPos = width-vertexWidth/2-20;
//		if(yPosition+vertexHeight >= this.height)
//			newYPos = height-vertexHeight/2-20;
		
		double borderWidth = width/50.0;
		
		if(xPosition < borderWidth) {
			xPosition = borderWidth + Math.random()*borderWidth*2.0;
		}
		else if(xPosition > (width-borderWidth)) {
			xPosition = width-borderWidth-Math.random()*borderWidth*2.0;
		}
		
		if(yPosition < borderWidth) {
			yPosition = borderWidth + Math.random() * borderWidth * 2.0;
		}
		else if(yPosition > (height-borderWidth)) {
			yPosition = height - borderWidth - Math.random() * borderWidth * 2.0;
		}
		
		double newXPos = xPosition-vertexWidth/2;
		double newYPos = yPosition-vertexHeight/2;
		
		child.setBounds((float)newXPos, (float)newYPos, (float)vertexWidth, (float)vertexHeight);
		currentNode.setCenter(xPosition, yPosition);
//		currentNode.setCoords((short)newXPos, (short)newYPos, currentNode.getVertex().getWidth(), currentNode.getVertex().getHeight());
		
		
	}
	
	
	private double calcDistance(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}
	
	private void coolDownTemp() {
		temperature -= cooldown;
		if(temperature < 0)
			temperature = 0;
	}
	

}
