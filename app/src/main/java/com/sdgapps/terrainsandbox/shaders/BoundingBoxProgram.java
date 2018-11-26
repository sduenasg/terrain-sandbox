package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class BoundingBoxProgram extends GLSLProgram {
    private BoundingBoxProgram(String id) {
        super(id, R.raw.boxvertex, R.raw.boxfragment);
        ShaderUniformMatrix4fv MVPMatrix=new ShaderUniformMatrix4fv("u_MVPMatrix");
        addUniform(MVPMatrix);
    }

    public static GLSLProgram createInstance(String id)
    {
        GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
        if(instance==null)
        {
            instance=new BoundingBoxProgram(id);
            Singleton.systems.sShaderSystem.addProgram(instance);
        }
        return instance;
    }
}
