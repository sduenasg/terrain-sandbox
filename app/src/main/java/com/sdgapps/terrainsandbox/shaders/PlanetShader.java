package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2DArray;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform2f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;

public class PlanetShader extends GLSLProgram {
    private PlanetShader(String id, ShaderSystem shaderSys) {
        super(id, "shaders/planet_vertex.glsl", "shaders/planet_fragment.glsl",shaderSys);

        configureTerrainShader();
    }

    public static GLSLProgram createInstance(String id, ShaderSystem shaderSys)
    {
       GLSLProgram instance=shaderSys.getProgram(id);
       if(instance==null)
       {
           instance=new PlanetShader(id,shaderSys);
           shaderSys.addProgram(instance);
       }
       return instance;
    }

    public void configureTerrainShader() {
        //samplers
        Sampler2D heightmap = new Sampler2D("u_heightMap");
        Sampler2D colormap=new Sampler2D("u_colorMap");
        Sampler2D normalmap=new Sampler2D("u_normalMap");
        Sampler2D splatmap=new Sampler2D("u_splatMap");
        Sampler2DArray splatarray=new Sampler2DArray("u_splatArray");
        Sampler2D gradient=new Sampler2D("u_atmoGradient");


        //uniforms
        ShaderUniform1f mode = new ShaderUniform1f("mode");

        ShaderUniform3f CDLODcampos = new ShaderUniform3f("cameraPosition");

        ShaderUniform3f CDLODMeshInfo = new ShaderUniform3f("meshInfo");
        ShaderUniform1f CDLODzfar = new ShaderUniform1f("zfar");

        ShaderUniform3f fogcolorTerrain = new ShaderUniform3f("u_Fogcolor");
        ShaderUniform3f lightpos = new ShaderUniform3f("u_LightPos");
        ShaderUniform3f lightambient = new ShaderUniform3f("ambientLight");

        ShaderUniform1f CDLODLodlevel = new ShaderUniform1f("lodlevel");
        ShaderUniform2f CDLODNodeOffset = new ShaderUniform2f("nodeoffset");
        ShaderUniform2f CDLODrange = new ShaderUniform2f("range");
        ShaderUniform1f CDLODQuadScale = new ShaderUniform1f("quad_scale");

        //matrices
        ShaderUniformMatrix4fv MVPMatrix=new ShaderUniformMatrix4fv("u_MVPMatrix");
        ShaderUniformMatrix4fv MVMatrix=new ShaderUniformMatrix4fv("u_MVMatrix");

        //attributes
        linkAttribute("a_gridPosition");
        linkAttribute("a_barycentric");

        //add everything to the shader
        addUniform(MVMatrix);
        addUniform(MVPMatrix);

        addUniform(mode);
        addUniform(heightmap);
        addUniform(colormap);
        addUniform(normalmap);
        addUniform(splatmap);
        addUniform(splatarray);
        addUniform(gradient);

        addUniform(CDLODQuadScale);
        addUniform(CDLODrange);
        addUniform(CDLODcampos);
        addUniform(CDLODMeshInfo);
        addUniform(CDLODzfar);
        addUniform(CDLODLodlevel);
        addUniform(CDLODNodeOffset);
        addUniform(fogcolorTerrain);
        addUniform(lightpos);
        addUniform(lightambient);
    }
}
