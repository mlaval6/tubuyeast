package simulation;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
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
    
    private DoubleParameter stepsize = new DoubleParameter( "step size", 0.0015, 1e-5, 1 );
    
    private IntParameter substeps = new IntParameter( "sub steps (integer)", 1, 1, 100);
    
    private static Dimension simsize = new Dimension(800, 600);

    private static Dimension wsize = new Dimension(800, 800);

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
        system = new ParticleSystem(simsize);
        createSystem(system, 3);

        // Add an interactor to manage mouse and keyboard controls
        interactor = new ParticleSimulationInteractor(system);
        
        ev = new OpenglViewer("Tubuyeast", this, new Dimension(wsize), new Dimension(650, wsize.height + 90) );

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
            	//BUD GROWING!
        		growBud();
            	system.step( stepsize.getValue());                
            }
            stepRequested = false;        
        }
	}
	
	private void growBud(){
		List<Particle> particlesWithoutBud = new ArrayList<Particle>();
		for(Particle p: system.particles){
			if (!(p instanceof BudParticle)){
				particlesWithoutBud.add(p);
			}
		}
		system.particles.clear();
		system.particles.addAll(particlesWithoutBud);
		
		List<Spring> springsWithoutBud = new ArrayList<Spring>();
		for(Spring s: system.springs){
			if (!(s instanceof BudSpring)){
				springsWithoutBud.add(s);
			}
		}
		system.springs.clear();
		system.springs.addAll(springsWithoutBud);
		
		
		if(system.getBud().getaRadius()*2 < (0.65*system.getCellDiameter() )){
			system.getBud().setaRadius(system.getBud().getaRadius() + (5e-7 * system.getPForce().getValue()));
			system.getBud().setYTranspose(system.getBud().getYTranspose() + 0.05);
		}
		
		
		
		double k = system.getK();
		double b = system.getB();
		List<Particle> budInnerMembrane = new LinkedList<Particle>();
		List<Particle> budOuterMembrane = new LinkedList<Particle>();
		BudParticle pF;
		//Coordinates of the center of the window
		int x0 = (int) (simsize.width / 2.0);
		int y0 = (int) (simsize.height / 2.0) + 100;

		
		//Part Creating my ellipse
		int counter = 0;
		//PI/14 = 0.2244
		double RadiusDividerForPIDivider = 5.0;
		double numberOfBudParticles = ((system.getBud().getaRadius()+20)/RadiusDividerForPIDivider) + 2;
		double angle = Math.PI/numberOfBudParticles;
		for(double t = 0; t < (2*Math.PI-angle); t = t + angle){
			counter++;
			if(counter > (numberOfBudParticles-12) && counter < 14){
				continue;
			}
			double B = system.getBud().getaRadius() * 2;
			double A = system.getBud().getaRadius() * 2;
			int x = (int)(A*Math.cos(t));
			int y = (int)(B*Math.sin(t));
			//Creating my particles at the wanted position
			pF = new BudParticle(x+x0,(y+y0)-system.getBud().getYTranspose(), 0, 0);
			pF.pinned = true;
			//Addind those particles to the list
			budInnerMembrane.add(pF);	
		}
		
		counter = 0;
		for(double t = 0; t < (2*Math.PI-angle); t = t + angle){
			counter++;
			if(counter > (numberOfBudParticles-12) && counter < 14){
				continue;
			}
			double B = system.getBud().getaRadius() * 2 + 20;
			double A = system.getBud().getaRadius() * 2 + 20;

			int x = (int)(A*Math.cos(t));
			int y = (int)(B*Math.sin(t));
			//Creating my particles at the wanted position
			pF = new BudParticle(x+x0,(y+y0)-system.getBud().getYTranspose(), 0, 0);
			pF.pinned = true;
			//Addind those particles to the list

			budOuterMembrane.add(pF);	
			
		}
		system.springs.add(new BudSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budInnerMembrane.get(0),k,b));
		system.springs.add(new BudSpring(budOuterMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
		system.springs.add(new BudSpring(budInnerMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
		system.springs.add(new BudSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budOuterMembrane.get(0),k,b));
		
		for(int i = 0; i < budOuterMembrane.size(); i++){
			system.springs.add(new BudSpring(budOuterMembrane.get(i),budInnerMembrane.get(i),k,b));
			if(i+1 < budOuterMembrane.size() && (i!=(int)(numberOfBudParticles-13))){
				system.springs.add(new BudSpring(budOuterMembrane.get(i),budOuterMembrane.get(i+1),k,b));
				system.springs.add(new BudSpring(budInnerMembrane.get(i),budInnerMembrane.get(i+1),k,b));
			}
			if(i+1 < budInnerMembrane.size() && (i!=(int)(numberOfBudParticles-13))){
				system.springs.add(new BudSpring(budOuterMembrane.get(i),budInnerMembrane.get(i+1),k,b));
			}
			if(i+1 < budOuterMembrane.size() && (i!=(int)(numberOfBudParticles-13))){
				system.springs.add(new BudSpring(budOuterMembrane.get(i+1),budInnerMembrane.get(i),k,b));
			}
		}
		
		
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(48),budOuterMembrane.get((int)(numberOfBudParticles-12)),k,b));
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(48),budInnerMembrane.get((int)(numberOfBudParticles-12)),k,b));
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(47),budOuterMembrane.get((int)(numberOfBudParticles-12)),k,b));
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(47),budInnerMembrane.get((int)(numberOfBudParticles-12)),k,b));
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(49),budOuterMembrane.get((int)(numberOfBudParticles-13)),k,b));
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(49),budInnerMembrane.get((int)(numberOfBudParticles-13)),k,b));
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(50),budOuterMembrane.get((int)(numberOfBudParticles-13)),k,b));
		system.springs.add(new BudSpring(system.getMotherCell().getOuterMembraneParticles().get(50),budInnerMembrane.get((int)(numberOfBudParticles-13)),k,b));
		
		system.particles.addAll(budInnerMembrane);
		system.particles.addAll(budOuterMembrane);
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
		LinearSpring newSpring;
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
			springs.add(new LinearSpring(p1, p2, k, b));
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
				springs.add(new LinearSpring(p3, p1, k, b));
				springs.add(new LinearSpring(p3, p2, k, b));
				springs.add(new LinearSpring(p4, p1, k, b));
				springs.add(new LinearSpring(p4, p2, k, b));
				springs.add(new LinearSpring(p4, p3, k, b));
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
				springs.add(new LinearSpring(p1, p2, k, b));
				p1.pinned = true;
				p2.pinned = true;
				p.add(d);
				p.add(d);
				N = 10;
				for (int i = 1; i < N; i++) {
					 d.set( 20*Math.cos(i*Math.PI/N), 20*Math.sin(i*Math.PI/N) );
					p3 = new Particle(p.x - d.y, p.y + d.x, 0, 0);
					
					if (i == N - 1) {
						p4 = new MotorParticle(p.x + d.y, p.y - d.x, 0, 0);
					}
					else {
						p4 = new Particle(p.x + d.y, p.y - d.x, 0, 0);
					}
					
					particles.add(p3);
					particles.add(p4);
					springs.add(new LinearSpring(p3, p1, k, b));
					springs.add(new LinearSpring(p3, p2, k, b));
					springs.add(new LinearSpring(p4, p1, k, b));
					springs.add(new LinearSpring(p4, p2, k, b));
					springs.add(new LinearSpring(p4, p3, k, b));
					p1 = p3;
					p2 = p4;

					p.add(d);
					p.add(d);
				}
				break;
			case 3: // COMPLETE CELL
				
				double yTranspose = system.getBud().getYTranspose();
				List<Particle> budInnerMembrane = new LinkedList<Particle>();
				List<Particle> budOuterMembrane = new LinkedList<Particle>();
				List<Particle> innerMembrane = new LinkedList<Particle>();
				List<Particle> outterMembrane = new LinkedList<Particle>();
				Particle pF;
				//Coordinates of the center of the window
				int x0 = (int) (simsize.width / 2.0);
				int y0 = (int) (simsize.height / 2.0) + 100;

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				int counter = 0;
				double RadiusDividerForPIDivider = 5.0;
				double numberOfBudParticles = ((system.getBud().getaRadius()+20)/RadiusDividerForPIDivider) + 2;
				double angle = Math.PI/numberOfBudParticles;
				for(double t = 0; t < (2*Math.PI); t = t + angle){
					counter++;
					if(counter > (numberOfBudParticles-12) && counter < 14){
						continue;
					}
					double B = system.getBud().getaRadius() * 2;
					double A = system.getBud().getaRadius() * 2;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new BudParticle(x+x0,(y+y0)-yTranspose, 0, 0);
					pF.pinned = true;
					//Addind those particles to the list
					budInnerMembrane.add(pF);	
				}
				
				counter = 0;
				for(double t = 0; t < (2*Math.PI); t = t + angle){
					counter++;
					if(counter > (numberOfBudParticles-12) && counter < 14){
						continue;
					}
					double B = system.getBud().getaRadius() * 2 + 20;
					double A = system.getBud().getaRadius() * 2 + 20;

					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new BudParticle(x+x0,(y+y0)-yTranspose, 0, 0);
					pF.pinned = true;
					//Addind those particles to the list

					budOuterMembrane.add(pF);	
					
				}
				springs.add(new BudSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budInnerMembrane.get(0),k,b));
				springs.add(new BudSpring(budOuterMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
				springs.add(new BudSpring(budInnerMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
				springs.add(new BudSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budOuterMembrane.get(0),k,b));
				
				for(int i = 0; i < budOuterMembrane.size(); i++){
					springs.add(new BudSpring(budOuterMembrane.get(i),budInnerMembrane.get(i),k,b));
					if(i+1 < budOuterMembrane.size() && (i!=1)){
						springs.add(new BudSpring(budOuterMembrane.get(i),budOuterMembrane.get(i+1),k,b));
						springs.add(new BudSpring(budInnerMembrane.get(i),budInnerMembrane.get(i+1),k,b));
					}
					if(i+1 < budInnerMembrane.size() && (i!=1)){
						springs.add(new BudSpring(budOuterMembrane.get(i),budInnerMembrane.get(i+1),k,b));
					}
					if(i+1 < budOuterMembrane.size() && (i!=1)){
						springs.add(new BudSpring(budOuterMembrane.get(i+1),budInnerMembrane.get(i),k,b));
					}
				}
				

				//ACTUAL Mother CELL
				//INNER MEMBRANE
				
				counter = 0;
				double yLower = system.getBud().getYTranspose()-10.0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > 49 && counter < 55){
						continue;
					}
					double B = ((simsize.height/2)-95);
					double A = 205.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yLower, 0, 0);
					pF.pinned = true;
					//Addind those particles to the list
					innerMembrane.add(pF);	
				}
				
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > 49 && counter < 55){
						continue;
					}
					
					double B = ((simsize.height/2)-75);
					double A = 225.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yLower, 0, 0);
					pF.pinned = true;
					//Addind those particles to the list

					outterMembrane.add(pF);	
					
				}
				
				springs.add(new LinearSpring(outterMembrane.get(outterMembrane.size()-1),innerMembrane.get(0),k,b));
				springs.add(new LinearSpring(outterMembrane.get(0),innerMembrane.get(innerMembrane.size()-1),k,b));
				springs.add(new LinearSpring(innerMembrane.get(0),innerMembrane.get(innerMembrane.size()-1),k,b));
				springs.add(new LinearSpring(outterMembrane.get(outterMembrane.size()-1),outterMembrane.get(0),k,b));
				
				for(int i = 0; i < outterMembrane.size(); i++){
					springs.add(new LinearSpring(outterMembrane.get(i),innerMembrane.get(i),k,b));
					if(i+1 < outterMembrane.size() && (i!=48)){
						springs.add(new LinearSpring(outterMembrane.get(i),outterMembrane.get(i+1),k,b));
						springs.add(new LinearSpring(innerMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < innerMembrane.size() && (i!=48)){
						springs.add(new LinearSpring(outterMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < outterMembrane.size() && (i!=48)){
						springs.add(new LinearSpring(outterMembrane.get(i+1),innerMembrane.get(i),k,b));
					}
				}
				
				springs.add(new BudSpring(outterMembrane.get(48),budOuterMembrane.get(2),k,b));
				springs.add(new BudSpring(outterMembrane.get(48),budInnerMembrane.get(2),k,b));
				springs.add(new BudSpring(outterMembrane.get(47),budOuterMembrane.get(2),k,b));
				springs.add(new BudSpring(outterMembrane.get(47),budInnerMembrane.get(2),k,b));
				springs.add(new BudSpring(outterMembrane.get(49),budOuterMembrane.get(1),k,b));
				springs.add(new BudSpring(outterMembrane.get(49),budInnerMembrane.get(1),k,b));
				springs.add(new BudSpring(outterMembrane.get(50),budOuterMembrane.get(1),k,b));
				springs.add(new BudSpring(outterMembrane.get(50),budInnerMembrane.get(1),k,b));
//				springs.add(new BudSpring(innerMembrane.get(48),budOuterMembrane.get(5),k,b));
//				springs.add(new BudSpring(innerMembrane.get(48),budOuterMembrane.get(4),k,b));
				
//				springs.add(new BudSpring(outterMembrane.get(49),budOuterMembrane.get(3),k,b));
//				springs.add(new BudSpring(outterMembrane.get(49),budOuterMembrane.get(2),k,b));
//				springs.add(new BudSpring(innerMembrane.get(49),budOuterMembrane.get(3),k,b));
//				springs.add(new BudSpring(innerMembrane.get(49),budOuterMembrane.get(2),k,b));

				
				//Putting my list of particles in this list which I guess is then drawn on the window
				system.getMotherCell().setInnerMembraneParticles(innerMembrane);
				system.getMotherCell().setOuterMembraneParticles(outterMembrane);
				
				particles.addAll(budInnerMembrane);
				particles.addAll(budOuterMembrane);
				particles.addAll(innerMembrane);
				particles.addAll(outterMembrane);
				
				//NUCLEUS
				yTranspose = system.getBud().getYTranspose() - 70;
				List<Particle> innerNuc = new LinkedList<Particle>();
				List<Particle> outerNuc = new LinkedList<Particle>();
				
				//Coordinates of the center of the window
				

				System.out.println(x0);
				System.out.println(y0);
				
				//Part Creating my ellipse
				counter = 0;
				for(double t = 0; t <= (2*Math.PI); t = t + 0.2244){
					counter++;
					double B = ((simsize.height/2)-220);
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
					double B = ((simsize.height/2)-200);
					double A = 100.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)+yTranspose, 0, 0);
					//Addind those particles to the list

					outerNuc.add(pF);	
					
				}
				springs.add(new LinearSpring(outerNuc.get(outerNuc.size()-1),innerNuc.get(0),k,b));
				springs.add(new LinearSpring(outerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new LinearSpring(innerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new LinearSpring(outerNuc.get(outerNuc.size()-1),outerNuc.get(0),k,b));
				
				for(int i = 0; i < outerNuc.size(); i++){
					springs.add(new LinearSpring(outerNuc.get(i),innerNuc.get(i),k,b));
					if(i+1 < outerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i),outerNuc.get(i+1),k,b));
						springs.add(new LinearSpring(innerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < innerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < outerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i+1),innerNuc.get(i),k,b));
					}
				}
				
				particles.addAll(innerNuc);
				particles.addAll(outerNuc);
				
				//outerNuc.get(21)
				//budInnerMembrane.get(10)
				
				//  the gap between subsequent particles in the chain
				int stepSize = 10;
				
				LinkedList<Particle> MTchainParticles = new LinkedList<Particle>();
				LinkedList<LinearSpring> MTchainSprings = new LinkedList<LinearSpring>();
				
				int numberOfParticlesInChain = 16;
				
				Particle lastParticle = budInnerMembrane.get(10);		//  y-coordinate of attachment point
				double lastY = lastParticle.p.y;
				
				// TODO: set this in UI
				double chainStiffness = 200000;
				
				for(int i=0; i<numberOfParticlesInChain; i++){
					
					Particle newParticle;
					
					double newY = lastY+stepSize;
					if (i == 0) {
						newParticle = new MotorParticle(x0, newY, 0, 0);
						newParticle.heavy = true;
					}
					else {
						newParticle = new Particle(x0, newY, 0, 0);
					}

					// Don't collide the chain
					newParticle.collidable = false;
										
					// FIXME: removed otherwise the whole membrane gets pulled up
					if (i != 0) {
						newSpring = new LinearSpring(lastParticle, newParticle, chainStiffness, b);
						
						newSpring.l0 = stepSize;
						// TODO: k is equal to step size? huh?
//						newSpring.setK(stepSize);
						newSpring.setB(1);
						MTchainSprings.add(newSpring);
					}
					
					MTchainParticles.add(newParticle);
					
					lastY = newY;
					lastParticle = newParticle;
				}
				particles.addAll(MTchainParticles);
				springs.addAll(MTchainSprings);

				
				//  attach the lastParticle to the top of the nucleus membrane
				newSpring = new LinearSpring(lastParticle, outerNuc.get(21), k, b);
				springs.add(newSpring);
				
				
				
				
				//  creating the MT chain from below the nucleus to the inside membrane of the mother cell
				Particle previousParticle = outerNuc.get(7);
				Particle membraneAttachmentPoint = innerMembrane.get(17);
				
				int numberOfParticlesInMTchain = 8;
				double distance = membraneAttachmentPoint.p.y - previousParticle.p.y;
				double kForMTchain2 = k;
				double bForMTchain2 = b;
				double stepSizeMTchain2 = distance / (double)numberOfParticlesInMTchain;
				
				
				
				LinkedList<Particle> MTparticleList2 = new LinkedList<Particle>();
				LinkedList<LinearSpring> MTspringList2 = new LinkedList<LinearSpring>();
				for(int i=0; i<numberOfParticlesInMTchain-1; i++){
					Particle newParticleMTchain = new Particle(previousParticle.p.x, previousParticle.p.y + stepSizeMTchain2, 0, 0);
					LinearSpring newSpringMTchain = new LinearSpring(previousParticle, newParticleMTchain, kForMTchain2, bForMTchain2);
					
					MTparticleList2.add(newParticleMTchain);
					MTspringList2.add(newSpringMTchain);
					
					previousParticle = newParticleMTchain;
				}
				
				LinearSpring newLastSpring = new LinearSpring(previousParticle, membraneAttachmentPoint, kForMTchain2, bForMTchain2);
				MTspringList2.add(newLastSpring);
				
				particles.addAll(MTparticleList2);
				springs.addAll(MTspringList2);
				
				
				break;
				
				
			case 4: // pendulum
				yTranspose = 60;
				budInnerMembrane = new LinkedList<Particle>();
				budOuterMembrane = new LinkedList<Particle>();
				innerMembrane = new LinkedList<Particle>();
				outterMembrane = new LinkedList<Particle>();
				
				//Coordonates of the center of the window
				x0 = (int) (simsize.width / 2.0);
				y0 = (int) (simsize.height / 2.0);

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
					double B = ((simsize.height/2)-100);
					double A = 200.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)-yTranspose, 0, 0);
					//Addind those particles to the list
					budInnerMembrane.add(pF);	
				}
				
			
				
				counter = 0;
				//PI/34
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > 12 && counter < 24){
						continue;
					}
					double B = ((simsize.height/2)-80);
					double A = 220.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0)-yTranspose, 0, 0);
					//Addind those particles to the list
					budOuterMembrane.add(pF);	
				}

				
				
				springs.add(new LinearSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budInnerMembrane.get(0),k,b));
				springs.add(new LinearSpring(budOuterMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
				springs.add(new LinearSpring(budInnerMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
				springs.add(new LinearSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budOuterMembrane.get(0),k,b));
				
				for(int i = 0; i < budOuterMembrane.size(); i++){
					springs.add(new LinearSpring(budOuterMembrane.get(i),budInnerMembrane.get(i),k,b));
					if(i+1 < budOuterMembrane.size() && (i!=11)){
						springs.add(new LinearSpring(budOuterMembrane.get(i),budOuterMembrane.get(i+1),k,b));
						springs.add(new LinearSpring(budInnerMembrane.get(i),budInnerMembrane.get(i+1),k,b));
					}
					if(i+1 < budInnerMembrane.size() && (i!=11)){
						springs.add(new LinearSpring(budOuterMembrane.get(i),budInnerMembrane.get(i+1),k,b));
					}
					if(i+1 < budOuterMembrane.size() && (i!=11)){
						springs.add(new LinearSpring(budOuterMembrane.get(i+1),budInnerMembrane.get(i),k,b));
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
					springs.add(new LinearSpring(outterMembrane.get(i),innerMembrane.get(i),k,b));
					if(i+1 < outterMembrane.size() && (i!=10)){
						springs.add(new LinearSpring(outterMembrane.get(i),outterMembrane.get(i+1),k,b));
						springs.add(new LinearSpring(innerMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < innerMembrane.size() && (i!=10)){
						springs.add(new LinearSpring(outterMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < outterMembrane.size() && (i!=10)){
						springs.add(new LinearSpring(outterMembrane.get(i+1),innerMembrane.get(i),k,b));
					}
				}
				
				springs.add(new LinearSpring(outterMembrane.get(10),budOuterMembrane.get(13),k,b));
				springs.add(new LinearSpring(outterMembrane.get(10),budOuterMembrane.get(12),k,b));
				springs.add(new LinearSpring(innerMembrane.get(10),budOuterMembrane.get(13),k,b));
				springs.add(new LinearSpring(innerMembrane.get(10),budOuterMembrane.get(12),k,b));
				
				springs.add(new LinearSpring(outterMembrane.get(11),budOuterMembrane.get(11),k,b));
				springs.add(new LinearSpring(outterMembrane.get(11),budOuterMembrane.get(10),k,b));
				springs.add(new LinearSpring(innerMembrane.get(11),budOuterMembrane.get(11),k,b));
				springs.add(new LinearSpring(innerMembrane.get(11),budOuterMembrane.get(10),k,b));
			

				
				//Putting my list of particles in this list which I guess is then drawn on the window
				particles.addAll(budInnerMembrane);
				particles.addAll(budOuterMembrane);
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
				springs.add(new LinearSpring(outerNuc.get(outerNuc.size()-1),innerNuc.get(0),k,b));
				springs.add(new LinearSpring(outerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new LinearSpring(innerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new LinearSpring(outerNuc.get(outerNuc.size()-1),outerNuc.get(0),k,b));
				
				for(int i = 0; i < outerNuc.size(); i++){
					springs.add(new LinearSpring(outerNuc.get(i),innerNuc.get(i),k,b));
					if(i+1 < outerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i),outerNuc.get(i+1),k,b));
						springs.add(new LinearSpring(innerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < innerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < outerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i+1),innerNuc.get(i),k,b));
					}
				}
				
				particles.addAll(innerNuc);
				particles.addAll(outerNuc);
				
				//outerNuc.get(21)
				//budInnerMembrane.get(14)
				
				//  the gap between subsequent particles in the chain
				stepSize = 10;
				
				MTchainParticles = new LinkedList<Particle>();
				MTchainSprings = new LinkedList<LinearSpring>();
				
				numberOfParticlesInChain = 37;
				
				lastParticle = budInnerMembrane.get(40);		//  y-coordinate of attachment point
				lastY = lastParticle.p.y;
				
				for(int i=0; i<numberOfParticlesInChain; i++){
					
					Particle newParticle;
					
					double newY = lastY+stepSize;
					newParticle = new Particle(x0, newY, 0, 0);
					
					newSpring = new LinearSpring(lastParticle, newParticle, k, b);
					
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
				newSpring = new LinearSpring(lastParticle, outerNuc.get(48), k, b);
				springs.add(newSpring);
				
				break;
				

			case 5: // ball
/*				int x0 = (int) (winsize.width / 2.0);
				int y0 = (int) (winsize.height / 2.0);
				double r = 100;
				List<Particle> budInnerMembrane = new LinkedList<Particle>();

				Particle pi, pn, po;
				pi = new Particle(x0 + r * Math.cos(0), y0 + r * Math.sin(0), 0, 0);
				budInnerMembrane.add(pi);
				po = pi;

				double dt = 2 * Math.PI / 8;
				for (double angle = dt; angle < 2 * Math.PI; angle += dt) {

					pn = new Particle(x0 + r * Math.cos(angle), y0 + r
							* Math.sin(angle), 0, 0);

					// springs.add( new LinearSpring(po, pn));
					
					if (angle >= Math.PI + dt) {
						pn.heavy = true;
					}

					budInnerMembrane.add(pn);

					po = pn;
				}
				springs.add(new LinearSpring(pi, po, k, b));

				for (Particle p1l : budInnerMembrane) {
					for (Particle p2l : budInnerMembrane) {
						if (p1l != p2l)
							springs.add(new LinearSpring(p1l, p2l, k, b));
					}
				}

				
*/
				// Lavallee's Code
				//List of particles I need to fill
				budInnerMembrane = new LinkedList<Particle>();
				budOuterMembrane = new LinkedList<Particle>();
				innerMembrane = new LinkedList<Particle>();
				outterMembrane = new LinkedList<Particle>();
				
				//Coordonates of the center of the window
				x0 = (int) (simsize.width / 2.0);
				y0 = (int) (simsize.height / 2.0);

	
				
				//Part Creating my ellipse
				counter = 0;
				int budBreakPoint = 7;
				//PI/34
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > budBreakPoint && counter < 29){
						continue;
					}
					double B = ((simsize.height/2)-50);
					double A = 200.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0), 0, 0);
					//Addind those particles to the list
					budInnerMembrane.add(pF);	
				}
				
			
				
				counter = 0;
				//PI/34
				for(double t = 0; t <= (2*Math.PI); t = t + 0.0924){
					counter++;
					if(counter > budBreakPoint && counter < 29){
						continue;
					}
					double B = ((simsize.height/2)-30);
					double A = 220.0;
					int x = (int)(A*Math.cos(t));
					int y = (int)(B*Math.sin(t));
					//Creating my particles at the wanted position
					pF = new Particle(x+x0,(y+y0), 0, 0);
					//Addind those particles to the list
					budOuterMembrane.add(pF);	
				}

				
				
				springs.add(new LinearSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budInnerMembrane.get(0),k,b));
				springs.add(new LinearSpring(budOuterMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
				springs.add(new LinearSpring(budInnerMembrane.get(0),budInnerMembrane.get(budInnerMembrane.size()-1),k,b));
				springs.add(new LinearSpring(budOuterMembrane.get(budOuterMembrane.size()-1),budOuterMembrane.get(0),k,b));
				
				for(int i = 0; i < budOuterMembrane.size(); i++){
					springs.add(new LinearSpring(budOuterMembrane.get(i),budInnerMembrane.get(i),k,b));
					if(i+1 < budOuterMembrane.size() && (i!=(budBreakPoint-1))){
						springs.add(new LinearSpring(budOuterMembrane.get(i),budOuterMembrane.get(i+1),k,b));
						springs.add(new LinearSpring(budInnerMembrane.get(i),budInnerMembrane.get(i+1),k,b));
					}
					if(i+1 < budInnerMembrane.size() && (i!= (budBreakPoint-1))){
						springs.add(new LinearSpring(budOuterMembrane.get(i),budInnerMembrane.get(i+1),k,b));
					}
					if(i+1 < budOuterMembrane.size() && (i!=(budBreakPoint-1))){
						springs.add(new LinearSpring(budOuterMembrane.get(i+1),budInnerMembrane.get(i),k,b));
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
					springs.add(new LinearSpring(outterMembrane.get(i),innerMembrane.get(i),k,b));
					if(i+1 < outterMembrane.size() && (i!=indexBreak)){
						springs.add(new LinearSpring(outterMembrane.get(i),outterMembrane.get(i+1),k,b));
						springs.add(new LinearSpring(innerMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < innerMembrane.size() && (i!=indexBreak)){
						springs.add(new LinearSpring(outterMembrane.get(i),innerMembrane.get(i+1),k,b));
					}
					if(i+1 < outterMembrane.size() && (i!=indexBreak)){
						springs.add(new LinearSpring(outterMembrane.get(i+1),innerMembrane.get(i),k,b));
					}
				}
				
				springs.add(new LinearSpring(outterMembrane.get(indexBreak),budOuterMembrane.get((budBreakPoint+1)),k,b));
				springs.add(new LinearSpring(outterMembrane.get(indexBreak),budOuterMembrane.get((budBreakPoint)),k,b));
				springs.add(new LinearSpring(innerMembrane.get(indexBreak),budOuterMembrane.get((budBreakPoint+1)),k,b));
				springs.add(new LinearSpring(innerMembrane.get(indexBreak),budOuterMembrane.get((budBreakPoint)),k,b));
				
				springs.add(new LinearSpring(outterMembrane.get(indexBreak+1),budOuterMembrane.get(budBreakPoint-1),k,b));
				springs.add(new LinearSpring(outterMembrane.get(indexBreak+1),budOuterMembrane.get((budBreakPoint-2)),k,b));
				springs.add(new LinearSpring(innerMembrane.get(indexBreak+1),budOuterMembrane.get(budBreakPoint-1),k,b));
				springs.add(new LinearSpring(innerMembrane.get(indexBreak+1),budOuterMembrane.get((budBreakPoint-2)),k,b));
			

				//Putting my list of particles in this list which I guess is then drawn on the window
				particles.addAll(budInnerMembrane);
				particles.addAll(budOuterMembrane);
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
				springs.add(new LinearSpring(outerNuc.get(outerNuc.size()-1),innerNuc.get(0),k,b));
				springs.add(new LinearSpring(outerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new LinearSpring(innerNuc.get(0),innerNuc.get(innerNuc.size()-1),k,b));
				springs.add(new LinearSpring(outerNuc.get(outerNuc.size()-1),outerNuc.get(0),k,b));
				
				for(int i = 0; i < outerNuc.size(); i++){
					springs.add(new LinearSpring(outerNuc.get(i),innerNuc.get(i),k,b));
					if(i+1 < outerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i),outerNuc.get(i+1),k,b));
						springs.add(new LinearSpring(innerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < innerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i),innerNuc.get(i+1),k,b));
					}
					if(i+1 < outerNuc.size()){
						springs.add(new LinearSpring(outerNuc.get(i+1),innerNuc.get(i),k,b));
					}
				}
				
				particles.addAll(innerNuc);
				particles.addAll(outerNuc);
				
				//outerNuc.get(21)
				//budInnerMembrane.get(14)
				
				//  the gap between subsequent particles in the chain
				stepSize = 10;
				
				MTchainParticles = new LinkedList<Particle>();
				MTchainSprings = new LinkedList<LinearSpring>();
				
				numberOfParticlesInChain = 37;
				
				lastParticle = budInnerMembrane.get(30);		//  y-coordinate of attachment point
				lastY = lastParticle.p.y;
				
				for(int i=0; i<numberOfParticlesInChain; i++){
					
					Particle newParticle;
					
					double newY = lastY+stepSize;
					newParticle = new Particle(x0, newY, 0, 0);
					
					newSpring = new LinearSpring(lastParticle, newParticle, k, b);
					
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
				newSpring = new LinearSpring(lastParticle, outerNuc.get(48), k, b);
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
