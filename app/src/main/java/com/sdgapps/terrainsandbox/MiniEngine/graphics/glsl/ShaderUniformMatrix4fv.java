package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

/**
 * Matrix or Matrix[] uniform
 */
public class ShaderUniformMatrix4fv extends ShaderUniform {

    public float[] array = new float[16];

    public ShaderUniformMatrix4fv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES20.glUniformMatrix4fv(glHandle, array.length/16, false, array, 0);
            //handle, number of matrices, transpose, data, location
        }
    }
}
