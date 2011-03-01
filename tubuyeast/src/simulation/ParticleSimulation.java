package simulation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;

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
public class ParticleSimulation implements SceneGraphNode, Interactor {

    private OpenglViewer ev;
    
    private ParticleSystem system;
    
    private double maxDist = 150;
    
    private double minDist = 50;

    private double grabThresh = 10;

    /** 
     * boolean to signal that the system was stepped and that a 
     * frame should be recorded if recording is enabled
     */
    private boolean stepRequest = false;
        
    /**
     * Base name of images to save
     */
    private String dumpName = "img";
    
    /**
     * Index for the frame number we're saving.
     */
    private int nextFrameNum = 0;
    
    /**
     * For formating the image file name when recording frames
     */
    private NumberFormat format = new DecimalFormat("00000");

    private BooleanParameter record = new BooleanParameter( "record each step to image file (press ENTER in canvas to toggle)", false );

    private BooleanParameter run = new BooleanParameter( "simulate", false );
    
    private DoubleParameter stepsize = new DoubleParameter( "step size", 0.05, 1e-5, 1 );
    
    private DoubleParameter substeps = new DoubleParameter( "sub steps (integer)", 1, 1, 100);
    

    /**
     * Entry point for application
     * @param args
     */
    public static void main(String[] args) {
        new ParticleSimulation();        
    }
        
    private ImplicitEuler backwardEuler = new ImplicitEuler();

    private Dimension winsize = new Dimension(800, 600);
    
    /**
     * Creates the application / scene instance
     */
    public ParticleSimulation() {
        system = new ParticleSystem(winsize, backwardEuler);
        system.createSystem(1);

        ev = new OpenglViewer( "Spring Madness", this, new Dimension(winsize), new Dimension(winsize.width, winsize.height + 90) );
        // we add ourselves as an interactor to set up mouse and keyboard controls
        ev.addInteractor(this);
    }
     
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL.GL_POINT_SMOOTH );
        system.init(drawable);
    }
        
    private FPSTimer fpsTimer = new FPSTimer();
    
    public void display(GLAutoDrawable drawable) {

        // First advance the system (if it is running or wants to be stepped)
        if ( run.getValue() || stepRequest ) {   
            for ( int i = 0; i < substeps.getValue(); i++ ) {
                system.step( stepsize.getValue() / (int)substeps.getValue() );                
            }
        }

        GL gl = drawable.getGL();

        OpenglViewer.beginOverlay(drawable);
        gl.glDisable( GL.GL_LIGHTING );
        
        system.display( drawable );

        // Display some extra stuff for the interface        
        if ( mouseDown ) {
            if ( ! grabbed ) {
                if ( ! run.getValue() ) {
                    gl.glPointSize( 5f );
                    gl.glLineWidth( 2f );                                
                    if ( ! run.getValue() ) {
                        system.drawLineToParticle( drawable, xcurrent, ycurrent, p1, d1, minDist, maxDist);
                        system.drawLineToParticle( drawable, xcurrent, ycurrent, p2, d2, minDist, maxDist);
                    }
                }
            } else {
                gl.glPointSize( 15f );
                gl.glBegin( GL.GL_POINTS );
                p1.glVertex2d(gl);
                gl.glEnd();        
            }
        } else {
            if ( mouseInWindow ) {
                findCloseParticles( xcurrent, ycurrent );
                if ( p1 != null && d1 < grabThresh ) {

                    if (p1.heavy) {
                        gl.glColor4d(0,0,1,0.95);
                    }
                    else if (p1.pinned) {
                        gl.glColor4d(1,0,0,0.95);
                    }
                    else {
                        gl.glColor4d(0,1,0,0.95);
                    }
                    
                    if (controlDown) {
                        gl.glColor4d(0,0,1,0.95);
                    }
                    else if (shiftDown) {
                        gl.glColor4d(1,0,0,0.95);
                    }

                    gl.glBegin( GL.GL_POINTS );
                    gl.glVertex2d( p1.p.x, p1.p.y );
                    gl.glEnd();        
                }
            }
        }

        // Display system info        
        
        fpsTimer.tick();
        String text = system.toString() + "\n" + 
                      "h = " + stepsize.getValue() + "\n" +
                      "substeps = " + (int) substeps.getValue() + "\n" + fpsTimer.toString();        
        OpenglViewer.printTextLines( drawable, text );
        OpenglViewer.endOverlay(drawable);    

        // If we're recording, we'll save the step to an image file.
        // we'll also clear the step request here.
        if ( run.getValue() || stepRequest ) {
            stepRequest = false;        
            if ( record.getValue() ) {
                // write the frame
                File file = new File( "stills/" + dumpName + format.format(nextFrameNum) + ".png" );                                             
                nextFrameNum++;
                file = new File(file.getAbsolutePath().trim());
                ev.snapshot(drawable, file);
            }
        }
        
        // Refresh the window size
        winsize.height = drawable.getHeight();
        winsize.width = drawable.getWidth();
        system.setWindowBounds(winsize);

    }
    
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();

        vfp.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Simulation"));

        JButton create1 = new JButton("create test system 1");
        vfp.add( create1 );
        create1.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                system.createSystem(1);
            }
        });
        
        JButton create2 = new JButton("create test system 2");
        vfp.add( create2 );
        create2.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                system.createSystem(2);
            }
        });
        
        JButton create3 = new JButton("create test system 3");
        vfp.add( create3 );
        create3.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                system.createSystem(3);
            }
        });

        JButton create4 = new JButton("create test system 4");
        vfp.add( create4 );
        create4.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                system.createSystem(4);
            }
        });

        JButton create5 = new JButton("create test system 5");
        vfp.add( create5 );
        create5.addActionListener( new ActionListener() {
            
            public void actionPerformed(ActionEvent e) {
                system.createSystem(5);
            }
        });
        
        vfp.add( record.getControls() );        
        vfp.add( run.getControls() );        
        vfp.add( stepsize.getSliderControls(true) );
        vfp.add( substeps.getSliderControls(false) );
        vfp.add( system.getControls() );
        return vfp.getPanel();
    }
    
    // Some member variables to help us keep track of close particles
    private Particle p1 = null;
    private Particle p2 = null;
    private double d1 = 0;
    private double d2 = 0;
    
    /**
     * Finds the two closest particles for showing potential spring connections
     * @param x 
     * @param y 
     */
    private void findCloseParticles( int x, int y ) {
        List<Particle> particles = system.getParticles();
        p1 = null;
        p2 = null;
        d1 = 0;
        d2 = 0;
        if ( particles.size() > 0 ) {
            for ( Particle p : particles ) {                
                double d = p.distance( x, y );
                if ( p1 == null || d < d1 ) {
                    p2 = p1; d2 = d1; p1 = p; d1 = d;
                } else if ( p2 == null || d < d2 ) {
                    p2 = p; d2 = d;
                }
            }      
        }
    }  
    
    private int xdown = 0;
    private int ydown = 0;
    private int xcurrent = 0;
    private int ycurrent = 0;
    private boolean mouseDown = false;
    private boolean shiftDown = false;
    private boolean controlDown = false;
    private boolean mouseInWindow = false;
    private boolean grabbed = false;
    
    
    public void attach(Component component) {
        component.addMouseMotionListener( new MouseMotionListener() {
            
            public void mouseDragged(MouseEvent e) {
                xcurrent = e.getPoint().x;
                ycurrent = e.getPoint().y;
                if ( grabbed ) {
                    if ( ! run.getValue() ) {
                        p1.p.set( xcurrent, ycurrent );
                        p1.v.set( 0, 0 ); 

                        p1.p0.set( p1.p );
                        p1.v0.set( p1.v );
                        for ( Spring s : p1.springs ) {
                            s.computeRestLength();
                        }
                    }
                    
                    system.updateGrab(xcurrent, ycurrent);

                } else {
                    findCloseParticles(xcurrent, ycurrent);
                }
            }
            
            public void mouseMoved(MouseEvent e) {
                xcurrent = e.getPoint().x;
                ycurrent = e.getPoint().y;
                
                controlDown = e.isControlDown();
                shiftDown = e.isShiftDown();

//                if (grabbed) system.updateGrab(xcurrent, ycurrent);
            }
        } );
        component.addMouseListener( new MouseListener() {
            
            public void mouseClicked(MouseEvent e) {
                // do nothing
            }
            
            public void mouseEntered(MouseEvent e) {
                mouseInWindow = true;
            }
            
            public void mouseExited(MouseEvent e) {
                // clear the potential spring lines we're drawing
                mouseInWindow = false;
            }
            
            public void mousePressed(MouseEvent e) {
                xdown = e.getPoint().x;
                ydown = e.getPoint().y;
                xcurrent = xdown;
                ycurrent = ydown;
                mouseDown = true;
                controlDown = e.isControlDown();
                shiftDown = e.isShiftDown();
                
                findCloseParticles(xcurrent, ycurrent);
                if ( p1 != null && d1 < grabThresh ) {
                    grabbed = true;
                    
                    system.grab(p1, xdown, ydown);
                    system.updateGrab(xcurrent, ycurrent);
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                mouseDown = false;
                controlDown = false;
                shiftDown = false;

                if ( ! grabbed && ! run.getValue() ) {
                    double x = e.getPoint().x;
                    double y = e.getPoint().y;
                    Particle p = system.createParticle( x, y, 0, 0 );
                    if ( p1 != null && d1 < maxDist ) {
                        system.createSpring( p, p1 );
                    }
                    if ( p2 != null && d2 < maxDist ) {
                        system.createSpring( p, p2 );
                    }  
                    // Keeps the particle pinned if shift is down on mouse release
                    p.pinned = e.isShiftDown();

                    // Makes the particle heavy if control is pressed
                    p.mass = e.isControlDown() ? Particle.massHeavy : Particle.massNormal;
                    p.heavy = e.isControlDown();
                    
                    System.out.println(p.p);
                } else {
                    // Keeps the particle pinned if shift is down on mouse release
                    if ( p1 != null ) {
                        p1.pinned = e.isShiftDown();
                        if (p1.pinned) p1.v.set(0, 0);

                        p1.mass = e.isControlDown() ? Particle.massHeavy : Particle.massNormal;
                        p1.heavy = e.isControlDown();
                    }

                    system.ungrab();
                }
                grabbed = false;
            }
        } );
        component.addKeyListener( new KeyAdapter() {
            
            public void keyPressed(KeyEvent e) {
                controlDown = e.isControlDown();
                shiftDown = e.isShiftDown();

                if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
                    run.setValue( ! run.getValue() ); 

                    // Initialize the particle system
                    system.updateSystem();

                } else if ( e.getKeyCode() == KeyEvent.VK_S ) {                    
                    stepRequest = true;
                } else if ( e.getKeyCode() == KeyEvent.VK_R ) {
                    run.setValue( ! run.getValue() ); 
                    system.resetParticles();                  
                } else if ( e.getKeyCode() == KeyEvent.VK_C ) {                   
                    system.clearParticles();
                    p1 = null;
                    p2 = null;
                } else if ( e.getKeyCode() == KeyEvent.VK_V ) {                   
                    // Sets all values to default
                    system.setDefaultValues();
                } else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                    // quit the program
                    ev.stop();
                } else if ( e.getKeyCode() == KeyEvent.VK_ENTER ) {
                    // toggle recording of steps to png files
                    record.setValue( ! record.getValue() );
                }
                if ( e.getKeyCode() != KeyEvent.VK_ESCAPE ) ev.redisplay();
            }
        } );
    }

	@Override
	public String getName() {
		return "Particle Simulation";
	}
    
}
