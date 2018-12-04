package com.sdgapps.terrainsandbox.shaders;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Sampler2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3F;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.Singleton;

public class CloudProgram extends GLSLProgram {
    private CloudProgram(String id) {
        super(id, R.raw.cloud_vert, R.raw.cloud_frag);
        initShader();
    }

    public static GLSLProgram createInstance(String id)
    {
        GLSLProgram instance=Singleton.systems.sShaderSystem.getProgram(id);
        if(instance==null)
        {
            instance=new CloudProgram(id);
            Singleton.systems.sShaderSystem.addProgram(instance);
        }
        return instance;
    }

    private void initShader() {

        Sampler2D atmotexture = new Sampler2D("u_Texture");
        addUniform(atmotexture);

        ShaderUniform3F campos = new ShaderUniform3F("camPos");
        addUniform(campos);

        ShaderUniform3F lightposworld = new ShaderUniform3F("lightPos");
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
