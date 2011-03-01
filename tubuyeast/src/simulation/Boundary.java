package simulation;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 * @author piuze
 */
public class Boundary {

    private Point2d p1;
    private final Point2d p0;
    private final Vector2d bd;
    
    private Vector2d n = new Vector2d();
    private Vector2d d = new Vector2d();
    
    private double length = 0;
    private double length2 = 0;
    
    /**
     * Used in computing line intersect
     */
    private double d01x, d01y, xyyx;

    /**
     * Boundary end point distance epsilon
     */
    private double bEpsilon;

    private double vEpsilon = 1e-5;
    
    /**
     * The constrained direction
     */
    private FlexCompRowMatrix p = new FlexCompRowMatrix(2,1);
    
    /**
     * The constraint matrix S_i
     */
    private FlexCompRowMatrix Si = new FlexCompRowMatrix(2,2);
    
    /**
     * The intersection constraint matrix S_ni
     */
    private FlexCompRowMatrix SiI = new FlexCompRowMatrix(2,2);

    private FlexCompRowMatrix tmpC = new FlexCompRowMatrix(2,1);
    private FlexCompRowMatrix tmpCt = new FlexCompRowMatrix(1,2);
    private Vector2d tmpV = new Vector2d();

    /**
     * @param e1 Boundary point 1.
     * @param e2 Boundary point 2.
     */
    public Boundary(Point2d e1, Point2d e2) {
        // Equation of the planar boundary
        p0 = new Point2d(e1);
        p1 = new Point2d(e2);
        
        // L = p0 + t*d
        bd = new Vector2d();
        bd.sub(p1, p0);
        
        length = bd.length();
        length2 = length * length;
        bEpsilon = 0.1 / length;

        // Precomputations for line intersections
        d01x = p0.x - p1.x;
        d01y = p0.y - p1.y;
        xyyx = p0.x * p1.y - p0.y * p1.x;
        
        // Normal pointing inside of the boundary
        n = new Vector2d(-bd.y, bd.x);
        n.normalize();
        
        // Normal pointing outside of the boundary
        d.set(n);
        d.negate();

        // Compute directional constraint = PT * P
        FlexCompRowMatrix pT = new FlexCompRowMatrix(1,2);
        FlexCompRowMatrix pN = new FlexCompRowMatrix(2,1);

        p.set(0, 0, d.x);
        p.set(1, 0, d.y);
        p.transpose(pT);
        p.mult(pT, Si);
        
        // Si = - PT * P
        Si.scale(-1);
        
        // Compute normal constraint = nT * n
        pN.set(0, 0, bd.x);
        pN.set(1, 0, bd.y);
        pN.transpose(pT);
        pN.mult(pT, SiN);
        
        // Precomputations
        double mx = p0.x + (p1.x - p0.x) / 2;
        double my = p0.y + (p1.y - p0.y) / 2;
        contactPoint.set(mx, my);
    }
    
    /**
     * Restricts the particle to move along the boundary
     * @return The directional constraint to use in a CG filter.
     */
    public FlexCompRowMatrix getDirectionalConstraint() {
        
        return Si;
    }
    
    /**
     * The normal constraint matrix S_ni
     */
    private FlexCompRowMatrix SiN = new FlexCompRowMatrix(2,2);

    /**
     * Restricts the particle to move along the normal.
     * @return The normal constraint to use in a CG filter.
     */
    public Matrix getNormalConstraint() {
        return SiN;
    }
    
    /**
     * Restricts the particle to move towards an intersection point
     * @param pos 
     * @param intersect 
     * @return The constrained direction
     */
    public Matrix getIntersectionConstraint(Point2d pos, Point2d intersect) {

        tmpV.sub(intersect, pos);
        
        tmpC.set(0, 0, -tmpV.y);
        tmpC.set(1, 0, tmpV.x);
        tmpC.transpose(tmpCt);
        tmpC.mult(tmpCt, SiI);

        return SiI;
    }

    private Vector2d w = new Vector2d();
    private Point2d Qp = new Point2d();

    /**
     * @param pos 
     * @return the collision normal.
     */
    public Vector2d getCollisionNormal(Point2d pos) {
        n.sub(pos, getClosestPoint(pos));
        n.normalize();
        
        return n;
    }

    /**
     * @param pos 
     * @return the closest point on the boundary.
     */
    public Point2d getClosestPoint(Point2d pos) {
        // Q' = P + w.v / ||v||^2 * v
        // distance = || Q - Q' ||
        
        w.sub(pos, p0);
        
        // Projection on boundary vector
        Qp.set(bd);
        Qp.scale(w.dot(bd)/length2);
        Qp.add(p0);
        
        return Qp;
    }
    /**
     * Check whether this boundary is currently intersecting a particle.
     * Variables set in this method are volatile and should not be trusted (for now)
     * @param p The particle intersecting this boundary.
     * @param tmin The minimal time a point can take to be in contact.
     * Set to 0 for spatial threshold only.
     * @param dmin The minimal distance to the boundary a point has to be for contact
     * Set to 0 for temporal threshold only;
     * @return whether or not this point intersects the boundary.
     */
    public boolean collide(Particle p, double tmin, double dmin) {
        Point2d pos = p.p;
        Vector2d v = p.v;
        
        boolean contact = false;
        
        // If v is not a point (a point cannot intersect a line...)
        if (v.length() > vEpsilon) {
            // Find the parameter t at which the point + k*velocity intersects the boundary
            double t = getIntersectionParameter(pos, v);

            // If the intersection is not on the boundary, we are not in contact
            // leave some threshold to avoid corner effects
            if (t < 0 || t > 1) return false;
            
            double time = ( d01y * pos.x - d01x * pos.y + xyyx ) / (d01x * v.y - d01y * v.x) ;

            // Verify temporal distance
            if (time < 0) return false;
            if (time < tmin) {
                contact = true;
            }

            // Verify spatial distance t
            Point2d pp = new Point2d(bd);
            pp.scaleAdd(t, p0);
            if (pp.distance(pos) < dmin) contact = true;

            if (contact) {
                contactPoint.set(pp);
                
                if (!p.contactBoundaries.contains(this)) {
                    p.addContact(this, contactPoint, getCollisionNormal(p.p));
                }

                return true;
            }
        }
        
        return false;
    }

    /**
     * Sets this particle to be in contact with the boundary if it is "close enough"
     * @param particle The particle intersecting the boundary.
     * @param dmin The minimal distance to the boundary a point has to be for contact
     * Set to 0 for temporal threshold only;
     * @param index of this boundary in this particle's list.
     * @return whether or not this particle is in contact.
     */
    public boolean contact(Particle particle, double dmin, int index) {
        Point2d pos = particle.p;
        
        // Find the scaling of the closest point to the boundary
        double t = getClosestParameter(pos);

        // If the closest point to the boundary lies outside
        // then this particle it is not in contact.
        // leave some threshold to avoid corner effects
        if (t < 0 || t > 1) {
            return false;
        }

        // If the particle is close enough
        // it is in contact
        contactPoint.set(bd);
        contactPoint.scaleAdd(t, p0);
        
        if (contactPoint.distance(pos) < dmin) {

            // Update this particle's contact point with the boundary
            particle.contactPoints.set(index, contactPoint);

            return true;
        }

        return false;
    }
    
    /**
     * For the boundary segment given by: U = p0 + t*(p1-p0)
     * find the parameter t which denotes U that is the closest
     * to the point.
     */
    private double getClosestParameter(Point2d pos) {
        // t = w.bd / (bd.bd)
        tmpV.sub(pos, p0);
        double t = bd.dot(tmpV) / ( length2 );

        // Apply small boundary epsilon to prevent corner effects
        if (t > -bEpsilon && t <= 0) {
            t = 0;
        }
        else if (t >= 1 && t < 1 + bEpsilon) {
            t = 1;
        }
        
        return t;
    }

    /**
     * For the boundary line given by: U = p0 + t*(p1-p0)
     * and the velocity line given by: V = p + k*v
     * find the parameter t at the crossing
     */
    private double getIntersectionParameter(Point2d pos, Vector2d v) {
        
        double t = ((p0.x-pos.x)*v.y + (pos.y-p0.y)*v.x) / ((p0.x-p1.x)*v.y+(p1.y-p0.y)*v.x);    

        // Apply small boundary epsilon to prevent corner effects
        if (t > -bEpsilon && t <= 0) {
            t = 0;
        }
        else if (t >= 1 && t < 1 + bEpsilon) {
            t = 1;
        }
        
        return t;
    }

    /**
     * Verifies if this boundary could ever intersect this particle's
     * trajectory. i.e. if the particle's trajectory passes through the two end
     * points.
     * 
     * @param particle
     * @return whether this boundary could ever intersect this particle. i.e. if
     *         the trajectory crosses inside the two end points.
     */
    public boolean intersects(Particle particle) {
        Point2d pos = particle.p;
        Vector2d v = particle.v;
        
        // Find the parameter t at which the point + k*velocity intersects the boundary
        double t = ((p0.x-pos.x)*v.y + (pos.y-p0.y)*v.x) / ((p0.x-p1.x)*v.y+(p1.y-p0.y)*v.x);

        // If the intersection is not on the boundary, we are not in contact
        // leave some threshold to avoid corner effects
        if (t <= 0 - bEpsilon || t >= 1 + bEpsilon) return false;

        return true;
    }

    /**
     * The position of last intersec with this boundary
     */
    public Point2d contactPoint = new Point2d();
    

    /**
     * @param p The constrained particle.
     * @param Si The constraint matrix (2*2 * n)
     * @param z The velocity delta seeding vector (2*n).
     */
    public static void applyCollisionConstraint(Particle p, Boundary b, FlexCompRowMatrix[] Si, Vector z) {
                
        if (p.grabbed) return;
        
        int i = p.index;
        int bindex = p.contactBoundaries.indexOf(b);
        
        Vector2d vN = new Vector2d();
        Vector2d vT = new Vector2d();
        double vdotn;
        
        Vector2d zi = new Vector2d();
        Vector2d normal;

        // Apply collision constraints for that particle and boundary
        normal = p.contactNormals.get(bindex);

        vdotn = p.v.dot(normal);

        // Find normal and tangential component of velocity
        // to that boundary
        vN.set(normal);
        vN.scale(vdotn);

        vT.add(vN);
        vT.scale(-1);
        vT.add(p.v);

        // Collision response with coefficient of restitution
        vN.scale(-1.5);
        vT.scale(0);

        Si[i].add(b.getDirectionalConstraint());

        zi.add(vT, vN);
        z.set(2 * i, zi.x);
        z.set(2 * i + 1, zi.y);
    }

    /**
     * @param p
     *            The particle in contact.
     * @param Si
     *            The constraint matrix (2*2 * n)
     * @param z
     *            The velocity delta seeding vector (2*n).
     */
    public static void applyContactConstraints(Particle p, FlexCompRowMatrix[] Si, Vector z) {
                
        int i = p.index;
        int bindex = 0;
        
        Vector2d vN = new Vector2d();
        Vector2d vT = new Vector2d();
        double vdotn;
        
        Vector2d zi = new Vector2d();
        Vector2d normal;

        // Apply contact constraints for all boundaries in contact with that particle.
        for (Boundary b : p.contactBoundaries) {
            normal = p.contactNormals.get(bindex);
            
            vdotn = p.v.dot(normal);
            
            // Find normal and tangential component of velocity
            // to that boundary
            vN.set(normal);
            vN.scale(vdotn);
            
            vT.add(vN);
            vT.scale(-1);
            vT.add(p.v);

            // Enforces contact if normal velocity
            // is small enough. (prevents drifting).
            // TODO: use position alteration method from Baraff SIG98 6.2
            // with position constraint on the normal?
            if (vN.length() < 0.5) {
                
                vT.scale(0);
                vN.scale(-1);
                    
                Si[i].add(b.getDirectionalConstraint());
                p.inContact = true;
            }
            // Otherwise let collision detection deal with it
            else {
                vN.scale(0);
                vT.scale(0);
            }

            zi.add(vT, vN);

            bindex++;
        }

        z.add(2*i, zi.x);
        z.add(2*i+1, zi.y);
    }

    /**
     * Display this constraint
     * @param drawable
     */
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        
        // Boundary
        gl.glColor4d( 1, 1, 1, 0.5 );
        gl.glLineWidth( 2 );
        gl.glBegin( GL.GL_LINES );
        gl.glVertex2d( p0.x, p0.y );
        gl.glVertex2d( p1.x, p1.y );

        // Boundary epsilon
        gl.glColor4d( 1, 0, 0, 0.3 );
        gl.glVertex2d(p0.x, p0.y);
        gl.glVertex2d(p0.x - bd.x * bEpsilon, p0.y - bd.y * bEpsilon);
        
        gl.glVertex2d(p1.x, p1.y);
        gl.glVertex2d(p1.x + bd.x * bEpsilon, p1.y + bd.y * bEpsilon);

        // Normal
//        gl.glColor4d( 0, 0, 1, 0.8 );
//        gl.glVertex2d(contactPoint.x, contactPoint.y);
//        gl.glVertex2d(contactPoint.x + 20*n.x, contactPoint.y + 20*n.y );
        
        gl.glEnd();

//        gl.glPointSize(15);
//        gl.glBegin( GL.GL_POINTS );
//        double alpha = 0.5;
//
//        gl.glColor4d( 1, 1, 0, alpha );
//
//        gl.glVertex2d( p0.x, p0.y );
//        gl.glEnd();

    }

}
