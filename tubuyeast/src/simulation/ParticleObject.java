package simulation;

import java.util.List;

import javax.media.opengl.GLAutoDrawable;

public interface ParticleObject {

	public abstract void display(GLAutoDrawable drawable);
	
	public List<Particle> getParticles();
	
}
