package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

/**
 * 3-component vector array (vec3[])
 */
public class ShaderUniform3fv extends ShaderUniform {

    public float[] array;

    public ShaderUniform3fv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform3fv(glHandle, array.length / 3, array, 0);
        }
    }
}
