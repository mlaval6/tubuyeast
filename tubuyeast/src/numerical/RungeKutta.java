package numerical;

import simulation.Integrator;
import simulation.Particle;
import simulation.ParticleSystem;

public class RungeKutta implements Integrator {

	private ParticleSystem system;

	public String toString() {
		return "RK4";
	}

	@Override
	public void initialize(ParticleSystem ps) {
		system = ps;
	}

	@Override
	public void step(double t, double h, int numIterations) {

		for (int it = 0; it < numIterations; it++) {

			int m = system.getParticles().size();
			int n = 4 * m;
			double dt;

			double[] y = new double[n];
			double[] yout = new double[n];

			// Current state for each particle
			// p_x, p_y, v_x, v_y
			for (int i = 0; i < m; i++) {
				y[4 * i + 0] = system.getParticles().get(i).p.x;
				y[4 * i + 1] = system.getParticles().get(i).p.y;
				y[4 * i + 2] = system.getParticles().get(i).v.x;
				y[4 * i + 3] = system.getParticles().get(i).v.y;
			}

			// RK4 integration:
			// y_n+1 = y_n + 1/6h * (k1 + 2k_2 + 2k_3 + k_4)
			// k_1 = f(t_n, y_n)
			// k_2 = f(t_n + h/2, y_n + h/2 * k_1)
			// k_3 = f(t_n + h/2, y_n + h/2 * k_2)
			// k_4 = f(t_n + h, y_n + h*k_3)

			// Computing k1 = f(t_n, y_n)
			double[] k1 = new double[2 * m];
			for (int i = 0; i < m; i++) {
				// Velocity
				k1[2 * i + 0] = y[4 * i + 2];
				k1[2 * i + 1] = y[4 * i + 3];
			}

			// Computing k2 = f(t_n + h/2, y_n + h/2 * k_1)
			double[] k2 = new double[2 * m];
			dt = h / 2;

			// Advance in position only
			for (int i = 0; i < m; i++) {
				// Position (x, y) vector: y_n + h/2 * k_1
				yout[4 * i + 0] = y[4 * i + 0] + dt * k1[2 * i + 0];
				yout[4 * i + 1] = y[4 * i + 1] + dt * k1[2 * i + 1];

				// Velocity (vx, vy) vector
				yout[4 * i + 2] = y[4 * i + 2];
				yout[4 * i + 3] = y[4 * i + 3];
			}

			// Set new system state
			setState(yout);

			// Update the system
			system.updateForces();

			// Compute the new velocity by finding a = f/m
			// and set k2
			for (int i = 0; i < m; i++) {
				Particle p = system.getParticles().get(i);
				k2[2 * i + 0] = y[4 * i + 2] + dt * p.f.x / p.mass;
				k2[2 * i + 1] = y[4 * i + 3] + dt * p.f.y / p.mass;
			}

			// Computing k3 = f(t_n + h/2, y_n + h/2 * k_2)
			double[] k3 = new double[n];
			dt = h / 2;

			// Resets the state to the initial one
			setState(y);

			// Advance in position only
			for (int i = 0; i < m; i++) {
				// Position (x, y) vector: y_n + h/2 * k_2
				yout[4 * i + 0] = y[4 * i + 0] + dt * k2[2 * i + 0];
				yout[4 * i + 1] = y[4 * i + 1] + dt * k2[2 * i + 1];

				// Velocity (vx, vy) vector
				yout[4 * i + 2] = y[4 * i + 2];
				yout[4 * i + 3] = y[4 * i + 3];
			}

			// Set new system state
			setState(yout);

			// Update the system
			system.updateForces();

			// Compute the new velocity by finding a = f/m
			// and set k3
			for (int i = 0; i < m; i++) {
				Particle p = system.getParticles().get(i);
				k3[2 * i + 0] = y[4 * i + 2] + dt * p.f.x / p.mass;
				k3[2 * i + 1] = y[4 * i + 3] + dt * p.f.y / p.mass;
			}

			// Computing k4 = f(t_n + h, y_n + h*k_3)
			double[] k4 = new double[n];
			dt = h;

			// Resets the state to the initial one
			setState(y);

			// Advance in position only
			for (int i = 0; i < m; i++) {
				// Position (x, y) vector: y_n + h/2 * k_3
				yout[4 * i + 0] = y[4 * i + 0] + dt * k3[2 * i + 0];
				yout[4 * i + 1] = y[4 * i + 1] + dt * k3[2 * i + 1];

				// Velocity (vx, vy) vector
				yout[4 * i + 2] = y[4 * i + 2];
				yout[4 * i + 3] = y[4 * i + 3];
			}

			// Set new system state
			setState(yout);

			// Update the system
			system.updateForces();

			// Compute the new velocity by finding a = f/m
			// and set k4
			for (int i = 0; i < m; i++) {
				Particle p = system.getParticles().get(i);
				k4[2 * i + 0] = y[4 * i + 2] + dt * p.f.x / p.mass;
				k4[2 * i + 1] = y[4 * i + 3] + dt * p.f.y / p.mass;
			}

			// RK4 position update
			// y_n+1 = y_n + 1/6h * (k1 + 2k_2 + 2k_3 + k_4)
			setState(y);
			system.updateForces();

			// Set position
			for (int i = 0; i < m; i++) {
				if (system.getParticles().get(i).pinned) {
					yout[4 * i + 0] = system.getParticles().get(i).p.x;
					yout[4 * i + 1] = system.getParticles().get(i).p.y;
					continue;
				}
				yout[4 * i + 0] = y[4 * i + 0]
						+ h
						/ 6
						* (k1[2 * i + 0] + 2 * k2[2 * i + 0] + 2
								* k3[2 * i + 0] + k4[2 * i + 0]);
				yout[4 * i + 1] = y[4 * i + 1]
						+ h
						/ 6
						* (k1[2 * i + 1] + 2 * k2[2 * i + 1] + 2
								* k3[2 * i + 1] + k4[2 * i + 1]);
			}

			// Set velocity
			for (int i = 0; i < m; i++) {
				if (system.getParticles().get(i).pinned)
					continue;
				Particle p = system.getParticles().get(i);
				yout[4 * i + 2] = y[4 * i + 2] + h * p.f.x / p.mass;
				yout[4 * i + 3] = y[4 * i + 3] + h * p.f.y / p.mass;
			}

			// Set the final state
			setState(yout);
		}
	}

	private void setState(double[] yout) {
		for (int i = 0; i < system.getParticles().size(); i++) {
			Particle p = system.getParticles().get(i);
			p.p.x = yout[4 * i + 0];
			p.p.y = yout[4 * i + 1];
			p.v.x = yout[4 * i + 2];
			p.v.y = yout[4 * i + 3];
		}
	}

}
