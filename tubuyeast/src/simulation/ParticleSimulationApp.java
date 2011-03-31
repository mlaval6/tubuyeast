package simulation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.vecmath.Point2d;
import javax.vecmath.Vector2d;
import tools.computations.FPSTimer;
import tools.gl.SceneGraphNode;
import tools.gl.Interactor;
import tools.gl.OpenglViewer;
import tools.parameters.BooleanParameter;
import tools.parameters.DoubleParameter;
import tools.parameters.IntParameter;
import tools.swing.VerticalFlowPanel;


/**
 * Sample code for particle simulation.
 * @author epiuze
 */
public class ParticleSimulationApp implements SceneGraphNode, Interactor  {

    private OpenglViewer ev;
    
    private ParticleSystem system;
    
    /** 
     * boolean to signal that the system was stepped and that a 
     * frame should be recorded if recording is enabled
     */
    private boolean stepRequested = false;
        
    private BooleanParameter run = new BooleanParameter( "run", false );
    
    private DoubleParameter stepsize = new DoubleParameter( "step size", 0.01, 1e-5, 1 );
    
    private IntParameter substeps = new IntParameter( "sub steps (integer)", 10, 1, 100);
    
    private static Dimension winsize = new Dimension(800, 600);

    private ParticleSimulationInteractor interactor;

    private FPSTimer fpsTimer = new FPSTimer();

    /**
     * Entry point for application
     * @param args
     */
    public static void main(String[] args) {
        new ParticleSimulationApp();        
    }
        
    /**
     * Creates the application / scene instance
     */
    public ParticleSimulationApp() {
        system = new ParticleSystem(winsize);
        createSystem(system, 4);

        // Add an interactor to manage mouse and keyboard controls
        interactor = new ParticleSimulationInteractor(system);
        
        ev = new OpenglViewer("Tubuyeast", this, new Dimension(winsize), new Dimension(650, winsize.height + 90) );

        ev.addInteractor(interactor);
        ev.addInteractor(this);
        
        ev.start();
    }
     
    /**
     * Set some initial gl parameters.
     */
    public void init(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glEnable( GL.GL_BLEND );
        gl.glBlendFunc( GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
        gl.glEnable( GL.GL_LINE_SMOOTH );
        gl.glEnable( GL.GL_POINT_SMOOTH );
        system.init(drawable);
    }
        
    public void display(GLAutoDrawable drawable) {
    	// Begin 2D drawing
        OpenglViewer.beginOverlay(drawable);

    	// Advance the simulation
    	simulationStep();
        
        // Set the GL context
        GL gl = drawable.getGL();
        gl.glDisable(GL.GL_LIGHTING);

        // Display particle system
        system.display( drawable );

        // Display simulation info
        displaySimulationInfo(drawable);
        
        interactor.display(drawable);
        
        // Done with 2D drawing
        OpenglViewer.endOverlay(drawable);    
    }

    /**
     * Display simulation info as an overlay in the OpenGL window.
     * @param drawable
     */
    private void displaySimulationInfo(GLAutoDrawable drawable) {
        fpsTimer.tick();
        String text = system.toString() + "\n" + 
                      "h = " + stepsize.getValue() + "\n" +
                      "substeps = " + (int) substeps.getValue() + "\n" + fpsTimer.toString();        
        OpenglViewer.printTextLines( drawable, text );
	}

	private void simulationStep() {
        // Advance the simulation by a number of substeps
    	// if it is running or wants to be stepped
        if ( isRunning() || stepRequested ) {   
            for ( int i = 0; i < substeps.getValue(); i++ ) {
                system.step( stepsize.getValue());                
            }
            stepRequested = false;        
        }
	}

	public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();

        vfp.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Simulation"));

        JPanel cpanel = new JPanel(new GridLayout(2, 3));
        cpanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Test systems"));
        
        for (int i = 1; i <= 5; i++) {
        	final int si = i;
            JButton cb = new JButton("test " + si);
            cpanel.add( cb );
            cb.addActionListener( new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    createSystem(system, si);
                }
            });
        }
        
        vfp.add(cpanel);
        
        vfp.add( run.getControls() );        
        vfp.add( stepsize.getSliderControls(true) );
        vfp.add( substeps.getSliderControls() );
        vfp.add( system.getControls() );
        
        vfp.add(interactor.getControls());
        return vfp.getPanel();
    }
    
    public void attach(Component component) {
        component.addKeyListener( new KeyAdapter() {
            
            public void keyPressed(KeyEvent e) {

                if ( e.getKeyCode() == KeyEvent.VK_SPACE ) {
                	setRunning(!isRunning());

                    // Initialize the particle system
                    system.updateSystem();

                } else if ( e.getKeyCode() == KeyEvent.VK_S ) {                    
                    stepRequested = true;
                } else if ( e.getKeyCode() == KeyEvent.VK_R ) {
                	setRunning(!isRunning());
                	
                    system.resetParticles();                  
                } else if ( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                    // quit the program
                    ev.stop();
                }
                if ( e.getKeyCode() != KeyEvent.VK_ESCAPE ) ev.redisplay();
            }
        } );
    }
    
	/**
	 * Creates test systems.
	 * @param system
	 * @param which
	 */
	private static void createSystem(ParticleSystem system, int which) {
		List<Particle> particles = system.getParticles();
		List<Spring> springs = system.getSprings();
		double k = system.getK();
		double b = system.getB();
		
		Particle p1, p2, p3, p4;
		int N;
			
		switch (which) {

		
		
		
		case 1: // Microtubules
			
			//  Rousseau's Code
			
							
			break;
		case 6: // structural beam
			Point2d p = new Point2d(100, 200);
			Vector2d d = new Vector2d(20, 0);
			p1 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
			particles.add(p1);
			p2 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
			particles.add(p2);
			springs.add(new Spring(p1, p2, k, b));
			p1.pinned = true;
			p2.pinned = true;
			p.add(d);
			p.add(d);
			N = 10;
			for (int i = 1; i < N; i++) {
				p3 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
				p4 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
				particles.add(p3);
				particles.add(p4);
				springs.add(new Spring(p3, p1, k, b));
				springs.add(new Spring(p3, p2, k, b));
				springs.add(new Spring(p4, p1, k, b));
				springs.add(new Spring(p4, p2, k, b));
				springs.add(new Spring(p4, p3, k, b));
				p1 = p3;
				p2 = p4;

				p.add(d);
				p.add(d);
			}
			break;
			case 2: // curved structural beam
				p = new Point2d(100, 100);
				d = new Vector2d(20, 0);
				p1 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
				particles.add(p1);
				p2 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
				particles.add(p2);
				springs.add(new Spring(p1, p2, k, b));
				p1.pinned = true;
				p2.pinned = true;
				p.add(d);
				p.add(d);
				N = 10;
				for (int i = 1; i < N; i++) {
					 d.set( 20*Math.cos(i*Math.PI/N), 20*Math.sin(i*Math.PI/N) );
					p3 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
					p4 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
					particles.add(p3);
					particles.add(p4);
					springs.add(new Spring(p3, p1, k, b));
					springs.add(new Spring(p3, p2, k, b));
					springs.add(new Spring(p4, p1, k, b));
					springs.add(new Spring(p4, p2, k, b));
					springs.add(new Spring(p4, p3, k, b));
					p1 = p3;
					p2 = p4;

					p.add(d);
					p.add(d);
				}
				break;
			case 3: // COMPLETE CELL
				int yTranspose = 190;
				List<Particle> bps = new LinkedList<Particle>();
				List<Particle> OuterRingBps = new LinkedList<Particle>();
				List<Particle> innerMembrane = new LinkedList<Particle>();
				List<Particle> outterMembrane = new LinkedList<Particle>();
				Particle pF, pS;
				//Coordonates of the center of the window
				int x0 = (int) (winsize.width / 2.0);
				int y0 = (int) (winsize.height / 2.0);

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				int counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.2244){
					counter++;
					if(counter > 4 && counter < 12){
						continue;
					}
					double B = ((winsize.height/2)-220);
					double A = 80.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)-yTranspose, 0, 0);
					//Addind those particles to the list
					bps.add(pF);	
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.2244){
					counter++;
					if(counter > 4 && counter < 12){
						continue;
					}
					double B = ((winsize.height/2)-200);
					double A = 100.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)-yTranspose, 0, 0);
					//Addind those particles to the list

					OuterRingBps.add(pF);	
					
				}
				springs.add(new Spring(OuterRingBps.get(OuterRingBps.size()-1),bps.get(0),k,b));
				springs.add(new Spring(OuterRingBps.get(0),bps.get(bps.size()-1),k,b));
				springs.add(new Spring(bps.get(0),bps.get(bps.size()-1),k,b));
				springs.add(new Spring(OuterRingBps.get(OuterRingBps.size()-1),OuterRingBps.get(0),k,b));
				
				for(int i = 0; i < OuterRingBps.size(); i++){
					springs.add(new Spring(OuterRingBps.get(i),bps.get(i),k,b));
					if(i+1 < OuterRingBps.size() && (i!=3)){
						springs.add(new Spring(OuterRingBps.get(i),OuterRingBps.get(i+1),k,b));
						springs.add(new Spring(bps.get(i),bps.get(i+1),k,b));
					}
					if(i+1 < bps.size() && (i!=3)){
						springs.add(new Spring(OuterRingBps.get(i),bps.get(i+1),k,b));
					}
					if(i+1 < OuterRingBps.size() && (i!=3)){
						springs.add(new Spring(OuterRingBps.get(i+1),bps.get(i),k,b));
					}
				}
				

				//ACTUAL CELL
				//INNER MEMBRANE
				counter = 0;
				int yLower = 60;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > 47 && counter < 57){
						continue;
					}
					double B = ((winsize.height/2)-95);
					double A = 205.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yLower, 0, 0);
					//Addind those particles to the list
					innerMembrane.add(pF);	
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > 47 && counter < 57){
						continue;
					}
					
					double B = ((winsize.height/2)-75);
					double A = 225.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yLower, 0, 0);
					//Addind those particles to the list

					outterMembrane.add(pF);	
					
				}
				
				springs.add(new Spring(outterMembrane.get(outterMembrane.size()-1),innerMembrane.get(0),k,b));
				springs.add(new Spring(outterMembrane.get(0),innerMembrane.get(innerMembrane.size()-1),k,b));
				springs.add(new Spring(innerMembrane.get(0),innerMembrane.get(innerMembrane.size()-1),k,b));
				springs.add(new Spring(outterMembrane.get(outterMembrane.size()-1),outterMembrane.get(0),k,b));
				
				for(int i = 0; i < outterMembrane.size(); i++){
					springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i),k,b));
					if(i+1 < outterMembrane.size() && (i!=46)){
						springs.add(new Spring(outterMembrane.get(i),outterMembrane.get(i+1),k,b));
						springs.add(new Spring(innerMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < innerMembrane.size() && (i!=46)){
						springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < outterMembrane.size() && (i!=46)){
						springs.add(new Spring(outterMembrane.get(i+1),innerMembrane.get(i),k,b));
					}
				}
				
				springs.add(new Spring(outterMembrane.get(46),OuterRingBps.get(5),k,b));
				springs.add(new Spring(outterMembrane.get(46),OuterRingBps.get(4),k,b));
				springs.add(new Spring(innerMembrane.get(46),OuterRingBps.get(5),k,b));
				springs.add(new Spring(innerMembrane.get(46),OuterRingBps.get(4),k,b));
				
				springs.add(new Spring(outterMembrane.get(47),OuterRingBps.get(3),k,b));
				springs.add(new Spring(outterMembrane.get(47),OuterRingBps.get(2),k,b));
				springs.add(new Spring(innerMembrane.get(47),OuterRingBps.get(3),k,b));
				springs.add(new Spring(innerMembrane.get(47),OuterRingBps.get(2),k,b));

				
				//Putting my list of particles in this list which I guess is then drawn on the window
				particles.addAll(bps);
				particles.addAll(OuterRingBps);
				particles.addAll(innerMembrane);
				particles.addAll(outterMembrane);
				
				//NUCLEUS
				yTranspose = 0;
				List<Particle> innerNuc = new LinkedList<Particle>();
				List<Particle> outerNuc = new LinkedList<Particle>();
				
				//Coordinates of the center of the window
				

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.2244){
					counter++;
					double B = ((winsize.height/2)-220);
					double A = 80.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
					//Addind those particles to the list
					innerNuc.add(pF);	
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.2244){
					counter++;
					double B = ((winsize.height/2)-200);
					double A = 100.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
					//Addind those particles to the list

					outerNuc.add(pF);	
					
				}
				springs.add(new Spring(outerNuc.get(outerNuc.size()-1),innerNuc.get(0),k,b));
				springs.add(new Spring(outerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new Spring(innerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new Spring(outerNuc.get(outerNuc.size()-1),outerNuc.get(0),k,b));
				
				for(int i = 0; i < outerNuc.size(); i++){
					springs.add(new Spring(outerNuc.get(i),innerNuc.get(i),k,b));
					if(i+1 < outerNuc.size()){
						springs.add(new Spring(outerNuc.get(i),outerNuc.get(i+1),k,b));
						springs.add(new Spring(innerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < innerNuc.size()){
						springs.add(new Spring(outerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < outerNuc.size()){
						springs.add(new Spring(outerNuc.get(i+1),innerNuc.get(i),k,b));
					}
				}
				
				particles.addAll(innerNuc);
				particles.addAll(outerNuc);
				
				//outerNuc.get(21)
				//bps.get(14)
				
				//  the gap between subsequent particles in the chain
				int stepSize = 10;
				
				LinkedList<Particle> MTchainParticles = new LinkedList<Particle>();
				LinkedList<Spring> MTchainSprings = new LinkedList<Spring>();
				
				int numberOfParticlesInChain = 16;
				
				Particle lastParticle = bps.get(14);		//  y-coordinate of attachment point
				double lastY = lastParticle.p.y;
				
				for(int i=0; i<numberOfParticlesInChain; i++){
					
					Particle newParticle;
					Spring newSpring;
					
					double newY = lastY+stepSize;
					newParticle = new Particle(x0, newY, 0, 0);
					
					newSpring = new Spring(lastParticle, newParticle, k, b);
					
					newSpring.l0 = stepSize;
					newSpring.setK(stepSize);
					newSpring.setB(1);
					
					MTchainParticles.add(newParticle);
					MTchainSprings.add(newSpring);
					
					
					lastY = newY;
					lastParticle = newParticle;
				}
				particles.addAll(MTchainParticles);
				springs.addAll(MTchainSprings);

				
				//  attach the lastParticle to the top of the nucleus membrane
				Spring newSpring = new Spring(lastParticle, outerNuc.get(21), k, b);
				springs.add(newSpring);
				
				
				break;
				
				
			case 4: // pendulum
				yTranspose = 60;
				bps = new LinkedList<Particle>();
				OuterRingBps = new LinkedList<Particle>();
				innerMembrane = new LinkedList<Particle>();
				outterMembrane = new LinkedList<Particle>();
				
				//Coordonates of the center of the window
				x0 = (int) (winsize.width / 2.0);
				y0 = (int) (winsize.height / 2.0);

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				counter = 0;
				
				//PI/34
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > 12 && counter < 24){
						continue;
					}
					double B = ((winsize.height/2)-100);
					double A = 200.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)-yTranspose, 0, 0);
					//Addind those particles to the list
					bps.add(pF);	
				}
				
			
				
				counter = 0;
				//PI/34
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > 12 && counter < 24){
						continue;
					}
					double B = ((winsize.height/2)-80);
					double A = 220.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)-yTranspose, 0, 0);
					//Addind those particles to the list
					OuterRingBps.add(pF);	
				}

				
				
				springs.add(new Spring(OuterRingBps.get(OuterRingBps.size()-1),bps.get(0),k,b));
				springs.add(new Spring(OuterRingBps.get(0),bps.get(bps.size()-1),k,b));
				springs.add(new Spring(bps.get(0),bps.get(bps.size()-1),k,b));
				springs.add(new Spring(OuterRingBps.get(OuterRingBps.size()-1),OuterRingBps.get(0),k,b));
				
				for(int i = 0; i < OuterRingBps.size(); i++){
					springs.add(new Spring(OuterRingBps.get(i),bps.get(i),k,b));
					if(i+1 < OuterRingBps.size() && (i!=11)){
						springs.add(new Spring(OuterRingBps.get(i),OuterRingBps.get(i+1),k,b));
						springs.add(new Spring(bps.get(i),bps.get(i+1),k,b));
					}
					if(i+1 < bps.size() && (i!=11)){
						springs.add(new Spring(OuterRingBps.get(i),bps.get(i+1),k,b));
					}
					if(i+1 < OuterRingBps.size() && (i!=11)){
						springs.add(new Spring(OuterRingBps.get(i+1),bps.get(i),k,b));
					}
				}

				
				
				yTranspose = 415;
				counter = 0;
				// PI/80 = 0.039269908169872
				for(double t = 0; t <= (2*Math.PI); t = t + 0.03927){
					counter++;
					if(counter > 115 && counter < 127){
						continue;
					}
					if(counter > 104 && counter < 138){
						double B = 285;
						double A = 610.0;
						int x = (int)(A*Math.cos(t));
						int y = (int)(B*Math.sin(t));
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
						//Addind those particles to the list
						innerMembrane.add(pF);
					}
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.03927){
					counter++;
					if(counter > 115 && counter < 127){
						continue;
					}
					if(counter > 104 && counter < 138){
						double B = 305;
						double A = 630.0;
						int x = (int)(A*Math.cos(t));
						int y = (int)(B*Math.sin(t));
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
						//Addind those particles to the list
						outterMembrane.add(pF);
					}
				}
				

				
				
				for(int i = 0; i < outterMembrane.size(); i++){
					springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i),k,b));
					if(i+1 < outterMembrane.size() && (i!=10)){
						springs.add(new Spring(outterMembrane.get(i),outterMembrane.get(i+1),k,b));
						springs.add(new Spring(innerMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < innerMembrane.size() && (i!=10)){
						springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < outterMembrane.size() && (i!=10)){
						springs.add(new Spring(outterMembrane.get(i+1),innerMembrane.get(i),k,b));
					}
				}
				
				springs.add(new Spring(outterMembrane.get(10),OuterRingBps.get(13),k,b));
				springs.add(new Spring(outterMembrane.get(10),OuterRingBps.get(12),k,b));
				springs.add(new Spring(innerMembrane.get(10),OuterRingBps.get(13),k,b));
				springs.add(new Spring(innerMembrane.get(10),OuterRingBps.get(12),k,b));
				
				springs.add(new Spring(outterMembrane.get(11),OuterRingBps.get(11),k,b));
				springs.add(new Spring(outterMembrane.get(11),OuterRingBps.get(10),k,b));
				springs.add(new Spring(innerMembrane.get(11),OuterRingBps.get(11),k,b));
				springs.add(new Spring(innerMembrane.get(11),OuterRingBps.get(10),k,b));
			

				
				//Putting my list of particles in this list which I guess is then drawn on the window
				particles.addAll(bps);
				particles.addAll(OuterRingBps);
				particles.addAll(innerMembrane);
				particles.addAll(outterMembrane);
				
				
				//NUCLEUS
				yTranspose = 350;
				innerNuc = new LinkedList<Particle>();
				outerNuc = new LinkedList<Particle>();
				
				//Coordinates of the center of the window
				

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.098){
					counter++;
					double B = 200;
					double A = 200.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
					//Addind those particles to the list
					innerNuc.add(pF);	
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.098){
					counter++;
					double B = 220.0;
					double A = 220.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
					//Addind those particles to the list

					outerNuc.add(pF);	
					
				}
				springs.add(new Spring(outerNuc.get(outerNuc.size()-1),innerNuc.get(0),k,b));
				springs.add(new Spring(outerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new Spring(innerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new Spring(outerNuc.get(outerNuc.size()-1),outerNuc.get(0),k,b));
				
				for(int i = 0; i < outerNuc.size(); i++){
					springs.add(new Spring(outerNuc.get(i),innerNuc.get(i),k,b));
					if(i+1 < outerNuc.size()){
						springs.add(new Spring(outerNuc.get(i),outerNuc.get(i+1),k,b));
						springs.add(new Spring(innerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < innerNuc.size()){
						springs.add(new Spring(outerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < outerNuc.size()){
						springs.add(new Spring(outerNuc.get(i+1),innerNuc.get(i),k,b));
					}
				}
				
				particles.addAll(innerNuc);
				particles.addAll(outerNuc);
				
				//outerNuc.get(21)
				//bps.get(14)
				
				//  the gap between subsequent particles in the chain
				stepSize = 10;
				
				MTchainParticles = new LinkedList<Particle>();
				MTchainSprings = new LinkedList<Spring>();
				
				numberOfParticlesInChain = 37;
				
				lastParticle = bps.get(40);		//  y-coordinate of attachment point
				lastY = lastParticle.p.y;
				
				for(int i=0; i<numberOfParticlesInChain; i++){
					
					Particle newParticle;
					
					double newY = lastY+stepSize;
					newParticle = new Particle(x0, newY, 0, 0);
					
					newSpring = new Spring(lastParticle, newParticle, k, b);
					
					newSpring.l0 = stepSize;
					newSpring.setK(stepSize);
					newSpring.setB(1);
					
					MTchainParticles.add(newParticle);
					MTchainSprings.add(newSpring);
					
					
					lastY = newY;
					lastParticle = newParticle;
				}
				particles.addAll(MTchainParticles);
				springs.addAll(MTchainSprings);

				
				//  attach the lastParticle to the top of the nucleus membrane
				newSpring = new Spring(lastParticle, outerNuc.get(48), k, b);
				springs.add(newSpring);
				
				break;
				

			case 5: // ball
/*				int x0 = (int) (winsize.width / 2.0);
				int y0 = (int) (winsize.height / 2.0);
				double r = 100;
				List<Particle> bps = new LinkedList<Particle>();

				Particle pi, pn, po;
				pi = new Particle(x0 + r * Math.cos(0), y0 + r * Math.sin(0), 0, 0);
				bps.add(pi);
				po = pi;

				double dt = 2 * Math.PI / 8;
				for (double angle = dt; angle < 2 * Math.PI; angle += dt) {

					pn = new Particle(x0 + r * Math.cos(angle), y0 + r
							* Math.sin(angle), 0, 0);

					// springs.add( new Spring(po, pn));
					
					if (angle >= Math.PI + dt) {
						pn.heavy = true;
					}

					bps.add(pn);

					po = pn;
				}
				springs.add(new Spring(pi, po, k, b));

				for (Particle p1l : bps) {
					for (Particle p2l : bps) {
						if (p1l != p2l)
							springs.add(new Spring(p1l, p2l, k, b));
					}
				}

				
*/
				// Lavallee's Code
				//List of particles I need to fill
				bps = new LinkedList<Particle>();
				OuterRingBps = new LinkedList<Particle>();
				innerMembrane = new LinkedList<Particle>();
				outterMembrane = new LinkedList<Particle>();
				
				//Coordonates of the center of the window
				x0 = (int) (winsize.width / 2.0);
				y0 = (int) (winsize.height / 2.0);

	
				
				//Part Creating my ellipse
				counter = 0;
				int budBreakPoint = 7;
				//PI/34
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > budBreakPoint && counter < 29){
						continue;
					}
					double B = ((winsize.height/2)-50);
					double A = 200.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0), 0, 0);
					//Addind those particles to the list
					bps.add(pF);	
				}
				
			
				
				counter = 0;
				//PI/34
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > budBreakPoint && counter < 29){
						continue;
					}
					double B = ((winsize.height/2)-30);
					double A = 220.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0), 0, 0);
					//Addind those particles to the list
					OuterRingBps.add(pF);	
				}

				
				
				springs.add(new Spring(OuterRingBps.get(OuterRingBps.size()-1),bps.get(0),k,b));
				springs.add(new Spring(OuterRingBps.get(0),bps.get(bps.size()-1),k,b));
				springs.add(new Spring(bps.get(0),bps.get(bps.size()-1),k,b));
				springs.add(new Spring(OuterRingBps.get(OuterRingBps.size()-1),OuterRingBps.get(0),k,b));
				
				for(int i = 0; i < OuterRingBps.size(); i++){
					springs.add(new Spring(OuterRingBps.get(i),bps.get(i),k,b));
					if(i+1 < OuterRingBps.size() && (i!=(budBreakPoint-1))){
						springs.add(new Spring(OuterRingBps.get(i),OuterRingBps.get(i+1),k,b));
						springs.add(new Spring(bps.get(i),bps.get(i+1),k,b));
					}
					if(i+1 < bps.size() && (i!= (budBreakPoint-1))){
						springs.add(new Spring(OuterRingBps.get(i),bps.get(i+1),k,b));
					}
					if(i+1 < OuterRingBps.size() && (i!=(budBreakPoint-1))){
						springs.add(new Spring(OuterRingBps.get(i+1),bps.get(i),k,b));
					}
				}

				
				
				
				counter = 0;
				yTranspose = 415;
				counter = 0;
				// PI/80 = 0.039269908169872
				
				int cellMemBreak = 112;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.03927){
					counter++;
					if(counter > cellMemBreak && counter < 130){
						continue;
					}
					if(counter > 104 && counter < 138){
						double B = 285;
						double A = 610.0;
						int x = (int)(A*Math.cos(t));
						int y = (int)(B*Math.sin(t));
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
						//Addind those particles to the list
						innerMembrane.add(pF);
					}
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.03927){
					counter++;
					if(counter > cellMemBreak && counter < 130){
						continue;
					}
					if(counter > 104 && counter < 138){
						double B = 305;
						double A = 630.0;
						int x = (int)(A*Math.cos(t));
						int y = (int)(B*Math.sin(t));
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
						//Addind those particles to the list
						outterMembrane.add(pF);
					}
				}
				

				
				int indexBreak = 7;
				for(int i = 0; i < outterMembrane.size(); i++){
					springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i),k,b));
					if(i+1 < outterMembrane.size() && (i!=indexBreak)){
						springs.add(new Spring(outterMembrane.get(i),outterMembrane.get(i+1),k,b));
						springs.add(new Spring(innerMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < innerMembrane.size() && (i!=indexBreak)){
						springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < outterMembrane.size() && (i!=indexBreak)){
						springs.add(new Spring(outterMembrane.get(i+1),innerMembrane.get(i),k,b));
					}
				}
				
				springs.add(new Spring(outterMembrane.get(indexBreak),OuterRingBps.get((budBreakPoint+1)),k,b));
				springs.add(new Spring(outterMembrane.get(indexBreak),OuterRingBps.get((budBreakPoint)),k,b));
				springs.add(new Spring(innerMembrane.get(indexBreak),OuterRingBps.get((budBreakPoint+1)),k,b));
				springs.add(new Spring(innerMembrane.get(indexBreak),OuterRingBps.get((budBreakPoint)),k,b));
				
				springs.add(new Spring(outterMembrane.get(indexBreak+1),OuterRingBps.get(budBreakPoint-1),k,b));
				springs.add(new Spring(outterMembrane.get(indexBreak+1),OuterRingBps.get((budBreakPoint-2)),k,b));
				springs.add(new Spring(innerMembrane.get(indexBreak+1),OuterRingBps.get(budBreakPoint-1),k,b));
				springs.add(new Spring(innerMembrane.get(indexBreak+1),OuterRingBps.get((budBreakPoint-2)),k,b));
			

				//Putting my list of particles in this list which I guess is then drawn on the window
				particles.addAll(bps);
				particles.addAll(OuterRingBps);
				particles.addAll(innerMembrane);
				particles.addAll(outterMembrane);
				
				
				
				//NUCLEUS
				yTranspose = 350;
				innerNuc = new LinkedList<Particle>();
				outerNuc = new LinkedList<Particle>();
				
				//Coordinates of the center of the window
				

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.098){
					counter++;
					double B = 200;
					double A = 200.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
					//Addind those particles to the list
					innerNuc.add(pF);	
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.098){
					counter++;
					double B = 220.0;
					double A = 220.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
					//Addind those particles to the list

					outerNuc.add(pF);	
					
				}
				springs.add(new Spring(outerNuc.get(outerNuc.size()-1),innerNuc.get(0),k,b));
				springs.add(new Spring(outerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new Spring(innerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new Spring(outerNuc.get(outerNuc.size()-1),outerNuc.get(0),k,b));
				
				for(int i = 0; i < outerNuc.size(); i++){
					springs.add(new Spring(outerNuc.get(i),innerNuc.get(i),k,b));
					if(i+1 < outerNuc.size()){
						springs.add(new Spring(outerNuc.get(i),outerNuc.get(i+1),k,b));
						springs.add(new Spring(innerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < innerNuc.size()){
						springs.add(new Spring(outerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < outerNuc.size()){
						springs.add(new Spring(outerNuc.get(i+1),innerNuc.get(i),k,b));
					}
				}
				
				particles.addAll(innerNuc);
				particles.addAll(outerNuc);
				
				//outerNuc.get(21)
				//bps.get(14)
				
				//  the gap between subsequent particles in the chain
				stepSize = 10;
				
				MTchainParticles = new LinkedList<Particle>();
				MTchainSprings = new LinkedList<Spring>();
				
				numberOfParticlesInChain = 37;
				
				lastParticle = bps.get(30);		//  y-coordinate of attachment point
				lastY = lastParticle.p.y;
				
				for(int i=0; i<numberOfParticlesInChain; i++){
					
					Particle newParticle;
					
					double newY = lastY+stepSize;
					newParticle = new Particle(x0, newY, 0, 0);
					
					newSpring = new Spring(lastParticle, newParticle, k, b);
					
					newSpring.l0 = stepSize;
					newSpring.setK(stepSize);
					newSpring.setB(1);
					
					MTchainParticles.add(newParticle);
					MTchainSprings.add(newSpring);
					
					
					lastY = newY;
					lastParticle = newParticle;
				}
				particles.addAll(MTchainParticles);
				springs.addAll(MTchainSprings);

				
				//  attach the lastParticle to the top of the nucleus membrane
				newSpring = new Spring(lastParticle, outerNuc.get(48), k, b);
				springs.add(newSpring);
				
				
				break;
				

		}
		system.updateSystem();		
	}
    
    public void setRunning(boolean b) {
    	run.setValue(b);
    	interactor.setCreationEnabled(!b);
    }
    
    public boolean isRunning() {
    	return run.getValue();
    }

	@Override
	public String getName() {
		return "Particle Simulation";
	}
    
}
