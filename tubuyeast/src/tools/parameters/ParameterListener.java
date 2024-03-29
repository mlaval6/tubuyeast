package tools.parameters;

/**
 * Listener interface for watching parameters change.
 */
public interface ParameterListener
{
    /**
     * Called by a <code>Parameter</code> with which this listener is registered
     * when the parameter's value is changed.
     * 
     * @param parameter the <code>Parameter</code> whose value changed
     */
    public void parameterChanged(Parameter parameter); 
}