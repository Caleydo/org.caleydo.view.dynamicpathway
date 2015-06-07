package org.caleydo.view.dynamicpathway.ui;

import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.contextmenu.GenericContextMenuItem;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.basic.EButtonIcon;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.AdvancedPick;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.core.view.opengl.picking.PickingMode;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.dynamicpathway.events.ChangeBubbleSetVisibilityEvent;
import org.caleydo.view.dynamicpathway.events.MakeFocusPathwayEvent;
import org.caleydo.view.dynamicpathway.events.RemoveDisplayedPathwayEvent;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayIconLabelRenderer;

import com.google.common.collect.Lists;

public class ControlbarPathwayTitleEntry extends GLElementContainer {
	private static final String SPACING = "       ";
	private static final float TEXT_HEIGHT = 13.0f;

	private static final int PATHWAY_COLOR_SQUARE_LOCATION = 3;
	private static final int PATHWAY_COLOR_SQUARE_SIZE = 18;
	private static final int CHECKBOX_TITLE_MARGIN = 2;

	private PathwayGraph representedPathway;
	private String pathwayTitle;

	private Color pathwayColor;

	private final GLButton enableDisableBubbleSetCheckBox;

	/**
	 * menu item in right click menu that triggers the deletion of the pathway represented by this entry
	 */
	private GenericContextMenuItem removePathwayMenuItem;
	private GenericContextMenuItem makeFocusGraphMenuItem;

	public ControlbarPathwayTitleEntry(PathwayGraph representedPathway, Color titleColor, final boolean isFocusPathway) {
		this.pathwayColor = titleColor;
		this.representedPathway = representedPathway;
		if (representedPathway != null)
			this.pathwayTitle = representedPathway.getTitle();
		else
			this.pathwayTitle = "";
		this.removePathwayMenuItem = new GenericContextMenuItem("Remove this pathway", new RemoveDisplayedPathwayEvent(
				this));
		this.makeFocusGraphMenuItem = new GenericContextMenuItem("Make this pathway the focus pathway (switch roles)",
				new MakeFocusPathwayEvent(this));

		enableDisableBubbleSetCheckBox = createCheckboxButton(pathwayTitle, "En/Disable BubbleSet");
		boolean setSelectedByDefault = !isFocusPathway;
		enableDisableBubbleSetCheckBox.setSelected(setSelectedByDefault);
		enableDisableBubbleSetCheckBox.onPick(new APickingListener() {
			@Override
			protected void clicked(Pick pick) {

				ChangeBubbleSetVisibilityEvent changeVisibility = new ChangeBubbleSetVisibilityEvent(
						getRepresentedPathway(), !enableDisableBubbleSetCheckBox.isSelected());
				
				EventPublisher.trigger(changeVisibility);
			}
		});

		add(enableDisableBubbleSetCheckBox);

		setSize(Float.NaN, TEXT_HEIGHT + 4);

		onPick(new IPickingListener() {

			@Override
			public void pick(Pick pick) {
				AdvancedPick p = (AdvancedPick) pick;

				/**
				 * if the user right clicked - show context menu
				 */
				if (pick.getPickingMode() == PickingMode.RIGHT_CLICKED) {
					// context.getSWTLayer().showContextMenu(Lists.newArrayList(removePathwayMenuItem));

					if (isFocusPathway)
						context.getSWTLayer().showContextMenu(Lists.newArrayList(removePathwayMenuItem));
					else
						context.getSWTLayer().showContextMenu(
								Lists.newArrayList(removePathwayMenuItem, makeFocusGraphMenuItem));
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
		super.renderImpl(g, w, h);

		// g.color(normalTitleColor).fillCircle(10, 8.5f, 7.5f);
		g.color(pathwayColor).fillRoundedRect(PATHWAY_COLOR_SQUARE_LOCATION, PATHWAY_COLOR_SQUARE_LOCATION,
				PATHWAY_COLOR_SQUARE_SIZE, PATHWAY_COLOR_SQUARE_SIZE, 1);

		g.drawText(SPACING + pathwayTitle, 0, CHECKBOX_TITLE_MARGIN, w, TEXT_HEIGHT);
		// g.color(normalTitleColor).fillRect(0, TEXT_HEIGHT+2, w, 2);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		super.renderPickImpl(g, w, h);
	}

	public String getPathwayTitle() {
		return pathwayTitle;
	}

	public void setPathway(PathwayGraph pathway, Color pathwayColor, boolean displayBubbleSet) {
		this.representedPathway = pathway;
		this.pathwayTitle = pathway.getTitle();
		this.pathwayColor = pathwayColor;
		selectDisplayBubbleSet(displayBubbleSet);
		repaint();
	}
	
	public void selectDisplayBubbleSet(boolean displayBubbleSet) {
		this.enableDisableBubbleSetCheckBox.setSelected(displayBubbleSet);
	}

	public PathwayGraph getRepresentedPathway() {
		return representedPathway;
	}

	/**
	 * creates a pickable checkbox {@link GLButton} of the size 16x16 Pixels
	 * 
	 * @param buttonLabel
	 *            the label of the newly created button
	 * @param toolTip
	 *            the text that is shown, when the mouse is hovered over the button
	 * @param parentRadioGroup
	 *            the radio group it belongs to -> only on button of this group can be selected at once
	 * @return the created button
	 */
	public GLButton createCheckboxButton(String buttonLabel, String toolTip) {
		GLButton pinButton = new GLButton(EButtonMode.CHECKBOX);
		pinButton.setVisibility(EVisibility.PICKABLE);
		pinButton.setSize(16, 16);
		pinButton.setTooltip(toolTip);
		pinButton.setLocation(4, 4);
		DynamicPathwayIconLabelRenderer dynamicPwIconLabelRenderer = new DynamicPathwayIconLabelRenderer("",
				EButtonIcon.CHECKBOX);
		pinButton.setRenderer(dynamicPwIconLabelRenderer);
		pinButton.setSelectedRenderer(dynamicPwIconLabelRenderer);
		return pinButton;
	}

}
