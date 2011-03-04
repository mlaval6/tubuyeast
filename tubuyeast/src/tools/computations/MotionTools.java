/*
 * Created on Jun 8, 2005
 */
package tools.computations;

import javax.vecmath.Matrix3d;
import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4d;
import javax.vecmath.SingularMatrixException;
import javax.vecmath.Tuple3d;
import javax.vecmath.Tuple3f;
import javax.vecmath.Tuple4d;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4d;
import javax.vecmath.Vector4f;

/**
 * A bunch of static methods for se3 computation.
 * 
 * @author Danny Kaufman
 * @author Paul Kry
 * @author Shinjiro Sueda
 */
public class MotionTools {
	/** The tolerance value used for convergence check */
	public static final double EPSILON = 1e-10;

	/** Maximum number of iterations */
	public static final double MAX_ITERS = 500;

	/**
	 * Private constructor. Use this class in a static way.
	 */
	private MotionTools() {
		// Do nothing.
		// 
		// 
	}

	/**
	 * Separates a 4x4 matrix into a rotation component and a translation
	 * component, taking advantage of the fact that the matrix is known to be
	 * (strictly) a rigid transformation. In essence, this method is simply a
	 * convenience to copy the 3x3 upper left and 3x1 upper right portions of a
	 * 4x4 matrix.
	 * 
	 * @param src
	 *            the original matrix
	 * @param destRot
	 *            the 3x3 matrix into which to copy the rotation part
	 * @param destTrans
	 *            the 3x1 tuple into which to copy the translation part
	 */
	public static void separateRigidTransformation(Matrix4d src,
			Matrix3d destRot, Tuple3d destTrans) {
		destRot.m00 = src.m00;
		destRot.m01 = src.m01;
		destRot.m02 = src.m02;
		destRot.m10 = src.m10;
		destRot.m11 = src.m11;
		destRot.m12 = src.m12;
		destRot.m20 = src.m20;
		destRot.m21 = src.m21;
		destRot.m22 = src.m22;

		destTrans.x = src.m03;
		destTrans.y = src.m13;
		destTrans.z = src.m23;
	}

	/**
	 * Sets m to be the skew symmetric matrix [w].
	 * 
	 * @param m
	 * @param w
	 */
	public static void bracket(Matrix3d m, Vector3d w) {
		m.m00 = 0;
		m.m01 = -w.z;
		m.m02 = w.y;
		m.m10 = w.z;
		m.m11 = 0;
		m.m12 = -w.x;
		m.m20 = -w.y;
		m.m21 = w.x;
		m.m22 = 0;
	}

	/**
	 * Sets m to be the skew symmetric matrix [(x y z)'].
	 * 
	 * @param m
	 * @param x
	 * @param y
	 * @param z
	 */
	public static void bracket(Matrix3d m, double x, double y, double z) {
		m.m00 = 0;
		m.m01 = -z;
		m.m02 = y;
		m.m10 = z;
		m.m11 = 0;
		m.m12 = -x;
		m.m20 = -y;
		m.m21 = x;
		m.m22 = 0;
	}

	/**
	 * Sets w from a skew symmetric matrix m = [w].
	 * 
	 * @param m
	 * @param w
	 */
	public static void unbracket(Matrix3d m, Vector3d w) {
		w.x = m.m21;
		w.y = m.m02;
		w.z = m.m10;
	}

	/**
	 * Sets w from a skew symmetric upper 3x3 portion of matrix m = [w].
	 * 
	 * @param m
	 * @param w
	 */
	public static void unbracket(Matrix4d m, Vector3d w) {
		w.x = m.m21;
		w.y = m.m02;
		w.z = m.m10;
	}

	/**
	 * Sets w from a skew symmetric upper 3x3 portion of matrix m = [w]. Sets v
	 * to be the translational component of m.
	 * 
	 * @param m
	 * @param w
	 * @param v
	 */
	public static void unbracket(Matrix4d m, Vector3d w, Vector3d v) {
		w.x = m.m21;
		w.y = m.m02;
		w.z = m.m10;
		v.x = m.m03;
		v.y = m.m13;
		v.z = m.m23;
	}

	/**
	 * Given normalized R^3 vector of rotation w, we compute exp([w]t) using
	 * Rodrigues' formula:
	 * 
	 * exp([w]t) = I + [w] sin(t) + [w](1-cos(t)).
	 * 
	 * @param R :=
	 *            exp([w]t)
	 * @param w
	 *            Normalized 3D vector.
	 * @param t
	 *            Step size (in radians).
	 */
	public static void expRodrigues(Matrix3d R, Vector3d w, double t) {
		double wX = w.x;
		double wY = w.y;
		double wZ = w.z;
		double c = Math.cos(t);
		double s = Math.sin(t);
		double c1 = 1 - c;

		R.m00 = c + wX * wX * c1;
		R.m10 = wZ * s + wX * wY * c1;
		R.m20 = -wY * s + wX * wZ * c1;

		R.m01 = -wZ * s + wX * wY * c1;
		R.m11 = c + wY * wY * c1;
		R.m21 = wX * s + wY * wZ * c1;

		R.m02 = wY * s + wX * wZ * c1;
		R.m12 = -wX * s + wY * wZ * c1;
		R.m22 = c + wZ * wZ * c1;
	}

	private static Matrix3d R = new Matrix3d();

	private static Vector3d p = new Vector3d();

	private static Matrix3d I = new Matrix3d();

	private static Matrix3d A = new Matrix3d();

	private static Vector3d cc = new Vector3d();

	private static Vector3d d = new Vector3d();

	/**
	 * Returns the frobenius norm of a Matrix4d.
	 * 
	 * @param src
	 *            The matrix to take the norm of
	 * @return The frobenius norm of the matrix.
	 */
	public static double frobenius(Matrix4d src) {
		double norm = 0;
		norm += src.m00 * src.m00;
		norm += src.m01 * src.m01;
		norm += src.m02 * src.m02;
		norm += src.m03 * src.m03;
		norm += src.m10 * src.m10;
		norm += src.m11 * src.m11;
		norm += src.m12 * src.m12;
		norm += src.m13 * src.m13;
		norm += src.m20 * src.m20;
		norm += src.m21 * src.m21;
		norm += src.m22 * src.m22;
		norm += src.m23 * src.m23;
		norm += src.m30 * src.m30;
		norm += src.m31 * src.m31;
		norm += src.m32 * src.m32;
		norm += src.m33 * src.m33;
		return Math.sqrt(norm);
	}

	/**
	 * Takes the square root of a Matrix4d. This is an iterative method. If more
	 * precision is required, change VecmathHelper.EPSILON and
	 * VecmathHelper.MAX_ITS.
	 * 
	 * @param src
	 *            The Matrix4d to take the square root of
	 * @param dst
	 *            The matrix into which the sqrt of the src matrix is to be
	 *            written
	 */
	public static void sqrtMatrix4d(Matrix4d src, Matrix4d dst) {
		// From Alexa 2002: Linear Combination of Transformations
		dst.set(src);
		Matrix4d y = new Matrix4d();
		y.setIdentity();
		Matrix4d xx = new Matrix4d(dst);
		xx.mul(dst);
		xx.sub(src);
		double norm = frobenius(xx);
		Matrix4d xi = new Matrix4d();
		Matrix4d yi = new Matrix4d();
		int loopCount = 0;
		while (norm > EPSILON && loopCount < MAX_ITERS) {
			xi.invert(dst);
			yi.invert(y);
			dst.add(yi);
			dst.mul(0.5);
			y.add(xi);
			y.mul(0.5);
			xx.mul(dst, dst);
			xx.sub(src);
			norm = frobenius(xx);
			++loopCount;
		}
	}

	/**
	 * Takes the log of a Matrix4d. This is an iterative method. If more
	 * precision is required, change VecmathHelper.EPSILON and
	 * VecmathHelper.MAX_ITS.
	 * 
	 * @param src
	 *            The Matrix4d to take the log of
	 * @param dst
	 *            The matrix into which the log of the src matrix is to be
	 *            written
	 */
	public static void logMatrix4d(Matrix4d src, Matrix4d dst) {
		// From Alexa 2002: Linear Combination of Transformations
		int k = 0;
		Matrix4d a = new Matrix4d(src);
		Matrix4d a2 = new Matrix4d(a);
		Matrix4d a3 = new Matrix4d();
		a2.m00 -= 1;
		a2.m11 -= 1;
		a2.m22 -= 1;
		a2.m33 -= 1;
		double norm = frobenius(a2);
		int loopCount = 0;
		while (norm > 0.5 && loopCount < MAX_ITERS) {
			sqrtMatrix4d(a, a3);
			a.set(a3);
			++k;
			a2.set(a);
			a2.m00 -= 1;
			a2.m11 -= 1;
			a2.m22 -= 1;
			a2.m33 -= 1;
			norm = frobenius(a2);
			++loopCount;
		}
		a.negate();
		a.m00 += 1;
		a.m11 += 1;
		a.m22 += 1;
		a.m33 += 1;
		Matrix4d z = new Matrix4d(a);
		Matrix4d z2 = new Matrix4d();
		dst.set(a);
		int i = 1;
		norm = frobenius(z);
		loopCount = 0;
		while (norm > EPSILON && loopCount < MAX_ITERS) {
			z.mul(a);
			++i;
			z2.set(z);
			z2.mul(1.0 / i);
			dst.add(z2);
			norm = frobenius(z);
			++loopCount;
		}
		dst.mul(2 << (k - 1)); // * 2^k
		dst.negate(); // For some reason, we get a negated version...
	}

	/**
	 * Takes the log of a 4x4 rigid transformation matrix.
	 * 
	 * @param src
	 *            The rigid transformation matrix to take the log of
	 * @param dst
	 *            The matrix into which the log of the src matrix is to be
	 *            written
	 */
	public static void logRigidTransformation(Matrix4d src, Matrix4d dst) {
		Matrix3d m3Tmp1 = new Matrix3d();
		Matrix3d m3Tmp2 = new Matrix3d();
		Quat4d qTmp1 = new Quat4d();
		Vector3d v3Tmp1 = new Vector3d();

		// First normalize the rotational part of the src matrix.
		Matrix3d rot = m3Tmp1;
		src.get(rot);
		rot.normalizeCP();
		// Clear the destination matrix.
		dst.setZero();

		// q_scalar = cos(\theta/2) and
		// q_vector = sin(\theta/2)\hat{w}
		// and so the rotation part of \epsilon is w = \theta \hat{w}

		Quat4d q = qTmp1;
		q.set(rot);
		double veclen = Math.sqrt(q.x * q.x + q.y * q.y + q.z * q.z);
		double theta = 2 * Math.atan2(veclen, q.w);
		Vector3d w = v3Tmp1;
		w.set(q.x, q.y, q.z);
		if (veclen > EPSILON) {
			w.scale(1.0 / veclen);
		} else {
			dst.setColumn(3, src.m03, src.m13, src.m23, 0);
			return;
		}
		w.scale(theta);

		dst.m01 = -w.z;
		dst.m02 = w.y;
		dst.m10 = w.z;
		dst.m12 = -w.x;
		dst.m20 = -w.y;
		dst.m21 = w.x;

		Matrix3d b = rot;
		b.negate();
		b.m00 += 1;
		b.m11 += 1;
		b.m22 += 1;

		Matrix3d wBracket = m3Tmp2;
		dst.getRotationScale(wBracket);
		b.mul(wBracket);

		Matrix3d c = m3Tmp2;
		c.m00 = w.x * w.x;
		c.m01 = w.x * w.y;
		c.m02 = w.x * w.z;
		c.m10 = c.m01;
		c.m11 = w.y * w.y;
		c.m12 = w.y * w.z;
		c.m20 = c.m02;
		c.m21 = c.m12;
		c.m22 = w.z * w.z;

		b.add(c);
		Matrix3d d = m3Tmp2;
		invertMatrix3d(b, d);
		d.mul(theta * theta);

		Vector3d v = v3Tmp1;
		v.set(src.m03, src.m13, src.m23);
		d.transform(v);

		dst.m03 = v.x;
		dst.m13 = v.y;
		dst.m23 = v.z;
	}

	/**
	 * Inverts a 3x3 matrix. Faster than calling mat.invert(), because vecmath
	 * does an LU decomposition, whereas this implementation is explicit. This
	 * is safe to do in-place, ie invertMatrix3d(src, src).
	 * 
	 * @param src
	 *            the matrix to be inverted
	 * @param dst
	 *            the matrix into which <code>src</code>'s inverse is to be
	 *            written
	 */
	public static void invertMatrix3d(Matrix3d src, Matrix3d dst) {
		// Taken from http://mathforum.org/library/drmath/view/51680.html
		double d = src.determinant();
		if (d == 0) {
			System.err.println(src);
			throw new SingularMatrixException("Singular Matrix");
		}
		double dInv = 1.0 / d;
		double m00 = (src.m11 * src.m22 - src.m21 * src.m12) * dInv;
		double m01 = (src.m01 * src.m22 - src.m21 * src.m02) * -dInv;
		double m02 = (src.m01 * src.m12 - src.m11 * src.m02) * dInv;
		double m10 = (src.m10 * src.m22 - src.m20 * src.m12) * -dInv;
		double m11 = (src.m00 * src.m22 - src.m20 * src.m02) * dInv;
		double m12 = (src.m00 * src.m12 - src.m10 * src.m02) * -dInv;
		double m20 = (src.m10 * src.m21 - src.m20 * src.m11) * dInv;
		double m21 = (src.m00 * src.m21 - src.m20 * src.m01) * -dInv;
		double m22 = (src.m00 * src.m11 - src.m10 * src.m01) * dInv;
		dst.m00 = m00;
		dst.m01 = m01;
		dst.m02 = m02;
		dst.m10 = m10;
		dst.m11 = m11;
		dst.m12 = m12;
		dst.m20 = m20;
		dst.m21 = m21;
		dst.m22 = m22;
	}

	/**
	 * Inverts a 4x4 matrix, taking advantage of the fact that the matrix is
	 * known to be a rigid transformation.
	 * 
	 * @param src
	 *            the matrix to be inverted
	 * @param dest
	 *            the matrix into which <code>src</code>'s inverse is to be
	 *            written
	 */
	public static Matrix4d invertRigidTransformation(Matrix4d src, Matrix4d dest) {
		Matrix3d rotationPart = new Matrix3d();
		src.get(rotationPart);
		Vector4d homogeneousTranslation = new Vector4d();
		src.getColumn(3, homogeneousTranslation);
		Vector3d translationPart = new Vector3d();
		dehomogenize(homogeneousTranslation, translationPart);

		// Invert the rotation
		rotationPart.transpose();
		// Invert the translation part
		rotationPart.transform(translationPart);
		translationPart.negate();

		dest.set(rotationPart, translationPart, 1.0);
		
		return dest;
	}

	/**
	 * Converts a homogeneous-coordinate tuple (<code>Tuple4d</code>) into
	 * an implicit weight tuple (<code>Tuple3d</code>). All non-vectors
	 * (weight != 0) are projected to their corresponding locations on the
	 * weight = 1 plane.
	 * 
	 * @param homogeneous
	 *            the homogeneous-coordinate tuple
	 * @param dehomogenized
	 *            where the result of dehomogenizing <code>homogeneous</code>
	 *            is to be placed
	 */
	public static void dehomogenize(Tuple4d homogeneous, Tuple3d dehomogenized) {
		dehomogenized.x = homogeneous.x;
		dehomogenized.y = homogeneous.y;
		dehomogenized.z = homogeneous.z;
		// If the homogeneous coordinate is not a vector, project it to the
		// point plane
		if (homogeneous.w != 0) {
			dehomogenized.scale(1.0 / homogeneous.w);
		}
	}

	/**
	 * Inverts a 4x4 matrix, taking advantage of the fact that the matrix is
	 * known to be a rigid transformation.
	 * 
	 * @param src
	 *            the matrix to be inverted
	 * @param dest
	 *            the matrix into which <code>src</code>'s inverse is to be
	 *            written
	 */
	public static void invertRigidTransformation(Matrix4f src, Matrix4f dest) {
		Matrix3f rotationPart = new Matrix3f();
		src.get(rotationPart);
		Vector4f homogeneousTranslation = new Vector4f();
		src.getColumn(3, homogeneousTranslation);
		Vector3f translationPart = new Vector3f();
		dehomogenize(homogeneousTranslation, translationPart);

		// Invert the rotation
		rotationPart.transpose();
		// Invert the translation part
		rotationPart.transform(translationPart);
		translationPart.negate();

		dest.set(rotationPart, translationPart, 1.0f);
	}

	/**
	 * Converts a homogeneous-coordinate tuple (<code>Tuple4f</code>) into
	 * an implicit weight tuple (<code>Tuple3f</code>). All non-vectors
	 * (weight != 0) are projected to their corresponding locations on the
	 * weight = 1 plane.
	 * 
	 * @param homogeneous
	 *            the homogeneous-coordinate tuple
	 * @param dehomogenized
	 *            where the result of dehomogenizing <code>homogeneous</code>
	 *            is to be placed
	 */
	public static void dehomogenize(Tuple4f homogeneous, Tuple3f dehomogenized) {
		dehomogenized.x = homogeneous.x;
		dehomogenized.y = homogeneous.y;
		dehomogenized.z = homogeneous.z;
		// If the homogeneous coordinate is not a vector, project it to the
		// point plane
		if (homogeneous.w != 0) {
			dehomogenized.scale(1.0f / homogeneous.w);
		}
	}

	/**
	 * Flattens a Matrix4d into a double array that can be passed to OpenGL.
	 * 
	 * @param mat
	 *            Matrix to flatten.
	 * @return Flattened matrix.
	 */
	public static double[] flatten(Matrix4d mat) {
		double[] outputArray = new double[16];
		outputArray[0] = mat.m00;
		outputArray[1] = mat.m10;
		outputArray[2] = mat.m20;
		outputArray[3] = mat.m30;
		outputArray[4] = mat.m01;
		outputArray[5] = mat.m11;
		outputArray[6] = mat.m21;
		outputArray[7] = mat.m31;
		outputArray[8] = mat.m02;
		outputArray[9] = mat.m12;
		outputArray[10] = mat.m22;
		outputArray[11] = mat.m32;
		outputArray[12] = mat.m03;
		outputArray[13] = mat.m13;
		outputArray[14] = mat.m23;
		outputArray[15] = mat.m33;
		return outputArray;
	}

	/**
	 * Test driver.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		Matrix4d m = new Matrix4d();
		m.set(new double[] { 0.04452816314851593, 0.9351572566251427,
				-0.3514230329217995, 0.03414986806107994, -0.9989315090807869,
				0.0460356592227994, -0.004069182412234307,
				-0.01834443957975035, 0.012372665525298254, 0.3512287338206533,
				0.9362079329329248, 0.12774076699644396, 0.0, 0.0, 0.0, 1.0 });
	}
}
