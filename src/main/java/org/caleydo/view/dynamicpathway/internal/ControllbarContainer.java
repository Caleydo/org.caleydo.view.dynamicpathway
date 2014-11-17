package org.caleydo.view.dynamicpathway.internal;

import java.awt.Checkbox;
import java.util.HashMap;
import java.util.Map;

import javax.media.opengl.GL;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.EButtonIcon;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.IconLabelRenderer;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.IPickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.dynamicpathway.ui.ControllbarPathwayTitleEntry;
import org.caleydo.vis.lineup.ui.RenderStyle;

public class ControllbarContainer extends AnimatedGLElementContainer implements ISelectionCallback {

	private static final String TITLE = "Controllbar";
	private static final String BULLET_POINT = "• ";

	private DynamicPathwayView view;

	/**
	 * radio group for allowing/ignoring zero degree nodes
	 */
	private RadioController zeroDegreeNodesRadioController;
	private GLButton ignoreZeroDegreeNodesButton;
	private GLButton allowZeroDegreeNodesButton;

	/**
	 * radio group for allowing/removing duplicate vertices
	 */
	private RadioController duplicateVerticesRadioController;
	private GLButton allowDuplicateVerticesButton;
	private GLButton removeDuplicateVerticesButton;

	private ControllbarPathwayTitleEntry focusGraphElement;
	private PathwayGraph focusPathway = null;
	private String focusGraphTitle = "";

	private GLElement focusContextLineSeparator;
	private GLElement contextGraphsLabel;
	private Map<String, GLElement> contextGraphs;
	private AnimatedGLElementContainer contextGraphElements;
	private boolean isFocusGraphSet = false;

	public ControllbarContainer(DynamicPathwayView view) {
		super();
		setLayout(GLLayouts.flowVertical(10));

		this.view = view;
		this.contextGraphs = new HashMap<String, GLElement>();

		/**
		 * create header
		 */
		GLElement controllbarHeader = new GLElement(GLRenderers.drawText(TITLE));
		controllbarHeader.setSize(Float.NaN, 26);

		add(controllbarHeader);

		/**
		 * Radio group: allow/ignore 0 degree nodes
		 */
		zeroDegreeNodesRadioController = new RadioController(this);
		ignoreZeroDegreeNodesButton = createRadioButton("Ignore 0° nodes", "Allow vertices with no edges",
				zeroDegreeNodesRadioController);
		allowZeroDegreeNodesButton = createRadioButton("Allow 0° nodes", "Disallow vertices with no edges",
				zeroDegreeNodesRadioController);
		GLElement allowIgnoreZeroDegreeNodesLabel = createSubHeader("Allow/Ignore 0° Nodes");

		add(allowIgnoreZeroDegreeNodesLabel);
		add(ignoreZeroDegreeNodesButton);
		add(allowZeroDegreeNodesButton);

		add(createLineSeparator());

		/**
		 * Radio group: allow/remove duplicate vertices
		 */
		duplicateVerticesRadioController = new RadioController(this);
		removeDuplicateVerticesButton = createRadioButton("Remove duplicate vertices",
				"Mandatory for merged graphs", duplicateVerticesRadioController);
		allowDuplicateVerticesButton = createRadioButton("Allow duplicate vertices",
				"Only possible for unmerged graphs", duplicateVerticesRadioController);

		GLElement allowIgnoreDuplicateVerticesLabel1 = createSubHeader("Allow/Remove");
		GLElement allowIgnoreDuplicateVerticesLabel2 = createSubHeader("duplicate vertices");

		add(allowIgnoreDuplicateVerticesLabel1);
		add(allowIgnoreDuplicateVerticesLabel2);
		add(removeDuplicateVerticesButton);
		add(allowDuplicateVerticesButton);

		add(createLineSeparator());

		/**
		 * current focus graph
		 */
		GLElement focusPathwayLabel = createSubHeader("Current Focus Pathway");
		add(focusPathwayLabel);
		this.focusGraphElement = new ControllbarPathwayTitleEntry(null, view);// createContentText(focusGraphTitle);
		this.focusGraphElement.setVisibility(EVisibility.HIDDEN);
		add(focusGraphElement);

		this.focusContextLineSeparator = createLineSeparator();
		this.focusContextLineSeparator.setVisibility(EVisibility.HIDDEN);
		add(focusContextLineSeparator);

		this.contextGraphsLabel = createSubHeader("Current Context Pathways");
		this.contextGraphsLabel.setVisibility(EVisibility.HIDDEN);
		add(contextGraphsLabel);

		this.contextGraphElements = new AnimatedGLElementContainer(GLLayouts.flowVertical(5));
		add(contextGraphElements);
	}

	public void addPathwayTitle(PathwayGraph pathwayToAdd, boolean isFocusPathway) throws Exception {
		if (isFocusPathway)
			addFocusPathwayTitle(pathwayToAdd);
		else
			addContextPathwayTitle(pathwayToAdd);
	}

	public void removeContextPathwayTitle(PathwayGraph pathwayToRemove) throws Exception {
		String pathwayTitle = pathwayToRemove.getTitle();
		
		GLElement elementToRemove = contextGraphs.get(pathwayTitle);
		if (elementToRemove == null)
			throw new Exception(
					"INTERNAL ERROR: Wanted to remove kontext pathway title from controllbar, but title to remove ("
							+ pathwayTitle + ") wasn't equal to any of the kontext pathay titles ("
							+ contextGraphs.keySet() + ")");

		contextGraphElements.remove(elementToRemove);
		contextGraphs.remove(pathwayTitle);
	}

	public void removeFocusPathwayTitle(PathwayGraph graphToRemove) throws Exception {
		if (!focusPathway.equals(graphToRemove))
			throw new Exception(
					"INTERNAL ERROR: Wanted to remove focus pathway title from controllbar, but title to remove ("
							+ graphToRemove.getTitle() + ") wasn't equal to the focus pathay title (" + focusGraphTitle + ")");

		this.focusPathway = null;
		this.focusGraphTitle = "";
		this.contextGraphElements.clear();
		this.contextGraphs.clear();
		this.focusGraphElement.setVisibility(EVisibility.HIDDEN);
		this.focusContextLineSeparator.setVisibility(EVisibility.HIDDEN);
		this.contextGraphsLabel.setVisibility(EVisibility.HIDDEN);
		this.contextGraphElements.setVisibility(EVisibility.HIDDEN);
	}

	private void addFocusPathwayTitle(PathwayGraph pathwayToAdd) {
		this.contextGraphElements.clear();
		this.contextGraphs.clear();
		this.focusPathway = pathwayToAdd;
		this.focusGraphTitle = pathwayToAdd.getTitle();

		this.focusGraphElement.setPathway(pathwayToAdd);
		this.focusGraphElement.setVisibility(EVisibility.PICKABLE);
		this.focusContextLineSeparator.setVisibility(EVisibility.VISIBLE);
		this.contextGraphsLabel.setVisibility(EVisibility.VISIBLE);
		this.contextGraphElements.setVisibility(EVisibility.VISIBLE);
	}

	/**
	 * method that adds the given parameter to the listed context pathways in the control bar
	 * 
	 * @param pathway
	 *            the context pathway to add
	 * @throws Exception
	 *             thrown when the pathway is null
	 */
	private void addContextPathwayTitle(PathwayGraph pathway) throws Exception {
		if (pathway == null)
			throw new Exception("Context pathway was null");

		if (contextGraphs.containsKey(pathway.getTitle()))
			return;

		ControllbarPathwayTitleEntry contextGraphTitle = new ControllbarPathwayTitleEntry(pathway, view);// createContentText(BULLET_POINT
		contextGraphTitle.setVisibility(EVisibility.PICKABLE); // +
		// title);
		contextGraphs.put(pathway.getTitle(), contextGraphTitle);
		contextGraphElements.add(contextGraphTitle);
	}

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {

		g.color(RenderStyle.COLOR_BACKGROUND_EVEN).fillRect(0, 0, w, h);

		super.renderImpl(g, w, h);

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
	private GLButton createRadioButton(String buttonLabel, String toolTip, RadioController parentRadioGroup) {
		GLButton pinButton = new GLButton(EButtonMode.CHECKBOX);
		pinButton.setVisibility(EVisibility.PICKABLE);
		pinButton.setSize(16, 16);
		pinButton.setTooltip(toolTip);
		DynamicPathwayIconLabelRenderer dynamicPwIconLabelRenderer = new DynamicPathwayIconLabelRenderer(
				buttonLabel, EButtonIcon.RADIO);
		pinButton.setRenderer(dynamicPwIconLabelRenderer);
		pinButton.setSelectedRenderer(dynamicPwIconLabelRenderer);
		parentRadioGroup.add(pinButton);
		return pinButton;
	}

	/**
	 * creates a label text of size 16 pixel
	 * 
	 * @param labelText
	 * @return created label
	 */
	private GLElement createSubHeader(String labelText) {
		GLElement label = new GLElement(GLRenderers.drawText(labelText));
		label.setSize(Float.NaN, 16);
		return label;
	}

	/**
	 * creates a text of size 12 pixel
	 * 
	 * @param contentText
	 * @return created label
	 */
	private GLElement createContentText(String contentText) {
		GLElement text = new GLElement(GLRenderers.drawText(contentText));
		text.setSize(Float.NaN, 12);
		return text;
	}

	/**
	 * 
	 * @return a black line with with 2 pixel thickness
	 */
	private GLElement createLineSeparator() {
		GLElement lineSeparator = new GLElement(GLRenderers.fillRect(Color.GRAY));
		lineSeparator.setSize(Float.NaN, 2);
		return lineSeparator;
	}

	public class DynamicPathwayIconLabelRenderer implements IGLRenderer {
		private final String label;
		private final EButtonIcon icon;

		private DynamicPathwayIconLabelRenderer(String label, EButtonIcon prefix) {
			this.label = label;
			this.icon = prefix;
		}

		@Override
		public void render(GLGraphics g, float w, float h, GLElement parent) {
			boolean s = ((GLButton) parent).isSelected();

			String icon = this.icon.get(s);
			g.fillImage(icon, 1, 1, h - 2, h - 2);
			if (label != null && label.length() > 0)
				g.drawText(label, 18, h - 16, 200, h - 2);

		}
	}

	/**
	 * if any of the buttons has changed
	 */
	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {

		if (!selected)
			return;

		if (button.equals(ignoreZeroDegreeNodesButton))
			view.paintGraphWithOrWithoutZeroDegreeVertices(true);
		else if (button.equals(allowZeroDegreeNodesButton))
			view.paintGraphWithOrWithoutZeroDegreeVertices(false);
		else if (button.equals(allowDuplicateVerticesButton))
			view.paintGraphWithOrWithoutDuplicateVertices(true);
		else if (button.equals(removeDuplicateVerticesButton))
			view.paintGraphWithOrWithoutDuplicateVertices(false);
	}

}
