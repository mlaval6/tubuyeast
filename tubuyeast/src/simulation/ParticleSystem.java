package simulation;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.sparse.CompRowMatrix;
import numerical.BoxCollider;
import numerical.ConjugateGradient;
import numerical.ImplicitEuler;
import tools.gl.SceneGraphNode;
import tools.parameters.BooleanParameter;
import tools.parameters.DoubleParameter;
import tools.parameters.Parameter;
import tools.parameters.ParameterListener;
import tools.swing.VerticalFlowPanel;

/**
 * Implementation of a particle system. Manages system integration calls and particle grabbing.
 * 
 * @author epiuze
 */
public class ParticleSystem implements SceneGraphNode {

	private ArrayList<Boundary> wall = new ArrayList<Boundary>();

	private ConjugateGradient cg;

	/**
	 * List of particles in the system.
	 */
	public ArrayList<Particle> particles = new ArrayList<Particle>();

	private List<Spring> springs = new LinkedList<Spring>();

	private Dimension wsize;

	private CompRowMatrix K, B;

	private BooleanParameter useg = new BooleanParameter("use gravity", true);

	public static DoubleParameter g = new DoubleParameter("gravity", 9.8, -100,
			100);

	private DoubleParameter k = new DoubleParameter("stiffness", 100, 0.001,
			100000);

	private DoubleParameter b = new DoubleParameter("damping", 0, 0, 100);

	private DoubleParameter numIterations = new DoubleParameter("iterations",
			5, 1, 1000);

	public static DoubleParameter friction = new DoubleParameter(
			"floor friction", 0.3, 0, 2);

	private DoubleParameter rc = new DoubleParameter(
			"coefficient of restitution", 0.6, 0, 1);

	/**
	 * Create an empty particle system
	 * @param bsize the dimension of the box in which this system lives.
	 * @param be
	 */
	public ParticleSystem(Dimension bsize, ImplicitEuler be) {
		wsize = new Dimension(bsize);
		backwardEuler = be;
	}

	/**
	 * Updates the system.
	 * 
	 * @param h
	 */
	public void step(double h) {

		// Integrates the system
		updateForces();

		computeStiffnessMatrix();

		backwardEuler.step(K, B, time, h, (int) Math.round(numIterations
				.getValue()));

		// Apply simple box-wall collision
		// TODO: implement constraints in conjugate gradient
		BoxCollider.collide(wsize.getWidth(), wsize.getHeight(), rc.getValue(),
				particles);

		time = time + h;

	}

	/**
	 * Updates relevant matrices (stiffness, etc)
	 */
	public void updateSystem() {
		/**
		 * Initialize the particles' index
		 */
		int ind = 0;
		for (Particle p : particles) {
			p.index = ind++;
		}

		int n = particles.size();

		// Builds the stiffness matrix using the fact that some
		// particles are never in contact such that some columns are always zero
		// TODO: if other kinds of interacting forces (e.g. gravitational,
		// electrical) were to be added
		// the shape of this matrix would need to be reconsidered.
		int p1, p2;
		ArrayList<ArrayList<Integer>> connections = new ArrayList<ArrayList<Integer>>(
				n);
		for (@SuppressWarnings("unused")
		Particle pa : particles) {
			connections.add(new ArrayList<Integer>());
		}

		for (Spring s : springs) {
			p1 = s.p1.index;
			p2 = s.p2.index;

			connections.get(p1).add(p2);
			connections.get(p2).add(p1);

		}

		// nz Goal: for each row (three per particle), add column index if it's
		// used

		int[][] nz = new int[2 * n][];

		int it = 0;

		// For each particle (row)
		for (int j = 0; j < n; j++) {

			// Three rows per particle
			// The number of columns used is 2 * the number of particles
			// connected
			// + the column for the gradient by its own position
			nz[2 * j + 0] = new int[2 * (connections.get(j).size() + 1)];
			nz[2 * j + 1] = new int[2 * (connections.get(j).size() + 1)];

			// Set the diagonal (each particle interacts with at least one
			// other)
			// this makes up the first three non-empty elements of each 2 rows
			// for this particle
			for (int d = 0; d < 2; d++) {
				nz[2 * j + 0][d] = 2 * j + d;
				nz[2 * j + 1][d] = 2 * j + d;

			}
			// We already have 2 values in each row
			it = 2;

			for (Integer index : connections.get(j)) {
				// Adds the contribution of this connection to the non-empty
				// elements for these 2 rows
				// 2 columns are added per row for this connection
				for (int d = 0; d < 2; d++) {
					nz[2 * j + 0][it] = 2 * index + d;
					nz[2 * j + 1][it] = 2 * index + d;

					it++;
				}
			}
		}

		// Mass and inverse mass matrices
		int[][] nz2 = new int[2 * n][2];
		// The matrix is diagonal -> only set a(i,i)
		for (int i = 0; i < n; i++) {
			nz2[2 * i + 0][0] = 2 * i + 0;
			nz2[2 * i + 1][0] = 2 * i + 1;
		}

		K = new CompRowMatrix(2 * n, 2 * n, nz);
		B = new CompRowMatrix(2 * n, 2 * n, nz);

		cg = new ConjugateGradient(particles);

		backwardEuler.initialize(this);
	}

	/**
	 * Gets the particles in the system
	 * 
	 * @return the particle set
	 */
	public List<Particle> getParticles() {
		return particles;
	}

	/**
	 * Resets the positions of all particles to their initial states
	 */
	public void resetParticles() {
		for (Particle p : particles) {
			p.reset();
		}
		time = 0;

		ungrab();

		updateSystem();
	}

	/**
	 * Deletes all particles
	 */
	public void clearParticles() {
		List<Particle> toRemove = new LinkedList<Particle>();
		for (Particle p : particles) {
			if (p.deleteable) {
				toRemove.add(p);
			}
		}

		particles.removeAll(toRemove);
		springs.clear();

		updateSystem();
	}

	private double time = 0;

	/**
	 * The integrator
	 */
	public ImplicitEuler backwardEuler = null;

	private void computeStiffnessMatrix() {
		// Reset stiffness and damping matrices
		K.zero();
		B.zero();
		for (Spring s : springs) {
			s.gradient(K, B);
		}
	}

	/**
	 * Updates the current forces
	 */
	public void updateForces() {

		// Initialize forces to the the gravity force (mg)
		Vector2d fg = useg.getValue() ? new Vector2d(0, g.getValue())
				: new Vector2d();
		for (Particle p : particles) {
			p.f.set(fg);
			p.f.scale(p.mass);

		}

		// Computes and adds the spring forces
		for (Spring spring : springs) {
			spring.apply();
		}

		// Adds friction force for the floor
		// F = mu * N where N = mg
		double sign = 0;
		double gv = g.getValue();
		double mu = friction.getValue();
		for (Particle p : particles) {

			// Use some offset
			if (p.p.y >= wsize.getHeight() - 13) {
				sign = -1 * Math.signum(p.v.x);
				p.addForce(new Vector2d(mu * sign * p.mass * gv, 0));
			}

		}

		// If a particle is grabbed, use soft pulling
		if (pgrabbed != null) {
			// Find the maximum spring attached to this particle
			double ks = 0;
			double kd = 0;
			for (Spring s : pgrabbed.springs) {
				if (s.getK() > ks) ks = s.getK();
				if (s.getB() > kd) kd = s.getB();
			}
			
			// Make pulling force stronger than max stiffness found
			ks *= 2;
			
			// Don't use damping. Remove this to use max damping found
			kd = 0;
			
			Vector2d ddx = new Vector2d();
			ddx.set(grabpos.x - grabpos0.x, grabpos.y - grabpos0.y);
			ddx.scale(0.5);

			Vector2d force = new Vector2d();

			// Spring
			force.sub(grabpos, pgrabbed.p);
			double l = force.length();
			force.normalize();
			force.scale((l) * ks);
			pgrabbed.addForce(force);
			force.scale(-1);

			// Damping
			force.sub(grabpos, pgrabbed.p);
			force.normalize();
			Vector2d v = new Vector2d();
			v.sub(ddx, pgrabbed.v);
			double rv = force.dot(v);
			force.scale(kd * rv);
			pgrabbed.addForce(force);
		}

	}

	/**
	 * Creates a new particle and adds it to the system.
	 * 
	 * @param x
	 * @param y
	 * @param vx
	 * @param vy
	 * @return the new particle
	 */
	public Particle createParticle(double x, double y, double vx, double vy) {
		Particle p = new Particle(x, y, vx, vy);
		particles.add(p);
		return p;
	}

	/**
	 * Creates a new spring between two particles and adds it to the system.
	 * 
	 * @param p1
	 * @param p2
	 * @return the new spring
	 */
	public Spring createSpring(Particle p1, Particle p2) {
		Spring s = new Spring(p1, p2, k.getValue(), b.getValue());
		springs.add(s);
		return s;
	}

	public void init(GLAutoDrawable drawable) {
		// do nothing
	}

	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();

		// Keep track of the width and the height
		// to be able to process collisions with walls
		wsize.setSize(drawable.getWidth(), drawable.getHeight());

		// Particle box
		Point2d b1, b2, b3, b4;
		b1 = new Point2d(BoxCollider.WDX, wsize.height - BoxCollider.WDY - 0);
		b2 = new Point2d(BoxCollider.WDX, BoxCollider.WDY);
		b3 = new Point2d(wsize.width - BoxCollider.WDX, BoxCollider.WDY);
		b4 = new Point2d(wsize.width - BoxCollider.WDX, wsize.height
				- BoxCollider.WDY - 0);
		wall = ConstraintTool.createBox(b1, b2, b3, b4);
		for (Boundary b : wall) {
			b.display(drawable);
		}

		for (Particle p : particles) {
			if (p.heavy) {
				gl.glPointSize(25);
			} else {
				gl.glPointSize(10);
			}

			gl.glBegin(GL.GL_POINTS);

			p.glVertex2d(gl);

			gl.glEnd();
		}

		gl.glColor4d(0, 0.5, 0.5, 0.5);
		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINES);
		for (Spring s : springs) {
			gl.glVertex2d(s.p1.p.x, s.p1.p.y);
			gl.glVertex2d(s.p2.p.x, s.p2.p.y);
		}
		gl.glEnd();

		if (pgrabbed != null) {
			ParticleSimulationInteractor.drawLineToParticle(drawable,
					grabpos.x, grabpos.y, pgrabbed, pgrabbed.distance(
							grabpos.x, grabpos.y), 0, Math.min(
							wsize.getWidth(), wsize.getHeight()));
		}
	}

	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Particle system"));

		vfp.add(numIterations.getSliderControls(true));
		vfp.add(useg.getControls());
		vfp.add(g.getSliderControls(true));
		vfp.add(k.getSliderControls(true));
		vfp.add(b.getSliderControls(false));

		// Update spring constant when necessary
		ParameterListener l = new ParameterListener() {
			@Override
			public void parameterChanged(Parameter parameter) {
				updateSprings();
			}
		};
		k.addParameterListener(l);
		b.addParameterListener(l);

		vfp.add(friction.getSliderControls(false));
		vfp.add(rc.getSliderControls(false));

		setDefaultValues();

		return vfp.getPanel();
	}

	/**
	 * Update all springs to use the UI-defined stiffness/damping constants.
	 */
	protected void updateSprings() {
		for (Spring spring : springs) {
			spring.setK(k.getValue());
			spring.setB(b.getValue());
		}
	}

	public String toString() {
		return "particles = " + particles.size() + "\n" + "integrator = "
				+ "Implicit Euler" + "\n" + "stiffness = " + k.getValue()
				+ "\n" + "damping = " + b.getValue() + "\n" + "time = " + time;
	}

	public void setDefaultValues() {
		useg.setValue(true);
		g.setValue(9.8);
		k.setValue(100);
		b.setValue(0);
		friction.setValue(0.5);
		rc.setValue(0.6);
	}

	private Particle pgrabbed = null;
	private Vector2d grabpos = new Vector2d();
	private Vector2d grabpos0 = new Vector2d();

	public void grab(Particle p, Point2d pos) {
		if (pgrabbed == null) {
			pgrabbed = p;
			p.grabbed = true;
			grabpos0.set(pos);
		}

		grabpos.set(pos);
	}

	/**
     * 
     */
	public void ungrab() {

		if (pgrabbed == null)
			return;

		pgrabbed.grabbed = false;
		pgrabbed = null;
	}

	@Override
	public String getName() {
		return "Particle System";
	}

	public List<Spring> getSprings() {
		return springs;
	}

	/**
	 * @return the default spring stiffness constant.
	 */
	public double getK() {
		return k.getValue();
	}

	/**
	 * @return the default spring damping constant.
	 */
	public double getB() {
		return b.getValue();
	}

}
