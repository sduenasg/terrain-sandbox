package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;

public class CloudProgram extends GLSLProgram {
    private CloudProgram(String id, ShaderSystem shaderSys) {
        super(id,"shaders/cloud_vert.glsl","shaders/cloud_frag.glsl",shaderSys,true);
        initShader();
    }

    public static GLSLProgram createInstance(String id, ShaderSystem shaderSys)
    {
        GLSLProgram instance=shaderSys.getProgram(id);
        if(instance==null)
        {
            instance=new CloudProgram(id,shaderSys);
            shaderSys.addProgram(instance);
        }
        return instance;
    }

    private void initShader() {

        Sampler2D atmotexture = new Sampler2D("u_Texture");
        addUniform(atmotexture);

        ShaderUniform3f campos = new ShaderUniform3f("camPos");
        addUniform(campos);

        ShaderUniform3f lightposworld = new ShaderUniform3f("lightPos");
        addUniform(lightposworld);

        ShaderUniformMatrix4fv MVPMatrix=new ShaderUniformMatrix4fv("u_MVPMatrix");
        addUniform(MVPMatrix);

        ShaderUniformMatrix4fv ModelMatrix=new ShaderUniformMatrix4fv("u_Modelmatrix");
        addUniform(ModelMatrix);

        //attributes
        linkAttribute("a_Position");
        linkAttribute("a_Normal");
        linkAttribute("a_TexCoordinate");
    }
}
