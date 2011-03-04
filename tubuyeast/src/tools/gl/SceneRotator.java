package tools.gl;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import tools.parameters.BooleanParameter;
import tools.parameters.DoubleParameter;
import tools.swing.VerticalFlowPanel;

public class SceneRotator {
    private double[] rot_angle = new double[3];
    private DoubleParameter xspeed = new DoubleParameter("x speed", 0.25, -2, 2);
    private DoubleParameter yspeed = new DoubleParameter("y speed", 0.25, -2, 2);
    private DoubleParameter zspeed = new DoubleParameter("z speed", 0.25, -2, 2);
    private BooleanParameter quadrot = new BooleanParameter("quad rotate", false);
    
    public SceneRotator() {
        this(false, false, false);
    }
    
    public SceneRotator(boolean xrot, boolean yrot, boolean zrot) {
        xspeed.setChecked(xrot);
        yspeed.setChecked(yrot);
        zspeed.setChecked(zrot);
    }
    
    public void rotate(GLAutoDrawable drawable) {
    	if (quadrot.getValue()) {
    		quadrotate(drawable);
    	}
    	
        GL gl = drawable.getGL();
        
        gl.glRotated(rot_angle[0], 1, 0, 0);
        gl.glRotated(rot_angle[1], 0, 1, 0);
        gl.glRotated(rot_angle[2], 0, 0, 1);
        
//        rot_angle[0] += xspeed.isChecked() ? xspeed.getValue() : -rot_angle[0];
//        rot_angle[1] += yspeed.isChecked() ? yspeed.getValue() : -rot_angle[1];
//        rot_angle[2] += zspeed.isChecked() ? zspeed.getValue() : -rot_angle[2];
        rot_angle[0] += xspeed.isChecked() ? xspeed.getValue() : 0;
        rot_angle[1] += yspeed.isChecked() ? yspeed.getValue() : 0;
        rot_angle[2] += zspeed.isChecked() ? zspeed.getValue() : 0;
    }

	private int[] rots = new int[3];
	{
		for (int i = 0; i < 3; i++)
			rots[i] = 1;
	}

    /**
     * Rotate in the positive quadrant back and forth.
     * @param drawable
     */
    private void quadrotate(GLAutoDrawable drawable) {
        GL gl = drawable.getGL();

        gl.glRotated(rot_angle[0], 1, 0, 0);
        gl.glRotated(rot_angle[1], 0, 1, 0);
        gl.glRotated(rot_angle[2], 0, 0, 1);

        double mod = 90;
        for (int i = 0; i < 3; i++) {
        	if (rot_angle[i] < 0) {
        		rots[i] = 1;
        	}
        	else if (rot_angle[i] > mod) {
        		rots[i] = -1;
        	}
        }
        
        rot_angle[0] += xspeed.isChecked() ? rots[0]*xspeed.getValue() : -rot_angle[0];
        rot_angle[1] += yspeed.isChecked() ? rots[1]*yspeed.getValue() : -rot_angle[1];
        rot_angle[2] += zspeed.isChecked() ? rots[2]*zspeed.getValue() : -rot_angle[2];
        
    }

    
    public JPanel getControls() {
        VerticalFlowPanel vfp = new VerticalFlowPanel();
        vfp.setBorder(BorderFactory.createTitledBorder(BorderFactory
                .createEtchedBorder(), "Scene Rotator"));

        vfp.add(quadrot.getControls());
        vfp.add(xspeed.getSliderControlsExtended());
        vfp.add(yspeed.getSliderControlsExtended());
        vfp.add(zspeed.getSliderControlsExtended());
        
        JPanel pview = new JPanel(new GridLayout(2, 2));
        
        JButton reset = new JButton("front view");
        JButton top = new JButton("top view");
        JButton side = new JButton("side view");
        JButton otherside = new JButton("side view 2");
        
        reset.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		reset();
        	}
        });

        top.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		top();
        	}
        });
        side.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		side();
        	}
        });
        otherside.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent e) {
        		otherside();
        	}
        });
        
        pview.add(reset);
        pview.add(top);
        pview.add(side);
        pview.add(otherside);
        vfp.add(pview);
        return vfp.getPanel();
    }
    
    private double elevation = 25;
    public void reset() {
    	elevation = 35;
		rot_angle[0] = elevation;
		rot_angle[1] = 0;
		rot_angle[2] = 0;
    }
    public void top() {
		rot_angle[0] = 90;
		rot_angle[1] = 0;
		rot_angle[2] = 0;
    }
    public void side() {
		rot_angle[0] = elevation-10;
		rot_angle[1] = -70;
		rot_angle[2] = 0;
    }
    public void otherside() {
		rot_angle[0] = elevation-10;
		rot_angle[1] = 70;
		rot_angle[2] = 0;
    }

}
