package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

/**
 * Represents a Sampler2D uniform in a shader
 */
public class Sampler2DArray extends Sampler {

    public Sampler2DArray(String name) {
        super(name);
    }

    @Override
    public void bind()
    {
        if(mTexture!=null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + activeTarget);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, mTexture.glID);
            GLES30.glUniform1i(glHandle, activeTarget);
        }
    }
}
