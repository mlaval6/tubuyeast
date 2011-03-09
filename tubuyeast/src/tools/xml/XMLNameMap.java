/*******************************************************************************
 * XMLStringMap.java
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
 * @author tedmunds
 * @param <V> the type of <code>XMLMappable</code> mapped
 * @deprecated in favour of <code>XMLNameMapExporter</code> and
 *             <code>XMLNameMapLoader</code> or
 *             <code>XMLNameMapConfiguredLoader</code>.
 */
@Deprecated
public class XMLNameMap<V extends XMLMappable> implements XMLMappable
{
    private Map<String, V> contents;

    /**
     * Creates an empty map.
     */
    public XMLNameMap()
    {
        super();

        contents = new HashMap<String, V>();
    }

    /**
     * Creates a map with the provided contents.
     * 
     * @param contents
     *        the underlying mapping.
     */
    public XMLNameMap(Map<String, V> contents)
    {
        super();
        this.contents = contents;
    }

    /**
     * Gets the underlying mapping.
     * 
     * @return the underlying mapping.
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

    public void exportAsXML(Node exportNode) throws XMLException
    {
        XMLHelper
            .setAsSelfDescribing(XMLHelper.verifyNodeAsElement(exportNode,
                                                               getClass()),
                                 getClass());

        for (Iterator<String> keyIter = contents.keySet().iterator(); keyIter
            .hasNext();)
        {
            String key = keyIter.next();
            V value = contents.get(key);
            Element entryElement = XMLHelper
                .createChildElement(exportNode, XMLHelper.MAP_ENTRY_TAG_NAME);
            XMLHelper.exportAttribute(entryElement,
                                      XMLHelper.MAP_KEY_ATTRIBUTE_NAME,
                                      key);
            XMLHelper.exportElement(entryElement,
                                    XMLHelper.MAP_VALUE_TAG_NAME,
                                    value);
        }
    }

}
