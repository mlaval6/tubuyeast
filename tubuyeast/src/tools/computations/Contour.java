package tools.computations;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.vecmath.Point3d;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import tools.gl.ViewerHelper;
import tools.parameters.BooleanParameter;
import tools.parameters.DoubleParameter;
import tools.spline.SplineFactory;
import tools.swing.FileChooser;
import tools.xml.XMLHelper;

import com.sun.opengl.util.GLUT;

public class Contour {
	private List<Point3d> points = new LinkedList<Point3d>();

	private double[] ipoints = null;

	private List<Point3d> spline = new LinkedList<Point3d>();

	public DoubleParameter contourres = new DoubleParameter(
			"contour resolution", 3, 1, 100);

	public BooleanParameter draw = new BooleanParameter("draw", true);

	// private BooleanParameter draw = new BooleanParameter("Draw", true);

	/**
	 * Add a new point to the contour.
	 * 
	 * @param p
	 */
	public void addPoint(Point3d p) {

		// Add the new point
		points.add(p);

		generatePoints();
	}

	private boolean sufficient() {
		return points.size() > 2;
	}

	public Point3d getPoint(int index) {
		return points.get(index);
	}

	public Point3d getLastPoint() {
		if (points.size() == 0) return null;
		return points.get(points.size() - 1);
	}

	public void generatePoints() {
		if (!sufficient())
			return;

		// Draw a spline interpolation of the contour points
		ipoints = SplineFactory.createCatmullRom(points, (int) contourres
				.getValue(), true);

		// Add points and skip the last point since it's the same
		// as the first one.
		spline.clear();
		for (int i = 0; i < ipoints.length - 3; i += 3) {
			Point3d p = new Point3d(ipoints[i], ipoints[i + 1], ipoints[i + 2]);
			spline.add(p);
		}
	}

	/**
	 * Clear this contour.
	 */
	public void clear() {
		points.clear();
		spline.clear();
		ipoints = null;
	}

	public void display(GLAutoDrawable drawable) {
		if (!draw.getValue())
			return;

		GL gl = drawable.getGL();

		generatePoints();

		gl.glDisable(GL.GL_LIGHTING);

		if (ipoints != null && ipoints.length > 1) {

			// First draw a linestrip connecting all points.
			gl.glColor4f(1, 0, 0, 0.8f);
			gl.glLineWidth(2);
			gl.glBegin(GL.GL_LINE_STRIP);
			for (Point3d p : spline) {
				gl.glVertex3d(p.x, p.y, p.z);
			}
			Point3d p0 = spline.get(0);
			gl.glVertex3d(p0.x, p0.y, p0.z);
			gl.glEnd();

			// Then draw the interpolated points.
			gl.glPointSize(4);
			gl.glColor4f(1, 1, 1, 0.7f);
			gl.glBegin(GL.GL_POINTS);

			for (Point3d p : spline) {
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();

			// Draw the control points
			gl.glPointSize(10.0f);
			gl.glColor4f(1, 1, 1, 1);
			gl.glBegin(GL.GL_POINTS);
			for (Point3d p : points) {
				gl.glVertex3d(p.x, p.y, p.z);
			}
			gl.glEnd();

			// Draw the indexes
			int j = 1;
			for (Point3d p : points) {
				gl.glRasterPos3d(p.x, p.y, p.z);
				ViewerHelper.glut.glutBitmapString(GLUT.BITMAP_8_BY_13, "P"
						+ (j++));
			}

		}
	}

	private FileChooser fc = new FileChooser();

	public Component getControls() {
		JPanel cpanel = new JPanel();
		cpanel.setBorder(BorderFactory.createTitledBorder(BorderFactory
				.createEtchedBorder(), "Contour"));
		cpanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.ipadx = 0;
		c.ipady = 0;
		c.weightx = 0;
		c.weighty = 0;

		fc.addOpenActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				loadContour(fc.getCurrentFile().getAbsolutePath());
			}

		});

		fc.addSaveActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				saveContour(fc.getCurrentFile().getAbsolutePath());
			}

		});

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		cpanel.add(fc, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 2;
		cpanel.add(contourres.getSliderControls(false), c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		JButton btnClear = new JButton("Clear contour");
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clear();
			}
		});
		cpanel.add(btnClear, c);

		c.gridx = 1;
		c.gridy = 3;
		c.gridwidth = 1;
		cpanel.add(draw.getControls(), c);

		return cpanel;
	}

	private String contourFile;

	public void loadContour(String filename) {
		contourFile = filename;

		fc.setPath(filename);

		clear();

		File f = new File(contourFile);
		System.out.println("Loading contour from " + f.getAbsolutePath());

		Document document = null;

		try {
			document = XMLHelper.openAsDOM(f.getAbsolutePath());

			Element header = document.getDocumentElement();

			Element res = XMLHelper.getFirstChildElementByTagName(header,
					"resolution");
			if (res != null) {
				contourres.setValue(XMLHelper.getDoubleAttribute(res, "value"));
			}

			List<Element> elements = XMLHelper.getChildElementListByTagName(
					header, "Point");
			for (Element e : elements) {

				Point3d p = new Point3d();
				p.x = XMLHelper.getFloatAttribute(e, "xpos", 0);
				p.y = XMLHelper.getFloatAttribute(e, "ypos", 0);
				p.z = XMLHelper.getFloatAttribute(e, "zpos", 0);

				addPoint(p);
			}

		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		generatePoints();
	}

	private void saveContour(String filename) {
		contourFile = filename;

		fc.setPath(filename);

		try {
			// Create file
			FileWriter fstream = new FileWriter(contourFile);
			BufferedWriter out = new BufferedWriter(fstream);

			System.out.println("Saving contour to "
					+ (new File(filename)).getAbsolutePath());

			// XML header
			out.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
			out.write("\n<contour>");

			out.write("\n\t<resolution value=\"" + contourres.getValue()
					+ "\"/>\n");
			for (Point3d p : points) {

				// Info we want to keep:
				// 1) id
				// 2) linkedID (Point3d id for virtual marker and virtual marker
				// id for mocap markers)
				// 3) the rest should be saved in the javabin. with 1) + 2), we
				// should have
				// enough to get started (and this way we won't need to hand
				// pick all
				// the vertices again...)
				out.write("\n\t<Point");
				out.write(" xpos=\"" + p.x + "\"");
				out.write(" ypos=\"" + p.y + "\"");
				out.write(" zpos=\"" + p.z + "\"");
				out.write("/>");
			}

			// Footer
			out.write("\n</contour>");

			out.close();
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}

	public String getPointCount() {
		return "" + spline.size();
	}

	public List<Point3d> getSpline() {
		return spline;
	}

	public List<Point3d> getControlPoints() {
		return points;
	}

	public void scale(double s) {
		for (Point3d p : points) {
			p.scale(s);
		}
		
		for (Point3d p : spline) {
			p.scale(s);
		}
	}

}
