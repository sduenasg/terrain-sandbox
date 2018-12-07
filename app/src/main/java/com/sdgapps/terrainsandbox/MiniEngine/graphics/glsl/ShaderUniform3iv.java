package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

/**
 * 3-component vector array (vec3[])
 */
public class ShaderUniform3iv extends ShaderUniform {

    public int[] array;

    public ShaderUniform3iv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform3iv(glHandle, array.length / 3, array, 0);
        }
    }
}
