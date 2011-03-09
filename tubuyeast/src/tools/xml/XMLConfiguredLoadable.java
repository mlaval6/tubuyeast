/*******************************************************************************
 * XMLConfiguredLoadable.java
 * <p>
 * Created on 13-Dec-2004
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import org.w3c.dom.Node;

/**
 * This is the interface for any object that is capable of unmarshalling itself
 * from XML, with guidance in the form of some configuration object.
 * 
 * @param <Configuration>
 *        the type of the already-initialized data that is needed by this
 *        loadable to load itself from XML
 * @author tedmunds
 */
public interface XMLConfiguredLoadable<Configuration>
{
    /**
     * Unmarshals this object from the provided XML <code>Node</code>,
     * possibly guided by the provided <code>configuration</code> information.
     * 
     * @param dataNode
     *        the <code>Node</code> from which to unmarshal this object
     * @param configuration
     *        the <code>Configuration</code> that provides any information the
     *        loader will need to decide what/how to load
     * @throws XMLException
     *         if this object cannot be unmarshalled from the provided
     *         <code>Node</code> (eg. if the node type or structure is not
     *         supported)
     * @throws InitializationException
     *         if this object cannot be initialized from the supplied data
     */
    public void loadFromXML(Node dataNode, Configuration configuration)
        throws XMLException,
            InitializationException;
}
