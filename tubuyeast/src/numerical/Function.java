package numerical;
/**
 *  Interface for a class that computes an unknown function's derivative.
 */
public interface Function {
    /**
     *  Evaluates derivatives for ODE integration.
     * @param t time 
     * @param y state
     * @param dydt to be filled with the derivative
     */
    public void derivs( double t, double y[], double dydt[] );

}




