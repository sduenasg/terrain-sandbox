package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3F;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

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

        ShaderUniform1f CDLODQuadScale = new ShaderUniform1f("quad_scale");
        ShaderUniform3F CDLODrange = new ShaderUniform3F("range");
        ShaderUniform3F CDLODcampos = new ShaderUniform3F("cameraPosition");
        ShaderUniform1f CDLODGriddim = new ShaderUniform1f("gridDim");
        ShaderUniform3F CDLODMeshInfo = new ShaderUniform3F("meshInfo");
        ShaderUniform1f CDLODzfar = new ShaderUniform1f("zfar");
        ShaderUniform1f CDLODLodlevel = new ShaderUniform1f("lodlevel");
        ShaderUniform3F CDLODNodeOffset = new ShaderUniform3F("nodeoffset");
        ShaderUniform3F fogcolorTerrain = new ShaderUniform3F("u_Fogcolor");

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
