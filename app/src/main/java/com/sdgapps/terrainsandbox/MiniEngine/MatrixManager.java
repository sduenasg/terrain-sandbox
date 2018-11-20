
package com.sdgapps.terrainsandbox.MiniEngine;


import android.opengl.Matrix;

public class MatrixManager {
    /* OpenGL Matrices  */
    public static float[] projectionMatrix; //Active frustum sets this matrix
    public static float[] modelViewMatrix = new float[16];
    public static float[] viewMatrix = new float[16];
    public static float[] modelMatrix = new float[16];
    public static float[] MVPMatrix = new float[16];

    public static float[] shadowmapProjectionMatrix;
    public static float[] shadowmapModelViewMatrix = new float[16];
    public static float[] shadowmapViewMatrix = new float[16];

    public static float[] identityMatrix = new float[16];

    public static void copy(float[] to, float[] from) {
        for (int i = 0; i < 16; i++) {
            to[i] = from[i];

        }
    }

    public MatrixManager() {
        Matrix.setIdentityM(identityMatrix, 0);
    }

    public static float[] refProjectionMatrix = new float[16];

}
