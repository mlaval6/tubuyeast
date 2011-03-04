package tools.swing;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.media.opengl.GLAutoDrawable;

/**
 * Wrapper class around mouse and keyboard listeners. Useful for
 * keeping a list of event listeners and giving the handle
 * to only one of them at a time.
 * @author piuze
 */
public abstract class ExtendedAdapter implements MouseListener, MouseMotionListener,
        KeyListener {

    protected AdapterState state = new AdapterState();
    
    protected boolean active = false;
    
    public void setActive(boolean v) {
        active = v;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public abstract String getHandleString();
    
    public abstract void setAdapterState(AdapterState state);
    
    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {}

    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.  Mouse drag events will continue to be delivered to
     * the component where the first originated until the mouse button is
     * released (regardless of whether the mouse position is within the
     * bounds of the component).
     */
    public void mouseDragged(MouseEvent e) {
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons no down).
     */
    public void mouseMoved(MouseEvent e) {
    }
    
    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of 
     * a key typed event.
     */
    public void keyTyped(KeyEvent e) {
        
    }

    /**
     * Invoked when a key has been pressed. 
     * See the class description for {@link KeyEvent} for a definition of 
     * a key pressed event.
     */
    public void keyPressed(KeyEvent e) {
   }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of 
     * a key released event.
     */
    public void keyReleased(KeyEvent e) {
    }

    /**
     * @return A short and (if possible) unique
     * description for this mouse adapter such that
     * it can be used in a UI list.
     */
    public abstract String toString();

    public void resetAdapterState() {
        state.reset();
    }

    public boolean requestPicking() {
        return false;
    }
    
    public void addComponent(Component c) {
        c.addMouseListener(state);
        c.addMouseMotionListener(state);
        c.addKeyListener(state);
    }

    public void doPicking(GLAutoDrawable drawable) {
        
    }
    
}
