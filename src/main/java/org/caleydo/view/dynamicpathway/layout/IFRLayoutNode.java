package org.caleydo.view.dynamicpathway.layout;

import org.caleydo.core.view.opengl.layout2.GLElement;
import org.caleydo.view.dynamicpathway.ui.DynamicPathwaysCanvas;

/**
 * Interface containing methods, which are needed for the Fruchterman Reingold Layout
 * 
 * @author Christiane Schwarzl
 * 
 */
public interface IFRLayoutNode {

	public double getCenterX();

	public double getCenterY();

	public void setCenter(double centerX, double centerY);

	public double getHeight();

	public double getWidth();

	public GLElement setBounds(float x, float y, float width, float height);
	
}
