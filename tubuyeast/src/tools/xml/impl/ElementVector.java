/*******************************************************************************
 * org.havensoft.tools.xml.impl.ElementVector.java
 * 
 * Created on Jun 12, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml.impl;

import java.util.Vector;

import org.w3c.dom.Element;

import tools.xml.ElementList;

/**
 * A <code>Vector&lt;Element&gt;</code> backed implementation of the
 * <code>ElementList</code> interface.
 * 
 * @author tedmunds
 */
public class ElementVector extends Vector<Element> implements ElementList
{

    /*
     * (non-Javadoc)
     * 
     * @see org.havensoft.tools.xml.ElementList#getElement(int)
     */
    public Element getElement(int index)
    {
        return get(index);
    }

    private static final long serialVersionUID = 3618134559040942133L;

}
