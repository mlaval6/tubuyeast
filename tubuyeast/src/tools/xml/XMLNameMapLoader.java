/*******************************************************************************
 * XMLNameMapLoader.java
 * <p>
 * Created on 05-Oct-2005
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
 * This is a helper class for loading a lookup map of (a particular type) of
 * <code>XMLLoadable</code>s.
 * 
 * @author tedmunds
 * @param <V>
 *        the type of the contents of the lookup map
 */
public class XMLNameMapLoader<V extends XMLLoadable> implements XMLLoadable
{
    private Map<String, V> contents;

    /**
     * Constructs an <code>XMLNameMapLoader</code> with no contents, ready to
     * be loaded from an XML node.
     */
    public XMLNameMapLoader()
    {
        super();

        contents = new HashMap<String, V>();
    }

    /**
     * Constructs an <code>XMLNameMapLoader</code> that will be ready to load
     * from an XML node into <code>contents</code>.
     * 
     * @param contents
     *        the map that will be loaded into when loading occurs
     */
    public XMLNameMapLoader(Map<String, V> contents)
    {
        super();
        this.contents = contents;
    }

    /**
     * Gets the contents of this loader.
     * 
     * @return the map loaded by this loader (or an empty map if no loading has
     *         yet occurred).
     */
    public Map<String, V> getContents()
    {
        return contents;
    }

    public void loadFromXML(Node dataNode)
        throws XMLException,
            InitializationException
    {
        List<Element> entryElements = XMLHelper
            .getChildElementListByTagName(dataNode,
                                          XMLHelper.MAP_ENTRY_TAG_NAME);
        for (Iterator<Element> entryIter = entryElements.iterator(); entryIter
            .hasNext();)
        {
            Element entryElement = entryIter.next();
            String key = XMLHelper
                .getStringAttribute(entryElement,
                                    XMLHelper.MAP_KEY_ATTRIBUTE_NAME,
                                    getClass());
            Element valueElement = XMLHelper
                .getRequiredElement(entryElement,
                                    XMLHelper.MAP_VALUE_TAG_NAME,
                                    getClass());
            V value = XMLHelper.loadSelfDescribingObject(valueElement,
                                                         getClass());
            contents.put(key, value);
        }
    }
}
