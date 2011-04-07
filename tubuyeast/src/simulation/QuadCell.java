package simulation;
import java.util.ArrayList;
import javax.vecmath.Point2d;

public class QuadCell {
	
	private Particle child;
	boolean leaf;
	private Point2d pll, pur, center;
	private QuadCell SW = null, SE = null, NE = null, NW = null, parent = null;
	
	/******************************************************
	 * c1 is lower left point, and c2 is upper right point
	 ******************************************************/
	public QuadCell(Point2d c1, Point2d c2){
		this.pll = c1;
		this.pur = c2;
		this.center = new Point2d( (c1.x + c2.x) / 2, (c1.y + c2.y) / 2);
		leaf = true;
	}
	
	
	/************************************************************************************
	 * Inserts a particle into the QuadTree and finds appropriate QuadCell it belongs to
	 ************************************************************************************/	
	public void insert(Particle p){
		//check if particle p is the same point as our child, if it is pring error
		if(this.child != null){
			if (this.child.p.x == p.p.x && this.child.p.y ==p.p.y){
				System.out.println("You have inserted the same point onto itself");
				return;
			}
		}
		
		// if this cell is a leaf with no children, p becomes its child
		if(leaf && child == null) this.child = p;
		else{
			if(leaf && child != null){	// leaf with a child
				this.leaf = false;
				InsertIntoBestQuadCell(this.child);
				this.child = null;
				InsertIntoBestQuadCell(p);
			}
			else{ //not a leaf
				InsertIntoBestQuadCell(p);
			}
		}
		
		
	}
	
	
	private void InsertIntoBestQuadCell(Particle p){
		// find best QuadCell to insert particle in
		
		//point goes to right QuadCells
		if(p.p.x >= this.center.x){	
			if(p.p.y >= this.center.y){
				if(this.SE != null) this.SE.insert(p);
				else{
					this.SE = new QuadCell(new Point2d(this.center.x, this.pll.y), new Point2d(this.pur.x, this.center.y));
					this.SE.parent = this;
					this.SE.insert(p);
				}
			}
			else{
				if(this.NE != null) this.NE.insert(p);
				else{
					this.NE = new QuadCell(this.center, this.pur);
					this.NE.parent = this;
					this.NE.insert(p);
				}
			}
		}
		else{	//point goes to left QuadCells
			if(p.p.y >= this.center.y){
				if(this.SW != null) this.SW.insert(p);
				else{
					this.SW = new QuadCell(this.pll, this.center);
					this.SW.parent = this;
					this.SW.insert(p);
				}
			}
			else{
				if(this.NW != null)	this.NW.insert(p);
				else{
					this.NW = new QuadCell(new Point2d(this.pll.x, this.center.y), new Point2d(this.center.x, this.pur.y));
					this.NW.parent = this;
					this.NW.insert(p);
				}
			}
		}
	}
	
	
	public QuadCell getBoundingQuad(double x, double y, double radius){
		
		if(this.leaf) return this;
		
		// check if one of QuadCell corners are inside circle region of particle, if true, retun parent, else dig deeper
		if( Math.pow(pur.x - x, 2) + Math.pow(pur.y - y, 2) < Math.pow(radius, 2)) return this.parent;	//testing upperRCorner
		if( Math.pow(pll.x - x, 2) + Math.pow(pll.y - y, 2) < Math.pow(radius, 2)) return this.parent;	//testing lowerLCorner
		if( Math.pow(pur.x - x, 2) + Math.pow(pll.y - y, 2) < Math.pow(radius, 2)) return this.parent;	//testing lowerRCorner
		if( Math.pow(pll.x - x, 2) + Math.pow(pur.y - y, 2) < Math.pow(radius, 2)) return this.parent;	//testing upperLCorner
		
		// we didn't return parent cell, and hence need to dig deeper
		if(x >= this.center.x){	
			if(y >= this.center.y){
				return this.SE.getBoundingQuad(x, y, radius);
			}
			else{
				return this.NE.getBoundingQuad(x, y, radius);
			}
		}
		else{	//point goes to left QuadCells
			if(y >= this.center.y){
				return this.SW.getBoundingQuad(x, y, radius);
			}
			else{
				return this.NW.getBoundingQuad(x, y, radius);
			}
		}
		
	}
	
	public void getChildrenParticles(ArrayList<Particle> particles){
		
		if( this.leaf){
			if (this.child != null) particles.add(child);
			return;
		}
		else{
			if(this.NE != null) this.NE.getChildrenParticles(particles);
			if(this.NW != null) this.NW.getChildrenParticles(particles);
			if(this.SW != null) this.SW.getChildrenParticles(particles);
			if(this.SE != null) this.SE.getChildrenParticles(particles);
		}
		
		return;
	}
	
	public void addChildParticle(Particle p){
		this.child = p;
	}
	
	public Particle getChildParticle(){
		return this.child;
	}
	
	public void setNECell(QuadCell cell){
		this.NE = cell;
	}
	
	public void setNWCell(QuadCell cell){
		this.NW = cell;
	}
	
	public void setSWCell(QuadCell cell){
		this.SW = cell;
	}
	
	public void setSECell(QuadCell cell){
		this.SE = cell;
	}

	public QuadCell getNECell(){
		return this.NE;
	}
	
	public QuadCell getNWCell(){
		return this.NW;
	}
	
	public QuadCell getSWCell(){
		return this.SW;
	}
	
	public QuadCell getSECell(){
		return this.SE;
	}
}
