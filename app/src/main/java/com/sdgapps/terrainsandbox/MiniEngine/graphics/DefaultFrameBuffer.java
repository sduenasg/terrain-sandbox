package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.GLES20;

import com.sdgapps.terrainsandbox.GLSurfaceRenderer;
import com.sdgapps.terrainsandbox.Singleton;

public class DefaultFrameBuffer implements FrameBufferInterface {
    @Override
    public void bind() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        //Recover the standard viewport
        GLES20.glViewport(0, 0, GLSurfaceRenderer.surface_width, GLSurfaceRenderer.surface_height);

        //setup the culling back to GL_BACK
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glColorMask(true, true, true, true);
        GLES20.glClearColor(Singleton.systems.mainLight.fogColor.r, Singleton.systems.mainLight.fogColor.g, Singleton.systems.mainLight.fogColor.b, 1);
    }

    @Override
    public void setup() {
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }
}
