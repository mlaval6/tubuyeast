package simulation;

import java.util.ArrayList;
import java.util.List;

public class MotherCell {

	private double radius;
	private List<Particle> particles;
	
	
	public MotherCell(){
		radius = 205.0/2.0;
		particles = new ArrayList<Particle>();
	}

	public double getaRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public List<Particle> getInnerMembraneParticles() {
		return particles;
	}

	public List<Particle> getOuterMembraneParticles() {
		return particles;
	}
	
	public void setInnerMembraneParticles(List<Particle> particles) {
		this.particles = particles;
	}
	
	public void setOuterMembraneParticles(List<Particle> particles) {
		this.particles = particles;
	}
}
