/*******************************************************************************
 * org.havensoft.tools.xml.XMLMappable.java
 * 
 * Created on Jun 13, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;


/**
 * This convenience interface incorporates the <code>XMLLoadable</code> and
 * <code>XMLExportable</code> interfaces.
 * 
 * @author tedmunds
 */
public interface XMLMappable extends XMLLoadable, XMLExportable
{
    // This interface exists only to combine the XMLLoadable and XMLExportable
    // interfaces
}
