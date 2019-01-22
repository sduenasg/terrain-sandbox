package com.sdgapps.terrainsandbox;

import android.content.res.AssetManager;
import android.opengl.GLES30;

import com.sdgapps.terrainsandbox.MVP.SceneInterface;
import com.sdgapps.terrainsandbox.MiniEngine.GameObject;
import com.sdgapps.terrainsandbox.MiniEngine.Scene;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Camera;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.CircleBillboard;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.FlyAround;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Light;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.OrbitAroundPivot;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Skybox;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Color4f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.DefaultFrameBuffer;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.ShadowMapFrameBuffer;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture2D;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.CDLODSettings;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.Planet;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.TerrainData;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.TerrainInterface;
import com.sdgapps.terrainsandbox.shaders.SkyboxProgram;
import com.sdgapps.terrainsandbox.shaders.SunProgram;

public class MainScene extends Scene implements SceneInterface {

    private static final String[] scenes = new String[]
            {
                    "planetinfo.txt"
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
    public void loadScene(AssetManager assetMngr) {
        TerrainData mTerrainData = new TerrainData(scenes[sceneIdx],assetMngr);
        mTerrainData.LoadTextures(engineManagers);
        SetupPlanetScene(mTerrainData);
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

        engineManagers.mainCamera = sceneCamera;

        /*Light initialization*/
        GameObject sunGO = new GameObject();

        sun = new Light();

        add(sunGO);

        Material sunMaterial = new Material();
        sunMaterial.shader = SunProgram.createInstance("sunshader", engineManagers.sShaderSystem);
        sunGO.add(new CircleBillboard(30,defaultFB,sunMaterial,10000000));
        engineManagers.mainLight = sun;

        /*Skybox initialization*/
        GameObject skyboxGO=new GameObject();
        Skybox skyboxBehavior=new Skybox();
        Material skyboxMaterial=new Material();
        skyboxMaterial.shader=SkyboxProgram.createInstance("skyboxshader",engineManagers.sShaderSystem);
        skyboxBehavior.material=skyboxMaterial;
        skyboxGO.add(skyboxBehavior);

        String[] skyboxtextures=new String[]{"textures/stars.pkm","textures/stars.pkm","textures/stars.pkm","textures/stars.pkm","textures/stars.pkm","textures/stars.pkm"};
        Texture skyboxtex = engineManagers.textureManager.addCubeTexture(skyboxtextures,false,false,Texture.FILTER_LINEAR,Texture.WRAP_REPEAT);
        skyboxMaterial.addTexture(skyboxtex,"skyboxTex");
        add(skyboxGO);

        /*Terrain initialization*/
        Planet planet = new Planet();
        planet.camFly = cameraFly;
        GameObject terrainGameObject = new GameObject();
        add(terrainGameObject);
        terrainGameObject.add(planet);
        Texture atmosphereGradient = engineManagers.textureManager.add2DTexture("textures/earth/atmogradient.png", true, false, Texture.FILTER_LINEAR, Texture2D.WRAP_REPEAT, false,true);
        planet.initialize(terrainData,atmosphereGradient);

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


        planet.initializeRenderModes(defaultFB, shadowMapFB);
        planet.freeHeightmapPixels();
        initialized = true;

        activeTerrain = planet;
    }

    @Override
    public void draw() {

        if (sun != null)
            GLES30.glClearColor(sun.fogColor.r, sun.fogColor.g, sun.fogColor.b, 1f);

        super.draw();
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


    @Override
    public void setAspectRatio(float aspectRatio) {
        super.setAspectRatio(aspectRatio);
        this.aspectRatio = aspectRatio;
        if (sceneCamera != null)
            sceneCamera.setAspectRatio(aspectRatio);
    }
}
