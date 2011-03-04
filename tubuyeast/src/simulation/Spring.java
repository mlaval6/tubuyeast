package simulation;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;

/**
 * @author epiuze
 */
public class Spring {

    public Particle p1 = null;
    public Particle p2 = null;
    
    /**
     * All springs share the same stiffness coefficient
     */
    public static double DEFAULT_K = 1;
    /**
     * All springs share the same camping coefficient
     */
    public static double DEFAULT_B = 1;
    
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
//        k = DEFAULT_K;
//        b = DEFAULT_B;
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

//        if (!p1.grabbed && !p2.grabbed) {
//            p1.addForce(fs);
//            p1.addForce(fd);
//            fs.scale(-1);
//            p2.addForce(fs);
//            
//            fd.scale(-1);
//            p2.addForce(fd);        
//        }
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
    
    /** 
     * Adds this spring's contribution to both the stiffness and damping matrices.
     * @param K the stiffness matrix
     * @param B the damping matrix
     */
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
    
    /** 
     * Adds only the contribution for the stiffness matrix
     * @param K the stiffness matrix
     */
    public void gradient(Matrix K) {
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
        // All entries are zero except on the diagonal where they are 1's
        // so add them directly
        double vscale = -k * (1 - l0/length);
        dfK.add(0, 0, vscale);
        dfK.add(1, 1, vscale);

        // Adds damping matrix
        dfB.set(df2);
        dfB.scale(-b / (length * length));

        // Now we have the stiffness contribution for this spring
        // Sets the 3x3 matrices for the current dFi / dXj
        // where i = index for p1 and j = index for p2
        // We have enough information to increment 4 of these matrices
        // 1. dF1 / dx1 (more precisely it is dF12 / dX1 since this is only the contribution from P2)
        // 2. dF1 / dx2
        // 3. dF2 / dx2 = -dF1 / dx1 (more precisely it is dF21 / dX2 since this is only the contribution from P1)
        // 4. dF2 / dx1 = -dF1 / dx2
        // f12 = -f21 so we add these in an antisymmetric way in K
        double kval, bval;
        
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
                K.add(2 * p1.index + i, 2 * p1.index + j, bval);

                // df1/dx2 += df12/dx2
                K.add(2 * p1.index + i, 2 * p2.index + j, -bval);

                // df2/dx1 += df21/dx1
                K.add(2 * p2.index + i, 2 * p1.index + j, -bval);

                // df2/dx2 += df21/dx2
                K.add(2 * p2.index + i, 2 * p2.index + j, bval);
            }
        }
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
}
