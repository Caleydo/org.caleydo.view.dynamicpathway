package org.caleydo.view.dynamicpathway.ui;


import java.util.List;
import java.util.Set;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;

import com.google.common.collect.Lists;

public class NodeCompoundElement extends NodeElement {
	protected static final int INNER_PADDING = 1;
	private static final int FONT_SIZE_MULTIPLIER = 8;
	private static final int TEXT_X_POS = -20;
	private static final int TEXT_Y_POS = 8;
	private static final float HIGHLIGHT_LEFT_PADDING = -0.2f;
	private static final float HIGHLIGHT_RIGHT_PADDING = INNER_PADDING + 1 + 0.5f;

	public NodeCompoundElement(PathwayVertexRep vertexRep, List<PathwayVertex> pathwayVertices,
			final DynamicPathwayGraphRepresentation parentGraph, Color nodeColor, Set<PathwayGraph> pathways) {
		super(vertexRep, pathwayVertices, parentGraph, nodeColor, pathways);

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
//					parentGraph.setOrResetFilteringNode(NodeCompoundElement.this);

					context.getSWTLayer().showContextMenu(Lists.newArrayList(focusNodeMenu));
				}

				/**
				 * if the user clicked on the node
				 */
				if (pick.getPickingMode() == PickingMode.CLICKED) {

					parentGraph.setOrResetSelectedNode(NodeCompoundElement.this);

					if (p.isCtrlDown()) {
//						parentGraph.setOrResetFilteringNode(NodeCompoundElement.this);
//						parentGraph.filterPathwayList();
						makeThisFocusNode();
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
		short width = vertexRep.getWidth();

		// contour
		if (isThisNodeUsedForFiltering) {
			g.color(FILTER_CONTOUR_COLOR).fillCircle(-HIGHLIGHT_LEFT_PADDING, HIGHLIGHT_LEFT_PADDING,
					width + HIGHLIGHT_RIGHT_PADDING);
		} else if (isThisNodeSelected) {
			g.color(SELECTION_CONTOUR_COLOR).fillCircle(-HIGHLIGHT_LEFT_PADDING, HIGHLIGHT_LEFT_PADDING,
					width + HIGHLIGHT_RIGHT_PADDING);
		} else if (isMouseOver) {
			g.color(MOUSEROVER_CONTOUR_COLOR).fillCircle(HIGHLIGHT_LEFT_PADDING, HIGHLIGHT_LEFT_PADDING,
					width + HIGHLIGHT_RIGHT_PADDING);
		} else {
			g.color(CONTOUR_COLOR).fillCircle(0, 0, width + INNER_PADDING);
		}

		// filling
		GLGraphics filling = g.color(nodeColor);
//		if (parentGraph.getDynamicPathway().getFocusGraph() == vertexRep.getPathway())
//			filling = g.color(FOCUS_FILLING_COLOR);
//		else if (parentGraph.getDynamicPathway().getCombinedGraph() == vertexRep.getPathway())
//			filling = g.color(COMBINED_FILLING_COLOR);
//		else
//			filling = g.color(KONTEXT_FILLING_COLOR);

		filling.fillCircle(0, 0, width);

		// TODO: label?
		g.drawText(vertexRep.getName(), TEXT_X_POS, TEXT_Y_POS, width * FONT_SIZE_MULTIPLIER, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {

		super.renderPickImpl(g, w, h);

	}

}
