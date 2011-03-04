package tools.swing;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

/**
 * Class
 * @author piuze
 */
public class AdapterState implements MouseListener,
        MouseMotionListener, KeyListener {

    private boolean shiftDown = false;

    private boolean altDown = false;
    
    private boolean controlDown = false;
    
    private boolean mouseLeftPressed = false;

    private boolean mouseRightPressed = false;
    
    private boolean mouseDragged = false;
    
    private int mouseX = 0;
    
    private int mouseY = 0;

    private InputEvent event;
    
    public void reset() {
        event = null;
        shiftDown = false;
        altDown = false;
        controlDown = false;
        mouseLeftPressed = false;
        mouseRightPressed = false;
        mouseDragged = false;
        mouseX = 0;
        mouseY = 0;
    }

    /**
     * Invoked when the mouse has been clicked on a component.
     */
    public void mouseClicked(MouseEvent e) {
        event = e;
    }

    /**
     * Invoked when a mouse button has been pressed on a component.
     */
    public void mousePressed(MouseEvent e) {
        event = e;

        mouseLeftPressed = e.getButton() == MouseEvent.BUTTON1;
        mouseRightPressed = e.getButton() == MouseEvent.BUTTON3;
    }

    /**
     * Invoked when a mouse button has been released on a component.
     */
    public void mouseReleased(MouseEvent e) {
        event = e;

        mouseLeftPressed = false;
        mouseRightPressed = false;
        
        mouseDragged = false;
    }

    /**
     * Invoked when the mouse enters a component.
     */
    public void mouseEntered(MouseEvent e) {
        event = e;
    }

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e) {
        event = e;
    }

    /**
     * Invoked when a mouse button is pressed on a component and then 
     * dragged.  Mouse drag events will continue to be delivered to
     * the component where the first originated until the mouse button is
     * released (regardless of whether the mouse position is within the
     * bounds of the component).
     */
    public void mouseDragged(MouseEvent e) {
        event = e;

        mouseX = e.getX();
        mouseY = e.getY();
        
        mouseDragged = true;
    }

    /**
     * Invoked when the mouse button has been moved on a component
     * (with no buttons no down).
     */
    public void mouseMoved(MouseEvent e) {
        event = e;

        mouseX = e.getX();
        mouseY = e.getY();
    }
    
    /**
     * Invoked when a key has been typed.
     * See the class description for {@link KeyEvent} for a definition of 
     * a key typed event.
     */
    public void keyTyped(KeyEvent e) {
        event = e;
 
    }

    /**
     * Invoked when a key has been pressed. 
     * See the class description for {@link KeyEvent} for a definition of 
     * a key pressed event.
     */
    public void keyPressed(KeyEvent e) {
        event = e;

        controlDown = e.isControlDown();
        shiftDown = e.isShiftDown();
        altDown = e.isAltDown();
   }

    /**
     * Invoked when a key has been released.
     * See the class description for {@link KeyEvent} for a definition of 
     * a key released event.
     */
    public void keyReleased(KeyEvent e) {
        event = e;

        controlDown = e.isControlDown();
        shiftDown = e.isShiftDown();
        altDown = e.isAltDown();
    }

    /**
     * @return whether the left mouse button is currently down.
     */
    public boolean mouseLeftPressed() {
        return mouseLeftPressed;
    }

    /**
     * @return whether the right mouse button is currently down.
     */
    public boolean mouseRightPressed() {
        return mouseRightPressed;
    }

    /**
     * @return the current mouse X coordinate.
     */
    public int getX() {
        return mouseX;
    }

    /**
     * @return the current mouse Y coordinate.
     */
    public int getY() {
        return mouseY;
    }
    
    /**
     * @return whether control is currently down.
     */
    public boolean controlDown() {
        return controlDown;
    }
    
    /**
     * @return whether shift is currently down.
     */
    public boolean shiftDown() {
        return shiftDown;
    }

    /**
     * @return whether alt is currently down.
     */
    public boolean altDown() {
        return altDown;
    }
}
