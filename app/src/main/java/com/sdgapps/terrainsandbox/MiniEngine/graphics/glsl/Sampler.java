package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;

/**
 * Generic texture sampler
 */
public class Sampler extends ShaderUniform {

    Texture mTexture;
    int activeTarget=0;

    public Sampler(String name) {
        super(name);
    }

    public void setTexture(Texture t)
    {
        mTexture=t;
    }


}
