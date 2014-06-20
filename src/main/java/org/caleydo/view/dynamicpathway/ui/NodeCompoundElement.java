package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

public class NodeCompoundElement extends NodeElement {
	
	private static final int INNER_BOUNDS = 1;
	private static final int FONT_SIZE_MULTIPLIER = 8;
	private static final int TEXT_X_POS = -20;
	private static final int TEXT_Y_POS = 8;

	public NodeCompoundElement(PathwayVertexRep vertexRep, final DynamicPathwayGraphRepresentation parentGraph) {
		super(vertexRep, parentGraph);
		setVisibility(EVisibility.PICKABLE);
		
		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				/**
				 * if the user clicked on the node
				 */
				if (pick.getPickingMode() == PickingMode.CLICKED) {
					
					/**
					 * if there was another node selected before, reset it
					 */
					if(parentGraph.getCurrentSelectedNode() != null) {
						parentGraph.getCurrentSelectedNode().setIsNodeSelected(false);				
					}
					
					isThisNodeSelected = true;
					parentGraph.setCurrentSelectedNode(NodeCompoundElement.this);
					
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

		
		if(isThisNodeSelected) {
			g.color(SELECTION_CONTOUR_COLOR).fillCircle(-1, -1, width+INNER_BOUNDS+1);;
		}
		else if(isMouseOver) {
			g.color(MOUSEROVER_CONTOUR_COLOR).fillCircle(-1, -1, width+INNER_BOUNDS+1+0.5f);;
		}
		else {
			g.color(CONTOUR_COLOR).fillCircle(0, 0, width+INNER_BOUNDS);
		}
		
		g.color(FILLING_COLOR).fillCircle(0, 0, width);

		
		g.drawText(vertexRep.getName(), TEXT_X_POS, TEXT_Y_POS, width*FONT_SIZE_MULTIPLIER, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub

	}

}
