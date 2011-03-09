/*******************************************************************************
 * XMLMatrix3d.java
 * <p>
 * Created on 15-Jun-2005
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import java.util.StringTokenizer;

import javax.vecmath.Matrix3d;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around a {@link javax.vecmath.Matrix3d}.
 * The text representation must be column major, with the entries space or comma
 * delimited.
 * 
 * @author tedmunds
 */
public class XMLMatrix3d implements XMLMappable
{
    /**
     * The underlying primitive value
     */
    private Matrix3d contents;

    /**
     * Constructs an <code>XMLMatrix3d</code> with no contents, ready to be
     * loaded from an XML node.
     */
    public XMLMatrix3d()
    {
        super();
    }

    /**
     * Constructs an <code>XMLMatrix3d</code> with the specified contents,
     * ready to be exported into an XML node.
     * 
     * @param contents
     *        the underlying primitive contents
     */
    public XMLMatrix3d(Matrix3d contents)
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
                String vectorString = dataNode.getNodeValue();
                try
                {
                    StringTokenizer st = new StringTokenizer(vectorString, ", ");
                    this.contents = new Matrix3d();
                    this.contents.m00 = Double.parseDouble(st.nextToken());
                    this.contents.m01 = Double.parseDouble(st.nextToken());
                    this.contents.m02 = Double.parseDouble(st.nextToken());
                    this.contents.m10 = Double.parseDouble(st.nextToken());
                    this.contents.m11 = Double.parseDouble(st.nextToken());
                    this.contents.m12 = Double.parseDouble(st.nextToken());
                    this.contents.m20 = Double.parseDouble(st.nextToken());
                    this.contents.m21 = Double.parseDouble(st.nextToken());
                    this.contents.m22 = Double.parseDouble(st.nextToken());
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException("Invalid Node value.  "
                                               + vectorString
                                               + " is not a valid double vector.",
                                           nfe);
                }
                break;
            case Node.ELEMENT_NODE:
                loadFromXML(XMLHelper
                    .getRequiredAttribute((Element)dataNode,
                                          VALUE_ATTRIBUTE_NAME));
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be loaded from "
                                       + "an Attribute, " + "a CDATA, "
                                       + "an Element, "
                                       + "or a Text Node.");
        }
    }

    public void exportAsXML(Node exportNode) throws XMLException
    {
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                String value = "";
                value += this.contents.m00 + ", ";
                value += this.contents.m01 + ", ";
                value += this.contents.m02 + ", ";
                value += this.contents.m10 + ", ";
                value += this.contents.m11 + ", ";
                value += this.contents.m12 + ", ";
                value += this.contents.m20 + ", ";
                value += this.contents.m21 + ", ";
                value += this.contents.m22 + ", ";
                exportNode.setNodeValue(value);
                break;
            case Node.ELEMENT_NODE:
                Element exportElement = (Element)exportNode;
                XMLHelper.setAsSelfDescribing(exportElement, getClass());
                exportAsXML(XMLHelper.createAttribute(exportElement,
                                                      VALUE_ATTRIBUTE_NAME));
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be exported as "
                                       + "an Attribute, " + "a CDATA, "
                                       + "an Element, "
                                       + "or a Text Node.");
        }
    }

    /**
     * Gets the underlying primitive value. To be called after the contents have
     * been filled (by {@link #setContents(Matrix3d)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Matrix3d getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents
     *        the value to which to set the contents
     */
    public void setContents(Matrix3d contents)
    {
        this.contents = contents;
    }

    private static final String VALUE_ATTRIBUTE_NAME = "value";
}
