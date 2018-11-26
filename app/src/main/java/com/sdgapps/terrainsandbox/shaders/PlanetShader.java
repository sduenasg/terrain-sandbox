package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderVariable3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.AppTextureManager;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;
import com.sdgapps.terrainsandbox.utils.Logger;

public class PlanetShader extends GLSLProgram {
    private PlanetShader(String id) {
        super(id, R.raw.terrain_vertex_planet, R.raw.planet_fragment_derivative, true, true, false, GLSLProgram.USES_MVMATRIX, false);

        configureTerrainShader();
    }

    public static GLSLProgram createInstance(String id)
    {
       GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
       if(instance==null)
       {
           instance=new PlanetShader(id);
           Singleton.systems.sShaderSystem.addProgram(instance);
       }
       return instance;
    }

    public void configureTerrainShader() {
        Sampler2D heightmap = new Sampler2D("u_heightMap");
        Sampler2D colormap=new Sampler2D("u_colorMap");
        Sampler2D normalmap=new Sampler2D("u_normalMap");

        ShaderVariable1f CDLODQuadScale = new ShaderVariable1f("quad_scale");
        ShaderVariable3f CDLODrange = new ShaderVariable3f("range");
        ShaderVariable3f CDLODcampos = new ShaderVariable3f("cameraPosition");
        ShaderVariable1f CDLODGriddim = new ShaderVariable1f("gridDim");
        ShaderVariable3f CDLODMeshInfo = new ShaderVariable3f("meshInfo");
        ShaderVariable1f CDLODzfar = new ShaderVariable1f("zfar");
        ShaderVariable1f CDLODLodlevel = new ShaderVariable1f("lodlevel");
        ShaderVariable3f CDLODNodeOffset = new ShaderVariable3f("nodeoffset");
        ShaderVariable3f fogcolorTerrain = new ShaderVariable3f("u_Fogcolor");

        addUniform(heightmap);
        addUniform(colormap);
        addUniform(normalmap);

        addUniform(CDLODQuadScale);
        addUniform(CDLODrange);
        addUniform(CDLODcampos);
        addUniform(CDLODGriddim);
        addUniform(CDLODMeshInfo);
        addUniform(CDLODzfar);
        addUniform(CDLODLodlevel);
        addUniform(CDLODNodeOffset);
        addUniform(fogcolorTerrain);
    }
}
