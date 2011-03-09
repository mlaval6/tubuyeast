package numerical;

import java.util.List;

import javax.vecmath.Vector2d;

import simulation.Particle;
import simulation.ParticleSystem;

public class VelocityVerlet implements Integrator {

	private ParticleSystem system;
	
	public String toString() {
		return "Velocity Verlet";
	}

	@Override
	public void initialize(ParticleSystem ps) {
		system = ps;
	}

	@Override
	public void step(double t, double h, int numIterations) {
		List<Particle> particles = system.getParticles();
		Vector2d apt = new Vector2d();
		Vector2d a = new Vector2d();
		
		double[] v_t12 = new double[2*particles.size()];

		int j = 0;

		for (Particle p : particles) {
			if (p.pinned) continue;

			a.scale(1 / p.mass, p.f);
			
			v_t12[2*j + 0] = p.v.x + 0.5 * h * (a.x + apt.x);
			v_t12[2*j + 1] = p.v.y + 0.5 * h * (a.y + apt.y);
			j++;
		}

		int i = 0;
		for (Particle p : particles) {
			if (p.pinned) continue;
			
			a.scale(1 / p.mass, p.f);
			p.p.x += h * v_t12[2*i + 0];
			p.p.y += h * v_t12[2*i + 1];
			
			i++;
		}
		
		system.updateForces();		
		
		int k = 0;
		for (Particle p : particles) {
			if (p.pinned) continue;
			
			a.scale(1 / p.mass, p.f);
			p.v.x = v_t12[2*k + 0] + 0.5 * h * a.x;
			p.v.y = v_t12[2*k + 1] + 0.5 * h * a.y;
			
			k++;
		}
	}
}
