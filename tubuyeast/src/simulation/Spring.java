package simulation;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import tools.computations.CollisionTools;

/**
 * @author epiuze
 */
public class Spring {

    public Particle p1 = null;
    public Particle p2 = null;
    
    /**
     * Spring stiffness.
     */
    private double k;
    
    /**
     * Spring damping.
     */
    private double b;

    /**
     * Rest length
     */
    double l0 = 0;
    
    /**
     * Creates a spring connecting two particles.
     * The rest length should be set
     * @param p1
     * @param p2
     */
    public Spring(Particle p1, Particle p2, double k, double b) {
        this.p1 = p1;
        this.p2 = p2;
        computeRestLength();
        p1.springs.add(this);
        p2.springs.add(this);

        this.k = k;
        this.b = b;
    }
    
    /**
     * Set the stiffness constant for this spring.
     * @param k
     */
    public void setK(double k) {
    	this.k = k;
    }
    
    /**
     * @return the stiffness constant of this spring.
     */
    public double getK() {
    	return k;
    }
    
    /**
     * Set the damping constant for this spring.
     * @param b
     */
    public void setB(double b) {
    	this.b = b;
    }
    
    /**
     * @return the damping constant of this spring.
     */
    public double getB() {
    	return b;
    }
 
    /**
     * Computes the rest length of the connected particles
     */
    public void computeRestLength() {
        l0 = p1.p0.distance( p2.p0 );
    }

    private Vector2d fs = new Vector2d();
    private Vector2d fd = new Vector2d();
    
    /**
     * Applies spring forces to the two particles
     */
    public void apply() {
        
        fs.sub( p2.p, p1.p );
        double l = fs.length();
        fs.normalize();
        fs.scale( (l-l0)*k );
        
        fd.sub( p2.p, p1.p );
        fd.normalize();
        Vector2d v = new Vector2d();
        v.sub(p2.v, p1.v);
        double rv = fd.dot(v);
        fd.scale(b*rv);

        p1.addForce(fs);
        p1.addForce(fd);

        fs.scale(-1);
        fd.scale(-1);
        p2.addForce(fs);
        p2.addForce(fd);        
        
    }
    
    public double getLength() {
        return p1.p.distance(p2.p);
    }
    
    private static Vector2d kforce = new Vector2d();
    private static Vector2d bforce = new Vector2d();
    
    /**
     * Creates a spring force between these two points.
     * The second point is assumed to be at rest.
     * The force points towards point 2.
     * @param x1
     * @param x2
     * @param v
     * @param ks
     * @param bs
     * @return The spring force.
     */
    public static Vector2d createSpringForce(Point2d x1, Point2d x2, Vector2d v, double ks, double bs) {
        Vector2d force = new Vector2d();
        
        kforce.sub( x2, x1 );
        kforce.normalize();
        kforce.scale( kforce.length() * ks );
        
        bforce.sub( x2, x1 );
        bforce.normalize();
        bforce.scale( bforce.dot(v) * bs);
        
        force.add(kforce, bforce);
        return force;
    }
    
//    private Vector2d n = new Vector2d();
//    private Vector2d u = new Vector2d();
//    private Vector2d v = new Vector2d();
//    private Vector2d w = new Vector2d();
//
//    public boolean intersect(Particle p, double step) {
//    	if (p == p1 || p == p2 || p.inContact ) return false;
//    	
//    	u.sub(p2.p, p1.p);
//
//    	v.set(p.v);
//    	v.normalize();
////    	v.scale(step);
//    	
//    	w.sub(p1.p, p.p);
//    	
//    	double denom = v.y * u.x - v.x * u.y;
//    	
//    	// Collinear
//    	if (denom == 0) return false;
//    	
//    	double s = (v.x * w.y - v.y * w.x) / denom;
//    	double t = (u.x * w.y - u.y * w.x) / denom;
//    	
//    	// First make sure the particle will hit the spring
//    	if (0 <= t && t <= 1) {
//    		if (0 <= s && s <= 1) {
//    			// Can apply force/impulse in here
//    			double theta = Math.acos(p.v.dot(u) / (u.length() * p.v.length()));
//    			n.set(-u.y, u.x);
//    			n.normalize();
//    			if (n.dot(v) > 0) n.scale(-1);
//    			
//    			n.scale(2*p.v.length() * Math.sin(theta));
//    			p.v.add(n);
//    			
//        		p.inContact = true;
//        		return true;
//    		}
//    	}
//    	return false;
//    }
//    public boolean intersect(Particle p, double stepSize) {
//    	// Don't intersect particles attached to this spring
////    	if (p == p1 || p == p2 || p.inContact ) return false;
//    	
//    	// Line segment for spring
//    	Point2d A = new Point2d(p1.p);
//    	Point2d B = new Point2d(p2.p);
//    	
//    	// Line segment from particle position to particle + step * velocity
//    	Point2d C = new Point2d(p.p);
//    	Point2d D = new Point2d();
//    	D.scaleAdd(stepSize, p.v, C);
//    	
//    	// n is a perpendicular to the spring (B-A)
//    	Vector2d n = new Vector2d();
//    	n.sub(B, A);
//		if (n.dot(p.v) < 0) n.scale(-1);
//    	u.set(n);
//    	n.set(n.y, -n.x);
//    	
//    	Vector2d AmC = new Vector2d();
//    	AmC.sub(A, C);
//    	
//    	Vector2d DmC = new Vector2d();
//    	DmC.sub(D, C);
//    	
//    	// Solve for the parameter of intersection t
//    	double t = n.dot(AmC) / n.dot(DmC);
//    	
//    	// if 0 <= t <= 1 then we have an intersection
//    	if (0 <= t && t <= 1) {
//    		System.out.println("INTERSECTING: " + t);
//    		p.inContact = true;
//    		p1.inContact = true;
//    		p2.inContact = true;
//    		
//    		
////			// Can apply force/impulse in here
//			double theta = Math.acos(p.v.dot(u) / (u.length() * p.v.length()));
//			n.normalize();
////			if (n.dot(p.v) > 0) n.scale(-1);
////			
//			n.scale(2*p.v.length() * Math.sin(theta));
//			p.v.add(n);
////
////			p1.heavy = true;
////			p2.heavy = true;
////			p.v.y = - p.v.y;
//			
//    		return true;
//    	}
//    	
////    	System.out.println(t);
//    	
//    	return false;
//    }
    
    public boolean intersect(Particle p, double stepSize) {
    	
    	// Don't intersect particles attached to this spring
    	if (p == p1 || p == p2 || p.inContact ) return false;

    	// Line segment for spring
    	Point2d A = new Point2d(p1.p);
    	Point2d B = new Point2d(p2.p);
    	
    	// Line segment from particle position to particle + step * velocity
    	Point2d C = new Point2d(p.p);
    	Point2d D = new Point2d();
    	D.scaleAdd(2 * stepSize, p.v, C);
    	
    	if (CollisionTools.areClockwise(A, C, D) == CollisionTools.areClockwise(B, C, D)) {
    		return false;
    	}
    	else if (CollisionTools.areClockwise(A, B, C) == CollisionTools.areClockwise(A, B, D)) {
    		return false;
    	}
    	else {
    		p.inContact = true;
    		p1.inContact = true;
    		p2.inContact = true;

    		CollisionTools.bounce(p, A, B, 1);
			
    		return true;
    	}
    }
    
}
