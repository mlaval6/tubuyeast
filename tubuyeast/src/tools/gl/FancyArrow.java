/*
 * Created on 10-Sep-2003
 */
package tools.gl;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;
import javax.vecmath.Color3f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Vector3d;

import com.sun.opengl.util.GLUT;

/**
 * @author kry
 */
public class FancyArrow {

	/** create our own GLUT... is there overhead for this? */
    private GLUT glut = new GLUT();
    /** create our own GLU... is there overhead for this? */
    private GLU glu = new GLU();
	
    private Point3d from;
    private Point3d to;
    private Vector3d dir;
    private Vector3d temp1;
    private Vector3d temp2;
    private Color3f color;
    double size;
    double length;
    private boolean fast;

    /**
     * transparency of the arrow, no transparency by default
     */
    public float transparency = 1;
    
    private static int dl = -1;
    
    final static double sqrt3div2 = Math.sqrt(3)/2;
    
    private final static int sides = 20;
    private final static int stacks = 10;
    private static double [] xpos = new double[sides]; 
    private static double [] ypos = new double[sides];

    static {
        for ( int i = 0; i < sides; i++ ) {
            xpos[i] = Math.cos( Math.PI * 2 * i / sides );
            ypos[i] = Math.sin( Math.PI * 2 * i / sides );
        } 
    }

    /**
     * Creates an ORANGE arrow from 0,0,0 to 0,0,1 with a girth of 0.1
     */
    public FancyArrow() {
        this( new Point3d(), new Point3d(0,0,1), Colour.orange, 0.1 );
    }

    /**
     * Create an arrow with the given parameters
     * @param from the starting point of the arrow
     * @param to   the end point (i.e., the tip of the arrow)
     * @param color the desired material colour
     * @param size the girth of the arrow
     */
    public FancyArrow( Tuple3d from, Tuple3d to, Color3f color, double size ) {
        this.from = new Point3d(from);
        this.to = new Point3d(to);
        this.size = size;
        this.color = color;
        temp1 = new Vector3d();
        temp2 = new Vector3d();
        dir = new Vector3d();
        computeArrowInfo();
        fast = false;
    }
    
    /**
     * Draws the arrow
     * @param gl
     */
    public void draw( GL gl ) {
        if( fast ) {
            drawCoarse( gl );
            return;
        }
        gl.glPushAttrib( GL.GL_ENABLE_BIT | GL.GL_LIGHTING_BIT );
        gl.glEnable( GL.GL_LIGHTING );
        gl.glPushMatrix();
        // translate from to origin
        gl.glTranslated( from.x, from.y, from.z );
        // and rotate the direction axis onto the z axis
        temp1.set( 0, 0, 1 );
        temp2.cross( temp1, dir );
        double angle = Math.acos( temp1.dot( dir ) / length ) * 180 / Math.PI;
        gl.glRotated( angle, temp2.x, temp2.y, temp2.z );
        float [] col = { color.x, color.y, color.z, transparency };        
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, col, 0 );

        // draw the shaft        
        gl.glBegin( GL.GL_QUAD_STRIP );
        for ( int i = 0; i <= sides; i++ ) {
            gl.glNormal3d( xpos[i%sides], ypos[i%sides], 0 );
            gl.glVertex3d( xpos[i%sides]*size/2, ypos[i%sides]*size/2, length - size * 2 );
            gl.glVertex3d( xpos[i%sides]*size/2, ypos[i%sides]*size/2, 0 );            
        }        
        gl.glEnd();
        // draw the bottom of the shaft
        gl.glBegin( GL.GL_POLYGON );
        gl.glNormal3d( 0,0,-1 );
        for ( int i = sides-1; i >= 0; i-- ) {
            gl.glVertex3d( xpos[i]*size/2, ypos[i]*size/2, 0 );
        }
        gl.glEnd();
        // draw the bottom of the cone
        gl.glBegin( GL.GL_POLYGON );
        gl.glNormal3d( 0,0,-1 );
        for ( int i = sides-1; i >= 0; i-- ) {
            gl.glVertex3d( xpos[i]*size, ypos[i]*size, length - size*2 );
        }
        gl.glEnd();
        // draw the top of the cone
        //gl.glPolygonMode( gl.GL_FRONT_AND_BACK, gl.GL_LINE );
        for ( int stack = 0; stack < stacks; stack++ ) {
            double a1 = (double) stack/ (double) stacks;
            double a2 = (double) (stack + 1) / (double) stacks;
            gl.glBegin( GL.GL_QUADS );                      
            for ( int i = 0; i < sides; i++ ) {
                gl.glNormal3d( sqrt3div2 * xpos[i%sides], sqrt3div2 * ypos[i%sides], 0.5 );
                gl.glVertex3d( xpos[i%sides]*size*a1, ypos[i%sides]*size*a1, length - size*2*a1 );
                gl.glVertex3d( xpos[i%sides]*size*a2, ypos[i%sides]*size*a2, length - size*2*a2 );                
                gl.glNormal3d( sqrt3div2 * xpos[(i+1)%sides], sqrt3div2 * ypos[(i+1)%sides], 0.5 );
                gl.glVertex3d( xpos[(i+1)%sides]*size*a2, ypos[(i+1)%sides]*size*a2, length - size*2*a2 );
                gl.glVertex3d( xpos[(i+1)%sides]*size*a1, ypos[(i+1)%sides]*size*a1, length - size*2*a1 );
            }
            gl.glEnd();
        }
        //gl.glPolygonMode( gl.GL_FRONT_AND_BACK, gl.GL_FILL );
        gl.glPopMatrix();
        gl.glDisable( GL.GL_LIGHTING );
        gl.glPopAttrib();
    }
        
    /**
     * Draws a coarse version of the arrow (i.e., faster if drawing many)
     * @param gl
     */
    public void drawCoarse( GL gl ) {
    	
        gl.glPushAttrib( GL.GL_ENABLE_BIT | GL.GL_LIGHTING_BIT );
        gl.glEnable( GL.GL_LIGHTING );
        gl.glPushMatrix();
        // translate from to origin
        gl.glTranslated( from.x, from.y, from.z );
        // and rotate the direction axis onto the z axis
        temp1.set( 0, 0, 1 );
        temp2.cross( temp1, dir );
        double angle = Math.acos( temp1.dot( dir ) / length ) * 180 / Math.PI;
        gl.glRotated( angle, temp2.x, temp2.y, temp2.z );
        gl.glScaled( 10 * size, 10 * size, 10 * size );
        float [] col = { color.x, color.y, color.z, transparency };        
        gl.glMaterialfv( GL.GL_FRONT, GL.GL_DIFFUSE, col, 0 );
        
        if( dl < 0 ) {
            dl = gl.glGenLists( 1 );
            gl.glNewList( dl, GL.GL_COMPILE_AND_EXECUTE );

            double r = 0.05;
            double h = 0.8;
            int nAround = 10;
            gl.glPushMatrix();
            
            
            
            GLUquadric cylinder = glu.gluNewQuadric();
            glu.gluQuadricNormals( cylinder, GLU.GLU_SMOOTH );
            glu.gluCylinder( cylinder, r, r, h, nAround, 1 );
            glu.gluDeleteQuadric( cylinder );
            
            GLUquadric disk = glu.gluNewQuadric();
            glu.gluQuadricOrientation( disk, GLU.GLU_INSIDE );
            glu.gluDisk( disk, 0, r, nAround, 1 );
            
            gl.glTranslated( 0, 0, h );
            glut.glutSolidCone( r * 2, 1 - h, nAround, 5 );
            
            glu.gluDisk( disk, 0, r * 2, nAround, 1 );
            glu.gluDeleteQuadric( disk );
            
            gl.glPopMatrix();
            
            gl.glEndList();
        } else {
            gl.glCallList( dl );
        }

        gl.glPopMatrix();
        gl.glDisable( GL.GL_LIGHTING );
        gl.glPopAttrib();
    }
    
    private void computeArrowInfo() {
        dir.sub( to, from );
        length = dir.length();
    }
            
    /**
     * @param color3f
     */
    public void setColor(Color3f color3f) {
        color = color3f;
    }
    
    /**
     * Get the current arrow colour
     * @return the current colour
     */
    public Color3f getColor() {
        return color;
    }
    
    /**
     * Sets the colour of the arrow 
     * @param r
     * @param g
     * @param b
     */
    public void setColor(float r, float g, float b) {
        // was overwriting the old colour!
        color = new Color3f(r,g,b);        
    }

    /**
     * @param point3d
     */
    public void setFrom(Tuple3d point3d) {
        setFrom(point3d.x, point3d.y, point3d.z);
    }
    
    /**
     * Sets the position of the arrow's tail.
     * @param x
     * @param y
     * @param z
     */
    public void setFrom(double x, double y, double z) {
        from.set(x, y, z);
        computeArrowInfo();
    }

    /**
     * Sets the position of the arrow's tail.
     * @param p
     */
    public void setFrom( Tuple3f p ) {
    	setFrom(p.x, p.y, p.z);
    }
    
    /**
     * Get the point from which the arrow is pointing
     * @return the from point
     */
    public Point3d getFrom() {
        return from;
    }

    /**
     * @param d
     */
    public void setSize(double d) {
        size = d;
    }

    /**
     * @param point3d
     */
    public void setTo(Point3d point3d) {
        setTo(point3d.x, point3d.y, point3d.z);
    }
    
    /** 
     * Sets the position of the arrow's head
     * @param p
     */
    public void setTo( Point3f p ) {
    	setTo( p.x, p.y, p.z );
    }
    
    /**
     * Sets the position of the arrow's head
     * @param p
     */
    public void setTo( Tuple3d p ) {
    	setTo( p.x, p.y, p.z );
    }
    
    /**
     * Sets the position of the arrow's head
     * @param p
     */
    public void setTo( double []p ) {
    	setTo( p[0], p[1], p[2] );
    }
    
    /**
     * Sets the position of the arrow's head
     * @param p
     */
    public void setTo( float []p ) {
    	setTo( p[0], p[1], p[2] );
    }
    
    /**
     * Sets the position of the arrow's head
     * @param x
     * @param y
     * @param z
     */
    public void setTo(double x, double y, double z) {
        to.set(x, y, z);
        computeArrowInfo();
    }
    
    /**
     * Gets the position of the arrow's head
     * @return the position of the arrow's head
     */
    public Point3d getTo() {
        return to;
    }
    
    /**
     * Gets the length of the arrow
     * @return the length of the arrow
     */
    public double getLength() {
        return length;
    }
    
    /**
     * Requests that this arrow be drawn with simplified geometry to improve speed
     * @param coarse
     */
    public void setFast( boolean coarse ) {
        this.fast = coarse;
    }
    
    /** 
     * @return true if this arrow is being drawn with simplified geometry to improve speed
     */
    public boolean isFast() {
        return fast;
    }

}
