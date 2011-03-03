package simulation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import numerical.ImplicitEuler;
import tools.computations.FPSTimer;
import tools.gl.Interactor;
import tools.gl.OpenglViewer;
import tools.gl.SceneGraphNode;
import tools.parameters.BooleanParameter;
import tools.parameters.DoubleParameter;
import tools.swing.VerticalFlowPanel;


/**
 * Sample code for particle simulation.
 * @author epiuze
 */
public class ParticleSimulation implements SceneGraphNode, Interactor  {

    private OpenglViewer ev;
    
    private ParticleSystem system;
    
    /** 
     * boolean to signal that the system was stepped and that a 
     * frame should be recorded if recording is enabled
     */
    private boolean stepRequested = false;
        
    private BooleanParameter run = new BooleanParameter( "run", false );
    
    private DoubleParameter stepsize = new DoubleParameter( "step size", 0.05, 1e-5, 1 );
    
    private DoubleParameter substeps = new DoubleParameter( "sub steps (integer)", 1, 1, 100);
    
    private ImplicitEuler backwardEuler = new ImplicitEuler();

    private Dimension winsize = new Dimension(800, 600);

    private ParticleSimulationInteractor interactor;

    private FPSTimer fpsTimer = new FPSTimer();

    /**
     * Entry point for application
     * @param args
     */
    public static void main(String[] args) {
        new ParticleSimulation();        
    }
        
    /**
     * Creates the application / scene instance
     */
    public ParticleSimulation() {
        system = new ParticleSystem(winsize, backwardEuler);
        system.createSystem(1);
        
        ev = new OpenglViewer( "Spring Madness", this, new Dimension(winsize), new Dimension(600, winsize.height + 90) );

        // Add an interactor to manage mouse and keyboard controls
        interactor = new ParticleSimulationInteractor(system);
        ev.addInteractor(interactor);
        ev.addInteractor(this);
        
        ev.start();
    }
     
    /**
     * Set some initial gl parameters.
     */
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL.GL_POINT_SMOOTH );
        system.init(drawable);
    }
        
    public void display(GLAutoDrawable drawable) {
    	// Begin 2D drawing
        OpenglViewer.beginOverlay(drawable);

    	// Advance the simulation
    	simulationStep();
        
        // Set the GL context
        GL gl = drawable.getGL();
        gl.glDisable(GL.GL_LIGHTING);

        // Display particle system
        system.display( drawable );

        // Display simulation info
        displaySimulationInfo(drawable);
        
        interactor.display(drawable);
        
        // Done with 2D drawing
        OpenglViewer.endOverlay(drawable);    
    }

    /**
     * Display simulation info as an overlay in the OpenGL window.
     * @param drawable
     */
    private void displaySimulationInfo(GLAutoDrawable drawable) {
        fpsTimer.tick();
        String text = system.toString() + "\n" + 
                      "h = " + stepsize.getValue() + "\n" +
                      "substeps = " + (int) substeps.getValue() + "\n" + fpsTimer.toString();        
        OpenglViewer.printTextLines( drawable, text );
	}

	private void simulationStep() {
        // Advance the simulation by a number of substeps
    	// if it is running or wants to be stepped
        if ( isRunning() || stepRequested ) {   
            for ( int i = 0; i < substeps.getValue(); i++ ) {
                system.step( stepsize.getValue() / (int)substeps.getValue() );                
            }
            stepRequested = false;        
        }
	}

	public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();

        vfp.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Simulation"));

        JPanel cpanel = new JPanel(new GridLayout(2, 3));
        cpanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Test systems"));
        
        for (int i = 1; i <= 5; i++) {
        	final int si = i;
            JButton cb = new JButton("test " + si);
            cpanel.add( cb );
            cb.addActionListener( new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    system.createSystem(si);
                }
            });
        }
        
        vfp.add(cpanel);
        
        vfp.add( run.getControls() );        
        vfp.add( stepsize.getSliderControls(true) );
        vfp.add( substeps.getSliderControls(false) );
        vfp.add( system.getControls() );
        return vfp.getPanel();
    }
    
    public void attach(Component component) {
        component.addKeyListener( new KeyAdapter() {
            
            public void keyPressed(KeyEvent e) {

                if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
                	setRunning(!isRunning());

                    // Initialize the particle system
                    system.updateSystem();

                } else if ( e.getKeyCode() == KeyEvent.VK_S ) {                    
                    stepRequested = true;
                } else if ( e.getKeyCode() == KeyEvent.VK_R ) {
                	setRunning(!isRunning());
                	
                    system.resetParticles();                  
                } else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                    // quit the program
                    ev.stop();
                }
                if ( e.getKeyCode() != KeyEvent.VK_ESCAPE ) ev.redisplay();
            }
        } );
    }
    
    public void setRunning(boolean b) {
    	run.setValue(b);
    	interactor.setModifiable(b);
    }
    
    public boolean isRunning() {
    	return run.getValue();
    }

	@Override
	public String getName() {
		return "Particle Simulation";
	}
    
}
