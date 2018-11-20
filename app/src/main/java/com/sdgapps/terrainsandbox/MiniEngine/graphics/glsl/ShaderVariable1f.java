package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

public class ShaderVariable1f extends ShaderVariable {

    public float v = 0;

    public ShaderVariable1f(String name) {
        super(name);
    }

    @Override
    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES20.glUniform1f(glHandle, v);
        }
    }
}
