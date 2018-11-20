package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.GLES20;
import android.util.Log;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.AppTextureManager;
import com.sdgapps.terrainsandbox.Singleton;
import com.sdgapps.terrainsandbox.utils.Logger;

public class ShadowMapFrameBuffer implements FrameBufferInterface {
    int[] shadowmap_fb, shadowmap_depthRb, shadowmap_renderTex; // the framebuffer, the renderbuffer and the texture to render
    private int mShadowMapWidth;
    private int mShadowMapHeight;
    private final int shadowmapSize = 8192;

    @Override
    public void bind() {
        GLES20.glColorMask(false, false, false, false);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_FRONT);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        Singleton.systems.mainCamera.updateShadowMapCamera();

        // bind the previously generated framebuffer
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowmap_fb[0]);
        GLES20.glViewport(0, 0, mShadowMapWidth, mShadowMapHeight);

        // Clear color and buffers
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT);
        AppTextureManager.shadowmap.glID = shadowmap_renderTex[0];
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
            GLES20.glGenFramebuffers(1, shadowmap_fb, 0);

            // create render buffer and bind 16-bit depth buffer
            GLES20.glGenRenderbuffers(1, shadowmap_depthRb, 0);
            GLES20.glBindRenderbuffer(GLES20.GL_RENDERBUFFER, shadowmap_depthRb[0]);
            GLES20.glRenderbufferStorage(GLES20.GL_RENDERBUFFER, GLES20.GL_DEPTH_COMPONENT16, mShadowMapWidth, mShadowMapHeight);

            // Try to use a texture depth component
            GLES20.glGenTextures(1, shadowmap_renderTex, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, shadowmap_renderTex[0]);

            // GL_LINEAR does not make sense for depth texture. However, next tutorial shows usage of GL_LINEAR and PCF. Using GL_NEAREST
          /* GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);*/
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Remove artifact on the edges of the shadowmap
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, shadowmap_fb[0]);

            Logger.log("Depth texture extension oes " + mHasDepthTextureExtension);
            // Use a depth texture, uses the oes_depth_texture extension, so no shadows if the extension is not available
            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_DEPTH_COMPONENT, mShadowMapWidth, mShadowMapHeight, 0, GLES20.GL_DEPTH_COMPONENT, GLES20.GL_UNSIGNED_INT, null);
            // Attach the depth texture to FBO depth attachment point
            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_DEPTH_ATTACHMENT, GLES20.GL_TEXTURE_2D, shadowmap_renderTex[0], 0);

            // check FBO status
            int FBOstatus = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (FBOstatus != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                Log.e("Shadowmap_fbuffer", "GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
                throw new RuntimeException("GL_FRAMEBUFFER_COMPLETE failed, CANNOT use FBO");
            }

            AppTextureManager.shadowmap.glID = shadowmap_renderTex[0];
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }
    }
}
