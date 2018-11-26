package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3F;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class AtmosphereProgram extends GLSLProgram {
    private AtmosphereProgram(String id) {
        super(id, R.raw.atmosphere_vert, R.raw.atmosphere_frag,false, false, false, GLSLProgram.USES_MODEL_MATRIX, false);
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

        ShaderUniform3F atmosphereColor = new ShaderUniform3F("u_atmosphere_color");
        addUniform(atmosphereColor);

        ShaderUniform3F campos = new ShaderUniform3F("camPos");
        addUniform(campos);

        ShaderUniform3F lightposworld = new ShaderUniform3F("lightPos");
        addUniform(lightposworld);
    }
}
