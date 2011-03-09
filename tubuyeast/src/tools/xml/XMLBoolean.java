/*******************************************************************************
 * org.havensoft.tools.xml.XMLBoolean.java
 * 
 * Created on Jan 7, 2004
 * Created by sueda
 * 
 ******************************************************************************/
package tools.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>boolean</code>
 * primitive.
 * 
 * @author sueda
 */
public class XMLBoolean implements XMLMappable
{

    /**
     * The underlying primitive value
     */
    private boolean contents;

    /**
     * Constructs an <code>XMLBoolean</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLBoolean()
    {
        super();
    }
    
    /**
     * Constructs an <code>XMLBoolean</code> with the specified contents, ready
     * to be exported into an XML node.
     * 
     * @param contents the underlying primitive contents
     */
    public XMLBoolean(boolean contents)
    {
        super();
        
        this.contents = contents;
    }

    /* (non-Javadoc)
     * @see org.havensoft.tools.xml.XMLLoadable#loadFromXML(org.w3c.dom.Node)
     */
    public void loadFromXML(Node dataNode)
        throws XMLException
    {
        switch (dataNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                String booleanString = dataNode.getNodeValue();
                try
                {
                    this.contents = 
                        booleanString != null &&
                        (booleanString.equals("true") ||
                         booleanString.equals("TRUE"));
                }
                catch (Exception nfe)
                {
                    throw new XMLException(
                        "Invalid Node value.  " + booleanString +
                        " is not a valid boolean.", nfe);
                }
                break;
            case Node.ELEMENT_NODE:
                loadFromXML(XMLHelper
                    .getRequiredAttribute((Element)dataNode,
                                          VALUE_ATTRIBUTE_NAME));
                break;
            default :
                throw new XMLException(
                    "Invalid Node type.  "
                        + getClass().getName()
                        + " can only be loaded from "
                        + "an Attribute, "
                        + "a CDATA, "
                        + "an Element, "
                        + "or a Text Node.");
        }
    }

    /* (non-Javadoc)
     * @see org.havensoft.tools.xml.XMLExportable#exportAsXML(javax.media.j3d.Node)
     */
    public void exportAsXML(Node exportNode)
        throws XMLException
    {
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                exportNode.setNodeValue(Boolean.toString(contents));
                break;
            case Node.ELEMENT_NODE:
                Element exportElement = (Element)exportNode;
                XMLHelper.setAsSelfDescribing(exportElement, getClass());
                exportAsXML(XMLHelper.createAttribute(exportElement,
                                                      VALUE_ATTRIBUTE_NAME));
                break;
            default :
                throw new XMLException(
                    "Invalid Node type.  "
                        + getClass().getName()
                        + " can only be exported as "
                        + "an Attribute, "
                        + "a CDATA, "
                        + "an Element, "
                        + "or a Text Node.");
        }
    }
    
    /**
     * Gets the underlying primitive value.  To be called after the contents
     * have been filled (by {@link #setContents(boolean)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public boolean getContents()
    {
        return contents;
    }
    
    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(boolean contents)
    {
        this.contents = contents;
    }

    private static final String VALUE_ATTRIBUTE_NAME = "value";
}
