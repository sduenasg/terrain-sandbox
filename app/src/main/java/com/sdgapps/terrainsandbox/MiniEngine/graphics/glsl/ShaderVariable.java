package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

public class ShaderVariable {

    public String name;
    /**
     * Handle of this variable in the shader it is bound to
     */
    public int glHandle;
    public boolean autobind = true;

    public ShaderVariable(String name) {
        this.name = name;
    }

    public void bind() {
    }
}
