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

        sendMatrices(modelMatrix, shadowmapMVPmatrix);
        bindMaterial(mat, shader);
    }

    private void bindMaterial(Material material, GLSLProgram shader) {
        /** bindGridMesh the diffuse texture*/
        if (shader.TextureUniformHandle != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, material.texture.glID);
            GLES20.glUniform1i(shader.TextureUniformHandle, 0);
        }

        /**bindGridMesh the heightmap if there is one*/
        if (material != null && material.displacementMap != null) {
            //heightmap para el vertex shader
            GLES20.glActiveTexture(GLES20.GL_TEXTURE4);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, material.displacementMap.glID);
            GLES20.glUniform1i(shader.DisplacementMapTextureUniformHandle, 4);
        }

        /**bindGridMesh the normal map if there is one*/
        if (material != null && material.bumpMap != null && shader.usesBumpMap && shader.BumpMapTextureUniformHandle != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, material.bumpMap.glID);
            GLES20.glUniform1i(shader.BumpMapTextureUniformHandle, 3);
        }

        if (OpenGLChecks.oes_depth_texture
                && shader.shadowMapTextureUniformHandle != -1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE5);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, AppTextureManager.shadowmap.glID);
            GLES20.glUniform1i(shader.shadowMapTextureUniformHandle, 5);
        }
    }

    private void sendMatrices(float[] modelMatrix, float[] shadowmapMVPmatrix) {
        if (targetProgram.MVMatrixHandle != -1) {
            Matrix.multiplyMM(MatrixManager.modelViewMatrix, 0, MatrixManager.viewMatrix, 0,
                    modelMatrix, 0);
            GLES20.glUniformMatrix4fv(targetProgram.MVMatrixHandle, 1, false, MatrixManager.modelViewMatrix, 0);
        }

        if (targetProgram.ViewMatrixHandle != -1) {
            GLES20.glUniformMatrix4fv(targetProgram.ViewMatrixHandle, 1, false, MatrixManager.viewMatrix, 0);
        }

        if (targetProgram.ModelMatrixHandle != -1) {
            GLES20.glUniformMatrix4fv(targetProgram.ModelMatrixHandle, 1, false, modelMatrix, 0);
        }

        if (targetProgram.ProjMatrixHandle != -1) {
            GLES20.glUniformMatrix4fv(targetProgram.ProjMatrixHandle, 1, false, MatrixManager.projectionMatrix, 0);
        }

        if (targetProgram.MVPMatrixHandle != -1) {
            if (targetProgram.MVMatrixHandle == -1)
                Matrix.multiplyMM(MatrixManager.modelViewMatrix, 0, MatrixManager.viewMatrix, 0,
                        modelMatrix, 0);

            Matrix.multiplyMM(MatrixManager.MVPMatrix, 0, MatrixManager.projectionMatrix, 0,
                    MatrixManager.modelViewMatrix, 0);

            GLES20.glUniformMatrix4fv(targetProgram.MVPMatrixHandle, 1, false, MatrixManager.MVPMatrix, 0);
        }

        if (targetProgram.usesShadowmapMVP && targetProgram.shadowmapMVPmatrixHandle != -1) {
            GLES20.glUniformMatrix4fv(targetProgram.shadowmapMVPmatrixHandle, 1, false, shadowmapMVPmatrix, 0);
        }
    }
}
