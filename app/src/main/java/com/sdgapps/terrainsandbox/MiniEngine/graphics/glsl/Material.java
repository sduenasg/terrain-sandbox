package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;

import java.util.HashMap;
import java.util.Map;

public class Material {
    public String name;

    public GLSLProgram shader;

    private HashMap<String,Texture> textures=new HashMap<>();

    /**
     * @param  nameInShader must be exactly the path of the sampler in the shader files
     */
    public void addTexture(Texture t, String nameInShader)
    {
        textures.put(nameInShader,t);
    }


    public Texture getTexture(String uniformName)
    {
        return textures.get(uniformName);
    }
    /**
     * Binds the shader, and the material's textures to the shader
     * If the texture's nameInShader doesn't match a sampler's path in the GLSL shader, this will crash
     * For performance reasons, not null checking inside a loop in the middle of the render code.
     */

    public void bindShader()
    {
        shader.useProgram();
    }
    public void bindTextures()
    {

        for (Map.Entry<String, Texture> entry  : textures.entrySet())
        {
            //TODO optimize
            Sampler sampler=(Sampler)shader.getUniform(entry.getKey());
            sampler.setTexture(entry.getValue());
            sampler.bind();
        }
    }
}
