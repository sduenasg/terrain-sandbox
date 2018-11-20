package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

public class ShaderVariableMatrix4fv extends ShaderVariable {

    public float[] array = new float[16];

    public ShaderVariableMatrix4fv(String name) {
        super(name);
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES20.glUniformMatrix4fv(glHandle, array.length / 16, false, array, 0);
        }
    }
}
