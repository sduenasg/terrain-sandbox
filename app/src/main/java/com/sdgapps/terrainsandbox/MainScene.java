package com.sdgapps.terrainsandbox;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.sdgapps.terrainsandbox.MVP.SceneInterface;
import com.sdgapps.terrainsandbox.MiniEngine.GameObject;
import com.sdgapps.terrainsandbox.MiniEngine.Scene;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.*;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.*;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.*;
import com.sdgapps.terrainsandbox.shaders.SunProgram;

public class MainScene extends Scene implements SceneInterface {

    private static final String[] scenes = new String[]
            {
                    "planetinfo", "sitgesinfo", "originalinfo"
            };

    private Light sun;
    private Camera sceneCamera;
    private FlyAround cameraFly;
    private OrbitAroundPivot orb;
    private SceneDataPOJO loadedSceneData;

    private int sceneIdx = 0;
    private ShadowMapFrameBuffer shadowMapFB = new ShadowMapFrameBuffer();
    private DefaultFrameBuffer defaultFB = new DefaultFrameBuffer();
    private float aspectRatio = 1;
    private boolean initialized = false;
    private TerrainInterface activeTerrain;

    /**
     * Requires to run on the GL thread
     */
    void SetupScene(Resources res) {
        TerrainData mTerrainData = new TerrainData(scenes[sceneIdx], res);
        mTerrainData.LoadTextures(res);

        if (mTerrainData.isPlanetaryScene())
            SetupPlanetScene(mTerrainData);
        else
            SetupFlatScene(res, mTerrainData);
    }

    private void SetupFlatScene(Resources res, TerrainData terrainData) {

    }

    private void SetupPlanetScene(TerrainData terrainData) {
        /*Camera initialization*/
        GameObject camGO = new GameObject();

        cameraFly=new FlyAround();

        sceneCamera = new Camera();
        camGO.add(sceneCamera);

        sceneCamera.shadowmapCaster = sun;
        sceneCamera.setAspectRatio(aspectRatio);

        add(camGO);
        Singleton.systems.mainCamera = sceneCamera;

        /*Light initialization*/
        GameObject sunGO = new GameObject();

        sun = new Light();

        add(sunGO);

        Material sunMaterial = new Material();
        sunMaterial.shader = SunProgram.createInstance("sunshader");
        sunGO.add(new CircleBillboard(30,defaultFB,sunMaterial,10000000));
        Singleton.systems.mainLight = sun;

        /*Terrain initialization*/
        Planet planet = new Planet();
        planet.camFly = cameraFly;
        GameObject terrainGameObject = new GameObject();
        terrainGameObject.add(planet);
        planet.initialize(terrainData);

        float planetRadius = planet.terrainXZ / 2;
        float planetXZ = planet.terrainXZ;

        camGO.transform.position.set(-planetRadius, 0, -planetRadius);
        camGO.transform.rotation.lookAt(new Vec3f(1, 0, 1), new Vec3f(0, 1, 0));
        camGO.add(cameraFly);

        //radius of a 2d circle (xz) that contains the whole terrain mesh
        float gridMaxRadius = (float) Math.sqrt(planetXZ * planetXZ + planetXZ * planetXZ);
        sceneCamera.setupShadowMapCamera(gridMaxRadius);


        orb=new OrbitAroundPivot();
        sunGO.add(orb);
        orb.setPivot(SimpleVec3fPool.create(planetRadius,
                0,
                planetRadius),gridMaxRadius *100 );

        sunGO.add(sun);
        sun.initData(
                planet.atmosphereColor,
                new Color4f(1, 1, 1, 1),
                new Color4f(1f, 1f, 1f, 1f),
                new Color4f(0f, 0.f, 0, 1));

        /*If there is loaded scene data, restore it*/
        if (loadedSceneData != null) {
            planet.setConfig(loadedSceneData.terrainSettings);
            sceneCamera.transform.position.set(loadedSceneData.cameraX, loadedSceneData.cameraY, loadedSceneData.cameraZ);

            sceneCamera.transform.rotation.setFromArray(loadedSceneData.cameraQuaternion);
            loadedSceneData = null;
        }

        add(terrainGameObject);
        planet.initializeRenderModes(defaultFB, shadowMapFB);
        planet.freeHeightmapPixels();
        initialized = true;

        activeTerrain = planet;
    }

    public void setupShadowMapFB() {
        shadowMapFB.setup();
    }

    @Override
    public void draw() {

        if (sun != null)
            GLES20.glClearColor(sun.fogColor.r, sun.fogColor.g, sun.fogColor.b, 1f);

        super.draw();
    }


    public void invalidateGLData() {

        if (activeTerrain != null)
            activeTerrain.invalidateGLData();
    }

    @Override
    public void shadowMapMode(boolean enabled) {
        activeTerrain.shadowMapMode(enabled);
    }

    @Override
    public void wireframeMode(boolean enabled) {
        activeTerrain.wireframeMode(enabled);
    }

    @Override
    public void textureMode(boolean enabled) {
        activeTerrain.textureMode(enabled);
    }

    @Override
    public void solidMode(boolean enabled) {
        activeTerrain.solidMode(enabled);
    }

    @Override
    public void drawAABBMode(boolean enabled) {
        activeTerrain.drawAABBMode(enabled);
    }

    @Override
    public void sunElevation(float f) {
        orb.rotateElevation(f);
    }

    @Override
    public void sunAzimuth(float f) {
        orb.rotateAzimuth(f);
    }

    @Override
    public void lightAutorotate(boolean enabled) {
        orb.setOrbitEnabled(enabled);
    }


    @Override
    public void setRangeDistance(float f) {
        activeTerrain.setRangeDetail(f);
    }

    @Override
    public void loadScene(int s) {

    }

    @Override
    public void resetCamera() {
        cameraFly.reset();
    }



    @Override
    public int getScene() {
        return sceneIdx;
    }

    @Override
    public void setScene(int scene) {

        sceneIdx = scene;
    }

    @Override
    public void setVerticalTranslation(float v) {
        cameraFly.setVerticalTranslation(v);
    }

    @Override
    public void setHorizontalTranslation(float h) {
        cameraFly.setHorizontalTranslation(h);
    }

    @Override
    public void setWalkForward(float walk){cameraFly.setWalk(walk);
    }

    @Override
    public void setCameraLookDirection(float x, float y, float z) {
        cameraFly.updateDirection(x, y, z);
    }

    @Override
    public int getRangeDistMax() {
        return activeTerrain.getRangeDistMax();
    }

    @Override
    public int getRangeDistMin() {
        return activeTerrain.getRangeDistMin();
    }

    @Override
    public int getRangeDistFactor() {
        return activeTerrain.getRangeSteps();
    }

    @Override
    public SceneDataPOJO getSceneDataPOJO() {

        SceneDataPOJO result = new SceneDataPOJO();
        result.terrainSettings = new CDLODSettings();
        if (activeTerrain != null && sceneCamera != null) {

            result.terrainSettings = activeTerrain.getSettings();

            result.cameraQuaternion = sceneCamera.transform.rotation.getQuaternionAsArray();

            result.cameraX = sceneCamera.transform.position.x;
            result.cameraY = sceneCamera.transform.position.y;
            result.cameraZ = sceneCamera.transform.position.z;
        }
        return result;
    }

    @Override
    public void setLoadedData(SceneDataPOJO sceneData) {
        loadedSceneData = sceneData;

    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public void update() {
        super.update();
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        if (sceneCamera != null)
            sceneCamera.setAspectRatio(aspectRatio);
    }
}
