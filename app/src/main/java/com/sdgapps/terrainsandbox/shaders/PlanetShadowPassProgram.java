package com.sdgapps.terrainsandbox.shaders;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable2f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable3f;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class PlanetShadowPassProgram extends GLSLProgram {
    private PlanetShadowPassProgram(String id) {
        super(id, R.raw.shadowmap_vertex, R.raw.shadowmap_fragment, false, false, false, GLSLProgram.USES_NONE, false);
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
        ShaderVariable1f shadowmapCDLODQuadScale = new ShaderVariable1f("quad_scale");
        ShaderVariable3f shadowmapCDLODrange = new ShaderVariable3f("range");
        ShaderVariable3f shadowmapCDLODcampos = new ShaderVariable3f("cameraPosition");
        ShaderVariable1f shadowmapCDLODGriddim = new ShaderVariable1f("gridDim");
        ShaderVariable2f shadowmapCDLODBaseXZ = new ShaderVariable2f("baseXZ");
        ShaderVariable3f shadowmapCDLODMeshInfo = new ShaderVariable3f("meshInfo");
        ShaderVariable3f shadowmapCDLODnodeoffset = new ShaderVariable3f("nodeoffset");

        addUniform(shadowmapCDLODQuadScale);
        addUniform(shadowmapCDLODrange);
        addUniform(shadowmapCDLODcampos);
        addUniform(shadowmapCDLODGriddim);
        addUniform(shadowmapCDLODBaseXZ);
        addUniform(shadowmapCDLODMeshInfo);
        addUniform(shadowmapCDLODnodeoffset);
    }
}
