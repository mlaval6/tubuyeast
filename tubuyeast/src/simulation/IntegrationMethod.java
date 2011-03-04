package simulation;

import no.uib.cipr.matrix.sparse.CompRowMatrix;

/**
 * Interface for methods integrating Newton's equations of motion.
 * @author epiuze
 *
 */
public interface IntegrationMethod {
	public abstract void initialize(ParticleSystem ps);
	
	/**
	public abstract void update();
	
	/**
	 * @param K stiffness matrix
	 * @param B damping matrix
	 * @param t current simulation time
	 * @param h step size
	 * @param numIterations
	 */
    public abstract void step(CompRowMatrix K, CompRowMatrix B, double t, double h, int numIterations);

}
