package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Color4f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;


public class ShaderVariable3f extends ShaderVariable {

    public float v0;
    public float v1;
    public float v2;

    public ShaderVariable3f(String name) {
        super(name);
    }

    public ShaderVariable3f(String name, ShaderVariable3f copyValuesFrom) {
        super(name);
        v0 = copyValuesFrom.v0;
        v1 = copyValuesFrom.v1;
        v2 = copyValuesFrom.v2;
    }

    public ShaderVariable3f(String name, float _v0, float _v1, float _v2) {
        super(name);
        v0 = _v0;
        v1 = _v1;
        v2 = _v2;
    }

    public void bind() {
        if (glHandle != -1) {
            super.bind();
            GLES20.glUniform3f(glHandle, v0, v1, v2);
        }
    }

    public void set(float a, float b, float c) {
        v0 = a;
        v1 = b;
        v2 = c;
    }

    public void set(Vec3f in) {
        v0 = in.x;
        v1 = in.y;
        v2 = in.z;
    }

    public void set(ShaderVariable3f in) {
        v0 = in.v0;
        v1 = in.v1;
        v2 = in.v2;
    }

    public void set(Color4f c) {
        v0 = c.r;
        v1 = c.g;
        v2 = c.b;
    }


}
