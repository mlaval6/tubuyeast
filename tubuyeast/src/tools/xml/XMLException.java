/*******************************************************************************
 * org.havensoft.tools.xml.XMLException.java
 * 
 * Created on Jun 13, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

/**
 * Thrown when an error occurs during the handling of some XML data.
 * 
 * @author tedmunds
 */
public class XMLException extends Exception
{

    /**
     * Constructs an <code>XMLException</code> with no detail
     * message.
     */
    public XMLException()
    {
        super();
    }

    /**
     * Constructs an <code>XMLException</code> with the specified
     * detail message.
     * 
     * @param message the detail message
     */
    public XMLException(String message)
    {
        super(message);
    }

    /**
     * Constructs an <code>XMLException</code> with the specified
     * detail message and cause.
     * 
     * @param message the detail message
     * @param cause the underlying cause of the exception
     */
    public XMLException(String message, Throwable cause)
    {
        super(message, cause);
    }

    /**
     * Constructs an <code>XMLException</code> with the specified
     * cause.
     * 
     * @param cause the underlying cause of the exception
     */
    public XMLException(Throwable cause)
    {
        super(cause);
    }

    private static final long serialVersionUID = 3832902178112223542L;
}
