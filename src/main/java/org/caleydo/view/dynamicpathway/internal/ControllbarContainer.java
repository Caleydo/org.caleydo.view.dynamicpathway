package org.caleydo.view.dynamicpathway.internal;

import java.awt.Checkbox;

import javax.media.opengl.GL;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.EButtonIcon;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
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

public class ControllbarContainer extends AnimatedGLElementContainer {
	
	private static final String TITLE = "Controllbar";
	private GLButton pinButton;
	

	public ControllbarContainer() {		
		super();
		
		RadioController radioController = new RadioController();
		
		
		
		setLayout(GLLayouts.flowVertical(10));
		pinButton = new GLButton(EButtonMode.CHECKBOX);
		pinButton.setVisibility(EVisibility.PICKABLE);
		pinButton.setSize(16, 16);
		pinButton.setTooltip("Pin");
//		pinButton.setRenderer(GLRenderers.fillImage(EButtonIcon.RADIO.get(false)));
		DynamicPathwayIconLabelRenderer dynamicPwIconLabelRenderer1 = new DynamicPathwayIconLabelRenderer("Button 1", EButtonIcon.RADIO);
		pinButton.setRenderer(dynamicPwIconLabelRenderer1);
		pinButton.setSelectedRenderer(dynamicPwIconLabelRenderer1);
		radioController.add(pinButton);

		GLButton pinButton2 = new GLButton(EButtonMode.CHECKBOX);
		pinButton2.setVisibility(EVisibility.PICKABLE);
		pinButton2.setSize(16, 16);
		pinButton2.setTooltip("Pin 2");
		DynamicPathwayIconLabelRenderer dynamicPwIconLabelRenderer2 = new DynamicPathwayIconLabelRenderer("Button 2", EButtonIcon.RADIO);
		pinButton2.setRenderer(dynamicPwIconLabelRenderer2);
		pinButton2.setSelectedRenderer(dynamicPwIconLabelRenderer2);
		radioController.add(pinButton2);
		
		GLElement label = new GLElement(GLRenderers.drawText(TITLE));
		label.setSize(Float.NaN, 20);
		add(label);
		add(pinButton);
		add(pinButton2);
	}
	
	




	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub
		super.renderImpl(g, w, h);
//		g.color(new Color(.95f, .95f, .95f)).fillRect(0, 0, w, h);
//		g.color(Color.LIGHT_GRAY).fillRect(0, 0, w, 35);
//		g.drawText(TITLE, (w/2-47), 5, 94, 18);
		

		
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
				g.drawText(label, 15, h-16, 100, h);
			
		}
	}

	
}
