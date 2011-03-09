/*******************************************************************************
 * org.havensoft.tools.xml.XMLFloat.java
 * 
 * Created on Dec 20, 2003
 * Created by sueda
 * 
 ******************************************************************************/
package tools.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>float</code> primitive.
 * 
 * @author Shinjiro Sueda
 */
public class XMLFloat implements XMLMappable
{

    /**
     * The underlying primitive value
     */
    private float contents;

    /**
     * Constructs an <code>XMLFloat</code> with no contents, ready to be loaded
     * from an XML node.
     * 
     */
    public XMLFloat()
    {
        super();
    }

    /**
     * Constructs an <code>XMLFloat</code> with the specified contents, ready
     * to be exported into an XML node.
     * 
     * @param contents the underlying primitive contents
     */
    public XMLFloat(float contents)
    {
        super();

        this.contents = contents;
    }

    /* (non-Javadoc)
     * @see org.havensoft.tools.xml.XMLLoadable#loadFromXML(org.w3c.dom.Node)
     */
    public void loadFromXML(Node dataNode) throws XMLException
    {
        switch (dataNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                String numberString = dataNode.getNodeValue();
                try
                {
                    this.contents = Float.parseFloat(numberString);
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException("Invalid Node value.  "
                            + numberString + " is not a valid float.", nfe);
                }
                break;
            case Node.ELEMENT_NODE:
                loadFromXML(XMLHelper
                    .getRequiredAttribute((Element)dataNode,
                                          VALUE_ATTRIBUTE_NAME));
                break;
            default :
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be loaded from "
                                       + "an Attribute, " + "a CDATA, "
                                       + "an Element, " + "or a Text Node.");
        }
    }

    /* (non-Javadoc)
     * @see org.havensoft.tools.xml.XMLExportable#exportAsXML(javax.media.j3d.Node)
     */
    public void exportAsXML(Node exportNode) throws XMLException
    {
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                exportNode.setNodeValue(Float.toString(contents));
                break;
            case Node.ELEMENT_NODE:
                Element exportElement = (Element)exportNode;
                XMLHelper.setAsSelfDescribing(exportElement, getClass());
                exportAsXML(XMLHelper.createAttribute(exportElement,
                                                      VALUE_ATTRIBUTE_NAME));
                break;
            default :
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be exported as "
                                       + "an Attribute, " + "a CDATA, "
                                       + "an Element, " + "or a Text Node.");
        }
    }

    /**
     * Gets the underlying primitive value.  To be called after the contents
     * have been filled (by {@link #setContents(float)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public float getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(float contents)
    {
        this.contents = contents;
    }

    private static final String VALUE_ATTRIBUTE_NAME = "value";
}
