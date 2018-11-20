package com.sdgapps.terrainsandbox.MVP;


/**
 * This interface corresponds to the main view of the app, where the user can click around the screen
 * to move the camera, open the navigation drawer and interact with the settings
 */
public interface MainViewMvp extends ViewMvp {

    //interface that listeners to this class need to implement
    interface MainViewMvpListener {
        void onShadowmapModeClicked(boolean enabled);

        void onWireframeModeClicked(boolean enabled);

        void onTextureModeClicked(boolean enabled);

        void onSolidModeClicked(boolean enabled);

        void onDebugAABBModeClicked(boolean enabled);

        void onSunElevationChanged(float f);

        void onSunAzimuthChanged(float f);

        void onRangeDistanceChanged(float f);

        void onRequestLoadScene(int s);

        void onResetCameraClicked();

        void onLightAutorotateClicked(boolean enabled);

        void onSceneSelected(int scene);

        int getScene();

        void onVerticalTranslation(float v);

        void onHorizontalTranslation(float h);

        void onWalkForward(float walk);

        void onCameraLookDirectionChanged(float x, float y, float z);

        void onFinishedCreatingSurface();

        void onUpdateFps(int fps);

        void onUpdateDrawcalls(int drawcalls);

        void onFinishedLoadingScene();

        void onFinishedRestoringEGLContext();
    }

    /**
     * Set a listener that will be notified by this MVC view
     *
     * @param listener listener that should be notified; null to clear
     */
    void setListener(MainViewMvpListener listener);

    void unregisterListener();

    void updateFps(int fps);

    void updateDrawCalls(int drawcalls);

    void hideLoadingProgress();

    void setupRangeSeekbar(int rangeSeekbarMax);

}

