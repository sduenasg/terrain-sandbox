package com.sdgapps.terrainsandbox;

import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MVP.MainViewMvp;
import com.sdgapps.terrainsandbox.MiniEngine.EngineManagers;
import com.sdgapps.terrainsandbox.MiniEngine.GameControl;
import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.OpenGLChecks;
import com.sdgapps.terrainsandbox.utils.Logger;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceRenderer implements GLSurfaceView.Renderer {

    private boolean firstSurfaceChange = false;
    private boolean first_load = true;
    public static int surface_width = 0;
    public static int surface_height = 0;
    private float ratio;
    public float centerX = 0;
    public float centerY = 0;

    GameControl engine=new GameControl();
    private MainViewMvp.MainViewMvpListener presenter;

    @Deprecated
    static final int FRAGMENT_SHADER_DERIVATIVE_HINT_OES = 0x8B8B; //default in GLES3

    public GLSurfaceRenderer(MainViewMvp.MainViewMvpListener _presenter) {
        presenter = _presenter;
        MainScene world=new MainScene();
        engine.addSceneAndSetCurrent(world);
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

        GLES30.glViewport(0, 0, surface_width, surface_height);
        ratio = (float) w / h;

        engine.setAspectRatio(ratio);
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config) {
        /* Opengl configuration */
        configGL();
        Logger.log("Lifecycle: ON SURFACE CREATED");
        if (first_load) // First surface creation
        {
            first_load = false;
            SimpleVec3fPool.init();
            SimpleQuaternionPool.init();
            OpenGLChecks.runChecks();

            //Accuracy of the derivative calculations. Default is GL_DONT_CARE, other possible values are GL_NICEST, GL_FASTEST
            GLES30.glHint(GLES30.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GLES30.GL_FASTEST);

            engine.getEngineManagers().sTime.tickStart();
            engine.getEngineManagers().textureManager.reset();

            //if (OpenGLChecks.oes_depth_texture)
            //    worldScene.setupShadowMapFB();

            if (presenter != null)
                presenter.onFinishedCreatingSurface();
        } else {

            /*
             * Assume EGL context loss, re-submit shaders and textures to the gpu.
             * The meshes will re-submit their data to the gpu themselves as
             * long as they've been notified about the GL Context loss.
             */
            engine.getEngineManagers().sShaderSystem.reloadShaders();
            engine.getEngineManagers().textureManager.reuploadTextures();

            //Setup the shadow map depth render buffer again
            //worldScene.setupShadowMapFB();

            if (presenter != null)
                presenter.onFinishedRestoringEGLContext();
        }
    }

    /*
     * Notify the scene objects that their GL data is not valid anymore
     * and needs to be resubmitted to the GPU.
     */
    public void invalidateGLData() {
        if (engine != null)
            engine.invalidateGLData();
    }

    public void update() {

        engine.update();
    }

    @Override
    public void onDrawFrame(GL10 unused) {
        engine.getEngineManagers().sTime.update();
        engine.getEngineManagers().sTime.tickEnd();
        Matrix.setIdentityM(MatrixManager.viewMatrix, 0);

        if (!first_load) {
            if (firstSurfaceChange) update();

            draw();
        }
    }

    private void draw() {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT | GLES30.GL_DEPTH_BUFFER_BIT);
        engine.draw();
    }

    private void configGL() {
        GLES30.glLineWidth(3);
        GLES30.glClearColor(0.8f, 0.8f, 0.9f, 1.0f);
        GLES30.glEnable(GLES30.GL_CULL_FACE);
        GLES30.glCullFace(GLES30.GL_BACK);

        /* Depth buffer */
        GLES30.glClearDepthf(1.0f);
        GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        GLES30.glDepthFunc(GLES30.GL_LEQUAL);

        GLES30.glDepthMask(true);
    }

    public EngineManagers getEngineManagers() {
        return engine.getEngineManagers();
    }
}
