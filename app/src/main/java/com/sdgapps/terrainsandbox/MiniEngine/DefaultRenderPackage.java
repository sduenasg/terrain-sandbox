package com.sdgapps.terrainsandbox.MiniEngine;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.FrameBufferInterface;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.OpenGLChecks;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.AppTextureManager;

public class DefaultRenderPackage extends RenderPackage {
    public DefaultRenderPackage(FrameBufferInterface fb, GLSLProgram shader) {
        super(fb, shader);
    }

    @Override
    public void setupForRendering(float[] modelMatrix, float[] shadowmapMVPmatrix, Material mat, GLSLProgram shader) {

        mat.bindTextures();

        /*if (OpenGLChecks.oes_depth_texture
                && shader.shadowMapTextureUniformHandle != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, AppTextureManager.shadowmap.glID);
            GLES20.glUniform1i(shader.shadowMapTextureUniformHandle, 5);
        }*/
    }

}
