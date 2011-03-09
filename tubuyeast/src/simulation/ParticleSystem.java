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

import numerical.BoxCollider;
import numerical.ForwardEuler;
import numerical.ImplicitEuler;
import numerical.Integrator;
import numerical.RungeKutta;
import numerical.VelocityIndependentVerlet;
import numerical.VelocityVerlet;
import tools.gl.SceneGraphNode;
import tools.parameters.DoubleParameter;
import tools.parameters.IntParameter;
import tools.parameters.Parameter;
import tools.parameters.ParameterListener;
import tools.swing.ListComboBox;
import tools.swing.VerticalFlowPanel;

/**
 * Implementation of a particle system. Manages system integration calls and
 * particle grabbing.
 * 
 * @author epiuze
 */
public class ParticleSystem implements SceneGraphNode {

	private List<Integrator> integrationMethods;

	private ArrayList<Boundary> wall = new ArrayList<Boundary>();

	/**
	 * List of particles in the system.
	 */
	public ArrayList<Particle> particles = new ArrayList<Particle>();

	private List<Spring> springs = new LinkedList<Spring>();

	private Dimension wsize;

	public static DoubleParameter g = new DoubleParameter("gravity", 9.8, -100,
			100);

	private DoubleParameter k = new DoubleParameter("stiffness", 100, 0.001,
			100000);

	private DoubleParameter b = new DoubleParameter("damping", 0.3, 0, 100);

	private IntParameter numIterations = new IntParameter("iterations", 5, 1,
			1000);

	public static DoubleParameter friction = new DoubleParameter(
			"floor friction", 0.3, 0, 2);

	private DoubleParameter rc = new DoubleParameter(
			"coefficient of restitution", 0.6, 0, 1);

	private ListComboBox<Integrator> integrationMethodsComboBox;

	/**
	 * Create an empty particle system
	 * 
	 * @param bsize
	 *            the dimension of the box in which this system lives.
	 * @param be
	 */
	public ParticleSystem(Dimension bsize) {
		wsize = new Dimension(bsize);

		integrationMethods = new ArrayList<Integrator>();
		integrationMethods.add(new ImplicitEuler());
		integrationMethods.add(new ForwardEuler());
		integrationMethods.add(new RungeKutta());
		integrationMethods.add(new VelocityIndependentVerlet());
		integrationMethods.add(new VelocityVerlet());

		integrationMethodsComboBox = new ListComboBox<Integrator>(
				integrationMethods);
		
		int method = 3;
		integrationMethodsComboBox.setSelected(method);
		integrationMethod = integrationMethods.get(method);

	}

	/**
	 * Updates the system.
	 * 
	 * @param h
	 */
	public void step(double h) {

		// Integrates the system
		updateForces();

		integrationMethod.step(time, h, numIterations.getValue());

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

		for (Integrator i : integrationMethods) {
			i.initialize(this);
		}
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
	public Integrator integrationMethod = null;

	/**
	 * Updates the current forces
	 */
	public void updateForces() {

		// Initialize forces to the the gravity force (mg)
		Vector2d fg = g.isChecked() ? new Vector2d(0, g.getValue())
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

			// System.out.println(p.index + ": " + p.f);
		}

		// If a particle is grabbed, use soft pulling
		ParticleSimulationInteractor.softPull(pgrabbed, grabpos0, grabpos);

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

		vfp.add(integrationMethodsComboBox.getControls());
		integrationMethodsComboBox
				.addParameterListener(new ParameterListener() {
					@Override
					public void parameterChanged(Parameter parameter) {
						integrationMethod = (Integrator) integrationMethodsComboBox.getSelected();
					}
				});

		vfp.add(numIterations.getSliderControls());
		vfp.add(g.getSliderControlsExtended("use"));
		vfp.add(k.getSliderControls(true));
		vfp.add(b.getSliderControls(false));

		// Update spring constant when necessary
		ParameterListener l = new ParameterListener() {
			@Override
			public void parameterChanged(Parameter parameter) {
				for (Spring spring : springs) {
					spring.setK(k.getValue());
					spring.setB(b.getValue());
				}
			}
		};
		k.addParameterListener(l);
		b.addParameterListener(l);

		vfp.add(friction.getSliderControls(false));
		vfp.add(rc.getSliderControls(false));

		return vfp.getPanel();
	}

	public String toString() {
		return "particles = " + particles.size() + "\n" + "integrator = "
				+ integrationMethod.toString() + "\n" + "stiffness = " + k.getValue()
				+ "\n" + "damping = " + b.getValue() + "\n" + "time = " + time;
	}

	private Particle pgrabbed = null;
	private Point2d grabpos = new Point2d();
	private Point2d grabpos0 = new Point2d();

	public void grab(Particle p, Point2d pos) {
		if (pgrabbed == null) {
			pgrabbed = p;
			p.grabbed = true;
			grabpos0.set(pos);
		}

		grabpos.set(pos);
	}

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
