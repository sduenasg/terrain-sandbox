package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

/**
 * 2-component vector array (vec2[])
 */
public class ShaderUniform2fv extends ShaderUniform {

    public float[] array;

    public ShaderUniform2fv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform2fv(glHandle, array.length / 2, array, 0);
        }
    }
}
