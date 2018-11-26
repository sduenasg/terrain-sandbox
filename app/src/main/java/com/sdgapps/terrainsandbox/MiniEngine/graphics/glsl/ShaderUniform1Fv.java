package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

/**
 * Float array
 */
public class ShaderUniform1Fv extends ShaderUniform {

    public float[] array;

    public ShaderUniform1Fv(String name) {
        super(name);
    }

    public void bind() {

        if (glHandle != -1) {
            super.bind();
            GLES20.glUniform1fv(glHandle, array.length, array, 0);

        }
    }
}
