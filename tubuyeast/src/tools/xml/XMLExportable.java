/*******************************************************************************
 * org.havensoft.tools.xml.XMLExportable.java
 * 
 * Created on Jun 13, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import org.w3c.dom.Node;

/**
 * This is the interface for any object that is capable of marshalling itself
 * as XML.
 * 
 * @author tedmunds
 */
public interface XMLExportable
{
    /**
     * Marshals this object as XML by setting the properties of the provided
     * <code>Node</code>.
     * 
     * @param exportNode the <code>Node</code> into which to marshal this object
     * @throws XMLException if this object cannot be marshalled into the
     *                      provided <code>Node</code> (eg. if the node type is
     *                      not supported)
     */
    public void exportAsXML(Node exportNode) throws XMLException;
}
