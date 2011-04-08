package simulation;

import java.util.ArrayList;
import java.util.List;

public class Bud {

	private double aRadius;
	private List<Particle> particles;
	private double yTranspose;
	
	
	public Bud(){
		aRadius = 40.0;
		yTranspose = 130;
		particles = new ArrayList<Particle>();
	}

	public double getaRadius() {
		return aRadius;
	}

	public void setaRadius(double aRadius) {
		this.aRadius = aRadius;
	}
	
	public double getYTranspose(){
		return this.yTranspose;
	}
	
	public void setYTranspose(double yTranspose){
		this.yTranspose = yTranspose;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public void setParticles(List<Particle> particles) {
		this.particles = particles;
	}
	
}
