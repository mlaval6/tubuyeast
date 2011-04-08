package simulation;

import javax.vecmath.Vector2d;

public class MotorParticle extends Particle {

    public MotorParticle(double x, double y, double vx, double vy) {
		super(x, y, vx, vy);
	}
    
    public void apply(double scale) {
    	Vector2d fp = new Vector2d(0, -scale);
//		this.f.set(fp);
		addForce(fp);
    }
    
	public Vector2d direction;
    
    

}
