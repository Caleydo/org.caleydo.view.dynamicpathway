package org.caleydo.view.dynamicpathway.layout;

public class GLFruchtermanReingoldLayoutBuilder {
	
	private static final int MAX_ITERATIONS = 700;
	
	private int maxIterations = MAX_ITERATIONS;		
	private double temperature = -1.0;
	private double cooldown = -1.0;
	private double repulsionMultiplier = 1.0;
	private double attractionMultiplier = 1.0;
	private double nodeBoundsExtension = 1.0;

	public GLFruchtermanReingoldLayout buildLayout() {		
		return (new GLFruchtermanReingoldLayout(maxIterations, temperature, cooldown, repulsionMultiplier, attractionMultiplier, nodeBoundsExtension));
	}
	
	public GLFruchtermanReingoldLayoutBuilder maxIterations(int maxIterations) {
		this.maxIterations = maxIterations;
		return this;
	}
	
	public GLFruchtermanReingoldLayoutBuilder temperatueAndCooldown(double temperature, double cooldown) {
		this.temperature = temperature;
		this.cooldown = cooldown;
		return this;
	}
	
	public GLFruchtermanReingoldLayoutBuilder repulsionMultiplier(double repulsionMultiplier) {
		this.repulsionMultiplier = repulsionMultiplier;
		return this;
	}
	
	public GLFruchtermanReingoldLayoutBuilder attractionMultiplier(double attractionMultiplier) {
		this.attractionMultiplier = attractionMultiplier;
		return this;
	}
	
	public GLFruchtermanReingoldLayoutBuilder nodeBoundsExtension(double nodeBoundsExtension) {
		this.nodeBoundsExtension = nodeBoundsExtension;
		return this;
	}

} 
