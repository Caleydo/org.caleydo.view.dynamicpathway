package org.caleydo.view.dynamicpathway.util;

import gleem.linalg.Vec2f;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.caleydo.core.view.opengl.layout2.GLGraphics;

public class CalculateIntersectionUtil {
	
	/**
	 * calculate the intersection point between 2 lines, if it exists
	 *
	 * needed for for drawing the edges between 2 nodes
	 * {@link org.caleydo.view.dynamicpathway.ui.EdgeElement#renderImpl(GLGraphics, float, float)} 
	 * 
	 * general idea of implementation  
	 * See <a
	 * href="http://stackoverflow.com/questions/13053061/circle-line-intersection-points">stackoverflow.
	 * com/circle-line-intersection-point</a>
	 *  
	 * @param line1
	 * @param line2
	 * @return the intersection point
	 */
	public static final Point2D.Double calcIntersectionPoint(Line2D line1, Line2D line2) {
		double px = line1.getX1();
		double py = line1.getY1();
		double rx = line1.getX2() - px;
		double ry = line1.getY2() - py;

		double qx = line2.getX1();
		double qy = line2.getY1();
		double sx = line2.getX2() - qx;
		double sy = line2.getY2() - qy;

		double determinate = sx * ry - sy * rx;
		double z = (sx * (qy - py) + sy * (px - qx)) / determinate;

		double xIntersect = px + z * rx;
		double yIntersect = py + z * ry;

		return new Point2D.Double(xIntersect, yIntersect);
	}
	
	
	public static final Point2D.Double calcIntersectionPoint(Line2D lineToCenter, double givenRadius) {
		
		
		double xSource = lineToCenter.getX1();
		double ySource = lineToCenter.getY1();
		double xTarget = lineToCenter.getX2();
		double yTarget = lineToCenter.getY2();
		
		double lineLength = EuclideanDistance(xSource, ySource, xTarget, yTarget);
		
		double xAdd = ((xSource-xTarget) > 0) ? -givenRadius : givenRadius;
		double yAdd = ((ySource-yTarget) > 0) ? -givenRadius : givenRadius;
		
		double newLineLength = EuclideanDistance(xSource+xAdd, ySource+yAdd, xTarget, yTarget);
		
		double newX = xSource + (newLineLength/lineLength)* (xTarget-xSource);
		double newY = ySource + (newLineLength/lineLength)* (yTarget-ySource);
		
		return new Point2D.Double(newX, newY);
		
//		double radius = givenRadius;		
//		Vec2f centerToCenterVector = new Vec2f((float)(xSource-xTarget), (float)(ySource-yTarget));
//		double distanceToIntersectingPoint = centerToCenterVector.length()-radius;
//		Vec2f centerToIntersectionVector = new Vec2f(centerToCenterVector);
//		centerToIntersectionVector.normalize();
//		centerToIntersectionVector.scale((float)distanceToIntersectingPoint);	
//		
//		Ellipse2D circleAroundSourceCenter = new Ellipse2D.Double(xSource-radius,ySource-radius, 2*radius, 2*radius);
//		
//		sourcePoint = new Point2D.Double(xTarget+centerToIntersectionVector.x(), yTarget+centerToIntersectionVector.y());	
//		
//		double i = 1.0;
//		while(circleAroundSourceCenter.contains(sourcePoint) == false) {		
//			
//			if(radius < (givenRadius/2.0))
//				break;
//			
//			radius -= i;
//			
//			centerToCenterVector = new Vec2f((float)(xSource-xTarget), (float)(ySource-yTarget));
//			distanceToIntersectingPoint = centerToCenterVector.length()-radius;
//			centerToIntersectionVector = new Vec2f(centerToCenterVector);
//			centerToIntersectionVector.normalize();
//			centerToIntersectionVector.scale((float)distanceToIntersectingPoint);	
//			
//			sourcePoint = new Point2D.Double(xTarget+centerToIntersectionVector.x(), yTarget+centerToIntersectionVector.y());	
//			
//			circleAroundSourceCenter = new Ellipse2D.Double(xSource-radius,ySource-radius, 2*radius, 2*radius);
//			
//			i += 1.0;
//		}
//		
//		return sourcePoint;
		
	}
	
	private static final double EuclideanDistance(double xSource, double ySource, double xTarget, double yTarget) {
		double distance = Math.sqrt(Math.pow(xTarget-xSource, 2) + Math.pow(yTarget-ySource, 2));
		return distance;
	}
}
