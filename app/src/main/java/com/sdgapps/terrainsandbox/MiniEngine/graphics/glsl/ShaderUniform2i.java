package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec2f;

public class ShaderUniform2i extends ShaderUniform {

    public int v0;
    public int v1;

    public ShaderUniform2i(String name) {
        super(name);
    }

    public ShaderUniform2i(String name, ShaderUniform2i copyValuesFrom) {
        super(name);
        v0 = copyValuesFrom.v0;
        v1 = copyValuesFrom.v1;
    }

    public ShaderUniform2i(String name, int _v0, int _v1, int _v2) {
        super(name);
        v0 = _v0;
        v1 = _v1;
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform2i(glHandle, v0, v1);
        }
    }

    public void set(int a, int b) {
        v0 = a;
        v1 = b;
    }

    public void set(Vec2f in) {
        v0 = (int)in.x;
        v1 = (int)in.y;
    }

    public void set(ShaderUniform2i in) {
        v0 = in.v0;
        v1 = in.v1;
    }
}
