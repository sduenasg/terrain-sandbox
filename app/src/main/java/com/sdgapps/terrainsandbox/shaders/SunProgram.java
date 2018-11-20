package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class SunProgram extends GLSLProgram {
    private SunProgram(String id) {
        super(id, R.raw.sun_vertex, R.raw.sun_fragment, false, false, false, GLSLProgram.USES_MVMATRIX, false);
    }

    public static GLSLProgram createInstance(String id)
    {
        GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
        if(instance==null)
        {
            instance=new SunProgram(id);
            Singleton.systems.sShaderSystem.addProgram(instance);
        }
        return instance;
    }
}
