package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;

public class SunProgram extends GLSLProgram {
    private SunProgram(String id, ShaderSystem shaderSys) {
        super(id, "shaders/sun_vertex.glsl", "shaders/sun_fragment.glsl",shaderSys);
        configure();
    }

    public static GLSLProgram createInstance(String id, ShaderSystem shaderSys)
    {
        GLSLProgram instance=shaderSys.getProgram(id);
        if(instance==null)
        {
            instance=new SunProgram(id,shaderSys);
            shaderSys.addProgram(instance);
        }
        return instance;
    }

    private void configure()
    {
        //matrices
        ShaderUniformMatrix4fv ProjectionMatrix=new ShaderUniformMatrix4fv("u_Projectionmatrix");
        ShaderUniformMatrix4fv MVMatrix=new ShaderUniformMatrix4fv("u_MVMatrix");

        addUniform(ProjectionMatrix);
        addUniform(MVMatrix);

        linkAttribute("a_Position");

    }
}
