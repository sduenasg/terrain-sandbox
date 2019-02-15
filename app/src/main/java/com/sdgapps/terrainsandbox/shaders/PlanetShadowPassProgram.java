package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform2f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3f;

public class PlanetShadowPassProgram extends GLSLProgram {
    private PlanetShadowPassProgram(String id, ShaderSystem shaderSys) {
        //super(id, R.raw.shadowmap_vertex, R.raw.shadowmap_fragment);
        super(id, "","",shaderSys,true);
        configureShadowmapShader();
    }

    public static GLSLProgram createInstance(String id, ShaderSystem shaderSys)
    {
        GLSLProgram instance=shaderSys.getProgram(id);
        if(instance==null)
        {
            instance=new PlanetShadowPassProgram(id,shaderSys);
            shaderSys.addProgram(instance);
        }
        return instance;
    }

    public void configureShadowmapShader() {
        ShaderUniform1f shadowmapCDLODQuadScale = new ShaderUniform1f("quad_scale");
        ShaderUniform3f shadowmapCDLODrange = new ShaderUniform3f("range");
        ShaderUniform3f shadowmapCDLODcampos = new ShaderUniform3f("cameraPosition");
        ShaderUniform1f shadowmapCDLODGriddim = new ShaderUniform1f("gridDim");
        ShaderUniform2f shadowmapCDLODBaseXZ = new ShaderUniform2f("baseXZ");
        ShaderUniform3f shadowmapCDLODMeshInfo = new ShaderUniform3f("meshInfo");
        ShaderUniform3f shadowmapCDLODnodeoffset = new ShaderUniform3f("nodeoffset");

        addUniform(shadowmapCDLODQuadScale);
        addUniform(shadowmapCDLODrange);
        addUniform(shadowmapCDLODcampos);
        addUniform(shadowmapCDLODGriddim);
        addUniform(shadowmapCDLODBaseXZ);
        addUniform(shadowmapCDLODMeshInfo);
        addUniform(shadowmapCDLODnodeoffset);
    }
}
