package simulation;

import no.uib.cipr.matrix.sparse.CompRowMatrix;

/**
 * Interface for numerical integration methods.
 * @author epiuze
 *
 */
public interface Integrator {
	
	/**
	 * Initialize the integration method with this particle system.
	 * @param ps
	 */
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
    public abstract void step(double t, double h, int numIterations);
    
    /**
     * @return the name of this method.
     */
    public abstract String toString();

}
