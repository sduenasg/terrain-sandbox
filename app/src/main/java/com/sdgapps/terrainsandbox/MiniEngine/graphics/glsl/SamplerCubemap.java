package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.opengl.GLES30;

public class SamplerCubemap extends Sampler {
    public SamplerCubemap(String name) {
        super(name);
    }

    @Override
    public void bind()
    {
        if(mTexture!=null) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE0 + activeTarget);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, mTexture.glID);
            GLES30.glUniform1i(glHandle, activeTarget);
        }
    }
}
