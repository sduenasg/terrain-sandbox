package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;


public class ShaderUniform2f extends ShaderUniform {

    public float v0;
    public float v1;


    public ShaderUniform2f(String name) {
        super(name);
    }

    public ShaderUniform2f(String name, ShaderUniform2f copyValuesFrom) {
        super(name);
        v0 = copyValuesFrom.v0;
        v1 = copyValuesFrom.v1;
    }

    public ShaderUniform2f(String name, float _v0, float _v1, float _v2) {
        super(name);
        v0 = _v0;
        v1 = _v1;
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES30.glUniform2f(glHandle, v0, v1);
        }
    }

    public void set(float a, float b) {
        v0 = a;
        v1 = b;
    }

    public void set(Vec3f in) {
        v0 = in.x;
        v1 = in.y;
    }

    public void set(ShaderUniform2f in) {
        v0 = in.v0;
        v1 = in.v1;
    }
}
