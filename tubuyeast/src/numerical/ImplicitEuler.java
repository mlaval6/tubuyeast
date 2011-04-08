package numerical;

import java.util.ArrayList;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import simulation.LinearSpring;
import simulation.Particle;
import simulation.ParticleSystem;
import simulation.Spring;

/**
 * @author epiuze
 */
public class ImplicitEuler implements Integrator {

	/**
	 * The list of particles in the system;
	 */
	private ParticleSystem system;

	private ConjugateGradient cg;
	private FlexCompRowMatrix A;
	private FlexCompRowMatrix Ad;
	private DenseMatrix M, W;
	private DenseVector b, dv;
	private DenseVector f0, v0;

	private CompRowMatrix K, B;

	/**
	 * @param particleList
	 * @param cg
	 */
	public void initialize(ParticleSystem ps) {
		system = ps;

		buildK();

		initMatrices();

		cg = new ConjugateGradient(system.getParticles().size());

		updateConstraints();
	}

	private void buildK() {
		int n = system.getParticles().size();

		// Builds the stiffness matrix using the fact that some
		// particles are never in contact such that some columns are always zero
		// TODO: if other kinds of interacting forces (e.g. gravitational,
		// electrical) were to be added
		// the shape of this matrix would need to be reconsidered.
		int p1, p2;
		ArrayList<ArrayList<Integer>> connections = new ArrayList<ArrayList<Integer>>(
				n);
		for (int i = 0; i < n; i++) {
			connections.add(new ArrayList<Integer>());
		}

		for (Spring s : system.getSprings()) {
			
			if (s instanceof LinearSpring) {
				LinearSpring ls = (LinearSpring) s;
				p1 = ls.p1.index;
				p2 = ls.p2.index;

				connections.get(p1).add(p2);
				connections.get(p2).add(p1);
			}
		}

		// nz Goal: for each row (three per particle), add column index if it's
		// used

		int[][] nz = new int[2 * n][];

		int it = 0;

		// For each particle (row)
		for (int j = 0; j < n; j++) {

			// Three rows per particle
			// The number of columns used is 2 * the number of particles
			// connected
			// + the column for the gradient by its own position
			nz[2 * j + 0] = new int[2 * (connections.get(j).size() + 1)];
			nz[2 * j + 1] = new int[2 * (connections.get(j).size() + 1)];

			// Set the diagonal (each particle interacts with at least one
			// other)
			// this makes up the first three non-empty elements of each 2 rows
			// for this particle
			for (int d = 0; d < 2; d++) {
				nz[2 * j + 0][d] = 2 * j + d;
				nz[2 * j + 1][d] = 2 * j + d;

			}
			// We already have 2 values in each row
			it = 2;

			for (Integer index : connections.get(j)) {
				// Adds the contribution of this connection to the non-empty
				// elements for these 2 rows
				// 2 columns are added per row for this connection
				for (int d = 0; d < 2; d++) {
					nz[2 * j + 0][it] = 2 * index + d;
					nz[2 * j + 1][it] = 2 * index + d;

					it++;
				}
			}
		}

		// Mass and inverse mass matrices
		int[][] nz2 = new int[2 * n][2];
		// The matrix is diagonal -> only set a(i,i)
		for (int i = 0; i < n; i++) {
			nz2[2 * i + 0][0] = 2 * i + 0;
			nz2[2 * i + 1][0] = 2 * i + 1;
		}

		K = new CompRowMatrix(2 * n, 2 * n, nz);
		B = new CompRowMatrix(2 * n, 2 * n, nz);
	}

	private void initMatrices() {

		int dim = 2 * system.getParticles().size();

		// Find M and M^-1
		M = new DenseMatrix(dim, dim);
		for (Particle p : system.getParticles()) {
			M.set(2 * p.index + 0, 2 * p.index + 0, p.mass);
			M.set(2 * p.index + 1, 2 * p.index + 1, p.mass);
		}

		// TODO: use pseudo
		DenseMatrix I = Matrices.identity(M.numColumns());
		W = new DenseMatrix(dim, dim);
		M.solve(I, W);

		// Force
		f0 = new DenseVector(dim);

		// Velocity
		v0 = new DenseVector(dim);

		A = new FlexCompRowMatrix(dim, dim);

		b = new DenseVector(dim);

		// Initializes the solver
		// Add a convergence monitor
		// Allocate storage for Conjugate Gradients
		dv = new DenseVector(dim);

		cF = new DenseVector(2 * system.getParticles().size());
	}

	private void computeStiffnessMatrix() {
		// Reset stiffness and damping matrices
		K.zero();
		B.zero();
		for (Spring s : system.getSprings()) {
			s.gradient(K, B);
		}
	}

	/**
	 * Updates velocity constraints
	 */
	public void updateConstraints() {
		cg.updateConstraints(system.getParticles());
	}

	/**
	 * Constraint forces to apply after solving the CG
	 */
	private Vector cF;

	/**
	 * Backward Euler integration step
	 * 
	 * @param K
	 * @param B
	 * @param t
	 * @param h
	 * @param cs
	 */
	public void step(double t, double h, int numIterations) {

		computeStiffnessMatrix();

		// (W - h^2*K - h*B) dv = hf0 + h^2*K*v0)
		K.scale(h * h);
		B.scale(h);

		// W - h^2*K - h*B
		A.zero();
		A.add(K);
		A.add(B);
		A.scale(-1);
		A.add(M);

		int i = 0;
		for (Particle p : system.getParticles()) {
			i = p.index;

			// Set f0
			f0.set(2 * p.index, p.f.x + cF.get(2 * p.index));
			f0.set(2 * p.index + 1, p.f.y + cF.get(2 * p.index + 1));

			// Set v0
			v0.set(2 * p.index, p.v.x);
			v0.set(2 * p.index + 1, p.v.y);
		}

		// h(f0 + h*K*v0)
		K.mult(v0, b);
		f0.scale(h);
		b.add(f0);

		// Update constraints
		// and add contact forces
		// e.g. friction
		cg.updateConstraints(system.getParticles());

		cF = cg.solve(A, b, dv, numIterations);

		// Updates the positions
		for (Particle p : system.getParticles()) {

			p.v.x += dv.get(2 * p.index + 0);
			p.v.y += dv.get(2 * p.index + 1);

			// Innocent until proven guilty
			p.illegal = false;

		}

		// Updates position
		for (Particle p : system.getParticles()) {
			p.p.x += h * p.v.x;
			p.p.y += h * p.v.y;
		}
	}

	@Override
	public String toString() {
		return "Implicit Euler";
	}

}
