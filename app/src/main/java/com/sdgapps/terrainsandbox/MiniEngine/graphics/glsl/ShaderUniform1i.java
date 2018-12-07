package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

public class ShaderUniform1i extends ShaderUniform {

    public int v = 0;

    public ShaderUniform1i(String name) {
        super(name);
    }

    @Override
    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform1i(glHandle, v);
        }
    }
}
