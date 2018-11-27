package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

public class ShaderUniform1f extends ShaderUniform {

    public float v = 0;

    public ShaderUniform1f(String name) {
        super(name);
    }

    @Override
    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform1f(glHandle, v);
        }
    }
}
