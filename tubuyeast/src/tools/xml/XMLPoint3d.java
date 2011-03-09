/*******************************************************************************
 * org.havensoft.tools.xml.XMLPoint3d.java
 * 
 * Created on Sep 20, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import java.util.StringTokenizer;

import javax.vecmath.Point3d;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the {@link Point3d} class.
 * A string representation of a Point3d must be whitespace-delimited.
 * 
 * @author tedmunds
 */
public class XMLPoint3d implements XMLMappable
{
    /**
     * The underlying "primitive" value
     */
    private Point3d contents;

    /**
     * Constructs an <code>XMLPoint3d</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLPoint3d()
    {
        super();
    }

    /**
     * Constructs an <code>XMLPoint3d</code> with the specified contents, ready
     * to be exported into an XML node.
     * 
     * @param contents the underlying "primitive" contents
     */
    public XMLPoint3d(Point3d contents)
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
                StringTokenizer tokenizer =
                    new StringTokenizer(dataNode.getNodeValue());
                if (tokenizer.countTokens() != 3)
                {
                    throw new XMLException(
                        "Invalid Node value.  "
                            + "A Point3d string must contain three "
                            + "whitespace-separated doubles.");
                }
                try
                {
                    this.contents = new Point3d();
                    this.contents.x = Double.parseDouble(tokenizer.nextToken());
                    this.contents.y = Double.parseDouble(tokenizer.nextToken());
                    this.contents.z = Double.parseDouble(tokenizer.nextToken());
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException(
                        "Invalid Node value.  "
                            + "A Point3d string must contain valid doubles.",
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

                this.contents =
                    new Point3d(
                        xDouble.getContents(),
                        yDouble.getContents(),
                        zDouble.getContents());
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
                exportNode.setNodeValue(
                    Double.toString(contents.x)
                        + " "
                        + Double.toString(contents.y)
                        + " "
                        + Double.toString(contents.z));
                break;
            case Node.ELEMENT_NODE :
                Element exportElement = (Element) exportNode;
                Attr xAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        X_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(xAttr);
                XMLDouble xDouble = new XMLDouble(contents.x);
                xDouble.exportAsXML(xAttr);
                Attr yAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        Y_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(yAttr);
                XMLDouble yDouble = new XMLDouble(contents.y);
                yDouble.exportAsXML(yAttr);
                Attr zAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        Z_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(zAttr);
                XMLDouble zDouble = new XMLDouble(contents.z);
                zDouble.exportAsXML(zAttr);
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
     * Gets the underlying "primitive" value.  To be called after the contents
     * have been filled (by {@link #setContents(Point3d)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Point3d getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying "primitive" contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(Point3d contents)
    {
        this.contents = contents;
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

}
