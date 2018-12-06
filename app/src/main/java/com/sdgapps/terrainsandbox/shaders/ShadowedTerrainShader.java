package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3F;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class ShadowedTerrainShader extends GLSLProgram {
    private ShadowedTerrainShader(String id) {
        super(id, -1,-1);
        //super(id, R.raw.terrain_vertex_shadowed, R.raw.terrain_fragment_derivative_shadowed_);
        configureShadowedTerrainShader();
    }

    public static GLSLProgram createInstance(String id)
    {
        GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
        if(instance==null)
        {
            instance=new ShadowedTerrainShader(id);
            Singleton.systems.sShaderSystem.addProgram(instance);
        }
        return instance;
    }

    public void configureShadowedTerrainShader() {

        ShaderUniform1f CDLODQuadScaleShadowed = new ShaderUniform1f("quad_scale");
        ShaderUniform3F CDLODrangeShadowed = new ShaderUniform3F("range");
        ShaderUniform3F CDLODcamposShadowed = new ShaderUniform3F("cameraPosition");
        ShaderUniform1f CDLODGriddimShadowed = new ShaderUniform1f("gridDim");
        ShaderUniform3F CDLODMeshInfoShadowed = new ShaderUniform3F("meshInfo");
        ShaderUniform1f CDLODzfarShadowed = new ShaderUniform1f("zfar");
        ShaderUniform1f CDLODLodlevelShadowed = new ShaderUniform1f("lodlevel");
        ShaderUniform3F CDLODNodeOffsetShadowed = new ShaderUniform3F("nodeoffset");

        addUniform(CDLODQuadScaleShadowed);
        addUniform(CDLODrangeShadowed);
        addUniform(CDLODcamposShadowed);
        addUniform(CDLODGriddimShadowed);
        addUniform(CDLODMeshInfoShadowed);
        addUniform(CDLODzfarShadowed);
        addUniform(CDLODLodlevelShadowed);
        addUniform(CDLODNodeOffsetShadowed);

        ShaderUniform3F fogcolorTerrainShadowed = new ShaderUniform3F("u_Fogcolor");
        addUniform(fogcolorTerrainShadowed);
    }
}
