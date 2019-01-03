package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.GLES30;
import android.util.Log;

import com.sdgapps.terrainsandbox.Singleton;
import com.sdgapps.terrainsandbox.utils.Logger;

public class ShadowMapFrameBuffer implements FrameBufferInterface {
    int[] shadowmap_fb, shadowmap_depthRb, shadowmap_renderTex; // the framebuffer, the renderbuffer and the texture to render
    private int mShadowMapWidth;
    private int mShadowMapHeight;
    private final int shadowmapSize = 8192;

    int glID=0;
    @Override
    public void bind() {
        GLES30.glColorMask(false, false, false, false);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_FRONT);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        Singleton.systems.mainCamera.updateShadowMapCamera();

        // bindTextures the previously generated framebuffer
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, shadowmap_fb[0]);
        GLES30.glViewport(0, 0, mShadowMapWidth, mShadowMapHeight);

        // Clear color and buffers
        GLES30.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES30.glClear(GLES30.GL_DEPTH_BUFFER_BIT);
        glID = shadowmap_renderTex[0];
    }

    @Override
    public void setup() {

        boolean mHasDepthTextureExtension = OpenGLChecks.oes_depth_texture;
        if (!mHasDepthTextureExtension) {

        } else {


            mShadowMapWidth = Math.min(shadowmapSize, OpenGLChecks.GL_MAX_TEXTURE_SIZE / 2);
            mShadowMapHeight = Math.min(shadowmapSize, OpenGLChecks.GL_MAX_TEXTURE_SIZE / 2);

            shadowmap_fb = new int[1];
            shadowmap_depthRb = new int[1];
            shadowmap_renderTex = new int[1];

            // create a framebuffer object
            GLES30.glGenFramebuffers(1, shadowmap_fb, 0);

            // create render buffer and bindTextures 16-bit depth buffer
            GLES30.glGenRenderbuffers(1, shadowmap_depthRb, 0);
            GLES30.glBindRenderbuffer(GLES30.GL_RENDERBUFFER, shadowmap_depthRb[0]);
            GLES30.glRenderbufferStorage(GLES30.GL_RENDERBUFFER, GLES30.GL_DEPTH_COMPONENT16, mShadowMapWidth, mShadowMapHeight);

            // Try to use a texture depth component
            GLES30.glGenTextures(1, shadowmap_renderTex, 0);
            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, shadowmap_renderTex[0]);

            // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
          /* GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);*/
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);

            // Remove artifact on the edges of the shadowmap
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);

            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, shadowmap_fb[0]);

            Logger.log("Depth texture extension oes " + mHasDepthTextureExtension);
            // Use a depth texture, uses the oes_depth_texture extension, so no shadows if the extension is not available
            GLES30.glTexImage2D(GLES30.GL_TEXTURE_2D, 0, GLES30.GL_DEPTH_COMPONENT, mShadowMapWidth, mShadowMapHeight, 0, GLES30.GL_DEPTH_COMPONENT, GLES30.GL_UNSIGNED_INT, null);
            // Attach the depth texture to FBO depth attachment point
            GLES30.glFramebufferTexture2D(GLES30.GL_FRAMEBUFFER, GLES30.GL_DEPTH_ATTACHMENT, GLES30.GL_TEXTURE_2D, shadowmap_renderTex[0], 0);

            // check FBO status
            int FBOstatus = GLES30.glCheckFramebufferStatus(GLES30.GL_FRAMEBUFFER);
            if (FBOstatus != GLES30.GL_FRAMEBUFFER_COMPLETE) {
                Log.e("Shadowmap_fbuffer", "GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
                throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
            }

            glID = shadowmap_renderTex[0];
            GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, 0);
        }
    }
}
