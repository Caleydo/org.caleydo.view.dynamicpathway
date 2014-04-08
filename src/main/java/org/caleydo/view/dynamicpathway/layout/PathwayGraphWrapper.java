package org.caleydo.view.dynamicpathway.layout;

import gleem.linalg.Vec2f;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.caleydo.datadomain.pathway.graph.PathwayGraph;
import org.caleydo.datadomain.pathway.graph.item.vertex.EPathwayVertexType;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertex;
import org.caleydo.datadomain.pathway.graph.item.vertex.PathwayVertexRep;
import org.caleydo.view.dynamicpathway.ui.NodeElement;
import org.jgrapht.graph.DefaultEdge;

public class PathwayGraphWrapper {

	PathwayGraph focusGraph;
	Vector<PathwayGraph> kontextGraphs;
	PathwayGraph combinedGraph;
	Vector<NodeElement> combindedGraphRepresentation;
	Map<PathwayVertexRep, Vec2f> displacements;

	public PathwayGraphWrapper(PathwayGraph graph) {
		focusGraph = graph;
		displacements = new HashMap<PathwayVertexRep, Vec2f>();
		kontextGraphs = new Vector<PathwayGraph>();
		combindedGraphRepresentation = new Vector<NodeElement>();

		combinedGraph = new PathwayGraph(graph.getType(), graph.getName(), graph.getTitle(), graph.getImage(),
				graph.getExternalLink());
		
		for (PathwayVertexRep vrep : graph.vertexSet()) {
			if(vrep.getType() == EPathwayVertexType.gene) {
//				PathwayVertexRep vrepForRepositioning = new PathwayVertexRep(vrep.getName(), vrep.getShapeType()
//						.toString(), vrep.getCenterX(), vrep.getCenterY(), vrep.getWidth(), vrep.getHeight());
				combinedGraph.addVertex(vrep);
				
				NodeElement node = new NodeElement(vrep);
				combindedGraphRepresentation.add(node);
			}
		}
		for (DefaultEdge edge : graph.edgeSet()) {
			PathwayVertexRep source = graph.getEdgeSource(edge);
			PathwayVertexRep target = graph.getEdgeTarget(edge);
			if(source.getType() == EPathwayVertexType.gene && target.getType() == EPathwayVertexType.gene) {
				combinedGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge), edge);
			}
			
//			combinedGraph.addEdge(graph.getEdgeSource(edge), graph.getEdgeTarget(edge));
		}
	}

	public PathwayVertexRep getEdgeSource(DefaultEdge e) {
		return focusGraph.getEdgeSource(e);
	}

	public PathwayVertexRep getEdgeTarget(DefaultEdge e) {
		return focusGraph.getEdgeTarget(e);
	}

	public void setDisplacement(PathwayVertexRep vrep, Vec2f distance) {
		displacements.put(vrep, distance);
	}

	public Vec2f getDisplacement(PathwayVertexRep vrep) {
		return displacements.get(vrep);
	}

	// methods forwarded to PathwayGraph
	public Set<PathwayVertexRep> focusGraphVertexSet() {
		return focusGraph.vertexSet();
	}
	
	public Set<PathwayVertexRep> combinedGraphVertexSet() {
		return combinedGraph.vertexSet();
	}
	
	public Vector<NodeElement> nodeElementSet() {
		return combindedGraphRepresentation;
	}
	

	public Set<DefaultEdge> focusGraphEdgeSet() {
		return focusGraph.edgeSet();
	}

	public void setVertexPosition(PathwayVertexRep vrep, double newXPosition, double newYPosition) {
		short x = (short) newXPosition;
		short y = (short) newYPosition;
		
		vrep.getCenterX();
		
//		PathwayVertexRep vrepWithNewPosition = new PathwayVertexRep(vrep.getName(), vrep.getShapeType()
//				.toString(), x, y, vrep.getWidth(), vrep.getHeight());
//		
//		for(PathwayVertex vertex : vrep.getPathwayVertices()) {
//			vrepWithNewPosition.addPathwayVertex(vertex);
//		}
//		
//		combinedGraph.removeVertex(vrep);		
//		combinedGraph.addVertex(vrepWithNewPosition);
				
		for(NodeElement node : combindedGraphRepresentation) {
			if(node.getVertex() == vrep) {
				node.setCoords(x, y, vrep.getCenterX(), vrep.getCenterY());
				break;
			}
		
		}
	}

}
