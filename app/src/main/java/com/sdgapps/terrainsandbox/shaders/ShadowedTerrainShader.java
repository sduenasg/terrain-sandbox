package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.AppTextureManager;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class ShadowedTerrainShader extends GLSLProgram {
    private ShadowedTerrainShader(String id) {
        super(id, R.raw.terrain_vertex_shadowed, R.raw.terrain_fragment_derivative_shadowed_, true, true, false, GLSLProgram.USES_MVMATRIX, true);
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

        ShaderVariable1f CDLODQuadScaleShadowed = new ShaderVariable1f("quad_scale");
        ShaderVariable3f CDLODrangeShadowed = new ShaderVariable3f("range");
        ShaderVariable3f CDLODcamposShadowed = new ShaderVariable3f("cameraPosition");
        ShaderVariable1f CDLODGriddimShadowed = new ShaderVariable1f("gridDim");
        ShaderVariable3f CDLODMeshInfoShadowed = new ShaderVariable3f("meshInfo");
        ShaderVariable1f CDLODzfarShadowed = new ShaderVariable1f("zfar");
        ShaderVariable1f CDLODLodlevelShadowed = new ShaderVariable1f("lodlevel");
        ShaderVariable3f CDLODNodeOffsetShadowed = new ShaderVariable3f("nodeoffset");

        addUniform(CDLODQuadScaleShadowed);
        addUniform(CDLODrangeShadowed);
        addUniform(CDLODcamposShadowed);
        addUniform(CDLODGriddimShadowed);
        addUniform(CDLODMeshInfoShadowed);
        addUniform(CDLODzfarShadowed);
        addUniform(CDLODLodlevelShadowed);
        addUniform(CDLODNodeOffsetShadowed);

        ShaderVariable3f fogcolorTerrainShadowed = new ShaderVariable3f("u_Fogcolor");
        addUniform(fogcolorTerrainShadowed);
    }
}
