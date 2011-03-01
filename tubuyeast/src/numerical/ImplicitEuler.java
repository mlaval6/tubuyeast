package numerical;

import java.util.ArrayList;

import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.DenseMatrix;
import no.uib.cipr.matrix.DenseVector;
import no.uib.cipr.matrix.Matrices;
import no.uib.cipr.matrix.Vector;
import no.uib.cipr.matrix.sparse.CompRowMatrix;
import no.uib.cipr.matrix.sparse.FlexCompRowMatrix;
import simulation.Particle;
import simulation.ParticleSystem;

/**
 * @author epiuze
 */
public class ImplicitEuler {

    /**
     * The list of particles in the system;
     */
    private ArrayList<Particle> particles = new ArrayList<Particle>();

    private ConjugateGradient cg;
    private FlexCompRowMatrix A;
    private FlexCompRowMatrix Ad;
    private DenseMatrix M, W;
    private DenseVector b, dv;
    private DenseVector f0, v0;

    /**
     * @param particleList
     * @param cg 
     */
    public void initialize(ArrayList<Particle> particleList, ConjugateGradient cg) {
        particles = particleList;
        
        initMatrices();

        this.cg = cg;
        
        updateConstraints();
    }
    
    private void initMatrices() {
        
        int dim = 2 * particles.size();

        // Find M and M^-1
        M = new DenseMatrix(dim, dim);
        for (Particle p : particles) {
            M.set(2 * p.index + 0, 2 * p.index + 0, p.mass);
            M.set(2 * p.index + 1, 2 * p.index + 1, p.mass);
        }
        
        DenseMatrix I = Matrices.identity(M.numColumns());
        W = new DenseMatrix(dim, dim);
        M.solve(I, W);

        // Force
        f0 = new DenseVector(dim);
        
        // Velocity
        v0 = new DenseVector(dim);
        
        A = new FlexCompRowMatrix(dim, dim);

        b = new DenseVector(dim);

        // Initializes the solver
        // Add a convergence monitor
        // Allocate storage for Conjugate Gradients
        dv = new DenseVector(dim);
        
        cF = new DenseVector(2 * particles.size());
    }
 
    /**
     * Updates velocity constraints
     */
    public void updateConstraints() {
        cg.updateSystem();
    }

    /**
     * Constraint forces to apply after solving the CG
     */
    private Vector cF;
    /**
     * Backward Euler integration step
     * @param K
     * @param B 
     * @param t
     * @param h
     * @param ps
     * @param cs
     */
    public void step(CompRowMatrix K, CompRowMatrix B, double t, double h, ParticleSystem ps, int numIterations) {
        
        // (W - h^2*K - h*B) dv = hf0 + h^2*K*v0)
        K.scale(h*h);
        B.scale(h);
        
        // W - h^2*K - h*B
        A.zero();
        A.add(K);
        A.add(B);
        A.scale(-1);
        A.add(M);
        
        int i = 0;
        for (Particle p : particles) {
            i = p.index;

            // Set f0
            f0.set(2*p.index, p.f.x + cF.get(2*p.index));
            f0.set(2*p.index + 1, p.f.y + cF.get(2*p.index + 1));

            // Set v0
            v0.set(2*p.index, p.v.x);
            v0.set(2*p.index + 1, p.v.y);
        }
        
        //h(f0 + h*K*v0)
        K.mult(v0, b);
        f0.scale(h);
        b.add(f0);

        // Update constraints
        // and add contact forces
        // e.g. friction
        cg.updateSystem();

        cF = cg.solve(A, b, dv, numIterations);
      
        // Prevent drifting by cutting down normal component if small
        double vdotn;
        Vector2d vT = new Vector2d();
        Vector2d vN = new Vector2d();
        Vector2d dP = new Vector2d();

        // Updates the positions
        for (Particle p : particles) {
            
            p.v.x += dv.get(2 * p.index + 0);
            p.v.y += dv.get(2 * p.index + 1);
            
            // Innocent until proven guilty
            p.illegal = false;
            
        }

      // Updates position
      for (Particle p : particles) {
          p.p.x += h * p.v.x;
          p.p.y += h * p.v.y;
      }
    }

    /**
     * If there is anything to be updated in the internal structure.
     */
    public void update() {
        initMatrices();

        updateConstraints();
    }

}
