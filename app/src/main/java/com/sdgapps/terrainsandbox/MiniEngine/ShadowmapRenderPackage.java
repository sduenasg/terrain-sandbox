package com.sdgapps.terrainsandbox.MiniEngine;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.FrameBufferInterface;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;


//TODO CLEANUP Required
public class ShadowmapRenderPackage extends RenderPackage {
    public ShadowmapRenderPackage(FrameBufferInterface fb, GLSLProgram shader) {
        super(fb, shader);
    }

    @Override
    public void setupForRendering(float[] modelMatrix, float[] shadowmapMVPmatrix, Material mat, GLSLProgram shader) {

       // sendMatrices(modelMatrix, shadowmapMVPmatrix);
        mat.bindTextures();
      //  bindMaterial(mat, shader);
    }

    private void bindMaterial(Material material, GLSLProgram shader) {
        /*if (material != null && material.displacementMap != null) {

            GLES30.glActiveTexture(GLES30.GL_TEXTURE4);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, material.displacementMap.glID);
            GLES30.glUniform1i(shader.DisplacementMapTextureUniformHandle, 4);
        }*/
    }

    /*private void sendMatrices(float[] modelMatrix, float[] shadowmapMVPmatrix) {
        Matrix.multiplyMM(MatrixManager.shadowmapModelViewMatrix, 0, MatrixManager.shadowmapViewMatrix, 0,
                modelMatrix, 0);
        Matrix.multiplyMM(shadowmapMVPmatrix, 0, MatrixManager.shadowmapProjectionMatrix, 0,
                MatrixManager.shadowmapModelViewMatrix, 0);
        GLES30.glUniformMatrix4fv(targetProgram.MVPMatrixHandle, 1, false, shadowmapMVPmatrix, 0);
    }*/

}
