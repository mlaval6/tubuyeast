/*******************************************************************************
 * XMLMap.java
 * <p>
 * Created on 23-May-2005
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This class represents a map from one type of <code>XMLMappable</code>
 * object to another type of <code>XMLMappable</code> object.
 * 
 * @param <K> the from type of the mapping
 * @param <V> the to type of the mapping
 * @author tedmunds
 */
public class XMLMap<K extends XMLMappable, V extends XMLMappable>
    implements
        XMLMappable
{
    private Map<K, V> contents;

    /**
     * Creates an empty map.
     */
    public XMLMap()
    {
        super();

        contents = new HashMap<K, V>();
    }

    /**
     * Creates a map with the provided contents.
     * 
     * @param contents
     *        the contents of the underlying mapping
     */
    public XMLMap(Map<K, V> contents)
    {
        super();
        this.contents = contents;
    }

    /**
     * Gets the underlying mapping.
     * 
     * @return the underlying map.
     */
    public Map<K, V> getContents()
    {
        return contents;
    }

    public void loadFromXML(Node dataNode)
        throws XMLException,
            InitializationException
    {
        List<Element> entryElements = XMLHelper
            .getChildElementListByTagName(dataNode, ENTRY_TAG_NAME);
        for (Iterator<Element> entryIter = entryElements.iterator(); entryIter
            .hasNext();)
        {
            Element entryElement = entryIter.next();
            Element keyElement = XMLHelper.getRequiredElement(entryElement,
                                                              KEY_TAG_NAME,
                                                              getClass());
            Element valueElement = XMLHelper.getRequiredElement(entryElement,
                                                                VALUE_TAG_NAME,
                                                                getClass());
            K key = XMLHelper.loadSelfDescribingObject(keyElement, getClass());
            V value = XMLHelper.loadSelfDescribingObject(valueElement,
                                                         getClass());
            contents.put(key, value);
        }
    }

    public void exportAsXML(Node exportNode) throws XMLException
    {
        XMLHelper
            .setAsSelfDescribing(XMLHelper.verifyNodeAsElement(exportNode,
                                                               getClass()),
                                 getClass());

        for (Iterator<K> keyIter = contents.keySet().iterator(); keyIter
            .hasNext();)
        {
            K key = keyIter.next();
            V value = contents.get(key);
            Element entryElement = XMLHelper.createChildElement(exportNode,
                                                                ENTRY_TAG_NAME);
            XMLHelper.exportElement(entryElement, KEY_TAG_NAME, key);
            XMLHelper.exportElement(entryElement, VALUE_TAG_NAME, value);
        }
    }

    private static final String ENTRY_TAG_NAME = "Entry";
    private static final String KEY_TAG_NAME = "Key";
    private static final String VALUE_TAG_NAME = "Value";
}
