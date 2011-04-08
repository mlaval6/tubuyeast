package simulation;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import no.uib.cipr.matrix.Matrix;

/**
 * @author epiuze
 */
public class AngularSpring implements Spring {

	private double theta0;
	
	private double k;
	
	/**
	 * The angular spring is applied on the angle formed
	 * by the lines p2-p1 and p3-p2.
	 */
	private Particle p1, p2, p3;
	
	/**
	 * Create an angular spring attached to these two linear springs. Assume
	 * the two springs are connected and form a chain consisting of three particles: p1, p2, p3
	 * @param s1
	 * @param s2
	 */
	public AngularSpring(LinearSpring s1, LinearSpring s2, double k) {
		this(s1.p1, s1.p2, s2.p2, k);
	}
	
	/**
	 * Create an angular spring on the angle formed
	 * by the lines p2-p1 and p3-p2.
	 * @param p1
	 * @param p2
	 * @param p3
	 */
	public AngularSpring(Particle p1, Particle p2, Particle p3, double k) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
		this.k = k;
	}
	
	/**
	 * The angle formed by the lines p2-p1 and p3-p2.
	 */
    private double computeAngle() {
		// Represent the two springs as vectors
		Vector2d v1 = new Vector2d();
		v1.sub(p2.p, p1.p);
		
		Vector2d v2 = new Vector2d();
		v2.sub(p3.p, p2.p);
		
		// Find the angle between them
		return Math.acos(v1.dot(v2) / (v1.length() * v2.length()));
    }
    
    private Vector2d f = new Vector2d();
	public void apply() {
		
		double theta = computeAngle();
		
		// Force is applied perpendicularly to p1-p2
		f.set(computeV1());
		f.set(-f.y, f.x);
		f.scale(k * (theta - theta0));
		p1.f.add(f);
		
//		f.set(computeV2());
//		f.set(-f.y, f.x);
//		f.scale(-k * (theta - theta0));
//		p3.f.add(f);
	}
	
	private Vector2d v1 = new Vector2d();
	private Vector2d v2 = new Vector2d();
	private Point2d mp1 = new Point2d();
	private Point2d mp2 = new Point2d();
	
	private Vector2d computeV1() {
		v1.sub(p1.p, p2.p);
		return v1;
	}

	private Vector2d computeV2() {
		v2.sub(p3.p, p2.p);
		return v2;
	}
	
	private Point2d computeMidV1() {
		Vector2d v1 = computeV1();
		mp1.scaleAdd(0.5, v1, p2.p);
		return mp1;
	}
	
	private Point2d computeMidV2() {
		Vector2d v2 = computeV2();
		mp2.scaleAdd(0.5, v2, p2.p);
		return mp2;
	}

	@Override
	public void display(GLAutoDrawable drawable) {
    	GL gl = drawable.getGL();
    	
		Point2d mp1 = computeMidV1();
		Point2d mp2 = computeMidV2();

		gl.glBegin(GL.GL_POINTS);
		gl.glColor4d(1, 0, 0, 1);
		gl.glVertex2d(mp1.x, mp1.y);
		gl.glColor4d(0, 0, 1, 1);
		gl.glVertex2d(mp2.x, mp2.y);
		gl.glEnd();
		
		// Distance from midpoints to center
		double r1 = computeV1().length() / 2;
		double r2 = computeV2().length() / 2;
		
		Vector2d v1 = computeV1();
		Vector2d v2 = computeV2();
		
		double theta1 = Math.atan2(v1.y, v1.x);
		double theta2 = Math.atan2(v2.y, v2.x);
		
		// Remap
		theta1 = theta1 < 0 ? 2 * Math.PI + theta1 : theta1;
		theta2 = theta2 < 0 ? 2 * Math.PI + theta2 : theta2;
		
		// Go from smallest to largest
		double theta1p = theta1;
		double r1p = r1;
		if (theta1 > theta2) {
			theta1 = theta2;
			theta2 = theta1p;
			r1 = r2;
			r2 = r1p;
		}
		
		double thetac = computeAngle();
		
		int n = 100;
		
		// Find absolute difference
		double diff = theta2 - theta1;
		
		double dtheta = 0;
		if (Math.abs(diff) > Math.PI) {
			gl.glColor4d(1, 0, 0, 0.9);
			dtheta = -1 * (theta1 + 2 * Math.PI - theta2) / n;
			diff = 2 * Math.PI - diff;
		}
		else {
			gl.glColor4d(0, 0, 1, 0.9);
			dtheta = (theta2 - theta1) / n;
		}
		
    	// Draw as an angle
		gl.glLineWidth(2);
		gl.glBegin(GL.GL_LINE_STRIP);
		Point2d p = new Point2d();
		for (int i = 0; i <= n; i++) {
			double theta = theta1 + i * dtheta;

			// Simple linear interpolation
			double lambda = Math.abs(theta - theta1) / (diff);

			double lambda0 = (thetac - theta0) / (2*Math.PI);
			gl.glColor4d(Math.pow(lambda0+0.5, 1), 0, Math.pow(1-lambda0-0.1, 2), 0.7);

			double r = lambda * r2 + (1 - lambda) * r1;
			p.x = p2.p.x + r * Math.cos(theta);
			p.y = p2.p.y + r * Math.sin(theta);
			
			gl.glVertex2d(p.x, p.y);
		}
		gl.glEnd();
    	
	}

	@Override
	public void gradient(Matrix K, Matrix B) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean intersect(Particle p, double stepSize) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setB(double value) {
		// TODO
	}

	@Override
	public void setK(double value) {
		k = value;
	}

	@Override
	public double getB() {
		return 0;
	}

	@Override
	public double getK() {
		return k;
	}
	
}
