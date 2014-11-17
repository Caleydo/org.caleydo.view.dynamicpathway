package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.dynamicpathway.internal.DynamicPathwayView;

import com.google.common.collect.Lists;

public class ControllbarPathwayTitleEntry extends GLElement {
	private static final String BULLET_POINT = "• ";
	private static final float TEXT_HEIGHT = 12.0f;

	private static final Color NORMAL_TEXT_COLOR = Color.BLACK;
	private static final Color RIGHT_CLICKED_TEXT_COLOR = Color.ORANGE;

	private PathwayGraph representedPathway;
	private String pathwayTitle;
	private boolean rightClicked = false;
	private final boolean isFocusPathway;
	
	private Color normalTitleColor;

	/**
	 * menu item in right click menu that triggers the deletion of the pathway represented by this entry
	 */
	GenericContextMenuItem removePathwayMenuItem;
	GenericContextMenuItem makeFocusGraphMenuItem;

	public ControllbarPathwayTitleEntry(PathwayGraph representedPathway, Color titleColor, final boolean isFocusPathway, final DynamicPathwayView view) {
		this.normalTitleColor = titleColor;
		this.representedPathway = representedPathway;
		this.isFocusPathway = isFocusPathway;
		if (representedPathway != null)
			this.pathwayTitle = representedPathway.getTitle();
		else
			this.pathwayTitle = "";
		this.removePathwayMenuItem = new GenericContextMenuItem("Remove this pathway",
				new RemoveDisplayedPathwayEvent(this));
		this.makeFocusGraphMenuItem = new GenericContextMenuItem(
				"Make this pathway the focus pathway (switch roles)", new MakeFocusPathwayEvent(this));

		setSize(Float.NaN, TEXT_HEIGHT);

		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				AdvancedPick p = (AdvancedPick) pick;

				/**
				 * if the user right clicked - show context menu
				 */
				if (pick.getPickingMode() == PickingMode.RIGHT_CLICKED) {
//					context.getSWTLayer().showContextMenu(Lists.newArrayList(removePathwayMenuItem));

					if(isFocusPathway)
						context.getSWTLayer().showContextMenu(Lists.newArrayList(removePathwayMenuItem));
					else
						context.getSWTLayer().showContextMenu(Lists.newArrayList(removePathwayMenuItem, makeFocusGraphMenuItem));

					rightClicked = true;

				} else {
					rightClicked = false;
				}

				// //TODO: change to strg + del key
				// if(p.isCtrlDown() && pick.getPickingMode() == PickingMode.CLICKED) {
				// view.removeGraph(pathwayTitle);
				// }

				repaint();

			}

		});
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		// super.renderImpl(g, w, h);

		Color textColor;
		if (rightClicked)
			textColor = RIGHT_CLICKED_TEXT_COLOR;
		else
			textColor = normalTitleColor;

		g.color(textColor).drawText(BULLET_POINT + pathwayTitle, 0, 0, w, TEXT_HEIGHT);
//		g.color(textColor).drawLine(0+3, TEXT_HEIGHT +1, w -2, TEXT_HEIGHT+ 1).;
		g.color(textColor).fillRect(0, TEXT_HEIGHT+2, w, 2);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);

	}

	public String getPathwayTitle() {
		return pathwayTitle;
	}

	public void setPathway(PathwayGraph pathway) {
		this.representedPathway = pathway;
		this.pathwayTitle = pathway.getTitle();
		repaint();
	}

	public PathwayGraph getRepresentedPathway() {
		return representedPathway;
	}

	public Color getNormalTitleColor() {
		return normalTitleColor;
	}

	public void setNormalTitleColor(Color normalTitleColor) {
		this.normalTitleColor = normalTitleColor;
		repaint();
	}

}
