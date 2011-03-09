/*******************************************************************************
 * org.havensoft.tools.xml.XMLString.java
 * 
 * Created on Jun 13, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>String</code> "primitive".
 * 
 * @author tedmunds
 */
public class XMLString implements XMLMappable
{
    private String contents;

    /**
     * Constructs an <code>XMLString</code> with no contents, ready to be loaded
     * from an XML node.
     * 
     */
    public XMLString()
    {
        super();
    }
    
    /**
     * Constructs an <code>XMLInt</code> with the specified contents, ready to
     * be exported into an XML node.
     * 
     * @param contents the underlying primitive contents
     */
    public XMLString(String contents)
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
            case Node.COMMENT_NODE :
            case Node.TEXT_NODE :
                this.contents = dataNode.getNodeValue();
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
                        + "a Comment, "
                        + "or a Text Node.");
        }
    }
    
    /**
     * Loads this <code>XMLString</code> from the provided attribute. Since an
     * attribute can always be read as as string, this method does not throw the
     * exception thrown by {@link #loadFromXML(Node)}.
     * 
     * @param dataAttribute
     */
    public void loadFromXML(Attr dataAttribute)
    {
        this.contents = dataAttribute.getNodeValue();
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
            case Node.COMMENT_NODE :
            case Node.TEXT_NODE :
                exportNode.setNodeValue(contents);
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
                        + "a Comment, "
                        + "or a Text Node.");
        }
    }
    
    /**
     * Gets the underlying primitive value.  To be called after the contents
     * have been filled (by {@link #setContents(String)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public String getContents()
    {
        return contents;
    }
    
    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(String contents)
    {
        this.contents = contents;
    }

    private static final String VALUE_ATTRIBUTE_NAME = "value";
}
