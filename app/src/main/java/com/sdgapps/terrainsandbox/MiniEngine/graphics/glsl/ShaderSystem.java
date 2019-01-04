package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.content.res.Resources;

import com.sdgapps.terrainsandbox.utils.Logger;

import java.util.HashMap;

/**
 * Basic shader system implementation
 */
public class ShaderSystem {



    private Resources res;
    private HashMap<String,GLSLProgram> shaders=new HashMap<>();

    public void setRes(Resources res) {
        this.res = res;
    }

    public Resources getRes() {
        return res;
    }

    public void reloadShaders() {

        for (GLSLProgram p : shaders.values()) {
            if (p != null) p.reloadToGPU(res);
        }
    }

    public void addProgram(GLSLProgram program)
    {
        if(shaders.get(program.shaderID)==null)
        {
            shaders.put(program.shaderID,program);
        }
        else
            Logger.warning("A program with id "+program.shaderID+" already exists in the system. Ignoring.");
    }

    /**
     * Returns the program identified by the parameter string (key).
     * Don't abuse this, store a reference to the program in case it is needed very often
     */
    public GLSLProgram getProgram(String id) {
        return shaders.get(id);
    }
}
