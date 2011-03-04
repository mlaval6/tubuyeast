package tools.gl;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.JPanel;
import javax.swing.border.TitledBorder;
import javax.vecmath.Point3f;

import tools.parameters.FloatParameter;
import tools.swing.VerticalFlowPanel;

public class LightRoom {
	private BoxRoom room;
	
	private FloatParameter sint = new FloatParameter("Spot light intensity", 1.0f, 0.0f, 1.0f);
	private FloatParameter aint = new FloatParameter("Ambient light intensity", 0.1f, 0.0f, 1.0f);
	private FloatParameter dist = new FloatParameter("Distance", 1.8f, 0.0f, 5.0f);
	private float s = 1.0f;
	
	public LightRoom(double size) {
		room = new BoxRoom(size);
		s = ((float)size) / 15.0f;
	}
	
	public LightRoom() {
		room = new BoxRoom();
	}
	
	public void display(GLAutoDrawable drawable) {
		GL gl = drawable.getGL();
		gl.glDisable(GL.GL_LIGHTING);
		
		room.display(drawable);
		
		float sc = s * dist.getValue();
		
		Point3f p1 = new Point3f(0, 13, 15);
		p1.scale(sc);
		Point3f c1 = new Point3f(1, 1, 1);
		c1.scale(sint.getValue());
		Point3f ac1 = new Point3f(1f, 1f, 1f);
		ac1.scale(aint.getValue());

		Point3f p2 = new Point3f(0, 11, -15);
		p2.scale(sc);
		Point3f c2 = new Point3f(1,1,1);
		c2.scale(0.2f);
		Point3f ac2 = new Point3f(0f, 0f, 0f);

		// main light is at the top front of the room.
		int lightNumber = 1;
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_SPECULAR, new float[] {
				c1.x, c1.y, c1.z, 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_DIFFUSE, new float[] {
				c1.x, c1.y, c1.z, 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_AMBIENT, new float[] {
				ac1.x, ac1.y, ac1.z, 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_POSITION, new float[] {
				p1.x, p1.y, p1.z, 1 }, 0);
		gl.glEnable(GL.GL_LIGHT0 + lightNumber);
		// put a dim light at the back of the room, in case anyone wants to
		// look at the back side of objects
		lightNumber++;
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_SPECULAR, new float[] {
				c2.x, c2.y, c2.z, 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_DIFFUSE, new float[] {
				c2.x, c2.y, c2.z, 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_AMBIENT, new float[] {
				ac2.x, ac2.y, ac2.z, 1 }, 0);
		gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_POSITION, new float[] {
				p2.x, p2.y, p2.z, 1 }, 0);
		gl.glEnable(GL.GL_LIGHT0 + lightNumber);

	}
	
	public JPanel getControls() {
		VerticalFlowPanel vfp = new VerticalFlowPanel();
		vfp.setBorder(new TitledBorder("light room"));
		vfp.add(sint.getSliderControls(false));
		vfp.add(aint.getSliderControls(false));
		vfp.add(dist.getSliderControls(false));
		vfp.add(room.getControls());
		
		return vfp.getPanel();
	}
	/**
	 * @param f
	 */
	public void setAmbient(float f) {
		aint.setValue(f);
	}
}
