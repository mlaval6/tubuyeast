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

import java.util.List;

import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

import com.sun.j3d.utils.behaviors.interpolators.CubicSplineCurve;

/**
 * A cubic spline object. Use the SplineFactory class to create splines of this
 * type.
 * 
 * @author <a href="mailto:jacob.dreyer@geosoft.no">Jacob Dreyer</a>
 */
class CubicSpline extends Spline {
	
	private boolean closed = false;
	/**
	 * Construct a cubic spline. Package local; Use the SplineFactory to create
	 * splines of this type. The control points are used according to the
	 * definition of cubic splines.
	 * 
	 * @param points
	 *            Control points of spline (x0,y0,z0,x1,y1,z1,...)
	 * @param nParts
	 *            Number of parts in generated spline.
	 */
	CubicSpline(List<Point3d> points, int nParts, boolean closed) {
		initialize(points, nParts, closed);
	}

	protected void initialize(List<Point3d> points, int nParts, boolean closed) {
		nParts_ = nParts;
		this.closed = closed;
		
		// Endpoints are added three times to get them include in the
	    // generated array
		
		int size = 3 * (points.size() + 3);
		int n = 3 * points.size();
		
		controlPoints_ = new double[size];

		Point3d p0 = points.get(0);
		Point3d p1 = points.get(1);
		Point3d pn = points.get(points.size()-1);
				
		// Add the first point twice
		controlPoints_[0] = p0.x;
		controlPoints_[1] = p0.y;
		controlPoints_[2] = p0.z;
//		controlPoints_[3] = p0.x;
//		controlPoints_[4] = p0.y;
//		controlPoints_[5] = p0.z;

		// Add all control points
		int off = 3;
		for (int i = 0; i < points.size(); i++) {
			Point3d p = points.get(i);
			controlPoints_[off + 3 * i + 0] = p.x;
			controlPoints_[off + 3 * i + 1] = p.y;
			controlPoints_[off + 3 * i + 2] = p.z;
		}
		// Add the last point twice
//		controlPoints_[size-12] = p0.x;
//		controlPoints_[size-11] = p0.y;
//		controlPoints_[size-10] = p0.z;
		
//		controlPoints_[size-9] = p0.x;
//		controlPoints_[size-8] = p0.y;
//		controlPoints_[size-7] = p0.z;

		// Add the last point twice
		controlPoints_[size-6] = p0.x;
		controlPoints_[size-5] = p0.y;
		controlPoints_[size-4] = p0.z;
		
		controlPoints_[size-3] = p0.x;
		controlPoints_[size-2] = p0.y;
		controlPoints_[size-1] = p0.z;

//		if (closed) {
//			// Add the first point again
//			controlPoints_[n + 3] = p0.x;
//			controlPoints_[n + 4] = p0.y;
//			controlPoints_[n + 5] = p0.z;
//			
//			// Add the second point again (for continuity)
//			controlPoints_[n + 6] = p1.x;
//			controlPoints_[n + 7] = p1.y;
//			controlPoints_[n + 8] = p1.z;
//			
////			// Add the second point again (for continuity)
////			controlPoints_[n + 9] = p1.x;
////			controlPoints_[n + 10] = p1.y;
////			controlPoints_[n + 11] = p1.z;
//
//		}
//		else {
//			controlPoints_[n + 3] = points.get(points.size()-1).x;
//			controlPoints_[n + 4] = points.get(points.size()-1).y;
//			controlPoints_[n + 5] = points.get(points.size()-1).z;
//		}

	}

	/**
	 * Generate this spline. Endpoints are the same for a closed spline.
	 * 
	 * @return Coordinates of the spline (x0,y0,z0,x1,y1,z1,...)
	 */
	double[] generate() {

		// Number of control points
		int n = controlPoints_.length / 3;
		
		// Number of divisions in between control points 
		int length = (n - 3) * nParts_ + 1;
		double spline[] = new double[length * 3];

		// Set the first control point
		p(2, 0, controlPoints_, spline, 0);

		int index = 3;
		
		// If the spline is closed reuse the second point
		if (closed) {
			for (int i = 2; i < n - 1; i++) {
				for (int j = 1; j <= nParts_; j++) {
					p(i, j / (double) nParts_, controlPoints_, spline, index);
					index += 3;
				}
			}
		}
		else {
			// first loop skips last control point index
			for (int i = 2; i < n - 1; i++) {
				// second loop goes through the number of divisions required
				for (int j = 1; j <= nParts_; j++) {
					// t = goes from 0 to 1, depending on which point we are currently interpolating
					p(i, j / (double) nParts_, controlPoints_, spline, index);
					index += 3;
				}
			}
		}
 
		return spline;
	}

	/**
	 * Interpolate at the given index.
	 * @param i
	 * @param t
	 * @param cp
	 * @param spline
	 * @param index
	 */
	private void p(int i, double t, double cp[], double spline[], int index) {
		double x = 0.0;
		double y = 0.0;
		double z = 0.0;
		int n = cp.length / 3;
//		System.out.println(i);
		
		boolean bo = false;

		// If this is a closed loop we can use a modulus
		if (closed && index == 0 && bo) {
			int[] inds = new int[] { n-1, 0, 1, 2};
			
			int j = -2;
			for (int k : inds) {
				double b = blend(j++, t);
				x += b * cp[3*k];
				y += b * cp[3*k+1];
				z += b * cp[3*k+2];
			}
		}
		else if (closed && i >= n - 2 && bo) {
//			System.out.println(i);
//			int[] inds = new int[] { n-1, 0, 1, 2};
//			int[] inds = new int[] { n-2, n-1, 0, 1};
			int[] inds = new int[] { n-3, n-2, n-1, 0};
//			int[] inds = new int[] { n-4, n-3, n-2, n-1};
			
			int j = -2;
			for (int k : inds) {
				double b = blend(j++, t);
				x += b * cp[3*k];
				z += b * cp[3*k+2];
				y += b * cp[3*k+1];
			}
		}
		else {
			int k = (i - 2) * 3;
			// Use four points, j = -2, -1, 0, 1
			for (int j = -2; j <= 1; j++) {
				// TODO: Precompute blending matrix
				double b = blend(j, t);

				x += b * cp[k++];
				y += b * cp[k++];
				z += b * cp[k++];
			}
		}

		spline[index + 0] = x;
		spline[index + 1] = y;
		spline[index + 2] = z;
	}

	protected double blend(int i, double t) {
		if (i == -2)
			return (((-t + 3) * t - 3) * t + 1) / 6;
		else if (i == -1)
			return (((3 * t - 6) * t) * t + 4) / 6;
		else if (i == 0)
			return (((-3 * t + 3) * t + 3) * t + 1) / 6;
		else
			return (t * t * t) / 6;
	}
}
