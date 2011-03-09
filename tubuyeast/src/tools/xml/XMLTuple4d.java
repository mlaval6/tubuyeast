/*******************************************************************************
 * XMLTuple4d.java
 * <p>
 * Created on 19-Jan-2005
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import java.util.StringTokenizer;

import javax.vecmath.Tuple4d;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around a {@link javax.vecmath.Tuple4d}
 * subclass. A string representation of a <code>Tuple4d</code> must be
 * whitespace-delimited.
 * 
 * @param <T>
 *        the actual type of the <code>Tuple4d</code>
 * @author tedmunds
 */
public class XMLTuple4d<T extends Tuple4d> implements XMLMappable
{
    /**
     * The underlying "primitive" value
     */
    private T contents;

    /**
     * Constructs an <code>XMLTuple4d</code> with the specified contents,
     * ready to be loaded from or exported into an XML node. The contents are
     * required for loading because this is a generic class, and so has no
     * mechanism to create an instance of the appropriate type of
     * <code>Tuple4d</code>.
     * 
     * @param contents
     *        the underlying "primitive" contents
     */
    public XMLTuple4d(T contents)
    {
        super();

        this.contents = contents;
    }

    /**
     * Gets the underlying "primitive" value. To be called after the contents
     * have been filled (by {@link #setContents(Tuple4d)}or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public T getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying "primitive" contents to the specified value.
     * 
     * @param contents
     *        the value to which to set the contents
     */
    public void setContents(T contents)
    {
        this.contents = contents;
    }

    /**
     * Extracts a <code>Tuple4d</code> from either a white-space delimited
     * text node or attribute, or from an attributed element.
     * 
     * @param dataNode
     *        the node from which the <code>Tuple4d</code> is to be extracted
     * @throws XMLException
     *         if the node cannot be extracted due to an invalid node type or
     *         invalid node structure.
     * @see tools.xml.XMLLoadable#loadFromXML(org.w3c.dom.Node)
     */
    public void loadFromXML(Node dataNode) throws XMLException
    {
        switch (dataNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                StringTokenizer tokenizer = new StringTokenizer(dataNode
                    .getNodeValue());
                if (tokenizer.countTokens() != 4)
                {
                    throw new XMLException("Invalid Node value.  "
                                           + "A Tuple4d string must contain"
                                           + " four whitespace-separated"
                                           + " doubles.");
                }
                try
                {
                    contents.x = Double.parseDouble(tokenizer.nextToken());
                    contents.y = Double.parseDouble(tokenizer.nextToken());
                    contents.z = Double.parseDouble(tokenizer.nextToken());
                    contents.w = Double.parseDouble(tokenizer.nextToken());
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException("Invalid Node value.  "
                                           + "A Tuple4d string must contain"
                                           + " valid doubles.", nfe);
                }
                break;
            case Node.ELEMENT_NODE:
                Element dataElement = (Element)dataNode;
                double x = XMLHelper.getDoubleAttribute(dataElement,
                                                        X_ATTRIBUTE_NAME,
                                                        getClass());
                double y = XMLHelper.getDoubleAttribute(dataElement,
                                                        Y_ATTRIBUTE_NAME,
                                                        getClass());
                double z = XMLHelper.getDoubleAttribute(dataElement,
                                                        Z_ATTRIBUTE_NAME,
                                                        getClass());
                double w = XMLHelper.getDoubleAttribute(dataElement,
                                                        W_ATTRIBUTE_NAME,
                                                        getClass());

                contents.set(x, y, z, w);
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be loaded from "
                                       + "an Attribute, " + "a CDATA, "
                                       + "a Text Node, " + "or an Element.");
        }
    }

    /**
     * Exports a <code>Tuple4d</code> into the specified xml node.
     * 
     * @param exportNode
     *        the node into which the quaternion should be exported
     * @throws XMLException
     *         if the provided node is not a valid type to carry a quaternion.
     * @see tools.xml.XMLExportable#exportAsXML(org.w3c.dom.Node)
     */
    public void exportAsXML(Node exportNode) throws XMLException
    {
        Tuple4d exportContents = getContents();
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                exportNode.setNodeValue(Double.toString(exportContents.x) + " "
                                        + Double.toString(exportContents.y)
                                        + " "
                                        + Double.toString(exportContents.z)
                                        + " "
                                        + Double.toString(exportContents.w));
                break;
            case Node.ELEMENT_NODE:
                Element exportElement = (Element)exportNode;
                XMLHelper.exportAttribute(exportElement,
                                          X_ATTRIBUTE_NAME,
                                          contents.x);
                XMLHelper.exportAttribute(exportElement,
                                          Y_ATTRIBUTE_NAME,
                                          contents.y);
                XMLHelper.exportAttribute(exportElement,
                                          Z_ATTRIBUTE_NAME,
                                          contents.z);
                XMLHelper.exportAttribute(exportElement,
                                          W_ATTRIBUTE_NAME,
                                          contents.w);
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
     * The name of the x attribute.
     */
    private static final String X_ATTRIBUTE_NAME = "x";
    /**
     * The name of the y attribute.
     */
    private static final String Y_ATTRIBUTE_NAME = "y";
    /**
     * The name of the z attribute.
     */
    private static final String Z_ATTRIBUTE_NAME = "z";
    /**
     * The name of the w attribute.
     */
    private static final String W_ATTRIBUTE_NAME = "w";

}
