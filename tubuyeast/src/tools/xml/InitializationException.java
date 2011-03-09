/*******************************************************************************
 * org.havensoft.human.simulation.InitializationException.java
 * 
 * Created on Jun 3, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

/**
 * Thrown when an error occurs when initializing an entity during an
 * application's startup phase.
 * 
 * @author tedmunds
 */
public class InitializationException extends Exception
{
    private static final long serialVersionUID = 6419670058842859324L;

    /**
     * Constructs an <code>InitializationException</code> with no detail
     * message.
     */
    public InitializationException()
    {
        super();
    }

    /**
     * Constructs an <code>InitializationException</code> with the specified
     * detail message.
     * 
     * @param message the detail message
     */
    public InitializationException(String message)
    {
        super(message);
    }

    /**
     * Constructs an <code>InitializationException</code> with the specified
     * detail message and cause.
     * 
     * @param message the detail message
     * @param cause the underlying cause of the exception
     */
    public InitializationException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs an <code>InitializationException</code> with the specified
     * cause.
     * 
     * @param cause the underlying cause of the exception
     */
    public InitializationException(Throwable cause)
    {
        super(cause);
    }

}
