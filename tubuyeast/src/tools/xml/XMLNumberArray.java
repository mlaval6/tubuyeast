package tools.xml;

import java.lang.reflect.Array;
import java.util.StringTokenizer;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An <code>XMLMappable</code> wrapper around the <code>Number</code> array.
 * A number array must be either space or comma delimited.
 * 
 * @param <N>
 *        the type of number encapsulated by this class
 * @author tedmunds
 */
public class XMLNumberArray<N extends Number> implements XMLMappable
{
    /**
     * The underlying primitive value
     */
    private N[] contents;
    private Class<N> numberClass;

    /**
     * Constructs an <code>XMLNumberArray</code> with no contents, ready to be
     * loaded from an XML node.
     */
    public XMLNumberArray()
    {
        super();
    }

    /**
     * Constructs an <code>XMLNumberArray</code> with no contents, ready to be
     * loaded from an XML node.
     * 
     * @param numberClass
     *        the concrete class of number
     */
    public XMLNumberArray(Class<N> numberClass)
    {
        super();
        this.numberClass = numberClass;
    }

    /**
     * Constructs an <code>XMLFloatArray</code> with the specified contents,
     * ready to be exported into an XML node.
     * 
     * @param contents
     *        the underlying primitive contents
     */
    public XMLNumberArray(N[] contents)
    {
        super();

        setContents(contents);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.havensoft.tools.xml.XMLLoadable#loadFromXML(org.w3c.dom.Node)
     */
    @SuppressWarnings(
    {
        "unchecked"
    })
    // The array instance cast is almost perfectly safe, unless you tried pretty
    // hard to get an incorrect Number class as the numberClass
    public void loadFromXML(Node dataNode)
        throws XMLException,
            InitializationException
    {
        switch (dataNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                if (numberClass == null)
                {
                    throw new XMLException("Invalid Operation.  Numbers can"
                                           + " only be loaded from text data"
                                           + " if the class of the number has"
                                           + " been provided.");
                }
                String vectorString = dataNode.getNodeValue();
                try
                {
                    StringTokenizer st = new StringTokenizer(vectorString, ", ");
                    int n = st.countTokens();
                    this.contents = (N[])Array.newInstance(numberClass, n);
                    for (int i = 0; i < n; ++i)
                    {
                        this.contents[i] = NumberHelper.parse(st.nextToken(),
                                                              numberClass);
                    }
                }
                catch (NumberFormatException nfe)
                {
                    throw new XMLException("Invalid Node value.  "
                                               + vectorString
                                               + " is not a valid float array.",
                                           nfe);
                }
                break;
            case Node.ELEMENT_NODE:
                Element dataElement = (Element)dataNode;
                numberClass = NumberHelper
                    .loadNumberClass(dataElement,
                                     NUMBER_CLASS_ATTRIBUTE_NAME,
                                     getClass());
                Attr arrayAttr = dataElement
                    .getAttributeNode(ARRAY_ATTRIBUTE_NAME);
                if (arrayAttr == null)
                {
                    throw new XMLException("Invalid Node structure.  "
                                           + getClass().getName()
                                           + " requires that the "
                                           + ARRAY_ATTRIBUTE_NAME
                                           + " attribute be present.");
                }
                loadFromXML(arrayAttr);
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be loaded from "
                                       + "an Element, " + "an Attribute, "
                                       + "a CDATA, " + "or a Text Node.");
        }
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.havensoft.tools.xml.XMLExportable#exportAsXML(org.w3c.dom.Node)
     */
    public void exportAsXML(Node exportNode) throws XMLException
    {
        switch (exportNode.getNodeType())
        {
            case Node.ATTRIBUTE_NODE:
            case Node.CDATA_SECTION_NODE:
            case Node.TEXT_NODE:
                String value = "";
                int i = 0;
                for (i = 0; i < this.contents.length - 1; ++i)
                {
                    value += this.contents[i] + " ";
                }
                if (i < contents.length)
                {
                    value += this.contents[i];
                }
                exportNode.setNodeValue(value);
                break;
            case Node.ELEMENT_NODE:
                Element exportElement = (Element)exportNode;
                XMLHelper.exportAttribute(exportElement,
                                          NUMBER_CLASS_ATTRIBUTE_NAME,
                                          numberClass);
                Attr arrayAttr = exportElement.getOwnerDocument()
                    .createAttribute(ARRAY_ATTRIBUTE_NAME);
                exportElement.setAttributeNode(arrayAttr);
                exportAsXML(arrayAttr);
                break;
            default:
                throw new XMLException("Invalid Node type.  "
                                       + getClass().getName()
                                       + " can only be exported as "
                                       + "an Element, " + "an Attribute, "
                                       + "a CDATA, " + "or a Text Node.");
        }
    }

    /**
     * Gets the underlying primitive value. To be called after the contents have
     * been filled (by {@link #setContents(N[])} or {@link #loadFromXML(Node)}).
     * 
     * @return the underlying primitive value
     */
    public N[] getContents()
    {
        return contents;
    }

    /**
     * Sets the underlying primitive contents to the specified value.
     * 
     * @param contents
     *        the value to which to set the contents
     */
    @SuppressWarnings(
    {
        "unchecked"
    })
    // Suppressed because the compiler will prevent all but the most egregious
    // errors
    public void setContents(N[] contents)
    {
        this.contents = contents;
        if (contents.length > 0)
        {
            // This cast is safe unless you make the first object a _subclass_
            // of N. The compiler will at least make sure that the array type is
            // right
            numberClass = (Class<N>)contents[0].getClass();
        }
    }

    private static final String NUMBER_CLASS_ATTRIBUTE_NAME = "numberClass";
    private static final String ARRAY_ATTRIBUTE_NAME = "array";
}
