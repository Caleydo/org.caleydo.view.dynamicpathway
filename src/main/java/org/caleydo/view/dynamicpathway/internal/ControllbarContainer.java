package org.caleydo.view.dynamicpathway.internal;

import java.awt.Checkbox;

import javax.media.opengl.GL;

import org.caleydo.core.util.color.Color;
import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.GLElement.EVisibility;
import org.caleydo.core.view.opengl.layout2.animation.AnimatedGLElementContainer;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.basic.GLButton.EButtonMode;
import org.caleydo.core.view.opengl.layout2.layout.GLLayouts;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.renderer.GLRenderers;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;

public class ControllbarContainer extends AnimatedGLElementContainer {
	
	private static final String TITLE = "Controllbar";
	private GLButton pinButton;
	

	public ControllbarContainer() {		
		super();
		// TODO Auto-generated constructor stub
		
		setLayout(GLLayouts.flowVertical(1));
		pinButton = new GLButton(EButtonMode.CHECKBOX);
		pinButton.setSize(16, 16);
		pinButton.setLocation(20, 50);
		pinButton.setTooltip("Pin");
		pinButton.setRenderer(GLRenderers.fillImage("resources/icons/general/pin.png"));
		pinButton.setSelectedRenderer(new IGLRenderer() {
			@Override
			public void render(GLGraphics g, float w, float h, GLElement parent) {
//				g.fillImage("resources/icons/icon.png", 0,0, w, h);
//				g.gl.glEnable(GL.GL_BLEND);
//				g.gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
//				g.gl.glEnable(GL.GL_LINE_SMOOTH);
//				g.color(new Color(1, 1, 1, 0.5f)).fillRoundedRect(0, 0, w, h, Math.min(w, h) * 0.25f);
//				g.gl.glBlendFunc(GL.GL_ONE, GL.GL_ONE_MINUS_SRC_ALPHA);
			}
		});
		pinButton.setVisibility(EVisibility.VISIBLE);
		add(pinButton);
	}




	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		// TODO Auto-generated method stub
		super.renderImpl(g, w, h);
//		g.color(new Color(.95f, .95f, .95f)).fillRect(0, 0, w, h);
//		g.color(Color.LIGHT_GRAY).fillRect(0, 0, w, 35);
		g.drawText(TITLE, (w/2-47), 5, 94, 18);
		

		
	}

	
}
