/*******************************************************************************
 * org.havensoft.tools.xml.XMLLoadable.java
 * 
 * Created on Jun 13, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import org.w3c.dom.Node;

/**
 * This is the interface for any object that is capable of unmarshalling itself
 * from XML.
 * 
 * @author tedmunds
 */
public interface XMLLoadable
{
    /**
     * Unmarshals this object from the provided XML <code>Node</code>.
     * 
     * @param dataNode
     *        the <code>Node</code> from which to unmarshal this object
     * @throws XMLException
     *         if this object cannot be unmarshalled from the provided
     *         <code>Node</code> (eg. if the node type or structure is not
     *         supported)
     * @throws InitializationException
     *         if the loaded object cannot be initialized
     */
    public void loadFromXML(Node dataNode)
        throws XMLException,
            InitializationException;
}
