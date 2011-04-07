package simulation;

import java.util.ArrayList;
import java.util.List;

public class Bud {

	private int aRadius;
	private List<Particle> particles;
	
	public Bud(int pRadius){
		aRadius = pRadius;
		particles = new ArrayList<Particle>();
	}

	public int getaRadius() {
		return aRadius;
	}

	public void setaRadius(int aRadius) {
		this.aRadius = aRadius;
	}

	public List<Particle> getParticles() {
		return particles;
	}

	public void setParticles(List<Particle> particles) {
		this.particles = particles;
	}
	
	
	
}
