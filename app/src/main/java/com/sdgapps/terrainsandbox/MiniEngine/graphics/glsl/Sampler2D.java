package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;

/**
 * Represents a Sampler2D uniform in a shader
 */
public class Sampler2D extends ShaderUniform {

    private Texture mTexture;
    int activeTarget=0;

    public Sampler2D(String name) {
        super(name);
    }

    public void setTexture(Texture t)
    {
        mTexture=t;
    }

    @Override
    public void bind()
    {
        if(mTexture!=null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + activeTarget);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, mTexture.glID);
            GLES30.glUniform1i(glHandle, activeTarget);
        }
    }
}
