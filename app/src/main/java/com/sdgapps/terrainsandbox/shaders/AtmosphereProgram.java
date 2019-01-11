package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class AtmosphereProgram extends GLSLProgram {
    private AtmosphereProgram(String id) {
        super(id,"shaders/atmosphere_vert.glsl","shaders/atmosphere_frag.glsl");
        initAtmosphereShader();
    }

    public static GLSLProgram createInstance(String id)
    {
        GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
        if(instance==null)
        {
            instance=new AtmosphereProgram(id);
            Singleton.systems.sShaderSystem.addProgram(instance);
        }
        return instance;
    }

    private void initAtmosphereShader() {

        Sampler2D atmotexture = new Sampler2D("u_Texture");
        addUniform(atmotexture);

        ShaderUniform3f atmosphereColor = new ShaderUniform3f("u_atmosphere_color");
        addUniform(atmosphereColor);

        ShaderUniform3f campos = new ShaderUniform3f("camPos");
        addUniform(campos);

        ShaderUniform3f lightposworld = new ShaderUniform3f("lightPos");
        addUniform(lightposworld);

        ShaderUniformMatrix4fv MVPMatrix=new ShaderUniformMatrix4fv("u_MVPMatrix");
        addUniform(MVPMatrix);

        ShaderUniformMatrix4fv ModelMatrix=new ShaderUniformMatrix4fv("u_Modelmatrix");
        addUniform(ModelMatrix);

        //attributes
        linkAttribute("a_Position");
        linkAttribute("a_Normal");
        linkAttribute("a_TexCoordinate");
    }
}
