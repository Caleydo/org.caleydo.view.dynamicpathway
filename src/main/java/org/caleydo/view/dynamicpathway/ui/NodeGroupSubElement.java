package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

/**
 * nodes which are part of a NodeGroupElement
 * @author Christiane Schwarzl
 *
 */
public class NodeGroupSubElement extends NodeElement {
	
	private static final int OUTER_BOUNDS = 2;
	private static final int INNER_BOUNDS = 1;
	private static final int ROUND_EDGE_RADIUS = 2;	

	float width;
	float height;

	public NodeGroupSubElement(PathwayVertexRep vertexRep, final DynamicPathwayGraphRepresentation parentGraph,
			double topLeftCornerX, double topLeftCornerY, float width, float height) {
		
		super(vertexRep, parentGraph);

		this.coords.setCoords(topLeftCornerX, topLeftCornerY, width, height);
		this.width = width;
		this.height = height;
		this.displayedVertex = vertexRep.getPathwayVertices().get(0);
		this.label = displayedVertex.getHumanReadableName();
		
		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				parentGraph.onSelect(displayedVertex, NodeGroupSubElement.this, pick);
				
				/**
				 * if the user clicked on the node
				 */
				AdvancedPick p = (AdvancedPick)pick;
				
				
				
				
				if (pick.getPickingMode() == PickingMode.CLICKED) {
					
					if(p.isCtrlDown()) {
						//TODO: filter
						System.out.println("jasdlkdsjlsakdjsak");
					}
					
					/** 
					 * select or deselect current node
					 */					
					
					parentGraph.setOrResetSelectedNode(NodeGroupSubElement.this);
					
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
		
		repaint();
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		float x = this.coords.getTopLeft().getFirst().floatValue();
		float y = this.coords.getTopLeft().getSecond().floatValue();

		g.color(CONTOUR_COLOR).fillRoundedRect(x, y, width + OUTER_BOUNDS, height + OUTER_BOUNDS, 2);
		g.color(FILLING_COLOR).fillRoundedRect(x + INNER_BOUNDS, y + INNER_BOUNDS, width,
				height, ROUND_EDGE_RADIUS);
		g.drawText(vertexRep.getShortName(), x, y, width, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub
		super.renderPickImpl(g, w, h);
	}
	
	

}
