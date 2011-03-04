package tools.gl;

import javax.media.opengl.GLAutoDrawable;
import javax.swing.JPanel;

/**
 * A simple scene graph node 
 * @author epiuze (adapted from kry)
 */
public interface SceneGraphNode {
    
    /**
     * Gives the scene a chance to perform initialization.  
     * @param drawable the drawable to use for initialization
     */
    public void init( GLAutoDrawable drawable );

    /**
     * Displays the contents of this scene graph node and its children.
     * @param drawable
     */
    public void display( GLAutoDrawable drawable );

    public String getName();
    
    /**
     * Gets a control panel associated with this node and its children.
     * @return a control panel
     */
    public JPanel getControls();

}