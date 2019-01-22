package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;

public class BoundingBoxProgram extends GLSLProgram {
    private BoundingBoxProgram(String id, ShaderSystem shaderSys) {
        super(id, "shaders/boxvertex.glsl", "shaders/boxfragment.glsl",shaderSys);
        ShaderUniformMatrix4fv MVPMatrix=new ShaderUniformMatrix4fv("u_MVPMatrix");
        addUniform(MVPMatrix);
        linkAttribute("a_Position");
    }

    public static GLSLProgram createInstance(String id, ShaderSystem shaderSys)
    {
        GLSLProgram instance=shaderSys.getProgram(id);
        if(instance==null)
        {
            instance=new BoundingBoxProgram(id,shaderSys);
            shaderSys.addProgram(instance);
        }
        return instance;
    }
}
