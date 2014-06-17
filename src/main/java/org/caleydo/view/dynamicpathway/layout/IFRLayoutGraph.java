/**
 * 
 */
package org.caleydo.view.dynamicpathway.layout;

import java.util.Set;

import org.caleydo.core.view.opengl.layout2.GLElementContainer;

/**
 * Interface containing methods, which are needed for
 * the Fruchterman Reingold Layout
 * 
 * @author Christiane Schwarzl
 *
 */
public interface IFRLayoutGraph {
	Set<IFRLayoutNode> getNodeSet();
	Set<IFRLayoutEdge> getEdgeSet();
	
}
