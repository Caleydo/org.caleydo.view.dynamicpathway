package org.caleydo.view.dynamicpathway.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.caleydo.core.util.base.ILabelProvider;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.IGLElementContext;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
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

				parentGraph.onSelect(displayedVertex, NodeGeneElement.this, pick);

				/**
				 * if the user right clicked - show context menu
				 */
				if (pick.getPickingMode() == PickingMode.RIGHT_CLICKED) {
					parentGraph.setOrResetSelectedNode(NodeGeneElement.this);
					// TODO: show context menu
					// parentGraph.filterOrUnfilterPathwayList();
					GenericContextMenuItem e = new GenericContextMenuItem("filter pathways",
							new FilterPathwayListByVertexEvent(NodeGeneElement.this));
					context.getSWTLayer().showContextMenu(Lists.newArrayList(e));
				}

				/**
				 * if the user clicked on the node
				 */
				if (pick.getPickingMode() == PickingMode.CLICKED) {

					parentGraph.setOrResetSelectedNode(NodeGeneElement.this);

					if (p.isCtrlDown()) {
						parentGraph.filterOrUnfilterPathwayList();
					}

					/**
					 * select or deselect current node
					 */

					// parentGraph.setOrResetSelectedNode(NodeGeneElement.this);

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
	protected void init(IGLElementContext context) {
		// TODO Auto-generated method stub
		super.init(context);

		// create a tooltip listener to render the tooltip of this element
		this.onPick(context.getSWTLayer().createTooltip(new ILabeled() {
			@Override
			public String getLabel() {
				StringBuilder builder = new StringBuilder();
				Set<PathwayVertex> vertices = new LinkedHashSet<>();
				for (PathwayVertex vRep : vertexRep.getPathwayVertices()) {
					vertices.add(vRep);
				}
				List<String> names = new ArrayList<>(vertices.size());
				for (PathwayVertex v : vertices) {
					names.add(v.getHumanReadableName());
				}
				Collections.sort(names);
				for (int i = 0; i < names.size(); i++) {
					builder.append(names.get(i));
					if (i < names.size() - 1)
						builder.append(", ");
				}
				return builder.toString();
				// return NodeGeneElement.this.vertexRep.getName();
			}
		}));
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		short width = vertexRep.getWidth();
		short height = vertexRep.getHeight();

		if (isThisNodeSelected) {
			g.color(SELECTION_CONTOUR_COLOR).fillRoundedRect(-1, -1, width + INNER_BOUNDS + 2,
					height + INNER_BOUNDS + 2, ROUND_EDGE_RADIUS);
		} else if (isMouseOver) {
			g.color(MOUSEROVER_CONTOUR_COLOR).fillRoundedRect(-1, -1, width + INNER_BOUNDS + 2,
					height + INNER_BOUNDS + 2, ROUND_EDGE_RADIUS);
		} else {
			g.color(CONTOUR_COLOR).fillRoundedRect(0, 0, width + INNER_BOUNDS, height + INNER_BOUNDS,
					ROUND_EDGE_RADIUS);
		}

		g.color(FILLING_COLOR).fillRoundedRect(OUTER_BOUNDS, OUTER_BOUNDS, width, height, ROUND_EDGE_RADIUS);

		if (displayedVertex == null)
			displayedVertex = vertexRep.getPathwayVertices().get(0);

		g.drawText(label, 0, 0, width, FONT_SIZE);

	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {

		super.renderPickImpl(g, w, h);

	}

}
