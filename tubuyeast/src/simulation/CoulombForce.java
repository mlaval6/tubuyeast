package simulation;

import javax.vecmath.Vector2d;

public class CoulombForce {

	public static final double k = 8.987551787368e09;
	
	public static final double e = -1.602176487e-19;

	public static void apply(Particle p1, Particle p2) {
		Vector2d dp = new Vector2d();
		dp.sub(p2.p, p1.p);
		double r = dp.length();
		
		// FIXME: this is just a hack... real juxtaposition doesn't exist
		if (r == 0) return;
		
		dp.normalize();

		// FIXME: remove the leading constant when we have true values
		double F = 1e29*k * p1.q * e * p2.q * e / (r*r);
		p1.f.x -= F * dp.x; 
		p1.f.y -= F * dp.y; 
	}
}
