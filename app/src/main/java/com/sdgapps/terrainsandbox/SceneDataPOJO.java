package com.sdgapps.terrainsandbox;

import com.sdgapps.terrainsandbox.MiniEngine.terrain.CDLODSettings;

/**
 * Very basic data class used to save the settings so that they can be restored
 * after switching applications and coming back.
 */
public class SceneDataPOJO {

    float cameraX;
    float cameraY;
    float cameraZ;

    float[] cameraQuaternion;

    CDLODSettings terrainSettings;

    static final String cameraXKey = "camX";
    static final String cameraYKey = "camY";
    static final String cameraZKey = "camZ";
    static final String cameraRotationKey = "cr";
    static final String wireframeKey = "wireframe";
    static final String solidKey = "solid";
    static final String textureKey = "texture";
    static final String debugKey = "debug";
    static final String shadowmapKey = "shadowmap";
}
