package tools.gl;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import javax.imageio.ImageIO;
import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLCanvas;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;
import javax.swing.JFrame;
import javax.vecmath.Vector3d;

import tools.swing.AdapterState;
import tools.swing.ControlFrame;

import com.sun.opengl.util.FPSAnimator;
import com.sun.opengl.util.GLUT;
import com.sun.opengl.util.ImageUtil;


/**
 * @author kry
 * @author piuze
 */
public class OpenglViewer implements GLEventListener {
    
    private AdapterState adapterState;
    
    /**
     * The trackball and camera
     */
    private TrackBallCamera trackBall;
    
    /**
     * The scene, needed in the loaded architecture when loading cameras
     */
    private SceneGraphNode scene;
    
    /**
     * The frame containing all controls for this viewing application
     */
    public ControlFrame controlFrame;
    
    /**
     * The dimension of the display screen
     */
    public Dimension size;
   
    public JFrame frame;
    
    private GLCanvas glCanvas;
    
    public GLCanvas getCanvas() {
    	return glCanvas;
    }
    
    /**
     * Note: fullscreen mode has issues with respect to getting
     * the size of the drawable.  This should most certainly not be used!
     */
    private boolean fullscreen = false;

    /**
     * Device used for full screen mode
     */
    private GraphicsDevice usedDevice;

    private FPSAnimator animator;
    
    /**
     * Creates a viewer for the given scene
     * @param name
     * @param scene
     */
    public OpenglViewer( String name, SceneGraphNode scene ) {
        this( name, scene, new Dimension(1280,800), new Dimension(600, 750), true);
    }

    /**
     * Creates a new easy viewer with given sizes for display and controls
     * @param name
     * @param scene
     * @param size
     * @param controlSize
     */
    public OpenglViewer( String name, SceneGraphNode scene, Dimension size, Dimension controlSize) {
        this(name,scene,size,controlSize, new Vector3d(10,10,10), new Vector3d(-10,10,10), true);
    }    

    /**
     * Creates a new easy viewer with given sizes for display and controls
     * @param name
     * @param scene
     * @param size
     * @param controlSize
     */
    public OpenglViewer( String name, SceneGraphNode scene, Dimension size, Dimension controlSize, boolean attachTrackBall) {
        this(name,scene,size,controlSize, new Vector3d(10,10,10), new Vector3d(-10,10,10), attachTrackBall);
    }    
    
    /**
     * Creates a new easy viewer with given sizes for display and controls
     * @param name
     * @param scene
     * @param size
     * @param controlSize
     * @param light1Pos
     * @param light2Pos
     */
    public OpenglViewer( String name, SceneGraphNode scene, Dimension size, Dimension controlSize, Vector3d light1Pos, Vector3d light2Pos ) {
        this(name, scene, size, controlSize, light1Pos, light2Pos, true);
    }
    
    /**
     * Creates a new easy viewer with given sizes for display and controls
     * @param name
     * @param scene
     * @param size
     * @param controlSize
     * @param light1Pos
     * @param light2Pos
     * @param attachTrackBall If the trackball should be receiving event notifications.
     */
    public OpenglViewer( String name, SceneGraphNode scene, Dimension size, Dimension controlSize, Vector3d light1Pos, Vector3d light2Pos, boolean attachTrackBall ) {
        
        this.scene = scene;
        this.size = size;

        adapterState = new AdapterState();
        
        trackBall = new TrackBallCamera();
                                      
        controlFrame = new ControlFrame("Controls");
        // We'll disable the camera tab for now, as nobody should need it until assignment 3
        // well, then again, perhaps assignment 2??
        String sname = scene.getName() == null || scene.getName().equals("") ? "Scene" : scene.getName();
        controlFrame.add("Camera", trackBall.getControls());
        controlFrame.add(sname, scene.getControls());
        controlFrame.setSelectedTab(scene.getName());
                                
        controlFrame.setSize(controlSize.width, controlSize.height);
        controlFrame.setLocation(size.width + 20, 0);
        controlFrame.setVisible(true);    
        
        controlFrame.setSelectedTab(sname);
                
        glCanvas = new GLCanvas(new GLCapabilities());
        glCanvas.setSize( size.width, size.height );
        glCanvas.setIgnoreRepaint( true );
        glCanvas.addGLEventListener( this );

        frame = new JFrame( name );
        frame.setUndecorated(true);
        frame.getContentPane().setLayout( new BorderLayout() );
        frame.getContentPane().add( glCanvas, BorderLayout.CENTER );
        frame.setLocation(0,0);
        
        animator = new FPSAnimator( glCanvas, 60 );
        animator.setRunAsFastAsPossible(false);
        
        if (attachTrackBall)
            addInteractor(trackBall);        
        
        glCanvas.addMouseListener(adapterState);
        glCanvas.addMouseMotionListener(adapterState);
        glCanvas.addKeyListener(adapterState);
    }

    public TrackBallCamera getCamera() {
        return trackBall;
    }
    
    /**
     * Starts the viewer
     */
    public void start() {
        addInteractor(trackBall);
        
        try {
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            frame.setUndecorated( fullscreen );

            frame.addWindowListener( new WindowAdapter() {
                @Override
                public void windowClosing( WindowEvent e ) {
                    stop();
                }
            });

            if ( fullscreen ) {
                usedDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
                usedDevice.setFullScreenWindow( frame );
                usedDevice.setDisplayMode(
                        findDisplayMode(
                                usedDevice.getDisplayModes(),
                                screenSize.width, screenSize.height, //size.width, size.height,
                                usedDevice.getDisplayMode().getBitDepth(),
                                usedDevice.getDisplayMode().getRefreshRate()
                        )
                );
            } else {
                frame.setSize( frame.getContentPane().getPreferredSize() );
//                frame.setLocation(
//                        ( screenSize.width - frame.getWidth() ) / 2,
//                        ( screenSize.height - frame.getHeight() ) / 2
//                );
                frame.setVisible( true );
            }

            glCanvas.requestFocus();

            if ( animator != null ) animator.start();
            
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }
    
    /**
     * Stops the viewer
     */
    public void stop() {
        try {
            if ( animator != null ) animator.stop();
            if ( fullscreen ) {
                usedDevice.setFullScreenWindow( null );
                usedDevice = null;
            }
            frame.dispose();
        } catch ( Exception e ) {
            e.printStackTrace();
        } finally {
            System.exit( 0 );
        }
    }
    
    private static final int DONT_CARE = -1;

    private DisplayMode findDisplayMode( DisplayMode[] displayModes, int requestedWidth, int requestedHeight, int requestedDepth, int requestedRefreshRate ) {
        // Try to find an exact match
        DisplayMode displayMode = findDisplayModeInternal( displayModes, requestedWidth, requestedHeight, requestedDepth, requestedRefreshRate );

        // Try again, ignoring the requested bit depth
        if ( displayMode == null )
            displayMode = findDisplayModeInternal( displayModes, requestedWidth, requestedHeight, DONT_CARE, DONT_CARE );

        // Try again, and again ignoring the requested bit depth and height
        if ( displayMode == null )
            displayMode = findDisplayModeInternal( displayModes, requestedWidth, DONT_CARE, DONT_CARE, DONT_CARE );

        // If all else fails try to get any display mode
        if ( displayMode == null )
            displayMode = findDisplayModeInternal( displayModes, DONT_CARE, DONT_CARE, DONT_CARE, DONT_CARE );

        return displayMode;
    }

    private DisplayMode findDisplayModeInternal( DisplayMode[] displayModes, int requestedWidth, int requestedHeight, int requestedDepth, int requestedRefreshRate ) {
        DisplayMode displayModeToUse = null;
        for ( int i = 0; i < displayModes.length; i++ ) {
            DisplayMode displayMode = displayModes[i];
            if ( ( requestedWidth == DONT_CARE || displayMode.getWidth() == requestedWidth ) &&
                    ( requestedHeight == DONT_CARE || displayMode.getHeight() == requestedHeight ) &&
                    ( requestedHeight == DONT_CARE || displayMode.getRefreshRate() == requestedRefreshRate ) &&
                    ( requestedDepth == DONT_CARE || displayMode.getBitDepth() == requestedDepth ) )
                displayModeToUse = displayMode;
        }

        return displayModeToUse;
    }
    
    /**
     * Causes the 3D viewer to be repainted.
     * Currently, this is ignored as we're using an FPSanimator 
     */
    public void redisplay() {        
        //frame.repaint();
        //glCanvas.display();
    }
    
    /**
     * Attaches the provided interactor to this viewer's 3D canvas
     * @param interactor
     */
    public void addInteractor( Interactor interactor ) {
        interactor.attach(glCanvas);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();
        gl.glClear( GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT );
        trackBall.prepareForDisplay(drawable);
        
        {
            // main light is at the top front of the room.
            int lightNumber = 1;
            float[] position = { 0f, 2f, 1f, 1 };
            float[] colour = { 1f, 1f, 1f, 1 };
            float[] acolour = { .05f, .05f, .05f, 1 };
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_SPECULAR, colour, 0);
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_DIFFUSE, colour, 0);
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_AMBIENT, acolour, 0);
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_POSITION, position, 0);
            gl.glEnable( GL.GL_LIGHT0 + lightNumber );
        }
        
        {
            // put a dim light at the back of the room, in case anyone wants to 
            // look at the back side of objects
            int lightNumber = 0;
            float[] position = { 0f, 2f, -2.5f, 1 };
            float[] colour = { .2f, .2f, .2f, 1 };
            float[] acolour = { .0f, .0f, .0f, 1 };
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_SPECULAR, colour, 0);
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_DIFFUSE, colour, 0);
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_AMBIENT, acolour, 0);
            gl.glLightfv(GL.GL_LIGHT0 + lightNumber, GL.GL_POSITION, position, 0);
            gl.glEnable( GL.GL_LIGHT0 + lightNumber );
        }
        
        gl.glEnable( GL.GL_LIGHTING );
        gl.glEnable( GL.GL_NORMALIZE );
        
        scene.display(drawable);
        trackBall.cleanupAfterDisplay(drawable);
        
    }
    
    /**
     * Saves a snapshot of the current canvas to a file.
     * The image is saved in png format and will be of the same size as the canvas.
     * Note that if you are assembling frames saved in this way into a video, 
     * for instance, using virtualdub, then you'll need to take care that the 
     * canvas size is nice (i.e., a multiple of 16 in each dimension), or add 
     * a filter in virtualdub to resize the image to be a codec friendly size.
     * @param drawable
     * @param file
     * @return true on success
     */
    public boolean snapshot( GLAutoDrawable drawable, File file ) {
        GL gl = drawable.getGL();
        int width = drawable.getWidth();
        int height = drawable.getHeight();
        gl.glReadPixels( 0, 0, width, height, GL.GL_ABGR_EXT, GL.GL_UNSIGNED_BYTE, imageBuffer );            
        ImageUtil.flipImageVertically(image);
        
        try {
            if ( ! ImageIO.write( image, "png", file) ) {
                System.err.println("Error writing file using ImageIO (unsupported file format?)");
                return false;
            }
        } catch (IOException e) {    
            System.err.println("trouble writing " + file );
            e.printStackTrace();
            return false;
        }
        
        // print a message in the display window
        beginOverlay( drawable );
        String text =  "RECORDED: "+ file.toString();
        gl.glDisable( GL.GL_LIGHTING );
        gl.glColor4f( 1, 0, 0, 1 );           
        printTextLines( drawable, text, 10, drawable.getHeight()-20, 10, GLUT.BITMAP_HELVETICA_10 );
        gl.glEnable( GL.GL_LIGHTING );
        endOverlay(drawable);
        return true;
    }    

    /** Image for sending to the image processor */
    private BufferedImage image;
    
    /** Image Buffer for reading pixels */
    private Buffer imageBuffer;
    
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {           
        image = new BufferedImage( width, height, BufferedImage.TYPE_4BYTE_ABGR );            
        imageBuffer = ByteBuffer.wrap(((DataBufferByte)image.getRaster().getDataBuffer()).getData());
    }
    
    @Override
    public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged) {
        // do nothing
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        drawable.setGL( new DebugGL(drawable.getGL()) );
        GL gl = drawable.getGL();
        gl.glShadeModel(GL.GL_SMOOTH);              // Enable Smooth Shading
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.5f);    // Black Background
        gl.glClearDepth(1.0f);                      // Depth Buffer Setup
        gl.glEnable(GL.GL_DEPTH_TEST);              // Enables Depth Testing
        gl.glDepthFunc(GL.GL_LEQUAL);               // The Type Of Depth Testing To Do
        scene.init(drawable);
    }
 
    /**
     * GLUT object to be shared (though perhaps not threadsafe)
     */
    static public GLUT glut = new GLUT();     
    
    /**
     * A <code>GLU</code> object (like a <code>GLUT</code> object) should
     * only be used by one thread at a time. So create your own if you need one.
     */
    private static GLU glu = new GLU();
    
    /**
     * Begin drawing overlay (e.g., text, screen pixel coordinate points and 
     * lines)
     * @param drawable
     */
    static public void beginOverlay( GLAutoDrawable drawable ) {
        GL gl = drawable.getGL();
        gl.glPushAttrib( GL.GL_DEPTH_BUFFER_BIT | GL.GL_ENABLE_BIT | 
                GL.GL_FOG_BIT | GL.GL_LIGHTING_BIT | GL.GL_DEPTH_BUFFER_BIT );
        gl.glPushMatrix();
        gl.glLoadIdentity();
        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glPushMatrix();
        gl.glLoadIdentity ();
        int width = drawable.getWidth();
        int height = drawable.getHeight();
        glu.gluOrtho2D( 0, width, height, 0 );
        gl.glDisable(GL.GL_DEPTH_TEST);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glDisable(GL.GL_LIGHTING);
        gl.glMatrixMode( GL.GL_MODELVIEW );

    }
    
    /**
     * Draws multi-line text.
     * @param drawable
     * @param text Text lines to draw, delimited by '\n'.
     */
    static public void printTextLines( GLAutoDrawable drawable, String text ) {
        GL gl = drawable.getGL();
        gl.glColor3f(1,1,1);
        printTextLines( drawable, text, 10, 10, 12, GLUT.BITMAP_HELVETICA_10 );
    }

    /**
     * Draws multi-line text.
     * @param drawable
     * @param text Text lines to draw, delimited by '\n'.
     * @param x    The starting x raster position.
     * @param y    The starting y raster position.
     * @param color
     * @param font The font to use (e.g. GLUT.BITMAP_HELVETICA_10).
     */
    static public void printTextLines( GLAutoDrawable drawable, String text, double x, double y, float[] color, int font) {
        GL gl = drawable.getGL();
        gl.glColor3f(color[0], color[1], color[2]);
        printTextLines( drawable, text, x, y, 12, font );
    }

    /**
     * Draws text.
     * @param drawable
     * @param text The String to draw.
     * @param x    The starting x raster position.
     * @param y    The starting y raster position.
     * @param h    The height of each line of text.
     * @param font The font to use (e.g. GLUT.BITMAP_HELVETICA_10).
     */
    static public void printTextLines(GLAutoDrawable drawable, String text, double x, double y, double h, int font)
    {
        GL gl = drawable.getGL();
        StringTokenizer st = new StringTokenizer( text, "\n" );
        int line = 0;
        while ( st.hasMoreTokens() ) {
            String tok = st.nextToken();
            gl.glRasterPos2d( x, y + line * h );            
            glut.glutBitmapString(font, tok);
            line++;
        }        
    }
    
    /**
     * End drawing overlay.
     * @param drawable
     */
    static public void endOverlay( GLAutoDrawable drawable ) {
        GL gl = drawable.getGL();
        gl.glMatrixMode( GL.GL_PROJECTION );
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glPopAttrib();        
    }    

    public AdapterState getAdapterState() {
        return adapterState;
    }
}
