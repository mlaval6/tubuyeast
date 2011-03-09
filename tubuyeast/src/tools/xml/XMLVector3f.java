/*******************************************************************************
 * XMLVector3f.java
 * 
 * Created on Jan 4, 2004
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import java.util.StringTokenizer;

import javax.vecmath.Vector3f;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the {@link Vector3f} class.
 * A string representation of a Vector3f must be whitespace-delimited.
 * 
 * @author tedmunds
 */
public class XMLVector3f implements XMLMappable
{
    /**
     * The underlying "primitive" value
     */
    private Vector3f contents;

    /**
     * Constructs an <code>XMLVector3f</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLVector3f()
    {
        super();
    }

    /**
     * Constructs an <code>XMLVector3f</code> with the specified contents, ready
     * to be exported into an XML node.
     * 
     * @param contents the underlying "primitive" contents
     */
    public XMLVector3f(Vector3f contents)
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
                            + "A Vector3f string must contain three "
                            + "whitespace-separated Floats.");
                }
                try
                {
                    this.contents = new Vector3f();
                    this.contents.x = Float.parseFloat(tokenizer.nextToken());
                    this.contents.y = Float.parseFloat(tokenizer.nextToken());
                    this.contents.z = Float.parseFloat(tokenizer.nextToken());
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException(
                        "Invalid Node value.  "
                            + "A Vector3f string must contain valid Floats.",
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
                XMLFloat xFloat = new XMLFloat();
                xFloat.loadFromXML(xAttr);
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
                XMLFloat yFloat = new XMLFloat();
                yFloat.loadFromXML(yAttr);
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
                XMLFloat zFloat = new XMLFloat();
                zFloat.loadFromXML(zAttr);

                this.contents =
                    new Vector3f(
                        xFloat.getContents(),
                        yFloat.getContents(),
                        zFloat.getContents());
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
                        Float.toString(contents.x)
                        + " "
                        + Float.toString(contents.y)
                        + " "
                        + Float.toString(contents.z));
                break;
            case Node.ELEMENT_NODE :
                Element exportElement = (Element) exportNode;
                Attr xAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        X_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(xAttr);
                XMLFloat xFloat = new XMLFloat(contents.x);
                xFloat.exportAsXML(xAttr);
                Attr yAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        Y_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(yAttr);
                XMLFloat yFloat = new XMLFloat(contents.y);
                yFloat.exportAsXML(yAttr);
                Attr zAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        Z_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(zAttr);
                XMLFloat zFloat = new XMLFloat(contents.z);
                zFloat.exportAsXML(zAttr);
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
     * have been filled (by {@link #setContents(Vector3f)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Vector3f getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying "primitive" contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(Vector3f contents)
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
