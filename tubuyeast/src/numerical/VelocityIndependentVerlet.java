package numerical;

import java.util.List;

import javax.vecmath.Vector2d;

import simulation.Particle;
import simulation.ParticleSystem;

/**
 * This integrator should only be used if the acceleration is independent of velocity.
 * @author piuze
 *
 */
public class VelocityIndependentVerlet implements Integrator {

	private ParticleSystem system;
	
	public String toString() {
		return "Velocity Verlet (vel. independent)";
	}

	@Override
	public void initialize(ParticleSystem ps) {
		system = ps;
	}

	@Override
	public void step(double t, double h, int numIterations) {
		List<Particle> particles = system.getParticles();
		
		double[] a_t = new double[2*particles.size()];
		
		Vector2d a = new Vector2d();
		int i = 0;
		for (Particle p : particles) {
			if (p.pinned) continue;
			
			a.scale(1 / p.mass, p.f);
			p.p.x += h * p.v.x + 0.5 * h * h * a.x;
			p.p.y += h * p.v.y + 0.5 * h * h * a.y;

			a_t[2*i + 0] = a.x;
			a_t[2*i + 1] = a.y;
			
			i++;
		}
		
		system.updateForces();
		
		int j = 0;
		Vector2d apt = new Vector2d();
		for (Particle p : particles) {
			if (p.pinned) continue;

			a.scale(1 / p.mass, p.f);
			apt.set(a_t[2*j + 0], a_t[2*j + 1]);
			
			p.v.x += 0.5 * h * (a.x + apt.x);
			p.v.y += 0.5 * h * (a.y + apt.y);
			j++;
		}
	}
}
