package numerical;

import java.util.List;

import simulation.Particle;
import simulation.ParticleSystem;

public class ForwardEuler implements Integrator {

	private ParticleSystem system;

	@Override
	public void initialize(ParticleSystem ps) {
		system = ps;
	}

	@Override
	public void step(double t, double h, int numIterations) {
		
		// Update velocity
		for (Particle p : system.getParticles()) {
			p.v.x += h * p.f.x / p.mass;
			p.v.y += h * p.f.y / p.mass;

		}

		// Update position
		for (Particle p : system.getParticles()) {
			if (p.pinned) continue;
			p.p.x += h * p.v.x;
			p.p.y += h * p.v.y;
		}
	}

	@Override
	public String toString() {
		return "Forward Euler";
	}

}
