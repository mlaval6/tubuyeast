
/*
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */
package tools.spline;

import java.util.LinkedList;
import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3d;



/**
 * A spline factory instance.
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */   
public class SplineFactory
{
  /**
   * Create a Bezier spline based on the given control points.
   * The generated curve starts in the first control point and ends
   * in the last control point.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...).
   * @param nParts         Number of parts to divide each leg into.
   * @return               Spline (x0,y0,z0,x1,y1,z1,...).
   */
  public static double[] createBezier (List<Point3d> controlPoints, int nParts)
  {
    Spline spline = new BezierSpline (controlPoints, nParts);
    return spline.generate();
  }


  
  /**
   * Create a cubic spline based on the given control points.
   * The generated curve starts in the first control point and ends
   * in the last control point.
   * 
   * @param controlPoints  Control points of spline (x0,y0,z0,x1,y1,z1,...).
   * @param nParts         Number of parts to divide each leg into.
   * @return               Spline (x0,y0,z0,x1,y1,z1,...).
   */
  public static double[] createCubic (List<Point3d> controlPoints, int nParts, boolean closed)
  {
    Spline spline = new CubicSpline (controlPoints, nParts, closed);
    return spline.generate();
  }

  /**
   * Create a Catmull-Rom spline based on the given control points.
   * The generated curve starts in the first control point and ends
   * in the last control point.
   * Im addition, the curve intersects all the control points.
   * 
   * @param c  Control points of spline (x0,y0,z0,x1,y1,z1,...).
   * @param nParts         Number of parts to divide each leg into.
   * @return               Spline (x0,y0,z0,x1,y1,z1,...).
   */
  public static double[] createCatmullRom (List<Point3d> c, int nParts, boolean closed)
  {
    Spline spline = new CatmullRomSpline (c, nParts, closed);
    return spline.generate();
  }

  public static double[] createCatmullRom (List<Point3d> c, int nParts)
  {
	  return createCatmullRom(c, nParts, false);
  }

  
  /**
   * Testing the spline package.
   * 
   * @param args  Not used.
   */
  public static void main (String[] args)
  {
      List<Point3d> c = new LinkedList<Point3d>();
      
      Point3d p0 = new Point3d(0, 0, 0);
      c.add(p0);
    
      Point3d p1 = new Point3d(1, 1, 0);
      c.add(p1);

      Point3d p2 = new Point3d(2, -1, 0);
      c.add(p2);

      Point3d p3 = new Point3d(10, 0, 0);
      c.add(p3);

    double[] spline1 = SplineFactory.createBezier (c,     20);
    double[] spline2 = SplineFactory.createCubic (c,      20, false);
    double[] spline3 = SplineFactory.createCatmullRom (c, 20);        

    System.out.println ("-- Bezier");
    for (int i = 0; i < spline1.length; i+=3)
      System.out.println (spline1[i] + "," + spline1[i+1] + "," + spline1[i+2]);
    
    System.out.println ("-- Cubic");
    for (int i = 0; i < spline2.length; i+=3)
      System.out.println (spline2[i] + "," + spline2[i+1] + "," + spline2[i+2]);

    System.out.println ("-- Catmull-Rom");
    for (int i = 0; i < spline3.length; i+=3)
      System.out.println (spline3[i] + "," + spline3[i+1] + "," + spline3[i+2]);
  }



}

