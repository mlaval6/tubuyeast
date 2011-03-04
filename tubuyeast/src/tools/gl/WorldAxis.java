package tools.gl;

import javax.media.opengl.GL;
import javax.vecmath.Matrix4d;

import com.sun.opengl.util.GLUT;

public class WorldAxis {

    public static void display(GL gl, float scale, boolean drawScale) {
        gl.glDisable(GL.GL_LIGHTING);
//        gl.glEnable(GL.GL_LINE_SMOOTH);

        float[] aoffset = { 0f, 0f, 0f};
        float[] offset = { 0.03f, 0.06f, 0f};
        gl.glPushMatrix();
        gl.glScalef(scale, scale, scale);
        gl.glTranslatef(aoffset[0], aoffset[1], aoffset[2]);
        
        gl.glLineWidth(1f);
        gl.glBegin(GL.GL_LINES);
        
        // x
        gl.glColor3f(1, 0, 0);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(1, 0, 0);
        
        // y
        gl.glColor3f(0, 1, 0);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(0, 1, 0);

        // z
        gl.glColor3f(0, 0, 1);
        gl.glVertex3d(0, 0, 0);
        gl.glVertex3d(0, 0, 1);

        gl.glEnd();
        
        gl.glPopMatrix();

        gl.glColor3f(1, 0, 0);
//        float shift = (float) (0.01*Math.exp(scale/200));
        float shift = 0.8f * scale;
        gl.glRasterPos3f(offset[0] + shift, offset[1] + 0, offset[2] + 0);
        ViewerHelper.glut.glutBitmapString(GLUT.BITMAP_9_BY_15, drawScale ? "x = " + scale : "x");

        gl.glColor3f(0, 1, 0);
        gl.glRasterPos3f(offset[0] + 0, offset[1] + shift, offset[2] + 0);
        ViewerHelper.glut.glutBitmapString(GLUT.BITMAP_9_BY_15, drawScale ? "y = " + scale : "y");

        gl.glColor3f(0, 0, 1);
        gl.glRasterPos3f(offset[0] + 0, offset[1] + 0, offset[2] + shift);
        ViewerHelper.glut.glutBitmapString(GLUT.BITMAP_9_BY_15, drawScale ? "z = " + scale : "z");
        
        gl.glEnable(GL.GL_LIGHTING);

    }

    public static void display(GL gl) {
        display(gl, 1.0f, false);
    }

    public static void display(GL gl, Matrix4d localFrame, float scale) {
        gl.glPushMatrix();
        FlatMatrix4d fm = new FlatMatrix4d(localFrame);
        gl.glMultMatrixd(fm.asArray(), 0);
        display(gl, scale, false);
        gl.glPopMatrix();
    }

	public static void display2D(GL gl) {
		display(gl, 1, true);
	}
	
	public static void display2D(GL gl, float scale, boolean drawScale) {
      float[] offset = { 0.03f, 0.03f};
      gl.glPushMatrix();
      gl.glScalef(scale, scale, scale);
      
      gl.glTranslatef(100, 100, 0);

      gl.glLineWidth(1f);
      gl.glBegin(GL.GL_LINES);
      
      // x
      gl.glColor3d(1, 0, 0);
      gl.glVertex2d(0, 0);
      gl.glVertex2d(1, 0);
      
      // y
      gl.glColor3d(0, 1, 0);
      gl.glVertex2d(0, 0);
      gl.glVertex2d(0, 1);

      gl.glEnd();
      
      gl.glPopMatrix();

      gl.glColor3f(1, 0, 0);
      float shift = 0.8f * scale;
      gl.glRasterPos3f(offset[0] + shift, offset[1] + 0, offset[2] + 0);
      ViewerHelper.glut.glutBitmapString(GLUT.BITMAP_9_BY_15, drawScale ? "x = " + scale : "x");

      gl.glColor3f(0, 1, 0);
      gl.glRasterPos3f(offset[0] + 0, offset[1] + shift, offset[2] + 0);
      ViewerHelper.glut.glutBitmapString(GLUT.BITMAP_9_BY_15, drawScale ? "y = " + scale : "y");

	}

}
