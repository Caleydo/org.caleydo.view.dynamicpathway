package org.caleydo.view.dynamicpathway.util;

import java.awt.geom.Line2D;

import org.caleydo.core.util.collection.Pair;

public class Coordinates {

	private Pair<Double, Double> topLeft;
	private Pair<Double, Double> topRight;
	private Pair<Double, Double> bottomLeft;
	private Pair<Double, Double> bottomRight;

	public Coordinates() {
		this.topLeft = new Pair<Double, Double>();
		this.topRight = new Pair<Double, Double>();
		this.bottomLeft = new Pair<Double, Double>();
		this.bottomRight = new Pair<Double, Double>();
	}

	public void setCoords(double centerX, double centerY, double width, double height) {
		this.topLeft = Pair.make((centerX - width / 2.0), (centerY - height / 2.0));
		this.topRight = Pair.make((centerX + width / 2.0), (centerY - height / 2.0));
		this.bottomLeft = Pair.make((centerX - width / 2.0), (centerY + height / 2.0));
		this.bottomRight = Pair.make((centerX + width / 2.0), (centerY + height / 2.0));
	}

	public Pair<Double, Double> getTopLeft() {
		return topLeft;
	}

	public Pair<Double, Double> getTopRight() {
		return topRight;
	}

	public Pair<Double, Double> getBottomLeft() {
		return bottomLeft;
	}

	public Pair<Double, Double> getBottomRight() {
		return bottomRight;
	}
	
	public Line2D getTopBound() {
		return (new Line2D.Double(topLeft.getFirst(), topLeft.getSecond(), topRight.getFirst(), topRight.getSecond()));
	}
	
	public Line2D getBottomBound() {
		return (new Line2D.Double(bottomLeft.getFirst(), bottomLeft.getSecond(), bottomRight.getFirst(), bottomRight.getSecond()));
	}
	
	public Line2D getLeftBound() {
		return (new Line2D.Double(topLeft.getFirst(), topLeft.getSecond(), bottomLeft.getFirst(), bottomLeft.getSecond()));
	}
	
	public Line2D getRightBound() {
		return (new Line2D.Double(topRight.getFirst(), topRight.getSecond(), bottomRight.getFirst(), bottomRight.getSecond()));
	}
	
}
