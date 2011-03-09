/*
 * Created on Sep 4, 2003
 */
package tools.xml;

import java.util.StringTokenizer;

import javax.vecmath.Matrix4d;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>double</code> vector.
 * A double vector must be either space or comma delimited.
 * @author Shinjiro Sueda
 */
public class XMLMatrix4d implements XMLMappable
{
    /**
     * The underlying primitive value
     */
    private Matrix4d contents;
    
    /**
     * Constructs an <code>XMLDoubleArray</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLMatrix4d()
    {
        super();
    }
    
    /**
     * Constructs an <code>XMLDoubleArray</code> with the specified contents,
     * ready to be exported into an XML node.
     * 
     * @param contents the underlying primitive contents
     */
    public XMLMatrix4d(Matrix4d contents)
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
            case Node.TEXT_NODE :
                String vectorString = dataNode.getNodeValue();
                try
                {
                    StringTokenizer st = new StringTokenizer(vectorString, ", ");
                    this.contents = new Matrix4d();
                    this.contents.m00 = Double.parseDouble(st.nextToken());
                    this.contents.m01 = Double.parseDouble(st.nextToken());
                    this.contents.m02 = Double.parseDouble(st.nextToken());
                    this.contents.m03 = Double.parseDouble(st.nextToken());
                    this.contents.m10 = Double.parseDouble(st.nextToken());
                    this.contents.m11 = Double.parseDouble(st.nextToken());
                    this.contents.m12 = Double.parseDouble(st.nextToken());
                    this.contents.m13 = Double.parseDouble(st.nextToken());
                    this.contents.m20 = Double.parseDouble(st.nextToken());
                    this.contents.m21 = Double.parseDouble(st.nextToken());
                    this.contents.m22 = Double.parseDouble(st.nextToken());
                    this.contents.m23 = Double.parseDouble(st.nextToken());
                    this.contents.m30 = Double.parseDouble(st.nextToken());
                    this.contents.m31 = Double.parseDouble(st.nextToken());
                    this.contents.m32 = Double.parseDouble(st.nextToken());
                    this.contents.m33 = Double.parseDouble(st.nextToken());
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException("Invalid Node value.  " + vectorString + " is not a valid double vector.", nfe);
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

    /* (non-Javadoc)
     * @see org.havensoft.tools.xml.XMLExportable#exportAsXML(org.w3c.dom.Node)
     */
    public void exportAsXML(Node exportNode) throws XMLException
    {
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE :
            case Node.CDATA_SECTION_NODE :
            case Node.TEXT_NODE :
                String value = "";
                value += this.contents.m00 + ", ";
                value += this.contents.m01 + ", ";
                value += this.contents.m02 + ", ";
                value += this.contents.m03 + ", ";
                value += this.contents.m10 + ", ";
                value += this.contents.m11 + ", ";
                value += this.contents.m12 + ", ";
                value += this.contents.m13 + ", ";
                value += this.contents.m20 + ", ";
                value += this.contents.m21 + ", ";
                value += this.contents.m22 + ", ";
                value += this.contents.m23 + ", ";
                value += this.contents.m30 + ", ";
                value += this.contents.m31 + ", ";
                value += this.contents.m32 + ", ";
                value += this.contents.m33;
                exportNode.setNodeValue(value);
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
     * have been filled (by {@link #setContents(Matrix4d)} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public Matrix4d getContents()
    {
        return contents;
    }
    
    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(Matrix4d contents)
    {
        this.contents = contents;
    }

    private static final String VALUE_ATTRIBUTE_NAME = "value";
}
