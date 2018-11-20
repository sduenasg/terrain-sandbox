package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable3f;
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
        ShaderVariable3f atmosphereColor = new ShaderVariable3f("u_atmosphere_color");
        addUniform(atmosphereColor);

        ShaderVariable3f campos = new ShaderVariable3f("camPos");
        addUniform(campos);

        ShaderVariable3f lightposworld = new ShaderVariable3f("lightPos");
        addUniform(lightposworld);
    }
}
