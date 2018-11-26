package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

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
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0 + activeTarget);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexture.glID);
            GLES20.glUniform1i(glHandle, activeTarget);
        }
    }
}
