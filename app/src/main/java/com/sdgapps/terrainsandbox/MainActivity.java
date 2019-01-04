package com.sdgapps.terrainsandbox;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.sdgapps.terrainsandbox.MVP.MainViewMvp;
import com.sdgapps.terrainsandbox.MVP.MainViewMvpImpl;
import com.sdgapps.terrainsandbox.MVP.SceneInterface;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.CDLODSettings;
import com.sdgapps.terrainsandbox.utils.Logger;

import java.util.concurrent.FutureTask;

/**
 * Activity as the presenter in the MVP pattern
 */
public class MainActivity extends AppCompatActivity
        implements Callback, MainViewMvp.MainViewMvpListener {

    private final String INSTRUCTIONS_SHOWN_KEY = "instructionsShown";

    //The view
    private MainViewMvpImpl MVPView;

    //The model
    private SceneInterface MVPModel;

    private GLSurfaceRenderer renderer = null;
    private boolean mInstructionsShown = false;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Logger.log("Lifecycle: CREATE ");

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs3 = configurationInfo.reqGlEsVersion >= 0x30000;
        //GLES300 0x30000
        //GLES301 0x30001
        //GLES301 0x30002
        //Logger.log("GLES "+ configurationInfo.reqGlEsVersion + " " +  0x30000 + " " + 0x30002);

        Logger.log("Supported OpenGL ES "+Double.parseDouble(configurationInfo.getGlEsVersion()));
        if (supportsEs3) {
            MVPView = new MainViewMvpImpl(getLayoutInflater(), null);
            Singleton.systems.sTime.setPresenter(this);
            renderer = new GLSurfaceRenderer(this, getResources());
            MVPModel = renderer.worldScene;

            AssetManager assetMngr = getAssets();
            if (savedInstanceState != null) {
                MVPModel.setLoadedData(unpackBundle(savedInstanceState));
            }

            setContentView(MVPView.getRootView());

        } else {
            Toast.makeText(this, "Error: OpenGL ES3 not supported on this device", Toast.LENGTH_LONG)
                    .show();
            throw new RuntimeException("Open GL ES 3.0 was not found on device");
        }

        Thread.UncaughtExceptionHandler h = new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread th, Throwable ex) {
                Log.e("CDLODterrainDemo", "exception", ex);
            }
        };

        Thread.setDefaultUncaughtExceptionHandler(h);


    }

    @Override
    protected void onStart() {
        super.onStart();
        Logger.log("Lifecycle: START");

        MVPView.setListener(this);
        MVPView.start(renderer);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Logger.log("Lifecycle: STOP");
        MVPView.stop();
        MVPView.unregisterListener();
        if (renderer != null)
            renderer.invalidateGLData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Logger.log("Lifecycle: PAUSE");
        MVPView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Logger.log("Lifecycle: RESUME");
        MVPView.resume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Logger.log("Lifecycle: DESTROY");
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

        if (hasFocus) {
            Logger.log("Lifecycle: HAS FOCUS");
            hideSystemUI();

        } else {
            Logger.log("Lifecycle: LOST FOCUS");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(INSTRUCTIONS_SHOWN_KEY, mInstructionsShown);

        if (MVPModel != null) {
            SceneDataPOJO sceneData = MVPModel.getSceneDataPOJO();
            outState.putFloat(SceneDataPOJO.cameraXKey, sceneData.cameraX);
            outState.putFloat(SceneDataPOJO.cameraYKey, sceneData.cameraY);
            outState.putFloat(SceneDataPOJO.cameraZKey, sceneData.cameraZ);

            outState.putFloatArray(SceneDataPOJO.cameraRotationKey, sceneData.cameraQuaternion);

            outState.putBoolean(SceneDataPOJO.wireframeKey, sceneData.terrainSettings.wireframe);
            outState.putBoolean(SceneDataPOJO.solidKey, sceneData.terrainSettings.solid);
            outState.putBoolean(SceneDataPOJO.textureKey, sceneData.terrainSettings.texture);
            outState.putBoolean(SceneDataPOJO.debugKey, sceneData.terrainSettings.debug);
            outState.putBoolean(SceneDataPOJO.shadowmapKey, sceneData.terrainSettings.shadowmap);
        }
    }

    private SceneDataPOJO unpackBundle(Bundle bundle) {
        mInstructionsShown = bundle.getBoolean(INSTRUCTIONS_SHOWN_KEY);

        SceneDataPOJO sceneData = new SceneDataPOJO();

        sceneData.cameraX = bundle.getFloat(SceneDataPOJO.cameraXKey);
        sceneData.cameraY = bundle.getFloat(SceneDataPOJO.cameraYKey);
        sceneData.cameraZ = bundle.getFloat(SceneDataPOJO.cameraZKey);

        sceneData.cameraQuaternion = bundle.getFloatArray(SceneDataPOJO.cameraRotationKey);
        sceneData.terrainSettings = new CDLODSettings();

        sceneData.terrainSettings.wireframe = bundle.getBoolean(SceneDataPOJO.wireframeKey);
        sceneData.terrainSettings.solid = bundle.getBoolean(SceneDataPOJO.solidKey);
        sceneData.terrainSettings.texture = bundle.getBoolean(SceneDataPOJO.textureKey);
        sceneData.terrainSettings.debug = bundle.getBoolean(SceneDataPOJO.debugKey);
        sceneData.terrainSettings.shadowmap = bundle.getBoolean(SceneDataPOJO.shadowmapKey);

        return sceneData;
    }

    @Override
    public void onUpdateFps(final int fps) {
        runOnUiThread(new Runnable() {
            public void run() {
                MVPView.updateFps(fps);
            }
        });
    }

    @Override
    public void onUpdateDrawcalls(final int drawcalls) {
        runOnUiThread(new Runnable() {
            public void run() {
                MVPView.updateDrawCalls(drawcalls);
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (MVPView.isDrawerOpen()) {
            MVPView.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }


    private void hideSystemUI() {

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void changeSceneFromUIThread() {
        MVPView.showProgressIndicator();
        Runnable task = new Runnable() {
            @Override
            public void run() {

                //renderer.loadScene();
                MVPModel.loadScene(getResources());
                //worldScene.SetupScene(resources);
                onFinishedLoadingScene();
            }
        };

        FutureTask preLoadSceneTask = new FutureTask(new CallBackTask(task, this), null);
        MVPView.queueTaskForGLView(preLoadSceneTask);
    }

    @Override
    public void onCallbackTaskFinished() {
    }

    @Override
    public void onFinishedLoadingScene() {
        runOnUiThread(new Runnable() {
            public void run() {
                MVPView.dismissProgressIndicator();
                int rangeSeekbarMax = (MVPModel.getRangeDistMax() - MVPModel.getRangeDistMin()) * MVPModel.getRangeDistFactor();
                MVPView.setupRangeSeekbar(rangeSeekbarMax);
            }
        });
    }

    @Override
    public void onFinishedCreatingSurface() {
        //renderer.loadScene();
        MVPModel.loadScene(getResources());
        onFinishedLoadingScene();
        /*
         * Be careful with runonuithread, if the activity gets destroyed
         * after the runnable has already been queued to run, it might attempt to reach
         * variables/fields that might have been destroyed (null)
         */
        runOnUiThread(new Runnable() {
            public void run() {
                MVPView.configureUIListeners();

                if (!mInstructionsShown) {
                    MVPView.showViewInstructions();
                    mInstructionsShown = true;
                }
            }
        });
    }

    @Override
    public void onFinishedRestoringEGLContext() {
        runOnUiThread(new Runnable() {
            public void run() {
                MVPView.configureUIListeners();

            }
        });
    }

    @Override
    public void onShadowmapModeClicked(boolean enabled) {
        if (MVPModel.isInitialized())
            MVPModel.shadowMapMode(enabled);
    }

    @Override
    public void onWireframeModeClicked(boolean enabled) {
        if (MVPModel.isInitialized())
            MVPModel.wireframeMode(enabled);
    }

    @Override
    public void onTextureModeClicked(boolean enabled) {
        if (MVPModel.isInitialized())
            MVPModel.textureMode(enabled);
    }

    @Override
    public void onSolidModeClicked(boolean enabled) {
        if (MVPModel.isInitialized())
            MVPModel.solidMode(enabled);
    }

    @Override
    public void onDebugAABBModeClicked(boolean enabled) {
        if (MVPModel.isInitialized())
            MVPModel.drawAABBMode(enabled);
    }

    @Override
    public void onSunElevationChanged(float f) {
        if (MVPModel.isInitialized())
            MVPModel.sunElevation(f);
    }

    @Override
    public void onSunAzimuthChanged(float f) {
        if (MVPModel.isInitialized())
            MVPModel.sunAzimuth(f);
    }

    @Override
    public void onRangeDistanceChanged(float f) {
        if (MVPModel.isInitialized()) {
            float range = (f / MVPModel.getRangeDistFactor()) + MVPModel.getRangeDistMin();
            MVPModel.setRangeDistance(range);
        }
    }

    @Override
    public void onResetCameraClicked() {
        if (MVPModel.isInitialized()) {
            MVPModel.resetCamera();
        }
    }

    @Override
    public void onLightAutorotateClicked(boolean enabled) {
        if (MVPModel.isInitialized())
            MVPModel.lightAutorotate(enabled);
    }

    @Override
    public void onSceneSelected(int scene) {
        if (MVPModel.isInitialized()) {
            MVPModel.setScene(scene);
            changeSceneFromUIThread();
        }
    }

    @Override
    public int getScene() {

        return MVPModel.getScene();
    }

    @Override
    public void onVerticalTranslation(float v) {
        if (MVPModel.isInitialized())
            MVPModel.setVerticalTranslation(v);
    }

    @Override
    public void onHorizontalTranslation(float h) {
        if (MVPModel.isInitialized())
            MVPModel.setHorizontalTranslation(h);
    }

    @Override
    public void onWalkForward(float walk) {
        if (MVPModel.isInitialized())
            MVPModel.setWalkForward(walk);
    }

    @Override
    public void onCameraLookDirectionChanged(float x, float y, float z) {
        if (MVPModel.isInitialized())
            MVPModel.setCameraLookDirection(x, y, z);
    }
}
