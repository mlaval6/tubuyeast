/*
 * Created on Jan 19, 2005
 */
package tools.gl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * A stop watch tree for nested stop watches.
 * 
 * @author Shinjiro Sueda
 */
public class StopWatchTree
{
    private TreeElem root;
    private List<TreeElem> elems;
    private String[] str;
    
    /**
     * Creates a new tree of StopWatches
     */
    public StopWatchTree()
    {
        root = new TreeElem("root", 0);
        elems = new LinkedList<TreeElem>();
    }
    
    /**
     * Adds a new node to the tree
     * @param name
     * @param parentName
     * @return the stopwatch
     */
    public StopWatch addStopWatch(String name, String parentName)
    {
        // Add to the tree.
        TreeElem parent = root.find(parentName);
        if(parent == null)
        {
            // Add it to the root if parent not found.
            parent = root;
        }
        parent.addChild(name);
        
        // Recreate the list.
        elems.clear();
        root.addToList(elems);
        str = new String[elems.size() - 1];
        
        return parent.find(name).getStopWatch();
    }
    
    /**
     * Gets a stopwatch with a given name
     * @param name
     * @return the stopwatch
     */
    public StopWatch getStopWatch(String name)
    {
        TreeElem elem = root.find(name);
        if(elem != null)
        {
            return elem.getStopWatch();
        }
        return null;
    }
    
    /** 
     * @return all stopwatches converted to strings
     */
    public String[] getStopWatchData()
    {
        Iterator<TreeElem> iter = elems.iterator();
        if(iter.hasNext())
        {
            iter.next(); // skip root
        }
        int i = 0;
        while(iter.hasNext())
        {
            str[i++] = iter.next().getStopWatch().toString();
        }
        return str;
    }
        
    @Override
    public String toString() {
        String[] strs = getStopWatchData();
        String out = "";
        for ( String s : strs ) {
            out = out + s + "\n";
        }
        return out;
    }
    
    private class TreeElem
    {
        private StopWatch me;
        private List<TreeElem> children;
        private int depth;
        
        /**
         * Creates a new tree element
         * @param name
         * @param depth
         */
        public TreeElem(String name, int depth)
        {
            this.depth = depth;
            for(int i = 1; i < depth; ++i)
            {
                name = "| " + name;
            }
            me = new StopWatch(name);
            children = new LinkedList<TreeElem>();
        }
        
        /**
         * Adds a child
         * @param name
         */
        public void addChild(String name)
        {
            TreeElem child = new TreeElem(name, depth + 1);
            children.add(child);
        }
        
        /**
         * Adds the children to the provided list
         * @param list
         */
        public void addToList(List<TreeElem> list)
        {
            list.add(this);
            for(TreeElem elem : children)
            {
                elem.addToList(list);
            }
        }
        
        /**
         * @return the stopwatch at this level
         */
        public StopWatch getStopWatch()
        {
            return me;
        }
        
        /**
         * Finds a node with the given name
         * @param name
         * @return the tree element
         */
        public TreeElem find(String name)
        {
            String myName = me.getName().replace('|', ' ').trim();
            String yourName = name.trim();
            if(myName.equals(yourName))
            {
                return this;
            }
            
            if(children.size() == 0)
            {
                return null;
            }
            
            for(TreeElem child : children)
            {
                TreeElem elem = child.find(name);
                if(elem != null)
                {
                    return elem;
                }
            }
            
            return null;
        }
        
        @Override
        public String toString()
        {
            return me.getName();
        }
    }
    
    /**
     * Tests the stopwatch tree
     * @param args
     */
    public static void main(String[] args) {
        
        StopWatchTree swt = new StopWatchTree();
        
        StopWatch swf1 = null;
        StopWatch swf11 = null;
        StopWatch swf12 = null;
        StopWatch swf2 = null;
        
        for ( int i = 0; i < 50; i++ ) {
            if ( swf1 == null) swf1 = swt.addStopWatch( "f1", "root" );
            swf1.start();
            // f1
            {
                try { Thread.sleep( 100 ); } catch ( Exception e ) { /**/ }
                
                // f11
                if ( swf11 == null) swf11 = swt.addStopWatch( "f11", "f1" );
                swf11.start();
                try { Thread.sleep( 100 ); } catch ( Exception e ) { /**/ }
                swf11.stop();
                
                // f12
                if ( swf12 == null) swf12 = swt.addStopWatch( "f12", "f1" );
                swf12.start();
                try { Thread.sleep( 100 ); } catch ( Exception e ) { /**/ }
                swf12.stop();            
            }
            swf1.stop();
            
            
            if ( swf2 == null ) swf2 = swt.addStopWatch( "f2", "root" );
            swf2.start();
            // f2
            {
                try { Thread.sleep( 100 ); } catch ( Exception e ) { /**/ }
            }
            swf2.stop();
            
            System.out.println( swt.toString() + "\n\n");
        }
        
        
    }
}
