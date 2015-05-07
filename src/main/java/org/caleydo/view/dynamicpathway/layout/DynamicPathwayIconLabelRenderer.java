package org.caleydo.view.dynamicpathway.layout;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.basic.EButtonIcon;
import org.caleydo.core.view.opengl.layout2.basic.GLButton;
import org.caleydo.core.view.opengl.layout2.renderer.IGLRenderer;

public class DynamicPathwayIconLabelRenderer implements IGLRenderer {
	private final String label;
	private final EButtonIcon icon;
	
	public DynamicPathwayIconLabelRenderer(String label, EButtonIcon prefix) {
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