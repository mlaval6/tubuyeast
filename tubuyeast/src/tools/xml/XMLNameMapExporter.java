/*******************************************************************************
 * XMLNameMapExporter.java
 * <p>
 * Created on 05-Oct-2005
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * This is a helper class for exporting a lookup map of (a particular type) of
 * <code>XMLExportable</code>s.
 * 
 * @author tedmunds
 * @param <V>
 *        the type of the contents of the lookup map
 */
public class XMLNameMapExporter<V extends XMLExportable>
    implements
        XMLExportable
{
    private Map<String, V> contents;

    /**
     * Constructs an <code>XMLNameMapExporter</code> that will export the
     * specified map.
     * 
     * @param contents
     *        the map that will be exported by this exporter
     */
    public XMLNameMapExporter(Map<String, V> contents)
    {
        super();
        this.contents = contents;
    }

    public void exportAsXML(Node exportNode) throws XMLException
    {
        // Note that we don't set ourselves as self-describing (because we are
        // not loadable - anything that wants to load this output will have to
        // use an XMLNameMapLoader or XMLNameMapConfiguredLoader as
        // appropriate).

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
