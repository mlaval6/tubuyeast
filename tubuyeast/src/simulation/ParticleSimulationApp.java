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
        createSystem(system, 1);

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
			
			
			//  find the middle of the window
			double xMid = (int) (winsize.width / 2.0);
			double yMid = (int) (winsize.height / 2.0);
			
			
			//  set the location of the anchor of the MT to the membrane
			double xAnchorToCellWall = xMid;
			double yAnchorToCellWall = 50;
			
			
			//  create the particle that anchors the MT to the membrane
			Particle anchorToCellWall;
			anchorToCellWall = new Particle(xAnchorToCellWall, yAnchorToCellWall, 0, 0);
			anchorToCellWall.pinned = true;
			particles.add(anchorToCellWall);
			
			
			
			//  create the chain of particles for the MT from the anchor point to the nucleus
			//  specify the distance between the particles of the MT
			double lastY = yAnchorToCellWall;
			Particle lastParticle = anchorToCellWall;
			
			//  the gap between subsequent particles in the chain
			int stepSize = 10;
			
			List<Particle> MTchainParticles = new LinkedList<Particle>();
			List<Spring> MTchainSprings = new LinkedList<Spring>();
			
			int numberOfParticlesInChain = 10;
			
			for(int i=0; i<numberOfParticlesInChain; i++){
				
				Particle newParticle;
				Spring newSpring;
				
				double newY = lastY+stepSize;
				newParticle = new Particle(xMid, newY, 0, 0);
				
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
			
			
			
			
			//  create the membrane of the nucleus
			double nucleusSpringK = 10;
			int nucleusSpringB = 10;
			double nucleusSpringLength = 10;
			
			Particle nucleusAttachmentParticle = MTchainParticles.get(MTchainParticles.size()-1);
			double xNucleusOrigin = nucleusAttachmentParticle.p.x;
			double yNucleusOrigin = nucleusAttachmentParticle.p.y + stepSize;
			
			
			List<Particle> nucleusChainParticles = new LinkedList<Particle>();
			List<Spring> nucleusChainSprings = new LinkedList<Spring>();
			
//			System.out.println("width = " + winsize.width);
//			System.out.println("height = " + winsize.height);
			
			//  the right half of the nucleus membrane
			for(int y = (int)yNucleusOrigin; y <= (winsize.height-150); y = y + 10){
				int yWin = y - (int)yMid;
				int yPos = yWin * yWin;
				double b2 = (winsize.height / 2) - 150;
				b2 *= b2;
				double a = 150;
				double yFrac = (double) yPos/b2;
				int x = (int) (Math.sqrt(1 - yFrac) * a);
				
				Particle particleMembrane = new Particle(x+xMid, y, 0, 0);
				
				//  add the particles to the list
				nucleusChainParticles.add(particleMembrane);
				
				//  (x^2/150^2)  +  (y^2/((winsize.height-150)^2))  =   1
			}
			
			//  the left half of the nucleus membrane
			List<Particle> mirrorListTemp = new LinkedList<Particle>();
			for(int i=nucleusChainParticles.size()-2; i>=0; i--){		//  note: skip the point at the apex
				double xMirror = (-1 * (nucleusChainParticles.get(i).p.x - xMid)) + xMid;
				double yMirror = nucleusChainParticles.get(i).p.y;
				Particle mirrorParticle = new Particle(xMirror, yMirror, 0, 0);
				mirrorListTemp.add(mirrorParticle);
			}
			for(int i=0; i<mirrorListTemp.size(); i++){
				nucleusChainParticles.add(mirrorListTemp.get(i));
			}
			mirrorListTemp = null;
			
			particles.addAll(nucleusChainParticles);
			
			
			//  attach the last particle of the MT chain to the first particle of the nucleus membrane
			Spring MTnucleusAttachSpringFirst = new Spring(nucleusAttachmentParticle, nucleusChainParticles.get(0), k, b);
			MTnucleusAttachSpringFirst.l0 = nucleusSpringLength;
			MTnucleusAttachSpringFirst.setK(nucleusSpringK);
			MTnucleusAttachSpringFirst.setB(nucleusSpringB);
			
			springs.add(MTnucleusAttachSpringFirst);
			
			//  attach the last particle of the MT chain to the last particle of the nucleus membrane
			Spring MTnucleusAttachSpringLast = new Spring(nucleusAttachmentParticle, nucleusChainParticles.get(nucleusChainParticles.size()-1), k, b);
			MTnucleusAttachSpringLast.l0 = nucleusSpringLength;
			MTnucleusAttachSpringLast.setK(nucleusSpringK);
			MTnucleusAttachSpringLast.setB(nucleusSpringB);
			
			springs.add(MTnucleusAttachSpringLast);
			
			//  create the springs between the particles of the nuclear membrane
			List<Spring> nucleusSprings = new LinkedList<Spring>();
			for(int i=0; i<nucleusChainParticles.size()-1; i++){
				Spring nucleusSpring = new Spring(nucleusChainParticles.get(i), nucleusChainParticles.get(i+1), k, b);
				nucleusSpring.l0 = nucleusSpringLength;
				nucleusSpring.setK(nucleusSpringK);
				nucleusSpring.setB(nucleusSpringB);
				
				nucleusSprings.add(nucleusSpring);
			}
			
			springs.addAll(nucleusSprings);
							
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
				for(int y = 100; y <= (winsize.height-100); y = y + 20){
					counter++;
					
					if(counter > 20){
						break;
					}
					int yWin = y - y0;
					int yPos = yWin*yWin;
					double b2 = Math.pow(((winsize.height/2)-100),2);
					double a = 200.0;
					double yFrac = (double) yPos/b2;
					int x = (int) (Math.sqrt(1 - yFrac) * a);
					
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,y-yTranspose, 0, 0);
					pS = new Particle((x*-1)+x0,y-yTranspose, 0, 0);
					//Addind those particles to the list
					if(x == 0){
						bps.add(pF);	
					}
					else{
						bps.add(pS);
						bps.add(pF);
					}
					//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
				}
				
				for(int i = 0; i < bps.size(); i++){
					if(i+2 < bps.size()){
						springs.add(new Spring(bps.get(i),bps.get(i+2),k,b));
					}
				}
				
				counter = 0;
				for(int y = 80; y <= (winsize.height-80); y = y + 22){
					counter++;
					if(counter > 9 && counter < 17){
						y = y - 1;
					}
					if(counter > 20){
						break;
					}
					int yWin = y - y0;
					int yPos = yWin*yWin;
					double b2 = Math.pow(((winsize.height/2)-80),2);
					double a = 220.0;
					double yFrac = (double) yPos/b2;
					int x = (int) (Math.sqrt(1 - yFrac) * a);
					
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,y-yTranspose, 0, 0);
					pS = new Particle((x*-1)+x0,y-yTranspose, 0, 0);
					//Addind those particles to the list
					if(x == 0){
						OuterRingBps.add(pF);	
					}
					else{
						OuterRingBps.add(pS);
						OuterRingBps.add(pF);
					}
					//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
				}
				
				for(int i = 0; i < OuterRingBps.size(); i++){
					if(i+2 < OuterRingBps.size()){
						springs.add(new Spring(OuterRingBps.get(i),OuterRingBps.get(i+2),k,b));
					}
				}
				
				springs.add(new Spring(OuterRingBps.get(0),bps.get(1),k,b));
				springs.add(new Spring(bps.get(0),OuterRingBps.get(1),k,b));

				springs.add(new Spring(OuterRingBps.get(0),OuterRingBps.get(1),k,b));
				springs.add(new Spring(bps.get(0),bps.get(1),k,b));
				
				for(int i = 0; i < OuterRingBps.size(); i++){
					springs.add(new Spring(OuterRingBps.get(i),bps.get(i),k,b));
					if(i+2 < bps.size()){
						springs.add(new Spring(OuterRingBps.get(i),bps.get(i+2),k,b));
						springs.add(new Spring(bps.get(i),OuterRingBps.get(i+2),k,b));
					}
					//if(i-2 >= 0){
					//	springs.add(new Spring(OuterRingBps.get(i),bps.get(i-2),k,b));
					//}
					
				}
				
				counter = 0;
				for(int y = 285; y >= 0; y = y - 10){
					counter++;
					if(counter == 1){
						y = y + 1;
					}
					if(counter > 7){
						break;
					}
					if(counter > 1){
						int originDistFromWindow = 114;
						int yPos = y - originDistFromWindow;
						yPos = winsize.height - yPos;
	//					if(counter > 9 && counter < 17){
	//						y = y - 1;
	//					}
	//					if(counter > 20){
	//						break;
	//					}
						//int yWin = y - y0;
						int ySquared = y*y;
						double b2 = 285*285;
						double a = 600.0;
						double yFrac = (double) ySquared/b2;
						int x = (int) (Math.sqrt(1 - yFrac) * a);
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,yPos, 0, 0);
						pS = new Particle((x*-1)+x0,yPos, 0, 0);
						//Addind those particles to the list
						if(x == 0){
							innerMembrane.add(pF);	
						}
						else{
							innerMembrane.add(pS);
							innerMembrane.add(pF);
						}
						//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
					}
				}
				
				counter = 0;
				for(int y = 306; y >= 0; y = y - 10){
					counter++;
					if(counter > 7){
						break;
					}
					if(counter > 1){
						int originDistFromWindow = 114;
						int yPos = y - originDistFromWindow;
						yPos = winsize.height - yPos;
	//					if(counter > 9 && counter < 17){
	//						y = y - 1;
	//					}
	//					if(counter > 20){
	//						break;
	//					}
						//int yWin = y - y0;
						int ySquared = y*y;
						double b2 = 306*306;
						double a = 630.0;
						double yFrac = (double) ySquared/b2;
						int x = (int) (Math.sqrt(1 - yFrac) * a);
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,yPos, 0, 0);
						pS = new Particle((x*-1)+x0,yPos, 0, 0);
						//Addind those particles to the list
						if(x == 0){
							outterMembrane.add(pF);	
						}
						else{
							outterMembrane.add(pS);
							outterMembrane.add(pF);
						}
						//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
					}
				}
				
				springs.add(new Spring(innerMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-2),k,b));
				springs.add(new Spring(innerMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-1),k,b));
				springs.add(new Spring(innerMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-4),k,b));
				springs.add(new Spring(innerMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-3),k,b));
				springs.add(new Spring(outterMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-3),k,b));
				springs.add(new Spring(outterMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-4),k,b));
				springs.add(new Spring(outterMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-2),k,b));
				springs.add(new Spring(outterMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-1),k,b));
				for(int i = 0; i < innerMembrane.size(); i++){
					springs.add(new Spring(innerMembrane.get(i),outterMembrane.get(i),k,b));
					
					if(i+2 < innerMembrane.size()){
						springs.add(new Spring(innerMembrane.get(i),innerMembrane.get(i+2),k,b));
						springs.add(new Spring(outterMembrane.get(i),outterMembrane.get(i+2),k,b));
						springs.add(new Spring(innerMembrane.get(i),outterMembrane.get(i+2),k,b));
						springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i+2),k,b));
					}
				}
				
				//Putting my list of particles in this list which I guess is then drawn on the window
				particles.addAll(bps);
				particles.addAll(OuterRingBps);
				particles.addAll(innerMembrane);
				particles.addAll(outterMembrane);
				
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

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				counter = 0;
				for(int y = 50; y <= (winsize.height-50); y = y + 20){
					counter++;
					if(counter > 20){
						break;
					}
					int yWin = y - y0;
					int yPos = yWin*yWin;
					double b2 = Math.pow(((winsize.height/2)-50),2);
					double a = 200.0;
					double yFrac = (double) yPos/b2;
					int x = (int) (Math.sqrt(1 - yFrac) * a);
					
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,y, 0, 0);
					pS = new Particle((x*-1)+x0,y, 0, 0);
					//Addind those particles to the list
					if(x == 0){
						bps.add(pF);	
					}
					else{
						bps.add(pS);
						bps.add(pF);
					}
					//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
				}
				
				for(int i = 0; i < bps.size(); i++){
					if(i+2 < bps.size()){
						springs.add(new Spring(bps.get(i),bps.get(i+2),k,b));
					}
				}
				
				counter = 0;
				for(int y = 30; y <= (winsize.height-30); y = y + 22){
					counter++;
					if(counter > 9 && counter < 17){
						y = y - 1;
					}
					if(counter > 20){
						break;
					}
					int yWin = y - y0;
					int yPos = yWin*yWin;
					double b2 = Math.pow(((winsize.height/2)-30),2);
					double a = 220.0;
					double yFrac = (double) yPos/b2;
					int x = (int) (Math.sqrt(1 - yFrac) * a);
					
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,y, 0, 0);
					pS = new Particle((x*-1)+x0,y, 0, 0);
					//Addind those particles to the list
					if(x == 0){
						OuterRingBps.add(pF);	
					}
					else{
						OuterRingBps.add(pS);
						OuterRingBps.add(pF);
					}
					//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
				}
				
				for(int i = 0; i < OuterRingBps.size(); i++){
					if(i+2 < OuterRingBps.size()){
						springs.add(new Spring(OuterRingBps.get(i),OuterRingBps.get(i+2),k,b));
					}
				}
				
				springs.add(new Spring(OuterRingBps.get(0),bps.get(1),k,b));
				springs.add(new Spring(bps.get(0),OuterRingBps.get(1),k,b));

				springs.add(new Spring(OuterRingBps.get(0),OuterRingBps.get(1),k,b));
				springs.add(new Spring(bps.get(0),bps.get(1),k,b));
				
				for(int i = 0; i < OuterRingBps.size(); i++){
					springs.add(new Spring(OuterRingBps.get(i),bps.get(i),k,b));
					if(i+2 < bps.size()){
						springs.add(new Spring(OuterRingBps.get(i),bps.get(i+2),k,b));
						springs.add(new Spring(bps.get(i),OuterRingBps.get(i+2),k,b));
					}
					//if(i-2 >= 0){
					//	springs.add(new Spring(OuterRingBps.get(i),bps.get(i-2),k,b));
					//}
					
				}
				
				counter = 0;
				for(int y = 285; y >= 0; y = y - 10){
					counter++;
					if(counter > 7){
						break;
					}
					if(counter > 2){
						int originDistFromWindow = 114;
						int yPos = y - originDistFromWindow;
						yPos = winsize.height - yPos;
	//					if(counter > 9 && counter < 17){
	//						y = y - 1;
	//					}
	//					if(counter > 20){
	//						break;
	//					}
						//int yWin = y - y0;
						int ySquared = y*y;
						double b2 = 285*285;
						double a = 600.0;
						double yFrac = (double) ySquared/b2;
						int x = (int) (Math.sqrt(1 - yFrac) * a);
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,yPos, 0, 0);
						pS = new Particle((x*-1)+x0,yPos, 0, 0);
						//Addind those particles to the list
						if(x == 0){
							innerMembrane.add(pF);	
						}
						else{
							innerMembrane.add(pS);
							innerMembrane.add(pF);
						}
						//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
					}
				}
				
				counter = 0;
				for(int y = 306; y >= 0; y = y - 10){
					counter++;
					if(counter > 7){
						break;
					}
					if(counter > 2){
						int originDistFromWindow = 114;
						int yPos = y - originDistFromWindow;
						yPos = winsize.height - yPos;
	//					if(counter > 9 && counter < 17){
	//						y = y - 1;
	//					}
	//					if(counter > 20){
	//						break;
	//					}
						//int yWin = y - y0;
						int ySquared = y*y;
						double b2 = 306*306;
						double a = 630.0;
						double yFrac = (double) ySquared/b2;
						int x = (int) (Math.sqrt(1 - yFrac) * a);
						//Creating my particles at the wanted position
						pF = new Particle(x+x0,yPos, 0, 0);
						pS = new Particle((x*-1)+x0,yPos, 0, 0);
						//Addind those particles to the list
						if(x == 0){
							outterMembrane.add(pF);	
						}
						else{
							outterMembrane.add(pS);
							outterMembrane.add(pF);
						}
						//(x^2/200^2)  +  (y^2/(winsize.height-50^2))  =   1
					}
				}
				
				springs.add(new Spring(innerMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-2),k,b));
				springs.add(new Spring(innerMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-1),k,b));
				springs.add(new Spring(innerMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-4),k,b));
				springs.add(new Spring(innerMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-3),k,b));
				springs.add(new Spring(outterMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-3),k,b));
				springs.add(new Spring(outterMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-4),k,b));
				springs.add(new Spring(outterMembrane.get(0),OuterRingBps.get(OuterRingBps.size()-2),k,b));
				springs.add(new Spring(outterMembrane.get(1),OuterRingBps.get(OuterRingBps.size()-1),k,b));
				for(int i = 0; i < innerMembrane.size(); i++){
					springs.add(new Spring(innerMembrane.get(i),outterMembrane.get(i),k,b));
					
					if(i+2 < innerMembrane.size()){
						springs.add(new Spring(innerMembrane.get(i),innerMembrane.get(i+2),k,b));
						springs.add(new Spring(outterMembrane.get(i),outterMembrane.get(i+2),k,b));
						springs.add(new Spring(innerMembrane.get(i),outterMembrane.get(i+2),k,b));
						springs.add(new Spring(outterMembrane.get(i),innerMembrane.get(i+2),k,b));
					}
				}
				
				//Putting my list of particles in this list which I guess is then drawn on the window
				particles.addAll(bps);
				particles.addAll(OuterRingBps);
				particles.addAll(innerMembrane);
				particles.addAll(outterMembrane);
				
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
