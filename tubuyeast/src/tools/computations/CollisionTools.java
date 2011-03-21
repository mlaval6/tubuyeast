package tools.computations;

import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;

import simulation.Particle;

/**
 * Helper class for 2D particle-boundary collision and response.
 * @author epiuze
 *
 */
public class CollisionTools {

	/**
	 * @param a
	 * @param b
	 * @param c
	 * @return 2 times the signed triangle area. The result is positive if //
	  abc is ccw, negative if abc is cw, zero if abc is degenerate.
	 */
	public static double signed2DTriArea(Point2d a, Point2d b, Point2d c) {
		return (a.x - c.x) * (b.y - c.y) - (a.y - c.y) * (b.x - c.x);
	}
    
	/**
	 * @param A
	 * @param B
	 * @param C
	 * @return whether A,B,C are oriented clockwise
	 */
	public static boolean areClockwise(Point2d A, Point2d B, Point2d C) {
    	return signed2DTriArea(A, B, C) > 0;
    }

	/**
	 * @param A
	 * @param B
	 * @param C
	 * @param D
	 * @return whether the line segment AB is intersecting the line segment CD
	 */
	public static boolean areIntersecting(Point2d A, Point2d B, Point2d C, Point2d D) {
    	if (CollisionTools.areClockwise(A, C, D) == CollisionTools.areClockwise(B, C, D)) {
    		return false;
    	}
    	else if (CollisionTools.areClockwise(A, B, C) == CollisionTools.areClockwise(A, B, D)) {
    		return false;
    	}
    	else
    		return true;
	}

	/**
	 * @param particlePosition
	 * @param particleVelocity
	 * @param stepSize
	 * @param boundaryA
	 * @param boundaryB
	 * @return whether a particle will end up crossing a boundary if a step is taken in the direction of its velocity.
	 */
	public static boolean areIntersecting(Point2d particlePosition, Vector2d particleVelocity, double stepSize, Point2d boundaryA, Point2d boundaryB) {
		
    	// Line segment from particle position to particle + step * velocity
    	Point2d D = new Point2d();
    	D.scaleAdd(2 * stepSize, particleVelocity, particleVelocity);
    	
    	return areIntersecting(particlePosition, D, boundaryA, boundaryB);
	}

	/**
	 * Apply a simple delta impulse to this particle following the normal to the boundary.
	 * @param p
	 * @param boundaryA
	 * @param boundaryB
	 */
	public static void bounce(Particle p, Point2d boundaryA, Point2d boundaryB, double restitution_coef) {
    	Vector2d n = new Vector2d();
    	n.sub(boundaryB, boundaryA);

    	// Angle between the boundary and the particle velocity
		double theta = Math.acos(p.v.dot(n) / (n.length() * p.v.length()));

    	n.set(n.y, -n.x);
		n.normalize();

		if (n.dot(p.v) > 0) n.scale(-1);

    	// Make sure we don't end up increasing the particle velocity
//		if (n.dot(p.v) > 0) n.scale(-1);

		n.scale(restitution_coef * 2 * p.v.length() * Math.sin(theta));
		p.v.add(n);
	
	}
	
}
