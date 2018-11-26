package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

/**
 * 3-component vector array (vec3[])
 */
public class ShaderUniform3Fv extends ShaderUniform {

    public float[] array;

    public ShaderUniform3Fv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES20.glUniform3fv(glHandle, array.length / 3, array, 0);
        }
    }
}
