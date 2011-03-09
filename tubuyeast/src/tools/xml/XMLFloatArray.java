/*
 * Created on Dec 20, 2003
 */
package tools.xml;

import java.util.StringTokenizer;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>float</code> array.
 * A float array must be either space or comma delimited.
 * @author Shinjiro Sueda
 */
public class XMLFloatArray implements XMLMappable
{
    /**
     * The underlying primitive value
     */
    private float[] contents;
    
    /**
     * Constructs an <code>XMLFloatArray</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLFloatArray()
    {
        super();
    }
    
    /**
     * Constructs an <code>XMLFloatArray</code> with the specified contents,
     * ready to be exported into an XML node.
     * 
     * @param contents the underlying primitive contents
     */
    public XMLFloatArray(float[] contents)
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
                    int n = st.countTokens();
                    this.contents = new float[n];
                    for(int i = 0; i < n; ++i)
                    {
                        this.contents[i] = Float.parseFloat(st.nextToken());
                    }
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException(
                            "Invalid Node value.  " + vectorString + 
                            " is not a valid float array.", nfe);
                }
                break;
            case Node.ELEMENT_NODE :
                Element dataElement = (Element) dataNode;
                Attr arrayAttr =
                    dataElement.getAttributeNode(ARRAY_ATTRIBUTE_NAME);
                if (arrayAttr == null)
                {
                    throw new XMLException(
                        "Invalid Node structure.  "
                            + getClass().getName()
                            + " requires that the "
                            + ARRAY_ATTRIBUTE_NAME
                            + " attribute be present.");
                }
                loadFromXML(arrayAttr);
                break;
            default :
                throw new XMLException(
                    "Invalid Node type.  "
                        + getClass().getName()
                        + " can only be loaded from "
                        + "an Element, "
                        + "an Attribute, "
                        + "a CDATA, "
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
                int i = 0;
                for(i = 0; i < this.contents.length - 1; ++i)
                {
                    value += this.contents[i] + " ";
                }
                value += this.contents[i]; 
                exportNode.setNodeValue(value);
                break;
            case Node.ELEMENT_NODE :
                Element exportElement = (Element) exportNode;
                Attr arrayAttr =
                    exportElement.getOwnerDocument().createAttribute(
                        ARRAY_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(arrayAttr);
                exportAsXML(arrayAttr);
                break;
            default :
                throw new XMLException(
                    "Invalid Node type.  "
                        + getClass().getName()
                        + " can only be exported as "
                        + "an Element, "
                        + "an Attribute, "
                        + "a CDATA, "
                        + "or a Text Node.");
        }
    }
    
    /**
     * Gets the underlying primitive value.  To be called after the contents
     * have been filled (by {@link #setContents(float[])} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public float[] getContents()
    {
        return contents;
    }
    
    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(float[] contents)
    {
        this.contents = contents;
    }

    private static final String ARRAY_ATTRIBUTE_NAME = "array";
}
