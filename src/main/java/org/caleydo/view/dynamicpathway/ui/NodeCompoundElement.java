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
	private static final float WIDTH_AND_HEIGHT_ADDEND = 2.5f;

	public NodeCompoundElement(PathwayVertexRep vertexRep, List<PathwayVertex> pathwayVertices,
			final DynamicPathwaysCanvas parentGraph, Set<PathwayGraph> pathways) {
		super(vertexRep, pathwayVertices, parentGraph, pathways, WIDTH_AND_HEIGHT_ADDEND);

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
		short width = (short) w;// vertexRep.getWidth();

		// contour
		if (isThisNodeUsedForFiltering) {
			g.color(FILTER_CONTOUR_COLOR).fillCircle(0, 0, width);
		} else if (isThisNodeSelected) {
			g.color(SELECTION_CONTOUR_COLOR).fillCircle(0, 0, width);
		} else if (isMouseOver) {
			g.color(MOUSEROVER_CONTOUR_COLOR).fillCircle(0, 0, width);
		} else {
			g.color(CONTOUR_COLOR).fillCircle(0, 0, width + INNER_PADDING- HIGHLIGHT_RIGHT_PADDING);
		}

		g.color(NODE_FILLING_COLOR).fillCircle(0, 0, width- HIGHLIGHT_RIGHT_PADDING);

		// TODO: label?
		g.drawText(vertexRep.getName(), TEXT_X_POS, TEXT_Y_POS, width * FONT_SIZE_MULTIPLIER, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {

		super.renderPickImpl(g, w, h);

	}

}
