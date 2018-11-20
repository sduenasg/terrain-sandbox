package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

/**
 * 3-component vector array (vec3[])
 */
public class ShaderVariable3fv extends ShaderVariable {

    public float[] array;

    public ShaderVariable3fv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES20.glUniform3fv(glHandle, array.length / 3, array, 0);
        }
    }
}
