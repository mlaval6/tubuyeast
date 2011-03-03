package simulation;

import java.util.ArrayList;
import java.util.LinkedList;

import javax.media.opengl.GL;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

/**
 * @author epiuze
 */
public class Particle {
    
    /**
     * Default heavy mass
     */
    static final double MassHeavy = 10;
    
    /**
     * Default normal mass
     */
    static final double MassNormal= 1;

    /**
     * If this particle is pinned.
     */
    public boolean pinned = false;
        
    /**
     * If this particle can be deleted.
     */
    boolean deleteable = true;
    
    /**
     * If this particle is heavy.
     */
    boolean heavy = false;
        
    /**
     * This particle's mass.
     */
    public double mass = MassNormal;

    /**
     * This particle's position.
     */
    public Point2d p = new Point2d();

    /**
     * This particle's velocity.
     */
    public Vector2d v = new Vector2d();

    /**
     * This particle's initial position.
     */
    public Point2d p0 = new Point2d();
    
    /**
     * This particle's initial velocity.
     */
    Vector2d v0 = new Vector2d();

    /**
     * The accumulated force on this particle.
     */
    public Vector2d f = new Vector2d();
    
    /**
     * A list of springs to which this particle is attached
     * (currently unused, but perhaps of future use)
     */
    public ArrayList<Spring> springs = new ArrayList<Spring>();

    /**
     * An index representing this particle. Set to zero initially.
     * There is no guarantee on the uniqueness of this index.
     */
    public int index = 0;

    /**
     * If this particle is in contact with a boundary.
     */
    public boolean inContact = false;
    
    /**
     * The list of boundaries in contact with that particle.
     */
    public LinkedList<Boundary> contactBoundaries = new LinkedList<Boundary>();

    /**
     * The list of contact points.
     */
    public LinkedList<Point2d> contactPoints = new LinkedList<Point2d>();

    /**
     * The list of normals for boundaries in contact with this particle.
     */
    public LinkedList<Vector2d> contactNormals= new LinkedList<Vector2d>();

    /**
     * Creates a particle with the given position and velocity
     * @param x
     * @param y
     * @param vx
     * @param vy
     */
    public Particle( double x, double y, double vx, double vy ) {
        p0.set(x,y);
        v0.set(vx,vy);
        reset();
    }
    
    /**
     * Resets the position of this particle
     */
    public void reset() {
        inContact = false;
        illegal = false;
        contactBoundaries.clear();
        contactPoints.clear();
        contactNormals.clear();

        p.set(p0);
        v.set(v0);
        f.set(0,0);
    }
    
    /**
     * Adds the given force to this particle
     * @param force
     */
    public void addForce( Vector2d force ) {
        f.add(force);
    }
    
    /**
     * Computes the distance of a point to this particle
     * @param x
     * @param y
     * @return the distance
     */
    public double distance( double x, double y ) {
        Point2d tmp = new Point2d( x, y );
        return tmp.distance(p);
    }
   
    public String toString() {
        return "(" + index + ")" + " mass=" + mass;
    }

    /**
     * Adds a contact between this particle and a boundary.
     * @param bc 
     * @param contactPoint 
     */
    public void addContact(Boundary bc, Point2d contactPoint, Vector2d normal) {
        contactBoundaries.add(bc);
        contactPoints.add(contactPoint);
        contactNormals.add(normal);
    }
    
    /**
     * If this particle is in a prohibited region
     */
    public boolean illegal = false;

    double alpha = 0.5;

    /**
     * If this particle is grabbed.
     */
    public boolean grabbed = false;
    
    public void glVertex2d(GL gl) {

        if ( pinned ) {
            gl.glColor4d( 1, 0, 0, alpha );
        }
        else if ( heavy ) {
            gl.glColor4d( 0, 0, 1, alpha );
        }
        else if ( inContact ) {
            gl.glColor4d( 1, 1, 1, 1 );
        }
        else {
            gl.glColor4d( 0, 0.95,0, alpha );
        }

        gl.glVertex2d( p.x, p.y );

        gl.glColor4d( 1, 1 ,0, 1);
        
        if (contactPoints.size() > 0) {
            for (Point2d cp : contactPoints) {
                gl.glVertex2d( cp.x, cp.y );
            }
        }

        if ( illegal ) {
            gl.glEnd();
            gl.glPointSize( 15f );
            gl.glBegin(GL.GL_POINTS);
            gl.glColor4d( 1, 0 ,0, 0.6);
            gl.glVertex2d( p.x, p.y );
        }
        
        gl.glEnd();
        
        if (contactNormals.size() > 0 && contactPoints.size() > 0) {
            gl.glBegin(GL.GL_LINES);
            gl.glColor4d( 0, 0, 1, 0.8 );
            int ci = 0;
            for (Point2d cp : contactPoints) {
                gl.glVertex2d(cp.x, cp.y);
                gl.glVertex2d(cp.x + 20*contactNormals.get(ci).x, cp.y + 20*contactNormals.get(ci).y );
                ci++;
            }
            
            gl.glEnd();
        }
        
        gl.glPointSize( 15f );
        gl.glBegin(GL.GL_POINTS);
//        gl.glColor4d( 1, 0 ,0, 0.6);

        
        
    }
}
