/*******************************************************************************
 * XMLLong.java
 * <p>
 * Created on Mar 31, 2004
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>long</code> primitive.
 * 
 * @author tedmunds
 */
public class XMLLong implements XMLMappable
{

    /**
     * The underlying primitive value
     */
    private long contents;

    /**
     * Constructs an <code>XMLLong</code> with no contents, ready to be loaded
     * from an XML node.
     * 
     */
    public XMLLong()
    {
        super();
    }

    /**
     * Constructs an <code>XMLLong</code> with the specified contents, ready to
     * be exported into an XML node.
     * 
     * @param contents the underlying primitive contents
     */
    public XMLLong(long contents)
    {
        super();

        this.contents = contents;
    }

    /**
     * Loads the contents of the provided <code>Node</code> as a long.
     * 
     * @param dataNode the node from which a long is to be loaded
     * @throws XMLException if the contents cannot be loaded
     * @see tools.xml.XMLLoadable#loadFromXML(org.w3c.dom.Node)
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
                    this.contents = Long.parseLong(numberString);
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException(
                        "Invalid Node value.  "
                            + numberString
                            + " is not a valid long.",
                        nfe);
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

    /**
     * Exports the encapsulated long primitive as XML.
     * 
     * @param exportNode the node to which the contents should be exported
     * @throws XMLException if the contents cannot be exported
     * @see tools.xml.XMLExportable#exportAsXML(org.w3c.dom.Node)
     */
    public void exportAsXML(Node exportNode) throws XMLException
    {
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                exportNode.setNodeValue(Long.toString(contents));
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
     * have been filled (by {@link #setContents(long)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public long getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(long contents)
    {
        this.contents = contents;
    }

    private static final String VALUE_ATTRIBUTE_NAME = "value";
}
