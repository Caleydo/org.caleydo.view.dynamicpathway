package org.caleydo.view.dynamicpathway.layout;

import java.awt.geom.Point2D;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.caleydo.core.view.opengl.layout2.layout.IGLLayout2;
import org.caleydo.core.view.opengl.layout2.layout.IGLLayoutElement;
import org.caleydo.view.dynamicpathway.internal.NodeMergingException;

/**
 * calculates new vertex position according to the Fruchterman & Reingold algorithm
 * 
 * @author Christiane Schwarzl
 * 
 */
public class GLFruchtermanReingoldLayout implements IGLLayout2 {

	/**
	 * current width*height
	 */
	private double area;
	private double width;
	private double height;

	private Map<IFRLayoutNode, Point2D> displacementMap;

	/**
	 * The optimal edge length for all edges. Used for the attraction & repulsion equations.
	 */
	private double globalEdgeLength;

	/**
	 * If temperature & cooldown are not set by the user, they are set when doLayout is called
	 * In order to not override the user's setting, we have to save (in the constructor) if
	 * it was set or not
	 */
	private boolean isTemperatureAndCooldownSetByUser;
	
	/**
	 * needed for setting the bounds, so nodes are (re)drawn
	 */
	private Set<IGLLayoutElement> iglLayoutElementNodeSet;

	/**
	 * needed for setting the bounds, so edged are (re)drawn
	 */
	private Set<IGLLayoutElement> iglLayoutElementEdgeSet;

	// parameterizable members

	/**
	 * Defines how many times the nodes are displaced.
	 */
	private final int maxIterations;
	/**
	 * Restricts the maximal displacement in the current iteration.
	 */
	private double temperature;
	/**
	 * Defines the subtrahend of the temperature - updated in each iteration.
	 */
	private double cooldown;

	/**
	 * Standard calculation may not be applicable for the current graph. (e.g. graph is too sparse) If the
	 * repulsionMultiplier is set higher, than not connected nodes are farer apart and vice versa.
	 * 
	 * If it is set to -1, it is set to the distance between the 2 nodes. Since the repulsionMultiplier is
	 * inversely proportional to the repulsion force, this strongly limits the repulsion force
	 */
	private double repulsionMultiplier;

	private boolean isRepulsionMultiplierSetToDistance;

	/**
	 * Standard calculation may not be applicable for the current graph. (e.g. graph is too sparse) If the
	 * attractionMultiplier is set smaller, than connected nodes are closer together and vice versa.
	 * 
	 * If it is set to -1, it is set to the GlobalEdgeLength. Since the attractionMultiplier is inversely
	 * proportional to the attraction force, this strongly limits the attraction force
	 */
	private double attractionMultiplier;



	/**
	 * added to the width & height, when setting the bounds -> sets bounds bigger
	 */
	private double nodeBoundsExtension;

	/**
	 * 
	 * @param maxIterations
	 *            {@link #maxIterations}
	 * @param temperature
	 *            {@link #temperature}
	 * @param cooldown
	 *            {@link #cooldown}
	 * @param repulsionMultiplier
	 *            {@link #repulsionMultiplier}
	 * @param attractionMultiplier
	 *            {@link #attractionMultiplier}
	 */
	public GLFruchtermanReingoldLayout(int maxIterations, double temperature, double cooldown,
			double repulsionMultiplier, double attractionMultiplier, double nodeBoundsExtension) {

		this.isTemperatureAndCooldownSetByUser = false;
		this.isRepulsionMultiplierSetToDistance = false;

		/**
		 * Temperature & cooldown are set to -1.0 by default, so if these are not -1.0, the user set them by
		 * the LayoutBuilder
		 */
		if (temperature >= 0.0) {
			this.isTemperatureAndCooldownSetByUser = true;
			this.temperature = temperature;
			this.cooldown = cooldown;
		}

		/**
		 * If it is set to -1, it is set to the distance between the 2 nodes. Since the repulsionMultiplier is
		 * inversely proportional to the repulsion force, this strongly limits the repulsion force
		 */
		if (repulsionMultiplier < 0.0)
			this.isRepulsionMultiplierSetToDistance = true;

		this.maxIterations = maxIterations;
		this.repulsionMultiplier = repulsionMultiplier;
		this.attractionMultiplier = attractionMultiplier;
		this.nodeBoundsExtension = nodeBoundsExtension;

		this.displacementMap = new HashMap<IFRLayoutNode, Point2D>();
		this.iglLayoutElementNodeSet = new HashSet<IGLLayoutElement>();
		this.iglLayoutElementEdgeSet = new HashSet<IGLLayoutElement>();

		this.width = 0.0;
		this.height = 0.0;
	}

	/**
	 * performs Fruchterman&Reingold layout
	 * 
	 * @param children
	 *            list of vertices & edge, which are redrawn afterwards
	 * @param w
	 *            width of the current frame
	 * @param h
	 *            width of the current frame
	 * @param parent
	 *            the graph, needed to get the vertex & edgeSet
	 * @param deltaTimeMs
	 *            the delta time between the last call and the current call
	 * @return whether a relayout is needed
	 */
	@Override
	public boolean doLayout(List<? extends IGLLayoutElement> children, float w, float h,
			IGLLayoutElement parent, int deltaTimeMs) {

		if (children.size() == 0)
			return false;

		displacementMap.clear();
		iglLayoutElementNodeSet.clear();
		iglLayoutElementEdgeSet.clear();

		width = w;
		height = h;

		if (!isTemperatureAndCooldownSetByUser) {
			temperature = width / 10;
			cooldown = temperature / maxIterations;
		}

		area = width * height;

		for (IGLLayoutElement child : children) {
			Boolean isNode = child.getLayoutDataAs(Boolean.class, false);
			if (isNode)
				iglLayoutElementNodeSet.add(child);
			else
				iglLayoutElementEdgeSet.add(child);

		}

		IFRLayoutGraph graph = (IFRLayoutGraph) parent.asElement();
		Set<IFRLayoutNode> nodeSet = graph.getNodeSet();
		Set<IFRLayoutEdge> edgeSet = graph.getEdgeSet();

		assert (iglLayoutElementNodeSet.size() == nodeSet.size());

		globalEdgeLength = (float) Math.sqrt(area / nodeSet.size());

		if (this.attractionMultiplier < 0.0) {
			this.attractionMultiplier = this.globalEdgeLength;
		}

		for (int i = 1; i <= maxIterations; i++) {

			/**
			 * reset Displacement for the current iteration otherwise it would get too big and the nodes would
			 * be out of bounds
			 */
			for (IFRLayoutNode node : nodeSet) {
				displacementMap.put(node, new Point2D.Double());
			}

			for (IFRLayoutNode currentNode : nodeSet) {

				for (IFRLayoutNode otherNode : nodeSet) {

					calcRepulsiveForces(currentNode, otherNode);

				}

			}

			for (IFRLayoutEdge edge : edgeSet) {
				calcAttractiveForces(edge);
			}
			
			/**
			 * setting bounds of edges, so they are updated (renderImpl is called)
			 */
			for (IGLLayoutElement child : this.iglLayoutElementEdgeSet) {
				child.setBounds(0.0f, 0.0f, w, h);
			}

			for (IGLLayoutElement child : this.iglLayoutElementNodeSet) {
				IFRLayoutNode node = (IFRLayoutNode) child.asElement();
				calcNewVertexPositions(child, node);
			}



			coolDownTemp();

		}

		return false;
	}

	/**
	 * Method that calculates the displacement, using the currentNode's distance to all other nodes
	 * 
	 * @param currentNode
	 *            node for which the displacement is calculated
	 * 
	 *            calculate the repulsive forces between all nodes, afterwards the displacement of the node is
	 *            accordingly set
	 * @param otherNode
	 *            current other node, used for calculating the repulsive force
	 */
	private void calcRepulsiveForces(IFRLayoutNode currentNode, IFRLayoutNode otherNode) {

		if (currentNode != otherNode) {
			double xDistance = currentNode.getCenterX() - otherNode.getCenterX();
			double yDistance = currentNode.getCenterY() - otherNode.getCenterY();

			double distance = calcDistance(xDistance, yDistance);

			if (isRepulsionMultiplierSetToDistance) {
				this.repulsionMultiplier = distance;
			}

			double repulsiveForce = globalEdgeLength * globalEdgeLength / (distance * repulsionMultiplier);

			if (distance > 0) {
				double xDisplacementFactor = (xDistance / distance) * repulsiveForce;
				double yDisplacementFactor = (yDistance / distance) * repulsiveForce;

				try {
					editDisplacement(currentNode, xDisplacementFactor, yDisplacementFactor);
				} catch (NodeMergingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

	}

	/**
	 * 
	 * @param graph
	 *            needed to get the source & target nodes of the edge
	 * @param currentEdge
	 *            edge, which gives us the nodes, which's displacement will be recalculated (to be closer)
	 */
	private void calcAttractiveForces(IFRLayoutEdge currentEdge) {
		IFRLayoutNode sourceNode = currentEdge.getSource();
		IFRLayoutNode targetNode = currentEdge.getTarget();

		double xDistance = sourceNode.getCenterX() - targetNode.getCenterX();
		double yDistance = sourceNode.getCenterY() - targetNode.getCenterY();

		double distance = calcDistance(xDistance, yDistance);

		double attractiveForce = distance * distance / (globalEdgeLength * attractionMultiplier);

		if (distance > 0) {
			double xDisplacementSourceFactor = -(xDistance / distance * attractiveForce);
			double yDisplacementSourceFactor = -(yDistance / distance * attractiveForce);
			double xDisplacementTargetFactor = xDistance / distance * attractiveForce;
			double yDisplacementTargetFactor = yDistance / distance * attractiveForce;

			try {
				editDisplacement(sourceNode, xDisplacementSourceFactor, yDisplacementSourceFactor);
			} catch (NodeMergingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				editDisplacement(targetNode, xDisplacementTargetFactor, yDisplacementTargetFactor);
			} catch (NodeMergingException e) {
				// TODO Auto-generated catch block
				System.out.println("tmp");
				e.printStackTrace();
			}

		}
	}

	/**
	 * @param child
	 *            used to set the drawing bounds for the rendered object
	 * @param currentNode
	 *            node, which's position should be changed
	 */
	private void calcNewVertexPositions(IGLLayoutElement child, IFRLayoutNode currentNode) {
		double xPosition = currentNode.getCenterX();
		double yPosition = currentNode.getCenterY();
		double xDisplacement = displacementMap.get(currentNode).getX();
		double yDisplacement = displacementMap.get(currentNode).getY();
		double displacementDistance = calcDistance(xDisplacement, yDisplacement);

		double maxDisplacementLimit = Math.min(displacementDistance, temperature);

		if (maxDisplacementLimit > 0) {
			xPosition += (xDisplacement / displacementDistance) * maxDisplacementLimit;
			yPosition += (yDisplacement / displacementDistance) * maxDisplacementLimit;
		}

		double vertexWidth = currentNode.getWidth();
		double vertexHeight = currentNode.getHeight();

		double borderWidth = width / 50.0;

		if (xPosition < borderWidth) {
			xPosition = borderWidth + Math.random() * borderWidth * 2.0;
		} else if (xPosition > (width - borderWidth)) {
			xPosition = width - borderWidth - Math.random() * borderWidth * 2.0;
		}

		if (yPosition < borderWidth) {
			yPosition = borderWidth + Math.random() * borderWidth * 2.0;
		} else if (yPosition > (height - borderWidth)) {
			yPosition = height - borderWidth - Math.random() * borderWidth * 2.0;
		}

		double newXPos = xPosition - vertexWidth / 2.0;
		double newYPos = yPosition - vertexHeight / 2.0;

		child.setBounds((float) newXPos, (float) newYPos, (float) (vertexWidth + nodeBoundsExtension),
				(float) (vertexHeight + nodeBoundsExtension));
		currentNode.setCenter(xPosition, yPosition);

	}

	private double calcDistance(double x, double y) {
		return Math.sqrt(x * x + y * y);
	}

	private void coolDownTemp() {
		temperature -= cooldown;
		if (temperature < 0)
			temperature = 0;
	}

	/**
	 * takes old displacement and adds x & y value to it
	 * 
	 * @param node
	 *            which's displacement has to be edited
	 * @param x the addend for the xDisplacement value
	 * @param y the addend for the yDisplacement value
	 * @throws NodeMergingException 
	 * 
	 */
	private void editDisplacement(IFRLayoutNode node, double x, double y) throws NodeMergingException {
		
		if(!displacementMap.containsKey(node))
			throw new NodeMergingException("Node(" + node + ") is not in displacementMap");
		
		if(displacementMap.get(node) == null)
			throw new NodeMergingException("Node(" + node + ") contained no value in displacementMap");
		
		double xDisplacement = displacementMap.get(node).getX() + x;
		double yDisplacement = displacementMap.get(node).getY() + y;
		displacementMap.remove(node);
		displacementMap.put(node, new Point2D.Double(xDisplacement, yDisplacement));
	}

}
