package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES20;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;

public class Sampler2D extends ShaderVariable {

    private Texture mTexture;
    public Sampler2D(String name) {
        super(name);
    }
    int activeTarget=0;

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
