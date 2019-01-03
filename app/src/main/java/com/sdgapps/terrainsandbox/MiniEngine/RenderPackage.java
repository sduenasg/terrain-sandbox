package com.sdgapps.terrainsandbox.MiniEngine;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.FrameBufferInterface;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;

/**
 * Describes and sets up a render operation.
 * - Frame buffer to render to
 * - Shader program to use
 **/
public class RenderPackage {
    public FrameBufferInterface targetFB;
    public GLSLProgram targetProgram;

    public RenderPackage(FrameBufferInterface fb, GLSLProgram shader) {
        targetFB = fb;
        targetProgram = shader;
    }

    public void bind() {
        targetFB.bind();
    }


    public void setupForRendering(float[] modelMatrix, float[] shadowmapMVPmatrix, Material mat, GLSLProgram shader) {

    }
}
