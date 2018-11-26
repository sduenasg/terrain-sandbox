package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

public class ShaderUniform {

    public String name;
    /**
     * Handle of this variable in the shader it is bound to
     */
    public int glHandle;

    public ShaderUniform(String name) {
        this.name = name;
    }

    public void bind() {
    }
}
