package org.caleydo.view.dynamicpathway.layout;

import java.awt.geom.Line2D;

/**
 * Interface containing methods, which are needed for
 * the Fruchterman Reingold Layout
 * 
 * @author Christiane Schwarzl
 *
 */
public interface IFRLayoutEdge {
	IFRLayoutNode getSource();
	IFRLayoutNode getTarget();
	Line2D getCenterToCenterLine();
	void setCenterToCenterLine(Line2D centerToCenterLine);
}
