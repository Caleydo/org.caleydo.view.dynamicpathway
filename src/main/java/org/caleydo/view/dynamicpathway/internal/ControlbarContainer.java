package org.caleydo.view.dynamicpathway.internal;

import java.util.HashMap;
import java.util.Map;

import org.caleydo.core.event.AEvent;
import org.caleydo.core.event.EventPublisher;
import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLElementContainer;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.EButtonIcon;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.ISelectionCallback;
import org.caleydo.core.view.opengl.layout2.basic.RadioController;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.GLPadding;
import org.caleydo.core.view.opengl.layout2.layout.GLSizeRestrictiveFlowLayout;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.picking.APickingListener;
import org.caleydo.core.view.opengl.picking.Pick;
import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.view.dynamicpathway.events.ChangeVertexEnvironmentEvent;
import org.caleydo.view.dynamicpathway.events.ClearCanvasEvent;
import org.caleydo.view.dynamicpathway.events.DuplicateVerticesSettingChangeEvent;
import org.caleydo.view.dynamicpathway.events.ZeroDegreeNodesSettingChangeEvent;
import org.caleydo.view.dynamicpathway.layout.DynamicPathwayIconLabelRenderer;
import org.caleydo.view.dynamicpathway.ui.ControlbarPathwayTitleEntry;
import org.caleydo.view.dynamicpathway.ui.VertexEnvironmentDialog;
import org.caleydo.vis.lineup.ui.RenderStyle;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;

public class ControlbarContainer extends AnimatedGLElementContainer implements ISelectionCallback {

	private static final int VERTEX_ENV_ON_START = 4;
	private static final String TITLE = "Control Bar";
	private static final String CLEAR_CANVAS_INFO_TEXT = "Remove Pathways:";

	/**
	 * radio group for allowing/ignoring zero degree nodes
	 */
	private RadioController zeroDegreeNodesRadioController;
	private GLButton ignoreZeroDegreeNodesButton;
	private GLButton allowZeroDegreeNodesButton;

	/**
	 * radio group for allowing/removing duplicate vertices
	 */
	private GLElement allowIgnoreDuplicateVerticesLabel1;
	private GLElement allowIgnoreDuplicateVerticesLabel2;
	private RadioController duplicateVerticesRadioController;
	private GLButton allowDuplicateVerticesButton;
	private GLButton removeDuplicateVerticesButton;

	// /**
	// * Node Environment
	// */
	//
	private GLElement vertexEnvironmentSizeValue;
	/**
	 * focus pathway
	 */
	private ControlbarPathwayTitleEntry focusGraphElement;
	private PathwayGraph focusPathway = null;
	private String focusGraphTitle = "";

	/**
	 * context pathways
	 */
	private GLElement focusContextLineSeparator;
	private GLElement contextGraphsLabel;
	private Map<String, GLElement> contextPathways;
	private AnimatedGLElementContainer contextPathwayElements;

	private Integer nodeEnvironmentSize;

	public ControlbarContainer() {
		super();
		setLayout(GLLayouts.flowVertical(10));

		this.contextPathways = new HashMap<String, GLElement>();
		this.nodeEnvironmentSize = new Integer(VERTEX_ENV_ON_START);

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
		removeDuplicateVerticesButton = createRadioButton("Remove duplicate vertices", "Mandatory for merged graphs",
				duplicateVerticesRadioController);
		allowDuplicateVerticesButton = createRadioButton("Allow duplicate vertices",
				"Only possible for unmerged graphs", duplicateVerticesRadioController);

		allowIgnoreDuplicateVerticesLabel1 = createSubHeader("Allow/Remove duplicate");
		allowIgnoreDuplicateVerticesLabel2 = createSubHeader("vertices");

		add(allowIgnoreDuplicateVerticesLabel1);
		add(allowIgnoreDuplicateVerticesLabel2);
		add(removeDuplicateVerticesButton);
		add(allowDuplicateVerticesButton);

		add(createLineSeparator());

		/**
		 * vertex environment size
		 */
		GLElement vertexEnvironmentSizeTitle = createSubHeader("Vertex Environment Size");
		vertexEnvironmentSizeTitle.setVisibility(EVisibility.VISIBLE);
		add(vertexEnvironmentSizeTitle);

		GLElementContainer editVertexEnvContainer = new GLElementContainer(new GLSizeRestrictiveFlowLayout(true, 30,
				new GLPadding(8, 0, 20, 0)));
		editVertexEnvContainer.setBounds(0, 0, this.getRectBounds().width(), 20);

		String nodeEnvironment = (nodeEnvironmentSize < 1) ? "Full Pathways" : nodeEnvironmentSize.toString();

		this.vertexEnvironmentSizeValue = createContentText(nodeEnvironment);
		this.vertexEnvironmentSizeValue.setVisibility(EVisibility.VISIBLE);
		editVertexEnvContainer.add(this.vertexEnvironmentSizeValue);

		GLButton editEnvSizeButton = new GLButton(EButtonMode.BUTTON);
		editEnvSizeButton.setVisibility(EVisibility.PICKABLE);
		editEnvSizeButton.setSize(16, 16);
		editEnvSizeButton.setRenderer(GLRenderers.fillImage("resources/icons/edit.png"));
		editVertexEnvContainer.add(editEnvSizeButton);

		editEnvSizeButton.onPick(new APickingListener() {

			@Override
			protected void clicked(Pick pick) {
				super.clicked(pick);

				Display.getDefault().asyncExec(new Runnable() {

					@Override
					public void run() {
						VertexEnvironmentDialog envDialog = new VertexEnvironmentDialog(Display.getDefault()
								.getActiveShell());
						int returnValue = envDialog.open();
						if (returnValue == Window.OK) {
							boolean nodeEnvSizeChanged = setNodeEnvironmentSize(envDialog.getVertexEnv());
							if (nodeEnvSizeChanged) {
								ChangeVertexEnvironmentEvent changeEnvSizeEvent = new ChangeVertexEnvironmentEvent(
										nodeEnvironmentSize);
								EventPublisher.trigger(changeEnvSizeEvent);
							}
						}
					}
				});
			}

		});

		add(editVertexEnvContainer);

		GLElement vertexEnvironmentSizeLineSeparator = createLineSeparator();
		vertexEnvironmentSizeLineSeparator.setVisibility(EVisibility.VISIBLE);
		add(vertexEnvironmentSizeLineSeparator);

		/**
		 * Clear all button
		 */
		GLElementContainer clearCanvasContainer = new GLElementContainer(new GLSizeRestrictiveFlowLayout(true, 10,
				new GLPadding(2, 0, 20, 0)));
		clearCanvasContainer.setBounds(0, 0, this.getRectBounds().width(), 20);

		GLElement clearCanvasInfoText = new GLElement(GLRenderers.drawText(CLEAR_CANVAS_INFO_TEXT));
		clearCanvasInfoText.setSize(Float.NaN, 16);
		clearCanvasContainer.add(clearCanvasInfoText);

		GLButton clearAllButton = new GLButton(EButtonMode.BUTTON);
		clearAllButton.setVisibility(EVisibility.PICKABLE);
		clearAllButton.setSize(17, 17);
		clearAllButton.setRenderer(GLRenderers.fillImage("resources/icons/clear_pathways.png"));
		clearAllButton.onPick(new APickingListener() {

			@Override
			protected void clicked(Pick pick) {
				super.clicked(pick);

				ClearCanvasEvent clearCanvasEvent = new ClearCanvasEvent();
				EventPublisher.trigger(clearCanvasEvent);
			}
		});
		clearCanvasContainer.add(clearAllButton);

		add(clearCanvasContainer);

		GLElement clearCanvasLineSeparator = createLineSeparator();
		clearCanvasLineSeparator.setVisibility(EVisibility.VISIBLE);
		add(clearCanvasLineSeparator);

		/**
		 * current focus graph
		 */
		GLElement focusPathwayLabel = createSubHeader("Current Focus Pathway");
		add(focusPathwayLabel);
		this.focusGraphElement = new ControlbarPathwayTitleEntry(null, Color.BLACK, true);// createContentText(focusGraphTitle);
		this.focusGraphElement.setVisibility(EVisibility.HIDDEN);
		add(focusGraphElement);

		this.focusContextLineSeparator = createLineSeparator();
		this.focusContextLineSeparator.setVisibility(EVisibility.HIDDEN);
		add(focusContextLineSeparator);

		// Composite comp = new Composite();
		// final Text clusterNumberText = new Text(this, SWT.BORDER);
		// clusterNumberText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/**
		 * current context graphs
		 */
		this.contextGraphsLabel = createSubHeader("Current Context Pathways");
		this.contextGraphsLabel.setVisibility(EVisibility.HIDDEN);
		add(contextGraphsLabel);

		this.contextPathwayElements = new AnimatedGLElementContainer(GLLayouts.flowVertical(5));
		add(contextPathwayElements);
	}

	public void addPathwayTitle(PathwayGraph pathwayToAdd, boolean isFocusPathway, Color titleColor) throws Exception {
		if (isFocusPathway)
			addFocusPathwayTitle(pathwayToAdd, titleColor);
		else
			addContextPathwayTitle(pathwayToAdd, titleColor);
	}

	/**
	 * remove the given context pathway from the listed pathway titles
	 * 
	 * @param pathwayToRemove
	 * @throws Exception
	 */
	public void removeContextPathwayTitle(PathwayGraph pathwayToRemove) throws Exception {
		String pathwayTitle = pathwayToRemove.getTitle();

		GLElement elementToRemove = contextPathways.get(pathwayTitle);
		if (elementToRemove == null)
			throw new Exception(
					"INTERNAL ERROR: Wanted to remove context pathway title from controllbar, but title to remove ("
							+ pathwayTitle + ") wasn't equal to any of the kontext pathay titles ("
							+ contextPathways.keySet() + ")");

		contextPathwayElements.remove(elementToRemove);
		contextPathways.remove(pathwayTitle);

		if (contextPathwayElements.size() < 1) {
			allowDuplicateVerticesButton.setVisibility(EVisibility.PICKABLE);
			removeDuplicateVerticesButton.setVisibility(EVisibility.PICKABLE);
		}

	}

	public void removeFocusPathwayTitle(PathwayGraph graphToRemove) throws Exception {
		if (!focusPathway.equals(graphToRemove))
			throw new Exception(
					"INTERNAL ERROR: Wanted to remove focus pathway title from controllbar, but title to remove ("
							+ graphToRemove.getTitle() + ") wasn't equal to the focus pathay title (" + focusGraphTitle
							+ ")");

		this.focusPathway = null;
		this.focusGraphTitle = "";
		this.contextPathwayElements.clear();
		this.contextPathways.clear();
		this.focusGraphElement.setVisibility(EVisibility.HIDDEN);
		this.focusContextLineSeparator.setVisibility(EVisibility.HIDDEN);
		this.contextGraphsLabel.setVisibility(EVisibility.HIDDEN);
		this.contextPathwayElements.setVisibility(EVisibility.HIDDEN);
		this.allowDuplicateVerticesButton.setVisibility(EVisibility.PICKABLE);
		this.removeDuplicateVerticesButton.setVisibility(EVisibility.PICKABLE);
	}

	public int getNodeEnvironmentSize() {
		return nodeEnvironmentSize.intValue();
	}

	public boolean setNodeEnvironmentSize(int nodeEnvironmentSize) {
		boolean nodeEnvSizeChanged = false;
		System.out.println("CHANGING ENV SIZE from " + this.nodeEnvironmentSize + " to " + nodeEnvironmentSize);
		if (this.nodeEnvironmentSize.intValue() != nodeEnvironmentSize) {
			this.nodeEnvironmentSize = nodeEnvironmentSize;

			String nodeEnvonmentSizeString = (this.nodeEnvironmentSize < 0) ? "Full Pathways"
					: this.nodeEnvironmentSize.toString();
			vertexEnvironmentSizeValue.setRenderer(GLRenderers.drawText(nodeEnvonmentSizeString));
			vertexEnvironmentSizeValue.repaint();
			nodeEnvSizeChanged = true;
		}

		return nodeEnvSizeChanged;
	}

	/**
	 * add the title of the focus pathway to the listed pathways
	 * 
	 * @param pathwayToAdd
	 *            the new focus pathway
	 * 
	 * @param titleColor
	 *            it's nodes color -> title will be underlined with the same color
	 */
	private void addFocusPathwayTitle(PathwayGraph pathwayToAdd, Color pathwayColor) {
		this.contextPathwayElements.clear();
		this.contextPathways.clear();
		this.focusPathway = pathwayToAdd;
		this.focusGraphTitle = pathwayToAdd.getTitle();

		this.focusGraphElement.setPathway(pathwayToAdd, pathwayColor, false);
		this.focusGraphElement.setVisibility(EVisibility.PICKABLE);
		this.focusContextLineSeparator.setVisibility(EVisibility.VISIBLE);
		this.contextGraphsLabel.setVisibility(EVisibility.VISIBLE);
		this.contextPathwayElements.setVisibility(EVisibility.VISIBLE);
	}

	public void setDisplayFocusPathwayBubbleSetCheckBox(boolean valueToSet) {
		focusGraphElement.selectDisplayBubbleSet(valueToSet);
	}

	/**
	 * method that adds the given parameter to the listed context pathways in the control bar
	 * 
	 * @param pathway
	 *            the context pathway to add
	 * @throws Exception
	 *             thrown when the pathway is null
	 */
	private void addContextPathwayTitle(PathwayGraph pathway, Color titleColor) throws Exception {
		if (pathway == null)
			throw new Exception("Context pathway was null");

		if (contextPathways.containsKey(pathway.getTitle()))
			return;

		ControlbarPathwayTitleEntry contextGraphTitle = new ControlbarPathwayTitleEntry(pathway, titleColor, false);// createContentText(BULLET_POINT
		contextGraphTitle.setVisibility(EVisibility.PICKABLE);

		/**
		 * disallowed with context pathways
		 */
		allowIgnoreDuplicateVerticesLabel2 = createSubHeader("vertices (DISABLED)");
		allowDuplicateVerticesButton.setVisibility(EVisibility.VISIBLE);
		removeDuplicateVerticesButton.setVisibility(EVisibility.VISIBLE);

		contextPathways.put(pathway.getTitle(), contextGraphTitle);
		contextPathwayElements.add(contextGraphTitle);

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
		DynamicPathwayIconLabelRenderer dynamicPwIconLabelRenderer = new DynamicPathwayIconLabelRenderer(buttonLabel,
				EButtonIcon.RADIO);
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
		text.setSize(Float.NaN, 15);
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

	/**
	 * if any of the buttons has changed
	 */
	@Override
	public void onSelectionChanged(GLButton button, boolean selected) {

		if (!selected)
			return;

		AEvent settingChangeEvent = null;

		if (button.equals(ignoreZeroDegreeNodesButton))
			settingChangeEvent = new ZeroDegreeNodesSettingChangeEvent(true);
		else if (button.equals(allowZeroDegreeNodesButton))
			settingChangeEvent = new ZeroDegreeNodesSettingChangeEvent(false);
		else if (button.equals(allowDuplicateVerticesButton))
			settingChangeEvent = new DuplicateVerticesSettingChangeEvent(true);
		else if (button.equals(removeDuplicateVerticesButton))
			settingChangeEvent = new DuplicateVerticesSettingChangeEvent(false);

		if (settingChangeEvent != null)
			EventPublisher.trigger(settingChangeEvent);

	}

	@Override
	public String toString() {
		String toPrint = "Focus: " + focusGraphTitle + "; Context: [";
		for (String contextTitles : contextPathways.keySet())
			toPrint += contextTitles + ", ";

		toPrint += "]";
		return toPrint;
	}

}
