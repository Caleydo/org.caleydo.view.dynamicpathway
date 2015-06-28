package org.caleydo.view.dynamicpathway.internal;

import org.caleydo.core.util.base.DefaultLabelProvider;
import org.caleydo.core.util.base.ILabeled;
import org.caleydo.core.view.opengl.layout2.GLGraphics;
import org.caleydo.core.view.opengl.layout2.util.GLElementWindow;
import org.caleydo.view.entourage.SlideInElement;

import com.google.common.collect.Iterables;

/**
 * The main window of the Dynamic Pathway View
 *
 */
public class DynamicPathwayWindow extends GLElementWindow {
	
	private final DynamicPathwayView view;

	public DynamicPathwayWindow(ILabeled titleLabelProvider, DynamicPathwayView view) {
		super(titleLabelProvider);
		this.view = view;
	}

	public DynamicPathwayWindow(String title, DynamicPathwayView view) {
		this(new DefaultLabelProvider(title), view);
	}
	
	

	@Override
	protected void renderImpl(GLGraphics g, float w, float h) {
		if (w <= 1 || h <= 1) { // just render the SlideInElements
			g.incZ();
			for (SlideInElement child : Iterables.filter(this, SlideInElement.class))
				child.render(g);
			g.decZ();
		} else
			// render normally
			super.renderImpl(g, w, h);
	}

	@Override
	protected void renderPickImpl(GLGraphics g, float w, float h) {
		if (w <= 1 || h <= 1) { // just render the SlideInElements
			g.incZ();
			for (SlideInElement child : Iterables.filter(this, SlideInElement.class))
				child.renderPick(g);
			g.decZ();
		} else {
			// render normally
			super.renderPickImpl(g, w, h);
		}
	}
	
	public void addSlideInElement(SlideInElement element) {
		add(element);
	}
	
	public void setActive(boolean active) {
		if (active == this.active)
			return;

		if (active) {
			view.setActiveWindow(this);
			repaint();

			for (int i = 1; i < titleBar.size(); i++) {
				titleBar.get(i).setVisibility(EVisibility.PICKABLE);
			}
			titleBar.setHighlight(true);
			if (!showCloseButton) {
				setShowCloseButton(false);
			}
		} else {
			for (int i = 1; i < titleBar.size(); i++) {
				titleBar.get(i).setVisibility(EVisibility.NONE);
			}
			titleBar.setHighlight(false);
		}
		// background.setHovered(active);
		this.active = active;
	}

}
