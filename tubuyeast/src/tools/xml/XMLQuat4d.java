/*******************************************************************************
 * XMLQuat4d.java
 * 
 * Created on Feb 16, 2004
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import java.util.StringTokenizer;

import javax.vecmath.Quat4d;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the {@link javax.vecmath.Quat4d}
 * class.  A string representation of a Quat4d must be whitespace-delimited.
 * 
 * @author tedmunds
 */
public class XMLQuat4d implements XMLMappable
{
    /**
     * The underlying "primitive" value
     */
    private Quat4d contents;

    /**
     * Constructs an <code>XMLQuat4d</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLQuat4d()
    {
        super();
    }

    /**
     * Constructs an <code>XMLQuat4d</code> with the specified contents, ready
     * to be exported into an XML node.
     * 
     * @param contents the underlying "primitive" contents
     */
    public XMLQuat4d(Quat4d contents)
    {
        super();

        this.contents = contents;
    }

    /**
     * Gets the underlying "primitive" value.  To be called after the contents
     * have been filled (by {@link #setContents(Quat4d)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Quat4d getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying "primitive" contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(Quat4d contents)
    {
        this.contents = contents;
    }

    /**
     * Extracts a <code>Quat4d</code> from either a white-space delimited text
     * node or attribute, or from an attributed element.
     * 
     * @param dataNode the node from which the <code>Quat4d</code> is to be
     *                 extracted
     * @throws XMLException if the node cannot be extracted due to an invalid
     *                      node type or invalid node structure.
     * @see tools.xml.XMLLoadable#loadFromXML(org.w3c.dom.Node)
     */
    public void loadFromXML(Node dataNode) throws XMLException
    {
        switch (dataNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                StringTokenizer tokenizer =
                    new StringTokenizer(dataNode.getNodeValue());
                if (tokenizer.countTokens() != 4)
                {
                    throw new XMLException(
                        "Invalid Node value.  "
                            + "A Quat4d string must contain four "
                            + "whitespace-separated doubles.");
                }
                try
                {
                    Quat4d loadedContents = new Quat4d();
                    loadedContents.x = Double
                        .parseDouble(tokenizer.nextToken());
                    loadedContents.y = Double
                        .parseDouble(tokenizer.nextToken());
                    loadedContents.z = Double
                        .parseDouble(tokenizer.nextToken());
                    loadedContents.w = Double
                        .parseDouble(tokenizer.nextToken());
                    setContents(loadedContents);
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException(
                        "Invalid Node value.  "
                            + "A Quat4d string must contain valid doubles.",
                        nfe);
                }
                break;
            case Node.ELEMENT_NODE :
                Element dataElement = (Element) dataNode;
                Attr xAttr = dataElement.getAttributeNode(X_ATTRIBUTE_NAME);
                if (xAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + X_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                XMLDouble xDouble = new XMLDouble();
                xDouble.loadFromXML(xAttr);
                Attr yAttr = dataElement.getAttributeNode(Y_ATTRIBUTE_NAME);
                if (yAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + Y_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                XMLDouble yDouble = new XMLDouble();
                yDouble.loadFromXML(yAttr);
                Attr zAttr = dataElement.getAttributeNode(Z_ATTRIBUTE_NAME);
                if (zAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + Z_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                XMLDouble zDouble = new XMLDouble();
                zDouble.loadFromXML(zAttr);
                Attr wAttr = dataElement.getAttributeNode(W_ATTRIBUTE_NAME);
                if (wAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + W_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                XMLDouble wDouble = new XMLDouble();
                wDouble.loadFromXML(wAttr);

                setContents(
                    new Quat4d(
                        xDouble.getContents(),
                        yDouble.getContents(),
                        zDouble.getContents(),
                        wDouble.getContents()));
                break;
            default :
                throw new XMLException(
                    "Invalid Node type.  "
                        + getClass().getName()
                        + " can only be loaded from "
                        + "an Attribute, "
                        + "a CDATA, "
                        + "a Text Node, "
                        + "or an Element.");
        }
    }

    /**
     * Exports a <code>Quat4d</code> into the specified xml node.
     * 
     * @param exportNode the node into which the quaternion should be exported
     * @throws XMLException if the provided node is not a valid type to carry
     *                      a quaternion.
     * @see tools.xml.XMLExportable#exportAsXML(org.w3c.dom.Node)
     */
    public void exportAsXML(Node exportNode) throws XMLException
    {
        Quat4d exportContents = getContents();
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                exportNode.setNodeValue(
                    Double.toString(exportContents.x)
                        + " "
                        + Double.toString(exportContents.y)
                        + " "
                        + Double.toString(exportContents.z)
                        + " "
                        + Double.toString(exportContents.w));
                break;
            case Node.ELEMENT_NODE :
                Element exportElement = (Element) exportNode;
                Attr xAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        X_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(xAttr);
                XMLDouble xDouble = new XMLDouble(exportContents.x);
                xDouble.exportAsXML(xAttr);
                Attr yAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        Y_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(yAttr);
                XMLDouble yDouble = new XMLDouble(exportContents.y);
                yDouble.exportAsXML(yAttr);
                Attr zAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        Z_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(zAttr);
                XMLDouble zDouble = new XMLDouble(exportContents.z);
                zDouble.exportAsXML(zAttr);
                Attr wAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        W_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(wAttr);
                XMLDouble wDouble = new XMLDouble(exportContents.w);
                wDouble.exportAsXML(wAttr);
                break;
            default :
                throw new XMLException(
                    "Invalid Node type.  "
                        + getClass().getName()
                        + " can only be exported as "
                        + "an Attribute, "
                        + "a CDATA, "
                        + "a Text Node, "
                        + "or an Element.");
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
