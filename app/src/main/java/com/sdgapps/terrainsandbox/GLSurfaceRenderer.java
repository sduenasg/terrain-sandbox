package com.sdgapps.terrainsandbox;

import android.content.res.Resources;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MVP.MainViewMvp;
import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.OpenGLChecks;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.AppTextureManager;
import com.sdgapps.terrainsandbox.utils.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceRenderer implements GLSurfaceView.Renderer {

    private boolean firstSurfaceChange = false;
    public Resources resources;
    private boolean first_load = true;
    public static int surface_width = 0;
    public static int surface_height = 0;
    private float ratio;
    public float centerX = 0;
    public float centerY = 0;
    MainScene worldScene = new MainScene();
    private MainViewMvp.MainViewMvpListener presenter;

    static final int FRAGMENT_SHADER_DERIVATIVE_HINT_OES = 0x8B8B;

    public GLSurfaceRenderer(MainViewMvp.MainViewMvpListener _presenter, Resources _resources) {
        presenter = _presenter;
        resources = _resources;
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int w, int h) {

        firstSurfaceChange = true;
        if (h == 0) {
            h = 1;
        }

        surface_width = w;
        surface_height = h;
        centerX = w / 2;
        centerY = h / 2;

        GLES20.glViewport(0, 0, surface_width, surface_height);
        ratio = (float) w / h;

        worldScene.setAspectRatio(ratio);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        /* Opengl configuration */
        configGL();

        if (first_load) // First surface creation
        {
            Logger.log("Lifecycle: ON SURFACE CREATED");
            first_load = false;
            SimpleVec3fPool.init();
            OpenGLChecks.runChecks();
            if (OpenGLChecks.standard_derivatives) {
                //Accuracy of the derivative calculations. Default is GL_DONT_CARE, other possible values are GL_NICEST, GL_FASTEST
                GLES20.glHint(FRAGMENT_SHADER_DERIVATIVE_HINT_OES, GLES20.GL_FASTEST);
            }

            Singleton.systems.sTime.tickStart();
            AppTextureManager.initialize(resources);
            Singleton.systems.sShaderSystem.res = resources;

            if (OpenGLChecks.oes_depth_texture)
                worldScene.setupShadowMapFB();

            if (presenter != null)
                presenter.onFinishedCreatingSurface();
        } else {
            Logger.log("Lifecycle: ON SURFACE CREATED");
            /*
             * Assume EGL context loss, re-submit shaders and textures to the gpu.
             * The meshes will re-submit their data to the gpu themselves as
             * long as they've been notified about the GL Context loss.
             *
             */
            Singleton.systems.sShaderSystem.reloadShaders();
            AppTextureManager.reuploadTextures(resources);

            //Setup the shadow map depth render buffer again
            worldScene.setupShadowMapFB();

            if (presenter != null)
                presenter.onFinishedRestoringEGLContext();
        }
    }

    /*
     * Notify the scene objects that their GL data is not valid anymore
     * and needs to be resubmitted to the GPU.
     */
    public void invalidateGLData() {
        if (worldScene != null)
            worldScene.invalidateGLData();
    }

    public void update() {

        worldScene.update();
    }

    public void loadScene() {
        worldScene.SetupScene(resources);
        presenter.onFinishedLoadingScene();
    }


    @Override
    public void onDrawFrame(GL10 unused) {
        Singleton.systems.sTime.update();
        Singleton.systems.sTime.tickEnd();
        Matrix.setIdentityM(MatrixManager.viewMatrix, 0);

        if (!first_load) {
            if (firstSurfaceChange) update();

            draw();
        }
    }

    private void draw() {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        worldScene.draw();
    }

    private void configGL() {
        GLES20.glLineWidth(3);
        GLES20.glClearColor(0.8f, 0.8f, 0.9f, 0.8f);
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);

        /* Depth buffer */
        GLES20.glClearDepthf(1.0f);
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        GLES20.glDepthFunc(GLES20.GL_LEQUAL);

        GLES20.glDepthMask(true);
    }
}
