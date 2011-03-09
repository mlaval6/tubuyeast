/*******************************************************************************
 * XMLConfiguredMappable.java
 * <p>
 * Created on 23-May-2005
 * <p>
 * Created by tedmunds
 ******************************************************************************/
package tools.xml;


/**
 * This interface combines the
 * <code>XMLConfiguredLoadable&lt;Configuration&gt;</code> and
 * <code>XMLExportable</code> interfaces.
 * <code>XMLConfiguredMappable&lt;Configuration&gt;</code> :
 * <code>XMLMappable</code> ::
 * <code>XMLConfiguredLoadable&lt;Configuration&gt;</code> :
 * <code>XMLLoadable</code>.
 * 
 * @param <Configuration>
 *        the configuration data required to load an instance
 * @author tedmunds
 */
public interface XMLConfiguredMappable<Configuration>
    extends
        XMLConfiguredLoadable<Configuration>,
        XMLExportable
{
    // No further interface
}
