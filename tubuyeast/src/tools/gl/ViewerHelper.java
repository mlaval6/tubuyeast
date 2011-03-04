/*
 * Created on Mar 27, 2005
 */
package tools.gl;

import java.util.EmptyStackException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLException;
import javax.media.opengl.glu.GLU;
import javax.vecmath.Point3d;

import com.sun.opengl.util.GLUT;

/**
 * Some helpers for drawing with the viewer, for example, using the regular 
 * pipeline while using shadows (useful for lines, unlit geometry, etc.).
 * @author kry
 */
public class ViewerHelper {
    
    /**
     * Please use me instead of your own glut, though I may not be threadsafe!
     */
    static public GLUT glut = new GLUT();     
    
    /**
     * A <code>GLU</code> object (like a <code>GLUT</code> object) should
     * only be used by one thread at a time. So create your own if you need one.
     */
    private static GLU glu = new GLU();
    
    /**
     * This can be set to false to make these helper functions still work on
     * systems with limited graphics hardware
     */
    public static boolean isProgrammable = true;
    
    /**
     * Uninstalls any currently installed program from the graphics processors.
     * Fixed functionality is restored to the pipeline.
     * 
     * @param gl
     *        the OpenGL context
     */
    public static void useNoProgramObject(GL gl)
    {
        gl.glUseProgramObjectARB(0);
    }

    /**
     * The program stacks used by <code>pushProgram()</code>/
     * <code>popProgram()</code>. Since a different program can be active in
     * each GL context, there is a different stack for each context.
     */
    private static Map<GL, Stack<Integer>> programStacks = new HashMap<GL, Stack<Integer>>();

    /**
     * Pushes a record of the current Slang Program for the specified GL
     * context. In the same manner as <code>GL.glPushAttrib()</code>, this
     * allows the rendering state to be restored (by calling
     * {@link #popProgram(GL)}).
     * 
     * @param gl
     *        the OpenGL context for which to push the active Slang Program
     *        record
     */
    public static void pushProgram(GL gl)
    {
        Stack<Integer> programStack = programStacks.get(gl);
        if (programStack == null)
        {
            programStack = new Stack<Integer>();
            programStacks.put(gl, programStack);
        }
        int currentProgram = gl.glGetHandleARB(GL.GL_PROGRAM_OBJECT_ARB);
        programStack.push(currentProgram);
    }

    /**
     * Pops the Slang Program state. The specified GL context is set to use the
     * program (or fixed functionality) that was in use before the most recent
     * call to {@link #pushProgram(GL)}(with the same <code>GL</code>).
     * 
     * @param gl
     *        the OpenGL context for which to pop the active Slang Program
     */
    public static void popProgram(GL gl)
    {
        Stack<Integer> programStack = programStacks.get(gl);
        if (programStack == null)
        {
            throw new RuntimeException("Slang Program stack underflow:"
                                     + "popProgram(GL) was called for a"
                                     + " GL context in which the Slang"
                                     + " Program state had not been"
                                     + " pushed.", new EmptyStackException());
        }
        gl.glUseProgramObjectARB(programStack.pop());
        if (programStack.isEmpty())
        {
            // Delete an empty stack, to avoid memory leaks in situations where
            // the GL for a given window changes
            programStacks.remove(gl);
        }
    }

    /**
     * Beging drawing with the regular opengl pipeline. <p>
     * NOTE: this is hacked to assume that you're using shadows and
     * these shadows are bound to units 3 and 4.  This could perhaps be 
     * fixed to work properly with arbitrary shadow texture settings, but
     * was necessary because push and pop attrib calls do not seem to 
     * save and restore this state properly.
     * @param gl 
     */
    static public void beginFixedPipelineMode(GL gl) {
        if ( ! isProgrammable ) return;
        ViewerHelper.pushProgram(gl);
        ViewerHelper.useNoProgramObject(gl);
        gl.glDisable( GL.GL_LIGHTING );         
        gl.glActiveTexture(GL.GL_TEXTURE0 + 3);
        gl.glDisable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0 + 4);
        gl.glDisable(GL.GL_TEXTURE_2D);
    }
    
    /**
     * Restore the previous vertex program (i.e., shadowed lighting mode)
     * NOTE: this is hacked to assume that you're using shadows and
     * these shadows are bound to units 3 and 4.  This could perhaps be 
     * fixed to work properly with arbitrary shadow texture settings, but
     * was necessary because push and pop attrib calls do not seem to 
     * save and restore this state properly.
     * @param gl 
     */
    static public void endFixedPipelineMode(GL gl) {
        if ( ! isProgrammable ) return;
        gl.glActiveTexture(GL.GL_TEXTURE0 + 3);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0 + 4);
        gl.glEnable(GL.GL_TEXTURE_2D);
        gl.glActiveTexture(GL.GL_TEXTURE0);  
        ViewerHelper.popProgram(gl);
    }
    
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
    }
    
    /**
     * Draws multi-line text.
     * @param drawable
     * @param text Text lines to draw, delimited by '\n'.
     */
    static public void printTextLines( GLAutoDrawable drawable, String text ) {
        GL gl = drawable.getGL();
        gl.glColor3f(1,1,1);
        printTextLines( drawable, text, 10, 10, 10, GLUT.BITMAP_HELVETICA_10 );
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
    static public void printTextLines(GLAutoDrawable drawable,
                                      String text,
                                      double x,
                                      double y,
                                      double h,
                                      int font)
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

    static public void print3dTextLines(GLAutoDrawable drawable, String text, Point3d p) {
    	print3dTextLines(drawable, text, p.x, p.y, p.z);
    }

    static public void print3dTextLines(GLAutoDrawable drawable, String text, double x, double y, double z) {
    	GL gl = drawable.getGL();
        gl.glDisable(GL.GL_LIGHTING);
        gl.glEnable(GL.GL_LINE_SMOOTH);

//        gl.glColor3f(0, 0, 0);
        gl.glRasterPos3d(x, y, z);
        ViewerHelper.glut.glutBitmapString(GLUT.BITMAP_9_BY_15, text);

    }
    
    /**
     * End drawing overlay.
     * @param drawable
     */
    static public void endOverlay( GLAutoDrawable drawable ) {
        GL gl = drawable.getGL();
        gl.glPopMatrix();
        gl.glMatrixMode(GL.GL_MODELVIEW);
        gl.glPopMatrix();
        gl.glPopAttrib();        
    }    
    
    public static int checkFramebufferStatus(GL gl, StringBuilder statusString)
    {
        int framebufferStatus = gl
            .glCheckFramebufferStatusEXT(GL.GL_FRAMEBUFFER_EXT);
        switch (framebufferStatus)
        {
            case GL.GL_FRAMEBUFFER_COMPLETE_EXT:
                statusString.append("GL_FRAMEBUFFER_COMPLETE_EXT");
                break;
            case GL.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT_EXT:
                statusString.append("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENTS_EXT");
                break;
            case GL.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT:
                statusString.append("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT_EXT");
                break;
            case GL.GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT:
                statusString.append("GL_FRAMEBUFFER_INCOMPLETE_DIMENSIONS_EXT");
                break;
            case GL.GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT:
                statusString.append("GL_FRAMEBUFFER_INCOMPLETE_FORMATS_EXT");
                break;
            case GL.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT:
                statusString.append("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER_EXT");
                break;
            case GL.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT:
                statusString.append("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER_EXT");
                break;
            case GL.GL_FRAMEBUFFER_UNSUPPORTED_EXT:
                statusString.append("GL_FRAMEBUFFER_UNSUPPORTED_EXT");
                break;
        }
        return framebufferStatus;
    }
    
    public static void assertFramebufferComplete(GL gl)
    {
        StringBuilder errorString = new StringBuilder();
        int framebufferStatus = checkFramebufferStatus(gl, errorString);
        if (framebufferStatus == GL.GL_FRAMEBUFFER_COMPLETE_EXT)
        {
            return;
        }
        StringBuilder buf = new StringBuilder("glCheckFrameBufferStatus()"
                                              + " returned the following"
                                              + " incomplete status: "
                                              + errorString);
        throw new GLException(buf.toString());
    }
}
