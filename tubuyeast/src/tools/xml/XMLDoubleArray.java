/*
 * Created on Sep 4, 2003
 */
package tools.xml;

import java.util.StringTokenizer;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>double</code> vector.
 * A double vector must be either space or comma delimited.
 * @author Shinjiro Sueda
 */
public class XMLDoubleArray implements XMLMappable
{
    /**
     * The underlying primitive value
     */
    private double[] contents;
    
    /**
     * Constructs an <code>XMLDoubleArray</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     */
    public XMLDoubleArray()
    {
        super();
    }
    
    /**
     * Constructs an <code>XMLDoubleArray</code> with the specified contents,
     * ready to be exported into an XML node.
     * 
     * @param contents the underlying primitive contents
     */
    public XMLDoubleArray(double[] contents)
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
                    this.contents = new double[n];
                    for(int i = 0; i < n; ++i)
                    {
                        this.contents[i] = Double.parseDouble(st.nextToken());
                    }
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
                int i = 0;
                for(i = 0; i < this.contents.length - 1; ++i)
                {
                    value += this.contents[i] + " ";
                }
                if (contents.length > 0)
                {
                    value += this.contents[i];
                }
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
     * have been filled (by {@link #setContents(double[])} or
     * {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public double[] getContents()
    {
        return contents;
    }
    
    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents the value to which to set the contents
     */
    public void setContents(double[] contents)
    {
        this.contents = contents;
    }

    private static final String VALUE_ATTRIBUTE_NAME = "value";
}
