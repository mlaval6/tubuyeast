/*******************************************************************************
 * XMLColour.java
 * <p>
 * Created on 1-Mar-2005
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import java.awt.Color;
import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the {@link java.awt.Color}
 * class. A string representation of a colour must be whitespace-delimited.
 * 
 * @author tedmunds
 */
public class XMLColour implements XMLMappable
{
    /**
     * The underlying "primitive" value
     */
    private Color contents;

    /**
     * Constructs an <code>XMLColour</code> with no contents, ready to be
     * loaded from an XML node.
     */
    public XMLColour()
    {
        super();
    }

    /**
     * Constructs an <code>XMLColour</code> with the specified contents, ready
     * to be exported into an XML node.
     * 
     * @param contents
     *        the underlying "primitive" contents
     */
    public XMLColour(Color contents)
    {
        super();

        this.contents = contents;
    }

    public void loadFromXML(Node dataNode) throws XMLException
    {
        switch (dataNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                StringTokenizer tokenizer = new StringTokenizer(dataNode
                    .getNodeValue());
                if (tokenizer.countTokens() < 3 || tokenizer.countTokens() > 4)
                {
                    throw new XMLException("Invalid Node value.  "
                                           + "A colour string must contain"
                                           + " three or four whitespace"
                                           + "-separated floats.");
                }
                try
                {
                    float red = Float.parseFloat(tokenizer.nextToken());
                    float green = Float.parseFloat(tokenizer.nextToken());
                    float blue = Float.parseFloat(tokenizer.nextToken());
                    if (tokenizer.countTokens() > 0)
                    {
                        float alpha = Float.parseFloat(tokenizer.nextToken());
                        contents = new Color(red, green, blue, alpha);
                    }
                    else
                    {
                        contents = new Color(red, green, blue);
                    }
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException("Invalid Node value.  "
                                           + "A colour string must contain"
                                           + " valid floats.", nfe);
                }
                break;
            case Node.ELEMENT_NODE:
                Element dataElement = (Element)dataNode;
                float red = XMLHelper.getFloatAttribute(dataElement,
                                                        RED_ATTRIBUTE_NAME,
                                                        getClass());
                float green = XMLHelper.getFloatAttribute(dataElement,
                                                          GREEN_ATTRIBUTE_NAME,
                                                          getClass());
                float blue = XMLHelper.getFloatAttribute(dataElement,
                                                         BLUE_ATTRIBUTE_NAME,
                                                         getClass());
                float alpha = XMLHelper.getFloatAttribute(dataElement,
                                                          ALPHA_ATTRIBUTE_NAME,
                                                          1);

                contents = new Color(red, green, blue, alpha);
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be loaded from "
                                       + "an Attribute, " + "a CDATA, "
                                       + "a Text Node, " + "or an Element.");
        }
    }

    public void exportAsXML(Node exportNode) throws XMLException
    {
        float[] components = contents.getComponents(null);
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                exportNode.setNodeValue(Float.toString(components[0]) + " "
                                        + Float.toString(components[1]) + " "
                                        + Float.toString(components[2]) + " "
                                        + Float.toString(components[3]));
                break;
            case Node.ELEMENT_NODE:
                Element exportElement = (Element)exportNode;
                XMLHelper.exportAttribute(exportElement,
                                          RED_ATTRIBUTE_NAME,
                                          components[0]);
                XMLHelper.exportAttribute(exportElement,
                                          GREEN_ATTRIBUTE_NAME,
                                          components[1]);
                XMLHelper.exportAttribute(exportElement,
                                          BLUE_ATTRIBUTE_NAME,
                                          components[2]);
                XMLHelper.exportAttribute(exportElement,
                                          ALPHA_ATTRIBUTE_NAME,
                                          components[3]);
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be exported as "
                                       + "an Attribute, " + "a CDATA, "
                                       + "a Text Node, " + "or an Element.");
        }
    }

    /**
     * Gets the underlying "primitive" value. To be called after the contents
     * have been filled (by {@link #setContents(Color)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Color getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying "primitive" contents to the specified value.
     * 
     * @param contents
     *        the value to which to set the contents
     */
    public void setContents(Color contents)
    {
        this.contents = contents;
    }

    /**
     * The name of the red attribute.
     */
    private static final String RED_ATTRIBUTE_NAME = "red";
    /**
     * The name of the green attribute.
     */
    private static final String GREEN_ATTRIBUTE_NAME = "green";
    /**
     * The name of the blue attribute.
     */
    private static final String BLUE_ATTRIBUTE_NAME = "blue";
    /**
     * The name of the alpha attribute.
     */
    private static final String ALPHA_ATTRIBUTE_NAME = "alpha";

}
