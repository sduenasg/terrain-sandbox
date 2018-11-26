package com.sdgapps.terrainsandbox.shaders;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform2F;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3F;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class PlanetShadowPassProgram extends GLSLProgram {
    private PlanetShadowPassProgram(String id) {
        super(id, R.raw.shadowmap_vertex, R.raw.shadowmap_fragment,  false);
        configureShadowmapShader();
    }

    public static GLSLProgram createInstance(String id)
    {
        GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
        if(instance==null)
        {
            instance=new PlanetShadowPassProgram(id);
            Singleton.systems.sShaderSystem.addProgram(instance);
        }
        return instance;
    }

    public void configureShadowmapShader() {
        ShaderUniform1f shadowmapCDLODQuadScale = new ShaderUniform1f("quad_scale");
        ShaderUniform3F shadowmapCDLODrange = new ShaderUniform3F("range");
        ShaderUniform3F shadowmapCDLODcampos = new ShaderUniform3F("cameraPosition");
        ShaderUniform1f shadowmapCDLODGriddim = new ShaderUniform1f("gridDim");
        ShaderUniform2F shadowmapCDLODBaseXZ = new ShaderUniform2F("baseXZ");
        ShaderUniform3F shadowmapCDLODMeshInfo = new ShaderUniform3F("meshInfo");
        ShaderUniform3F shadowmapCDLODnodeoffset = new ShaderUniform3F("nodeoffset");

        addUniform(shadowmapCDLODQuadScale);
        addUniform(shadowmapCDLODrange);
        addUniform(shadowmapCDLODcampos);
        addUniform(shadowmapCDLODGriddim);
        addUniform(shadowmapCDLODBaseXZ);
        addUniform(shadowmapCDLODMeshInfo);
        addUniform(shadowmapCDLODnodeoffset);
    }
}
