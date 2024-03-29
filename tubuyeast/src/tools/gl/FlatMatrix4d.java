package tools.gl;

import java.io.Serializable;

import javax.vecmath.Matrix4d;

/**
 * Simple wrapper class to flatten vecmath matrices into a format suitable
 * for openGL.  Use the toArray method to get the backing matrix as an array.
 * Use the reconstitute method to set the backing matrix to this array if it
 * is modified.
 * @author kry
 */
public class FlatMatrix4d implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -801222162963046561L;

    /**
     * The backing backingMatrix.
     */
    private Matrix4d backingMatrix;
    
    /**
     * The outputArray used for dumping out the contents as a double array.
     */
    private double[] outputArray;
    
    /**
     * Constructs a <code>Matrix4dBackedDoubleMatrixImpl</code> backed by the
     * provided <code>Matrix4d</code>.
     * 
     * @param matrix
     *        the matrix that will back this flat matrix abstraction
     */
    public FlatMatrix4d(Matrix4d matrix){
        this.backingMatrix = matrix;
        outputArray = new double[16];
    }

    /**
     * Constructs a <code>Matrix4dBackedFloatMatrixImpl</code> backed by 
     * a newly created <code>Matrix4d</code>.
     */
    public FlatMatrix4d() {
    	this.backingMatrix = new Matrix4d();
        outputArray = new double[16];
    }
    
    /**
     * Sets the backing matrix.
     * @param backingMatrix
     */
    public void setBackingMatrix( Matrix4d backingMatrix ) {
    	this.backingMatrix = backingMatrix;
    }

    /**
     * Gets the backing matrix.
     * @return the backing matrix
     */
    public Matrix4d getBackingMatrix() {
    	return backingMatrix;
    }

    /**
     * Gets the backing matrix as an array column-major 
     * @return a 16 element double array
     */
    public double[] asArrayTranspose() {
        outputArray[0] = backingMatrix.m00;
        outputArray[4] = backingMatrix.m10;
        outputArray[8] = backingMatrix.m20;
        outputArray[12] = backingMatrix.m30;
        outputArray[1] = backingMatrix.m01;
        outputArray[5] = backingMatrix.m11;
        outputArray[9] = backingMatrix.m21;
        outputArray[13] = backingMatrix.m31;
        outputArray[2] = backingMatrix.m02;
        outputArray[6] = backingMatrix.m12;
        outputArray[10] = backingMatrix.m22;
        outputArray[14] = backingMatrix.m32;
        outputArray[3] = backingMatrix.m03;
        outputArray[7] = backingMatrix.m13;
        outputArray[11] = backingMatrix.m23;
        outputArray[15] = backingMatrix.m33;
        return outputArray;
    }

    /**
     * Gets the backing matrix as an array
     * @return a 16 element double array
     */
    public double[] asArray() {
        outputArray[0] = backingMatrix.m00;
        outputArray[1] = backingMatrix.m10;
        outputArray[2] = backingMatrix.m20;
        outputArray[3] = backingMatrix.m30;
        outputArray[4] = backingMatrix.m01;
        outputArray[5] = backingMatrix.m11;
        outputArray[6] = backingMatrix.m21;
        outputArray[7] = backingMatrix.m31;
        outputArray[8] = backingMatrix.m02;
        outputArray[9] = backingMatrix.m12;
        outputArray[10] = backingMatrix.m22;
        outputArray[11] = backingMatrix.m32;
        outputArray[12] = backingMatrix.m03;
        outputArray[13] = backingMatrix.m13;
        outputArray[14] = backingMatrix.m23;
        outputArray[15] = backingMatrix.m33;
        return outputArray;
    }
    
    /**
     * Rebuilds the backing matrix from the flat 16 element array.
     */
    public void reconstitute() {
        backingMatrix.m00 = outputArray[0];
        backingMatrix.m10 = outputArray[1];
        backingMatrix.m20 = outputArray[2];
        backingMatrix.m30 = outputArray[3];
        backingMatrix.m01 = outputArray[4];
        backingMatrix.m11 = outputArray[5];
        backingMatrix.m21 = outputArray[6];
        backingMatrix.m31 = outputArray[7];
        backingMatrix.m02 = outputArray[8];
        backingMatrix.m12 = outputArray[9];
        backingMatrix.m22 = outputArray[10];
        backingMatrix.m32 = outputArray[11];
        backingMatrix.m03 = outputArray[12];
        backingMatrix.m13 = outputArray[13];
        backingMatrix.m23 = outputArray[14];
        backingMatrix.m33 = outputArray[15];
    }

    @Override
    public String toString() {
        return backingMatrix.toString();
    }
    
}
