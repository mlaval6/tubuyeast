package simulation;

import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector2d;

import tools.computations.Contour;
import tools.gl.Interactor;
import tools.parameters.IntParameter;
import tools.swing.VerticalFlowPanel;

public class ParticleSimulationInteractor implements Interactor {

	/**
	 * Use soft pulling during simulation and hard pulling otherwise.
	 */
	private boolean softPulling = false;
	
    private double maxDist = 150;
    private double minDist = 50;
    private double grabThresh = 10;
    private int xdown = 0;
    private int ydown = 0;
    private int xcurrent = 0;
    private int ycurrent = 0;
    private boolean mouseDown = false;
    private boolean shiftDown = false;
    private boolean controlDown = false;
    private boolean altDown = false;
    private boolean mouseInWindow = false;
    private boolean grabbed = false;

    private Particle p1 = null;
    private Particle p2 = null;
    private double d1 = 0;
    private double d2 = 0;
    
    private Contour contour;
    
    private IntParameter pointsres = new IntParameter("draw res", 30,
            1, 1000);


    private ParticleSystem system;
    public ParticleSimulationInteractor(ParticleSystem system) {
    	this.system = system;
    	
    	contour = new Contour();
    }
    
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();

        vfp.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Simulation"));

        vfp.add(contour.getControls());
        vfp.add(pointsres.getSliderControls());
        
        return vfp.getPanel();
    }
    
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
    
    private Point2d dummyPt = new Point2d();

    public void attach(Component component) {
        component.addMouseMotionListener( new MouseMotionListener() {
            
            public void mouseDragged(MouseEvent e) {
                Point2d downPt = new Point2d(xdown, ydown);
                
                xcurrent = e.getPoint().x;
                ycurrent = e.getPoint().y;
                dummyPt.set(xcurrent, ycurrent);
                
                if ( grabbed ) {
                    if (! softPulling) {
                        p1.p.set(xcurrent, ycurrent);
                        p1.v.set(0, 0); 

                        p1.p0.set( p1.p );
                        p1.v0.set( p1.v );
                        for ( Spring s : p1.springs ) {
                            s.computeRestLength();
                        }
                    }
                    
                    system.grab(p1, dummyPt);

                } else {
                	if (shiftDown && altDown) {
                		Point3d lpt = contour.getLastPoint();
                		if (lpt == null) {
                        	contour.addPoint(new Point3d(xcurrent, ycurrent, 0));
                		}
                		else {
                    		Point2d lastpt = new Point2d(lpt.x, lpt.y);
                            if (dummyPt.distance(lastpt) >= pointsres.getValue()) {
                            	contour.addPoint(new Point3d(xcurrent, ycurrent, 0));
                            }
                		}
                	}
                	else {
                        findCloseParticles(xcurrent, ycurrent);
                	}
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
                altDown = e.isAltDown();
                
                boolean leftClick = (e.getButton() == MouseEvent.BUTTON1);
                
                if (leftClick) {
                	findCloseParticles(xcurrent, ycurrent);
                	if ( p1 != null && d1 < grabThresh ) {
                		grabbed = true;
                		
                		dummyPt.set(xdown, ydown);
                		system.grab(p1, dummyPt);
                	}
                }
                else {
                	createContour();
                }
            }
            
            public void mouseReleased(MouseEvent e) {
                boolean leftClick = (e.getButton() == MouseEvent.BUTTON1);

                if (!leftClick || (shiftDown && altDown)) {
                	// Creating contour, do nothing
                	return;
                }
                else if (! grabbed && ! softPulling) {
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
                    p.pinned = shiftDown;

                    if (e.isShiftDown())
                    	p.pinned = true;
                    
                    // Make the particle heavy if control is pressed
                    if (e.isControlDown()) {
                    	p.mass = Particle.MassHeavy;
                        p.heavy = controlDown;
                    }
                    
                    if (e.isAltDown()) {
                    	p.pinned = false;
                    	p.mass = Particle.MassNormal;
                        p.heavy = false;
                    }
                } else {
                    // Keeps the particle pinned if shift is down on mouse release
                    if ( p1 != null ) {
                        p1.pinned = e.isShiftDown();
                        if (p1.pinned) p1.v.set(0, 0);

                        p1.mass = e.isControlDown() ? Particle.MassHeavy : Particle.MassNormal;
                        p1.heavy = e.isControlDown();
                    }

                    system.ungrab();
                }
                grabbed = false;
                
                mouseDown = false;
                controlDown = false;
                shiftDown = false;
                altDown = false;
            }
        } );
        component.addKeyListener( new KeyAdapter() {
            
            public void keyPressed(KeyEvent e) {
                controlDown = e.isControlDown();
                shiftDown = e.isShiftDown();

                if ( e.getKeyCode() == KeyEvent.VK_C ) {                   
                    system.clearParticles();
                    p1 = null;
                    p2 = null;
                }
            }
        } );
    }
    
    private void createContour() {
        if (contour.getSpline().size() > 0) {
            // Generate contour particles
            // skip the first and last ones to avoid overlap
            for (Point3d pt : contour.getSpline()) {
                system.createParticle(pt.x, pt.y, 0, 0);
            }
            contour.clear();
        }
    }

    public void display(GLAutoDrawable drawable) {
    	GL gl = drawable.getGL();
    	
    	contour.display(drawable);
    	
        // Display some extra stuff for the interface        
        if ( mouseDown ) {
        	if (shiftDown && altDown) {
        		// Do nothing, we are creating a contour
        	}
        	else if ( ! grabbed) {
                if (!softPulling) {
                    gl.glPointSize(5f);
                    gl.glLineWidth(2f);                                
                    if (! softPulling) {
                        drawLineToParticle( drawable, xcurrent, ycurrent, p1, d1, minDist, maxDist);
                        drawLineToParticle( drawable, xcurrent, ycurrent, p2, d2, minDist, maxDist);
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
    }

    public void setModifiable(boolean b) {
    	softPulling = b;
    }
    
    
    /**
     * draws a line from the given point to the given particle
     * @param drawable
     * @param x
     * @param y
     * @param p
     * @param d
     */
    public static void drawLineToParticle( GLAutoDrawable drawable, double x, double y, Particle p, double d, double minDist, double maxDist) {
        if ( p == null ) return;
//        if ( d > maxDist ) return;        
        GL gl = drawable.getGL();        
        double col = d < minDist ? 1 : (maxDist-d) / (maxDist-minDist);
        gl.glColor4d( 1-col,0,col,0.75f);
        gl.glBegin(GL.GL_LINES);
        gl.glVertex2d( x, y );
        gl.glVertex2d( p.p.x, p.p.y );
        gl.glEnd();    
    }
    
	/**
	 * Apply a soft pulling force on a particle.
	 * @param p the particle to pull
	 * @param pt0 the initial position of grab
	 * @param pt the pulling location
	 */
	public static void softPull(Particle p, Point2d pt0, Point2d pt) {
		if (p != null) {
			// Find the strongest spring attached to this particle
			double ks = 0;
			double kd = 0;
			for (Spring s : p.springs) {
				if (s.getK() > ks) ks = s.getK();
				if (s.getB() > kd) kd = s.getB();
			}
			
			// Make pulling force stronger than max stiffness found
//			ks *= 0.5;
			ks = 1;
			
			// Don't use damping. Remove this to use max damping found
			kd = 0;
			
			Vector2d ddx = new Vector2d();
			ddx.sub(pt, pt0);
//			ddx.set(pt.x - pt0.x, pt.y - pt0.y);
			ddx.scale(0.5);

			Vector2d force = new Vector2d();

			// Spring
			force.sub(pt, p.p);
			double l = force.length();
			force.normalize();
			force.scale((l) * ks);
			p.addForce(force);
			force.scale(-1);

			// Damping
			force.sub(pt, p.p);
			force.normalize();
			Vector2d v = new Vector2d();
			v.sub(ddx, p.v);
			double rv = force.dot(v);
			force.scale(kd * rv);
			p.addForce(force);
		}
	}
}
