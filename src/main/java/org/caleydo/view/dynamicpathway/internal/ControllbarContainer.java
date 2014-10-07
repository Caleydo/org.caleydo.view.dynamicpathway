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
		// TODO Auto-generated constructor stub
		
		setLayout(GLLayouts.flowVertical(1));
		pinButton = new GLButton(EButtonMode.CHECKBOX);
		pinButton.setVisibility(EVisibility.PICKABLE);
		pinButton.setSize(16, 16);
//		pinButton.setLocation(20, 50);
		pinButton.setTooltip("Pin");
//		pinButton.setRenderer(GLRenderers.fillImage("resources/icons/general/pin.png"));
		pinButton.setRenderer(GLRenderers.fillImage(EButtonIcon.RADIO.get(false)));
		pinButton.setSelectedRenderer(GLRenderers.fillImage(EButtonIcon.RADIO.get(true)));
//		pinButton.onPick(new APickingListener() {
//			@Override
//			protected void clicked(Pick pick) {
////				pinButton.setSelected(true);
////				repaint();
//			}
//		}
//			
//		);

//		pinButton.setVisibility(EVisibility.VISIBLE);
		GLElement label = new GLElement(GLRenderers.drawText(TITLE));
		label.setSize(Float.NaN, 20);
		add(label);
		add(pinButton);
	}




	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub
		super.renderImpl(g, w, h);
//		g.color(new Color(.95f, .95f, .95f)).fillRect(0, 0, w, h);
//		g.color(Color.LIGHT_GRAY).fillRect(0, 0, w, 35);
//		g.drawText(TITLE, (w/2-47), 5, 94, 18);
		

		
	}

	
}
