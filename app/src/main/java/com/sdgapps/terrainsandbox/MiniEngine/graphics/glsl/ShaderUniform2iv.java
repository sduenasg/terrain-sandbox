package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

public class ShaderUniform2iv extends ShaderUniform {

    public int[] array;

    public ShaderUniform2iv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform2iv(glHandle, array.length / 2, array, 0);
        }
    }
}
