/*
 * Created on 11-Sep-2003
 */
package tools.gl;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.media.opengl.glu.GLUquadric;

import com.sun.opengl.util.GLUT;

/**
 * Class for drawing reference frames with arrow geometry.
 * The axis is a grey ball, with red greed and blue arrows pointing 
 * in the x y and z directions.
 * @author kry
 */
public class FancyAxis {
    
	/** create our own GLUT... is there overhead for this? */
    private GLUT glut = new GLUT();
    /** create our own GLU... is there overhead for this? */
    private GLU glu = new GLU();
        
    double size;
    
    /**
     * Creates a new axis of size 1
     */
    public FancyAxis() {
        this( 1 );
    }
    
    /**
     * Creates a new axis of the desired size
     * @param size
     */
    public FancyAxis( double size ) {
        this.size = size;
    }
    
    /**
     * Sets the size of the axis 
     * @param size
     */
    public void setSize( double size ) {
        this.size = size;
    }
        
    /**
     * Draws the axis 
     * @param gl
     */
    public void draw( GL gl ) {
    	
    	gl.glPushMatrix();
    	gl.glScaled( size, size, size );
    	
            float [] ballCol = { 0.5f, 0.5f, 0.5f, 1 };            
            
            gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, ballCol, 0 );
            gl.glEnable( GL.GL_LIGHTING );
            glut.glutSolidSphere( 0.15, 20, 20 );
            
            gl.glPushMatrix();
            float [] xCol = { 1, 0, 0, 1 };
            gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, xCol, 0 );
            gl.glRotated( 90, 0, 1, 0 );
            drawArrow( gl );
            gl.glPopMatrix();
            
            gl.glPushMatrix();
            float [] yCol = { 0, 1, 0, 1 };
            gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, yCol, 0 );
            gl.glRotated( -90, 1, 0, 0 );
            drawArrow( gl );
            gl.glPopMatrix();
            
            gl.glPushMatrix();
            float [] zCol = { 0, 0, 1, 1 };
            gl.glMaterialfv( GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, zCol, 0 );
            drawArrow( gl );
            gl.glPopMatrix();
           
        gl.glPopMatrix();
    }
    
    /**
     * Draws an arrow (i.e., one axis)
     * @param gl
     */
    public void drawArrow( GL gl )
    {
        double r = 0.07;
        double h = 0.8;
        int nAround = 10;
        gl.glPushMatrix();
        
        GLUquadric cylinder = glu.gluNewQuadric();
        glu.gluQuadricNormals( cylinder, GLU.GLU_SMOOTH );
        glu.gluCylinder( cylinder, r, r, h, nAround, 1 );
        glu.gluDeleteQuadric( cylinder );
        
        gl.glTranslated( 0, 0, h );        
        glut.glutSolidCone( r * 2, 1 - h, nAround, 5 );
        
        GLUquadric disk = glu.gluNewQuadric();
        glu.gluQuadricOrientation( disk, GLU.GLU_INSIDE );
        glu.gluDisk( disk, 0, r * 2, nAround, 1 );
        glu.gluDeleteQuadric( disk );
        
        gl.glPopMatrix();
    }
}
