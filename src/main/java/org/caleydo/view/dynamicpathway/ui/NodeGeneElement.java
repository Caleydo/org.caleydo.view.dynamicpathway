package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeGeneElement extends NodeElement {
	
	private static final int INNER_BOUNDS = 2;
	private static final int OUTER_BOUNDS = 1;
	private static final int ROUND_EDGE_RADIUS = 2;	

	public NodeGeneElement(PathwayVertexRep vertexRep, final DynamicPathwayGraphRepresentation parentGraph) {
		
		super(vertexRep, parentGraph);
		
		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				/**
				 * if the user clicked on the node
				 */
				if (pick.getPickingMode() == PickingMode.CLICKED) {
					
					/** 
					 * select or deselect current node
					 */
					parentGraph.setOrResetSelectedNode(NodeGeneElement.this);
					
				}
				/**
				 * if the user moved the curser over this node
				 */
				if (pick.getPickingMode() == PickingMode.MOUSE_OVER) {
					isMouseOver = true;
				}
				
				/**
				 * if the user's curser left the node
				 */
				if (pick.getPickingMode() == PickingMode.MOUSE_OUT) {
					isMouseOver = false;
				}
				
				/** 
				 * renderImpl is called
				 */
				repaint();	

			}
		});
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		
		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();
		
		if(isThisNodeSelected) {
			g.color(SELECTION_CONTOUR_COLOR).fillRoundedRect(-1, -1, width+INNER_BOUNDS+2, height+INNER_BOUNDS+2,ROUND_EDGE_RADIUS);
		}
		else if(isMouseOver) {
			g.color(MOUSEROVER_CONTOUR_COLOR).fillRoundedRect(-1, -1, width+INNER_BOUNDS+2, height+INNER_BOUNDS+2,ROUND_EDGE_RADIUS);
		}
		else {
			g.color(CONTOUR_COLOR).fillRoundedRect(0, 0, width+INNER_BOUNDS, height+INNER_BOUNDS,ROUND_EDGE_RADIUS);
		}
		
		g.color(FILLING_COLOR).fillRoundedRect(OUTER_BOUNDS, OUTER_BOUNDS, width, height,ROUND_EDGE_RADIUS);
		g.drawText(vertexRep.getName(), 0, 0, width, FONT_SIZE);
		
		
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
	
		super.renderPickImpl(g,w,h);
		

	}
	


}
