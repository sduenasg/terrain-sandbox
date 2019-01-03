package com.sdgapps.terrainsandbox.MiniEngine;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.FrameBufferInterface;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;


public class DefaultRenderPackage extends RenderPackage {
    public DefaultRenderPackage(FrameBufferInterface fb, GLSLProgram shader) {
        super(fb, shader);
    }

    @Override
    public void setupForRendering(float[] modelMatrix, float[] shadowmapMVPmatrix, Material mat, GLSLProgram shader) {

        mat.bindTextures();

        /*if (OpenGLChecks.oes_depth_texture
                && shader.shadowMapTextureUniformHandle != -1) {
            GLES30.glActiveTexture(GLES30.GL_TEXTURE5);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, AppTextureManager.shadowmap.glID);
            GLES30.glUniform1i(shader.shadowMapTextureUniformHandle, 5);
        }*/
    }

}
