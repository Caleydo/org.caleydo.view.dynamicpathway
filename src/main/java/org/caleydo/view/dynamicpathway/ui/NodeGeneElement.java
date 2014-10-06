package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

import com.google.common.collect.Lists;

public class NodeGeneElement extends NodeElement {

	private static final int INNER_BOUNDS = 2;
	private static final int OUTER_BOUNDS = 1;
	private static final int ROUND_EDGE_RADIUS = 2;
	
	public NodeGeneElement(final PathwayVertexRep vertexRep,
			final DynamicPathwayGraphRepresentation parentGraph) {
		super(vertexRep, parentGraph);
		
		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				AdvancedPick p = (AdvancedPick) pick;
				
				/**
				 * inform other views about picking event
				 */
				parentGraph.onSelect(vertices, NodeGeneElement.this, pick);

				/**
				 * if the user right clicked - show context menu
				 */
				if (pick.getPickingMode() == PickingMode.RIGHT_CLICKED) {
					parentGraph.setOrResetFilteringNode(NodeGeneElement.this);

					context.getSWTLayer().showContextMenu(Lists.newArrayList(filterPathwayMenu));
					
				}

				/**
				 * if the user clicked on the node
				 */
				if (pick.getPickingMode() == PickingMode.CLICKED) {

					parentGraph.setOrResetSelectedNode(NodeGeneElement.this);

					if (p.isCtrlDown()) {
						parentGraph.setOrResetFilteringNode(NodeGeneElement.this);
						
						parentGraph.filterPathwayList();
					}


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

//		short width = vertexRep.getWidth();
//		short height = vertexRep.getHeight();
		
		short width = (short)this.width;
		short height = (short)this.height;		

		/**
		 * represent BORDER of node different:
		 * if it was used for filtering, clicked on, the mouse is moved over or nothing of these was done
		 */
		if (isThisNodeUsedForFiltering) {
			g.color(FILTER_CONTOUR_COLOR).fillRoundedRect(-1, -1, width + INNER_BOUNDS + 2,
					height + INNER_BOUNDS + 2, ROUND_EDGE_RADIUS);
		}
		else if (isThisNodeSelected) {
			g.color(SELECTION_CONTOUR_COLOR).fillRoundedRect(-1, -1, width + INNER_BOUNDS + 2,
					height + INNER_BOUNDS + 2, ROUND_EDGE_RADIUS);
		} else if (isMouseOver) {
			g.color(MOUSEROVER_CONTOUR_COLOR).fillRoundedRect(-1, -1, width + INNER_BOUNDS + 2,
					height + INNER_BOUNDS + 2, ROUND_EDGE_RADIUS);
		} else {
			g.color(CONTOUR_COLOR).fillRoundedRect(0, 0, width + INNER_BOUNDS, height + INNER_BOUNDS,
					ROUND_EDGE_RADIUS);
		}
		
		/**
		 * choose filling color according to which graph it belongs
		 */
		GLGraphics filling;
		if(parentGraph.getDynamicPathway().getFocusGraph() == vertexRep.getPathway())
			filling = g.color(FOCUS_FILLING_COLOR);
		else if(parentGraph.getDynamicPathway().getCombinedGraph() == vertexRep.getPathway())
			filling = g.color(COMBINED_FILLING_COLOR);
		else
			filling = g.color(KONTEXT_FILLING_COLOR);
			
		filling.fillRoundedRect(OUTER_BOUNDS, OUTER_BOUNDS, width, height, ROUND_EDGE_RADIUS);

		if (displayedVertex == null)
			displayedVertex = vertexRep.getPathwayVertices().get(0);

		g.drawText(label, 0, 0, width, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {

		super.renderPickImpl(g, w, h);

	}



	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return super.toString();
	}
	
	

}
