package simulation;
import java.util.ArrayList;

import javax.media.opengl.GLAutoDrawable;
import javax.vecmath.Point2d;

public class QuadTree {

	private ArrayList<Particle> particles;
	private QuadCell root;
	
	/***************************************************
	 * Instantiates a QuadTree given a set of particles
	 ***************************************************/
	public QuadTree(ArrayList<Particle> po, Point2d upperRCorner, Point2d lowerLCorner){
		particles = po;
		this.BuildQuadTree( upperRCorner, lowerLCorner); // I found that simulation lower left corner = (9, 747) and upper right corner = (792, 10)
	}

	/*******************************************************************
	 * Splits space into QuadCells and assigns each particle to a cell
	 *******************************************************************/
	private void BuildQuadTree(Point2d upperRC, Point2d lowerLC){
		root = new QuadCell(upperRC, lowerLC);  
		
		// Fill tree with Particles
		for(Particle p: this.particles){
			root.insert(p);
		}
	}
	
	/*******************************************************************
	 * Returns the Particles that are a radius r away from p
	 *******************************************************************/
	public ArrayList<Particle> getParticles(Particle p, double r){
		
		// Find the Quad that has all the Particles a radius r from p
		QuadCell boundary = root.getBoundingQuad(p.p.x, p.p.y, r);
		
		// Get all particles inside bounding QuadCell
		ArrayList<Particle> allParticles = new ArrayList<Particle>();
		boundary.getChildrenParticles(allParticles);

		return allParticles;
	}
	
	public void printBoxes(GLAutoDrawable drawable){
		root.printBoxes(drawable);
	}
	
	
	
}

