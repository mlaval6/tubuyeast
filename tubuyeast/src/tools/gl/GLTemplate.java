package tools.gl;

import java.awt.Component;
import java.awt.Dimension;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JPanel;

import tools.parameters.BooleanParameter;
import tools.swing.VerticalFlowPanel;

import com.sun.opengl.util.GLUT;

public class GLTemplate implements SceneGraphNode, Interactor {

    public static void main(String[] args) {
        new GLTemplate();
    }

    /**
     * Display the world axis.
     */
    private BooleanParameter displayWorld = new BooleanParameter(
            "display world", true);


    private OpenglViewer ev;
    
    public GLTemplate() {
        
        Dimension winsize = new Dimension(800, 800);
        ev = new OpenglViewer("",
                this, new Dimension(winsize), new Dimension(650,
                        winsize.height + 90), true);
        ev.getCamera().zoom(300);
        ev.addInteractor(this);
        ev.start();
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glClearColor(1, 1, 1, 1);

        if (displayWorld.getValue())
            WorldAxis.display(gl);
        
        
        OpenglViewer.beginOverlay(drawable);
        OpenglViewer.printTextLines(drawable, "Sample Text", 10, 20, new float[] {0, 0, 0},
                GLUT.BITMAP_8_BY_13);
        OpenglViewer.endOverlay(drawable);
    }


    @Override
    public JPanel getControls() {

        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.add(displayWorld.getControls());

        return vfp.getPanel();
    }


    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void init(GLAutoDrawable drawable) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public void attach(Component component) {
		// TODO Auto-generated method stub
		
	}
}
