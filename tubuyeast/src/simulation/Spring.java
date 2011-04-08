package simulation;

import javax.media.opengl.GLAutoDrawable;

import no.uib.cipr.matrix.Matrix;

public interface Spring {
	
	/**
	 * Apply the force exerted by this spring.
	 */
	public void apply();

	/** 
     * Adds this spring's contribution to stiffness and damping matrices.
     * @param K the stiffness matrix
     * @param B the damping matrix
     */
    public void gradient(Matrix K, Matrix B);
    
    /**
     * Intersect a particle with this spring.
     * @param p
     * @param stepSize
     * @return
     */
    public boolean intersect(Particle p, double stepSize);

    /**
     * Set the stiffness constant for this spring.
     * @param k
     */
	public void setK(double value);

    /**
     * @return the stiffness constant of this spring.
     */
    public double getK();

    /**
     * Set the damping constant for this spring.
     * @param b
     */
	public void setB(double value);
	
    /**
     * @return the damping constant of this spring.
     */
    public double getB();

	/**
	 * Display this spring.
	 * @param drawable
	 */
	public void display(GLAutoDrawable drawable);
}
