package com.sdgapps.terrainsandbox.MiniEngine.terrain;

public interface TerrainInterface {
    void shadowMapMode(boolean enabled);

    void wireframeMode(boolean enabled);

    void textureMode(boolean enabled);

    void solidMode(boolean enabled);

    void drawAABBMode(boolean enabled);

    void invalidateGLData();

    void setRangeDetail(float f);

    CDLODSettings getSettings();

    int getRangeDistMax();

    int getRangeDistMin();

    int getRangeSteps();
}
