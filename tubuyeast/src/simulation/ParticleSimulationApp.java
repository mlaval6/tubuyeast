package simulation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import tools.computations.FPSTimer;
import tools.gl.Interactor;
import tools.gl.OpenglViewer;
import tools.gl.SceneGraphNode;
import tools.parameters.BooleanParameter;
import tools.parameters.DoubleParameter;
import tools.parameters.IntParameter;
import tools.swing.VerticalFlowPanel;


/**
 * Sample code for particle simulation.
 * @author epiuze
 */
public class ParticleSimulationApp implements SceneGraphNode, Interactor  {

    private OpenglViewer ev;
    
    private ParticleSystem system;
    
    /** 
     * boolean to signal that the system was stepped and that a 
     * frame should be recorded if recording is enabled
     */
    private boolean stepRequested = false;
        
    private BooleanParameter run = new BooleanParameter( "run", false );
    
    private DoubleParameter stepsize = new DoubleParameter( "step size", 0.01, 1e-5, 1 );
    
    private IntParameter substeps = new IntParameter( "sub steps (integer)", 10, 1, 100);
    
    private static Dimension winsize = new Dimension(800, 600);

    private ParticleSimulationInteractor interactor;

    private FPSTimer fpsTimer = new FPSTimer();

    /**
     * Entry point for application
     * @param args
     */
    public static void main(String[] args) {
        new ParticleSimulationApp();        
    }
        
    /**
     * Creates the application / scene instance
     */
    public ParticleSimulationApp() {
        system = new ParticleSystem(winsize);
        createSystem(system, 1);

        // Add an interactor to manage mouse and keyboard controls
        interactor = new ParticleSimulationInteractor(system);
        
        ev = new OpenglViewer("Tubuyeast", this, new Dimension(winsize), new Dimension(650, winsize.height + 90) );

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
                system.step( stepsize.getValue());                
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
                    createSystem(system, si);
                }
            });
        }
        
        vfp.add(cpanel);
        
        vfp.add( run.getControls() );        
        vfp.add( stepsize.getSliderControls(true) );
        vfp.add( substeps.getSliderControls() );
        vfp.add( system.getControls() );
        
        vfp.add(interactor.getControls());
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
    
	/**
	 * Creates test systems.
	 * @param system
	 * @param which
	 */
	private static void createSystem(ParticleSystem system, int which) {
		List<Particle> particles = system.getParticles();
		List<Spring> springs = system.getSprings();
		double k = system.getK();
		double b = system.getB();
		
		Particle p1, p2, p3, p4;
		int N;
			
		switch (which) {
		case 1: // structural beam
			Point2d p = new Point2d(100, 100);
			Vector2d d = new Vector2d(20, 0);
			p1 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
			particles.add(p1);
			p2 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
			particles.add(p2);
			springs.add(new Spring(p1, p2, k, b));
			p1.pinned = true;
			p2.pinned = true;
			p.add(d);
			p.add(d);
			N = 10;
			for (int i = 1; i < N; i++) {
				p3 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
				p4 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
				particles.add(p3);
				particles.add(p4);
				springs.add(new Spring(p3, p1, k, b));
				springs.add(new Spring(p3, p2, k, b));
				springs.add(new Spring(p4, p1, k, b));
				springs.add(new Spring(p4, p2, k, b));
				springs.add(new Spring(p4, p3, k, b));
				p1 = p3;
				p2 = p4;

				p.add(d);
				p.add(d);
			}
			break;
			case 2: // curved structural beam
				p = new Point2d(100, 100);
				d = new Vector2d(20, 0);
				p1 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
				particles.add(p1);
				p2 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
				particles.add(p2);
				springs.add(new Spring(p1, p2, k, b));
				p1.pinned = true;
				p2.pinned = true;
				p.add(d);
				p.add(d);
				N = 10;
				for (int i = 1; i < N; i++) {
					 d.set( 20*Math.cos(i*Math.PI/N), 20*Math.sin(i*Math.PI/N) );
					p3 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
					p4 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
					particles.add(p3);
					particles.add(p4);
					springs.add(new Spring(p3, p1, k, b));
					springs.add(new Spring(p3, p2, k, b));
					springs.add(new Spring(p4, p1, k, b));
					springs.add(new Spring(p4, p2, k, b));
					springs.add(new Spring(p4, p3, k, b));
					p1 = p3;
					p2 = p4;

					p.add(d);
					p.add(d);
				}
				break;
			case 3: // chain
				int ypos = 100;
				p1 = new Particle(320, ypos, 0, 0);
				p1.pinned = true;
				particles.add(p1);
				N = 10;
				for (int i = 0; i < N; i++) {
					ypos += 20;
					p2 = new Particle(320, ypos, 0, 0);
					particles.add(p2);
					springs.add(new Spring(p1, p2, k, b));
					p1 = p2;
				}
				break;
			case 4: // pendulum
				double springl = 100;
				double x1 = (int) (winsize.width / 2.0);
				double y1 = (int) (winsize.height / 2.0);

				double x2 = (int) (winsize.width / 2.0) + springl + 0.01;
				double y2 = (int) (winsize.height / 2.0);

				Particle wall;
				wall = new Particle(x1, y1, 0, 0);
				wall.pinned = true;

				p1 = new Particle(x2, y2, 0, 0);

				Spring spring = new Spring(wall, p1, k, b);
				spring.l0 = springl;
				spring.setK(100);
				spring.setB(10);

				particles.add(wall);
				particles.add(p1);
				springs.add(spring);
				break;
			case 5: // ball
				int x0 = (int) (winsize.width / 2.0);
				int y0 = (int) (winsize.height / 2.0);
				double r = 100;
				List<Particle> bps = new LinkedList<Particle>();

				Particle pi, pn, po;
				pi = new Particle(x0 + r * Math.cos(0), y0 + r * Math.sin(0), 0, 0);
				bps.add(pi);
				po = pi;

				double dt = 2 * Math.PI / 8;
				for (double angle = dt; angle < 2 * Math.PI; angle += dt) {

					pn = new Particle(x0 + r * Math.cos(angle), y0 + r
							* Math.sin(angle), 0, 0);

					// springs.add( new Spring(po, pn));
					
					if (angle >= Math.PI + dt) {
						pn.heavy = true;
					}

					bps.add(pn);

					po = pn;
				}
				springs.add(new Spring(pi, po, k, b));

				for (Particle p1l : bps) {
					for (Particle p2l : bps) {
						if (p1l != p2l)
							springs.add(new Spring(p1l, p2l, k, b));
					}
				}

				particles.addAll(bps);

				break;
		}

		system.updateSystem();
	}
    
    public void setRunning(boolean b) {
    	run.setValue(b);
    	interactor.setCreationEnabled(!b);
    }
    
    public boolean isRunning() {
    	return run.getValue();
    }

	@Override
	public String getName() {
		return "Particle Simulation";
	}
    
}
