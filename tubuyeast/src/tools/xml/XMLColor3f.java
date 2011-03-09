/*******************************************************************************
 * XMLColor3f.java
 * 
 * Created on Jan 4, 2004
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import java.util.StringTokenizer;

import javax.vecmath.Color3f;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the {@link Color3f} class.
 * A string representation of a Color3f must be whitespace-delimited.
 * 
 * @author tedmunds
 */
public class XMLColor3f implements XMLMappable
{
    /**
     * The underlying "primitive" value
     */
    private Color3f contents;

    /**
     * Constructs an <code>XMLColor3f</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLColor3f()
    {
        super();
    }

    /**
     * Constructs an <code>XMLColor3f</code> with the specified contents, ready
     * to be exported into an XML node.
     * 
     * @param contents the underlying "primitive" contents
     */
    public XMLColor3f(Color3f contents)
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
                            + "A Color3f string must contain three "
                            + "whitespace-separated Floats.");
                }
                try
                {
                    this.contents = new Color3f();
                    this.contents.x = Float.parseFloat(tokenizer.nextToken());
                    this.contents.y = Float.parseFloat(tokenizer.nextToken());
                    this.contents.z = Float.parseFloat(tokenizer.nextToken());
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException(
                        "Invalid Node value.  "
                            + "A Color3f string must contain valid Floats.",
                        nfe);
                }
                break;
            case Node.ELEMENT_NODE :
                Element dataElement = (Element) dataNode;
                Attr xAttr = dataElement.getAttributeNode(R_ATTRIBUTE_NAME);
                if (xAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + R_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                XMLFloat xFloat = new XMLFloat();
                xFloat.loadFromXML(xAttr);
                Attr yAttr = dataElement.getAttributeNode(G_ATTRIBUTE_NAME);
                if (yAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + G_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                XMLFloat yFloat = new XMLFloat();
                yFloat.loadFromXML(yAttr);
                Attr zAttr = dataElement.getAttributeNode(B_ATTRIBUTE_NAME);
                if (zAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + B_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                XMLFloat zFloat = new XMLFloat();
                zFloat.loadFromXML(zAttr);

                this.contents =
                    new Color3f(
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
                        R_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(xAttr);
                XMLFloat xFloat = new XMLFloat(contents.x);
                xFloat.exportAsXML(xAttr);
                Attr yAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        G_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(yAttr);
                XMLFloat yFloat = new XMLFloat(contents.y);
                yFloat.exportAsXML(yAttr);
                Attr zAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        B_ATTRIBUTE_NAME);
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
     * have been filled (by {@link #setContents(Color3f)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Color3f getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying "primitive" contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(Color3f contents)
    {
        this.contents = contents;
    }

    /**
     * The name of the x attribute.
     */
    private static final String R_ATTRIBUTE_NAME = "r";
    /**
     * The name of the y attribute.
     */
    private static final String G_ATTRIBUTE_NAME = "g";
    /**
     * The name of the z attribute.
     */
    private static final String B_ATTRIBUTE_NAME = "b";

}
