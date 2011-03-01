package numerical;

import simulation.ParticleSystem;

/**
 * Interface for a numerical integration method
 * @author kry
 */
public interface Integrator {

    /** 
     * @return the name of this numerical integration method
     */
    public String getName();
        
    /** 
     * advances the system at t by h 
     * @param y 
     * @param n 
     * @param t 
     * @param h 
     * @param yout 
     * @param derivs 
     */
    public void step(double[] y, int n, double t, double h, double[] yout, Function derivs, ParticleSystem ps);

    /**
     * @return The number of steps (derivative calls) required by this integrator
     */
    public double getSubSteps(); 
}
