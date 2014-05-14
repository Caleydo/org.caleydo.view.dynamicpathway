package org.caleydo.view.dynamicpathway.util;

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
}
