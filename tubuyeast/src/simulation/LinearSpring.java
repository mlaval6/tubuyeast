package simulation;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import tools.computations.CollisionTools;

/**
 * @author epiuze
 */
public class LinearSpring implements Spring{

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
    public LinearSpring(Particle p1, Particle p2, double k, double b) {
        this.p1 = p1;
        this.p2 = p2;
        computeRestLength();
        p1.springs.add(this);
        p2.springs.add(this);

        this.k = k;
        this.b = b;
    }
    
	@Override
    public void setK(double k) {
    	this.k = k;
    }
    
    @Override
    public double getK() {
    	return k;
    }
    
    @Override
    public void setB(double b) {
    	this.b = b;
    }
    
    @Override
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
    
    @Override
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
    
    private FlexCompRowMatrix dfK = new FlexCompRowMatrix(2, 2);
    private FlexCompRowMatrix dfB = new FlexCompRowMatrix(2, 2);
    private FlexCompRowMatrix df2 = new FlexCompRowMatrix(2, 2);
    private Vector2d diff = new Vector2d();
    private double lx, ly;
    
    @Override
    public void gradient(Matrix K, Matrix B) {
        // Length of the spring
        double length = getLength();

        // Difference vector between the two particles
        diff.sub(p1.p, p2.p);
        lx = diff.x;
        ly = diff.y;

        df2.set(0, 0, lx*lx);
        df2.set(0, 1, lx*ly);

        df2.set(1, 0, ly*lx);
        df2.set(1, 1, ly*ly);
        
        // Add second term of the stiffness matrix -k*r / |l|^3
        dfK.set(df2);
        dfK.scale(- k * l0 / (length * length * length) );        
        // Add first term of the stiffness matrix -k*(1 - r / l)
        // All entries are zero except on the diagonal
        double vscale = -k * (1 - l0/length);
        dfK.add(0, 0, vscale);
        dfK.add(1, 1, vscale);

        // Adds damping matrix
        dfB.set(df2);
        dfB.scale(-b / (length * length));

        // Now we have the stiffness contribution for this spring
        // Sets the 2x2 matrices for the current dFi / dXj
        // where i = index for p1 and j = index for p2
        // We have enough information to increment 4 of these matrices
        // 1. dF1 / dx1 (more precisely it is dF12 / dX1 since this is only the contribution from P2)
        // 2. dF1 / dx2
        // 3. dF2 / dx2 = -dF1 / dx1 (more precisely it is dF21 / dX2 since this is only the contribution from P1)
        // 4. dF2 / dx1 = -dF1 / dx2
        // f12 = -f21 so we add these in an antisymmetric way in K
        double kval = 0;
        double bval = 0;
        
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                /*
                 * Stiffness
                 */
                kval = dfK.get(i, j);
                
                // df1/dx1 += df12/dx1
                K.add(2 * p1.index + i, 2 * p1.index + j, kval);

                // df1/dx2 += df12/dx2
                K.add(2 * p1.index + i, 2 * p2.index + j, -kval);

                // df2/dx1 += df21/dx1
                K.add(2 * p2.index + i, 2 * p1.index + j, -kval);

                // df2/dx2 += df21/dx2
                K.add(2 * p2.index + i, 2 * p2.index + j, kval);

                /*
                 * Damping
                 */
                bval = dfB.get(i, j);

                // df1/dx1 += df12/dx1
                B.add(2 * p1.index + i, 2 * p1.index + j, bval);

                // df1/dx2 += df12/dx2
                B.add(2 * p1.index + i, 2 * p2.index + j, -bval);

                // df2/dx1 += df21/dx1
                B.add(2 * p2.index + i, 2 * p1.index + j, -bval);

                // df2/dx2 += df21/dx2
                B.add(2 * p2.index + i, 2 * p2.index + j, bval);
            }
        }
    }
     
    private double getLength() {
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
    
	@Override
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
    
	@Override
    public void display(GLAutoDrawable drawable) {
		if (!p1.collidable && p1.inContact) return;
		if (!p2.collidable && p2.inContact) return;
		
    	GL gl = drawable.getGL();
    	
		gl.glColor4d(0, 0.5, 0.5, 0.5);
		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINES);
		gl.glVertex2d(p1.p.x, p1.p.y);
		gl.glVertex2d(p2.p.x, p2.p.y);
		gl.glEnd();
    	
    }
    
}
