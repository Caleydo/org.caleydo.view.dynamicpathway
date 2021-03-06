package org.caleydo.view.dynamicpathway.ui;

import java.util.List;
import java.util.Set;

import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

import com.google.common.collect.Lists;

public class NodeCompoundElement extends ANodeElement {
	protected static final int INNER_PADDING = 1;
	private static final int FONT_SIZE_MULTIPLIER = 8;
	private static final int TEXT_X_POS = -20;
	private static final int TEXT_Y_POS = 8;
	private static final float HIGHLIGHT_RIGHT_PADDING = INNER_PADDING + 1 + 0.5f;
	private static final float WIDTH_AND_HEIGHT_ADDEND = 2.5f;
	
	private static final float RADIUS = 10.0f;

	public NodeCompoundElement(PathwayVertexRep vertexRep, List<PathwayVertex> pathwayVertices,
			final DynamicPathwaysCanvas parentGraph, Set<PathwayGraph> pathways) {
		super(vertexRep, pathwayVertices, parentGraph, pathways, WIDTH_AND_HEIGHT_ADDEND);
	
		
//		this.width = getSize().x();
//		this.height = getSize().y();

		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				AdvancedPick p = (AdvancedPick) pick;

				/**
				 * inform other views about picking event
				 */
				parentGraph.onSelect(vertices, NodeCompoundElement.this, pick);

				/**
				 * if the user right clicked - show context menu
				 */
				if (pick.getPickingMode() == PickingMode.RIGHT_CLICKED) {
					// parentGraph.setOrResetFilteringNode(NodeCompoundElement.this);

					context.getSWTLayer().showContextMenu(Lists.newArrayList(focusNodeMenu));
				}

				/**
				 * if the user clicked on the node
				 */
				if (pick.getPickingMode() == PickingMode.CLICKED) {

					parentGraph.setOrResetSelectedNode(NodeCompoundElement.this);

					if (p.isCtrlDown()) {
						// parentGraph.setOrResetFilteringNode(NodeCompoundElement.this);
						// parentGraph.filterPathwayList();
						makeThisFocusNode();
					}

				}
				/**
				 * if the user moved the curser over this node
				 */
				if (pick.getPickingMode() == PickingMode.MOUSE_OVER) {
					NodeCompoundElement.this.setNodeState(ENodeState.MOUSE_OVER);
				}

				/**
				 * if the user's curser left the node
				 */
				if (pick.getPickingMode() == PickingMode.MOUSE_OUT) {
					NodeCompoundElement.this.setNodeState(ENodeState.DEFAULT);
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
		/*
		 * Set the contour & filling colour according to the node's state (clicked, mouse over, used for filtering,...)
		 */
		
		String contourColor = state.getContourColor();
		String fillingColor = state.getFillingColor();
		
		this.height = h;
		this.width = w;
		this.centerX = this.getBounds().get(0);
		this.centerY = this.getBounds().get(1);

		// contour
		g.color(contourColor).fillCircle(0, 0, RADIUS + INNER_PADDING- HIGHLIGHT_RIGHT_PADDING);
		
		g.color(fillingColor).fillCircle(0, 0, RADIUS- HIGHLIGHT_RIGHT_PADDING);

		g.drawText(vertexRep.getName(), TEXT_X_POS, TEXT_Y_POS, RADIUS * FONT_SIZE_MULTIPLIER, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {

		super.renderPickImpl(g, w, h);

	}

}
