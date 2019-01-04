package com.sdgapps.terrainsandbox.MVP;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.sdgapps.terrainsandbox.SceneDataPOJO;

public interface SceneInterface {

    void shadowMapMode(boolean enabled);

    void wireframeMode(boolean enabled);

    void textureMode(boolean enabled);

    void solidMode(boolean enabled);

    void drawAABBMode(boolean enabled);

    void sunElevation(float f);

    void sunAzimuth(float f);

    void setRangeDistance(float f);

    void loadScene(Resources res, AssetManager am);

    void resetCamera();

    void lightAutorotate(boolean enabled);

    void setScene(int scene);

    int getScene();

    void setVerticalTranslation(float v);

    void setHorizontalTranslation(float h);

    void setWalkForward(float walk);

    void setCameraLookDirection(float x, float y, float z);

    int getRangeDistMax();

    int getRangeDistMin();

    int getRangeDistFactor();

    SceneDataPOJO getSceneDataPOJO();


    void setLoadedData(SceneDataPOJO unbundledSceneData);

    boolean isInitialized();

}
