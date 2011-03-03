package numerical;

import java.util.ArrayList;

import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Matrix;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import simulation.Particle;

/**
 * @author epiuze
 */
public class ConjugateGradient {
    
    /**
     * Residual vector
     */
    private Vector r;
    
    /**
     * Residual vector filtered
     */
    private Vector rf;
    
    /**
     * Residual vector tranpose
     */
    private DenseMatrix rT;
    
    /**
     * Filtered preconditioned residual 
     */
    private Vector c, s;
    
    /**
     * Filtered preconditioned residual transpose
     */
    private DenseMatrix cT;

    /**
     * Change of velocity we wish to enforce in the
     * constrained direction
     */
    private Vector z;
    
    private Vector tmpVector;
    
    /**
     * Filtered b
     */
    private Vector bf;
    
    private Vector q;

    /**
     * A * 
     */
    private Vector Ap;
    
    private double rmag;
    
    /**
     * Preconditioning matrix
     */
    private CompRowMatrix P, Pinv;
    
    private DenseMatrix rowMatrix;

    private DenseVector unitVector;

    /**
     * b transpose
     */
    private DenseMatrix bfT;
    
    private ArrayList<Particle> particles;
    
    /**
     * Number of particles in the system
     */
    private int n = 0;
        
    private FlexCompRowMatrix[] Si;
    private DenseMatrix identity;
    
    /**
     * Creates a new conjugate gradient solver
     * @param n the size of the system
     */
    public ConjugateGradient( ArrayList<Particle> particles) {
        n = particles.size();
        r = new DenseVector(2*n);
        rf = new DenseVector(2*n);
        rT = new DenseMatrix(1, 2*n);
        bf = new DenseVector(2*n);
        c = new DenseVector(2*n);
        s = new DenseVector(2*n);
        z = new DenseVector(2*n);
        cT = new DenseMatrix(1, 2*n);
        q = new DenseVector(2*n );
        Ap = new DenseVector(2*n);
        bfT = new DenseMatrix(1, 2*n);
        tmpVector = new DenseVector(2*n);
        rowMatrix = new DenseMatrix(1, 2*n);
        unitVector = new DenseVector(1);
        this.particles = particles;

        int[][] nz = new int[2*n][1];
        for (int i = 0; i< 2*n; i++) {
            nz[i][0] = i;
        }
        P = new CompRowMatrix(2*n, 2*n, nz);
        Pinv = new CompRowMatrix(2*n, 2*n, nz);
        
        identity = Matrices.identity(2);
        
        // Initialize the modified inverse mass matrix with identity
        Si = new FlexCompRowMatrix[n];
        for (int i = 0; i < n; i++) {
            Si[i] = new FlexCompRowMatrix(2, 2);
        }

        updateSystem();
    }
    
    /** 
     * Performs conjugate gradient for the given number of iterations.
     * Note the comments in the code as to places where one might want
     * to filter values to satisfy constraints.
     * @param A
     * @param b
     * @param dv
     * @param numIts
     */
    public Vector solve( Matrix A, Vector b, Vector dv, int numIts ) {     
//        updateSystem(); // Could maybe update each N CG calls?
        
        // Set the preconditioning matrix to Pii = 1/Aii
        for (int i = 0; i < 2*n; i++) {
            if (A.get(i, i) == 0) {
                P.set(i, i, Double.MAX_VALUE);
            }
            else {
                P.set(i, i, 1 / A.get(i, i));
            }
            
            Pinv.set(i, i, A.get(i, i));
        }

        /*
         * Procedure modified-pcg by Baraff and Witkin.
         * SIGGRAPH 1998.
         */
        double delta0, deltaNew, deltaOld, alpha;
        // filter(b)
        bf.set(b);
        filter(bf);
        // filter(b) transpose
        transpose(bf, bfT);

        // delta_v = z
        dv.set(z);

        // delta_0 = filter(b)T * P * filter(b)
        bfT.mult(P, rowMatrix);
        rowMatrix.mult(bf, unitVector);
        delta0 = unitVector.get(0);

        // r = filter(b - Adv)
        A.mult(dv, r);
        r.scale(-1);
        r.add(b);  
        filter( r );        

        // r transpose
        transpose(r, rT);
        
        // c = filter(P^-1 * r)
        Pinv.mult(r, c);
        filter(c);
        
        // delta_new = rT * c
        rT.mult(c, unitVector);
        deltaNew = unitVector.get(0);
        
        double epsilon = 1e-12;
        for (int i = 0; i < numIts; i++) {
            // q = filter(Ac)
            A.mult(c, q);
            filter(q);
            
            // alpha = deltaNew / (cT * q)
            alpha = deltaNew / transpose(c, cT).mult(q, unitVector).get(0);

            // dv = dv + alpha * c
            dv.add(alpha, c);
            
            // r = r - alpha * q
            r.add(-alpha, q);
            
            // s = Pinv * r
            Pinv.mult(r, s);
            
            // delta_old = delta_new
            deltaOld = deltaNew;
            
            // delta_new = rT * s
            deltaNew = rT.mult(s, unitVector).get(0);

            // c = filter(s + delta_new / delta_old * c)
            s.add(deltaNew / deltaOld, c);
            c.set(filter(s));
            
            // Break if we reach the threshold
            if (deltaNew < epsilon * epsilon * delta0) {
//                if (i > 0) System.out.println("Breaking after " + i);
                break;
            }
        }
        
        // Determine the constraint force
        // e = Adv - b
        A.mult(dv, tmpVector);
        tmpVector.add(-1, b);
        
        return tmpVector;
    }

    private Vector2d a = new Vector2d();
    
    /**
     * Sets the matrix to the the transpose of the vector
     * @param v
     * @param vT
     * @return The transpose
     */
    public Matrix transpose(Vector v, Matrix vT) {
        int vs = v.size();
        for (int i = 0; i < vs; i++) {
            vT.set(0, i, v.get(i));
        }
        
        return vT;
    }
    
    /**
     * Filters the vector by enforcing constrained directions.
     * @param x
     */
    private Vector filter(Vector x) {
        for (int i = 0; i < n; i++) {
            a.set(x.get(2*i), x.get(2*i+1));

            x.set(2*i, Si[i].get(0, 0) * a.x + Si[i].get(0, 1) * a.y);
            x.set(2*i+1, Si[i].get(1, 0) * a.x + Si[i].get(1, 1) * a.y);
        }
        
        return x;
    }
    
    /**
     * Updates the constraints
     */
    public void updateSystem() {
        // Initialize velocity constraint Si to identity
        for (int i = 0; i < n; i++) {
            Si[i].set(identity);
        }
       
        // Set pin constraints
        int i;
        for (Particle p : particles) {
            i = p.index;
            // Pinned = 0 dof
            if (p.pinned) {
                Si[i].zero();
            }      

            // Initialize to zero since we are not enforcing a change of velocity
            // in any constrained direction
            z.set(2*i, 0);
            z.set(2*i + 1, 0);
        }
    }

}
