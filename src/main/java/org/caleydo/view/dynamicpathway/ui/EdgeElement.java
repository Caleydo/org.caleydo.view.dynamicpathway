package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.jgrapht.graph.DefaultEdge;


public class EdgeElement extends GLElement {
	private DefaultEdge edge;
	private NodeElement sourceNode;
	private NodeElement targetNode;
	private Direction direction;
	
	private enum Direction {
		TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
	}


	public EdgeElement(DefaultEdge edge, NodeElement sourceNode, NodeElement targetNode) {
		this.edge = edge;
		this.sourceNode = sourceNode;
		this.targetNode = targetNode;
		
		setDirection();

	}
	
	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		setDirection();
		
		double xSource = sourceNode.getCenterX();
		double ySource = sourceNode.getCenterY();
		double xTarget = targetNode.getCenterX();
		double yTarget = targetNode.getCenterY();	
				
//		switch(direction) {
//			case TOP_LEFT:
//				ySource -= (sourceNode.getVertex().getHeight()/2.0);
//				yTarget += (targetNode.getVertex().getHeight()/2.0);
//				break;
//			case TOP_RIGHT:
//				ySource -= (sourceNode.getVertex().getHeight()/2.0);
//				yTarget += (targetNode.getVertex().getHeight()/2.0);
//				break;
//			case BOTTOM_LEFT:
//				ySource += (sourceNode.getVertex().getHeight()/2.0);
//				yTarget -= (targetNode.getVertex().getHeight()/2.0);
//				if(yTarget <= ySource) {
//					ySource -= (sourceNode.getVertex().getHeight()/2.0);
//					yTarget += (targetNode.getVertex().getHeight()/2.0);
//					xSource -= (sourceNode.getVertex().getWidth()/2.0);
//					xTarget += (targetNode.getVertex().getWidth()/2.0);
//				}
//					
//				break;
//			case BOTTOM_RIGHT:
//				ySource += (sourceNode.getVertex().getHeight()/2.0);
//				yTarget -= (targetNode.getVertex().getHeight()/2.0);
//				break;
//			
//		}
		
		
		
		g.drawLine((float)xSource, (float)ySource, (float)xTarget, (float)yTarget);
		
		
	}
	
	/**
	 * determine which from which source edge to which target edge to draw, i.e. direction
	 * [source]						  				  	  [target]
	 * 		   \					 				 	 /	
	 *          \										/
	 *          [target]						[source]	
	 *  target node is on the bottom right 		target node is on the top right
	 *  -> xDirection is positive 				-> xDirection is positive
	 *  -> yDirection is positive 				-> yDirection is negative
	 *  
	 *  note: origin (0,0) is on the top left corner
	 */
	private void setDirection() {
		double xDirection = targetNode.getCenterX()-sourceNode.getCenterX();
		double yDirection = targetNode.getCenterY()-sourceNode.getCenterY();
		
		if(xDirection >= 0.0) {
			if(yDirection >= 0.0) 
				this.direction = Direction.BOTTOM_RIGHT;
			else
				this.direction = Direction.TOP_RIGHT;
		}
		else {
			if(yDirection >= 0.0)
				this.direction = Direction.BOTTOM_LEFT;
			else
				this.direction = Direction.TOP_LEFT;
		}
	}
	
}
