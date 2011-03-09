/*******************************************************************************
 * XMLRectangle.java
 * <p>
 * Created on Jun 16, 2004
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import java.awt.Rectangle;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the {@link Rectangle}
 * "primitive".
 * 
 * @author tedmunds
 */
public class XMLRectangle implements XMLMappable
{
    private Rectangle contents;

    /**
     * Constructs an <code>XMLRectangle</code> with no contents, ready to be
     * loaded from an XML node.
     */
    public XMLRectangle()
    {
        super();
    }

    /**
     * Constructs an <code>XMLRectangle</code> with the specified contents,
     * ready to be exported into an XML node.
     * 
     * @param contents
     *        the underlying primitive contents
     */
    public XMLRectangle(Rectangle contents)
    {
        super();

        this.contents = contents;
    }

    public void loadFromXML(Node dataNode) throws XMLException
    {
        switch (dataNode.getNodeType())
        {
            case Node.ELEMENT_NODE:
                Element dataElement = XMLHelper.verifyNodeAsElement(dataNode,
                                                                    getClass());
                contents = new Rectangle();
                XMLInt intXML = new XMLInt();
                intXML.loadFromXML(XMLHelper
                    .getRequiredAttribute(dataElement,
                                          X_ATTRIBUTE_NAME,
                                          getClass()));
                contents.x = intXML.getContents();
                intXML.loadFromXML(XMLHelper
                    .getRequiredAttribute(dataElement,
                                          Y_ATTRIBUTE_NAME,
                                          getClass()));
                contents.y = intXML.getContents();
                intXML.loadFromXML(XMLHelper
                    .getRequiredAttribute(dataElement,
                                          WIDTH_ATTRIBUTE_NAME,
                                          getClass()));
                contents.width = intXML.getContents();
                intXML.loadFromXML(XMLHelper
                    .getRequiredAttribute(dataElement,
                                          HEIGHT_ATTRIBUTE_NAME,
                                          getClass()));
                contents.height = intXML.getContents();
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be loaded from "
                                       + "an Element.");
        }
    }

    public void exportAsXML(Node exportNode) throws XMLException
    {
        switch (exportNode.getNodeType())
        {
            case Node.ELEMENT_NODE:
                Element exportElement = XMLHelper
                    .verifyNodeAsElement(exportNode, getClass());
                XMLInt intXMl = new XMLInt(contents.x);
                XMLHelper.exportAttribute(exportElement,
                                          X_ATTRIBUTE_NAME,
                                          intXMl);
                intXMl.setContents(contents.y);
                XMLHelper.exportAttribute(exportElement,
                                          Y_ATTRIBUTE_NAME,
                                          intXMl);
                intXMl.setContents(contents.width);
                XMLHelper.exportAttribute(exportElement,
                                          WIDTH_ATTRIBUTE_NAME,
                                          intXMl);
                intXMl.setContents(contents.height);
                XMLHelper.exportAttribute(exportElement,
                                          HEIGHT_ATTRIBUTE_NAME,
                                          intXMl);
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be exported as "
                                       + "an Element.");
        }
    }

    /**
     * Gets the underlying primitive value. To be called after the contents have
     * been filled (by {@link #setContents(Rectangle)}or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Rectangle getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents
     *        the value to which to set the contents
     */
    public void setContents(Rectangle contents)
    {
        this.contents = contents;
    }

    private static final String X_ATTRIBUTE_NAME = "x";
    private static final String Y_ATTRIBUTE_NAME = "y";
    private static final String WIDTH_ATTRIBUTE_NAME = "width";
    private static final String HEIGHT_ATTRIBUTE_NAME = "height";
}