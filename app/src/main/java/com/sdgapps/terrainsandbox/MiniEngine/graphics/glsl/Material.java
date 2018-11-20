package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;


import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.TextureManagerGL;


public class Material {
    public String name;
    String diffuseTextureName;
    public Texture texture;
    public Texture bumpMap;
    String bumpMapName;
    String displacementMapName;
    public Texture displacementMap;
    Texture specularMap;
    String specularMapName;
    public GLSLProgram shader;

    Vec3f kd_color;

    public Material() {
    }

    public void bind_texture_to_part() {

        if (diffuseTextureName != null)
            texture = TextureManagerGL.getTexture(diffuseTextureName);

        if (bumpMapName != null)
            bumpMap = TextureManagerGL.getTexture(bumpMapName);

        if (specularMapName != null)
            specularMap = TextureManagerGL.getTexture(specularMapName);


        if (displacementMapName != null)
            displacementMap = TextureManagerGL.getTexture(displacementMapName);
    }

    public Material(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTextureFile() {
        return diffuseTextureName;
    }

    public void setTextureFile(String textureFile) {
        this.diffuseTextureName = textureFile;
    }

    public String toString() {
        String str = new String();
        str += "Material name: " + name;
        str += "\n texture ID: " + texture.glID;
        str += "\texture file: " + diffuseTextureName;
        return str;
    }
}
