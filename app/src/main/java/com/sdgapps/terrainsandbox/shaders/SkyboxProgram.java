package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.SamplerCubemap;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class SkyboxProgram extends GLSLProgram {
    private SkyboxProgram(String id) {
        super(id, R.raw.skybox_vertex, R.raw.skybox_fragment);
        configure();
    }

    public static GLSLProgram createInstance(String id)
    {
        GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
        if(instance==null)
        {
            instance=new SkyboxProgram(id);
            Singleton.systems.sShaderSystem.addProgram(instance);
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
