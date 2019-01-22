package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.SamplerCubemap;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;

public class SkyboxProgram extends GLSLProgram {
    private SkyboxProgram(String id, ShaderSystem shaderSys) {
        super(id, "shaders/skybox_vertex.glsl","shaders/skybox_fragment.glsl",shaderSys);
        configure();
    }

    public static GLSLProgram createInstance(String id, ShaderSystem shaderSys)
    {
        GLSLProgram instance=shaderSys.getProgram(id);
        if(instance==null)
        {
            instance=new SkyboxProgram(id,shaderSys);
            shaderSys.addProgram(instance);
        }
        return instance;
    }

    private void configure()
    {
        //matrices
        ShaderUniformMatrix4fv ProjectionMatrix=new ShaderUniformMatrix4fv("u_Projectionmatrix");
        ShaderUniformMatrix4fv MVMatrix=new ShaderUniformMatrix4fv("u_Viewatrix");

        addUniform(ProjectionMatrix);
        addUniform(MVMatrix);

        SamplerCubemap tex=new SamplerCubemap("skyboxTex");
        addUniform(tex);

        linkAttribute("a_Position");

    }
}
