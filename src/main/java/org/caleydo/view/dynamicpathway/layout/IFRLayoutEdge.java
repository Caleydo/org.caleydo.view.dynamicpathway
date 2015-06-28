package org.caleydo.view.dynamicpathway.layout;


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

}
