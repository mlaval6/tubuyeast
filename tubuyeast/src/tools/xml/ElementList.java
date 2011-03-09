/*******************************************************************************
 * org.havensoft.tools.xml.ElementList.java
 * 
 * Created on Jun 12, 2003
 * Created by tedmunds
 * 
 ******************************************************************************/
package tools.xml;

import java.util.List;

import org.w3c.dom.Element;

/**
 * The <code>ElementList</code> interface provides the abstraction of an ordered
 * collection of <code>Element</code>s, without defining or constraining how
 * this collection is implemented.
 * <p>
 * The items in the <code>ElementList</code> are accessible via an integral
 * index, starting from 0.
 * 
 * @author tedmunds
 */
public interface ElementList extends List<Element>
{
    /**
     * Returns the <code>Element</code> at the specified position in this list.
     *
     * @param index index of the <code>Element</code> to return
     * @return the <code>Element</code> at the specified position in this list
     * 
     * @throws IndexOutOfBoundsException if the index is out of range (index
     *        &lt; 0 || index &gt;= size())
     */
    public Element getElement(int index);

}
