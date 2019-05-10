package com.sdgapps.terrainsandbox.MiniEngine.terrain;

import android.opengl.GLES30;

import com.sdgapps.terrainsandbox.MiniEngine.DefaultRenderPackage;
import com.sdgapps.terrainsandbox.MiniEngine.RenderPackage;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.FlyAround;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Light;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Renderer;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Sphere;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Color4f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.FrameBufferInterface;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.LineCube;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.MiniMath;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Quaternion;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;
import com.sdgapps.terrainsandbox.SimpleQuaternionPool;
import com.sdgapps.terrainsandbox.shaders.AtmosphereProgram;
import com.sdgapps.terrainsandbox.shaders.BoundingBoxProgram;
import com.sdgapps.terrainsandbox.shaders.CloudProgram;
import com.sdgapps.terrainsandbox.shaders.PlanetShader;
import com.sdgapps.terrainsandbox.utils.Logger;
import com.sdgapps.terrainsandbox.utils.TimingHelper;

/**
 * Class that represents a planet
 * <p>
 * This class builds a cube by generating 6 CDLOD planes and implements their rendering
 * and terrain interface operations.
 * <p>
 * Spherization of the cube and heightmap displacement are performed in the gpu (vertex shader)
 * <p>
 * Spherization: The simplest and fastest way to spherize a cube is centering it around the origin and normalizing
 * it's points. The drawback is that this doesn't provide the best vertex distribution (the mesh is denser
 * around the borders of the mesh).
 * <p>
 * The following site
 * (https://mathproofs.blogspot.com/2005/07/mapping-cube-to-sphere.html) describes a method that distributes the vertices
 * more evenly around the sphere. This is a more complex/slower calculation for each vertex.
 *
 * <p>
 * Texturing: The following textures are loaded as cubemap faces (separately, every face is an image) that have been
 * prepared for this class using World Machine and a Python script that pre-processes the set of textures.
 * <p>
 * <p>
 * - Heightmap: grayscale image where every pixel represents a height value. An external script has been used so that
 * the edges of the cube (where 2 CDLOD meshes meet) share the same averaged height values to avoid cracks. A pixel should be
 * provided for every vertex of the most detailed mesh (lod 0)
 * <p>
 * - Normal map: object space normal map. This normal map is baked on blender and then spherized in an external python script
 * The python script also fixes the edges of the images to avoid discontinuities.
 * <p>
 * - Color map: Just the color map of the terrain, procedurally generated on World Machine, converted to 6 cubemap face textures
 * using blender.
 *
 * -Splat map: Every channel in the image references a set color or texture. The value of the channel is that color's or texture's weight
 * on that area.
 */
public class Planet extends Renderer implements TerrainInterface {
    private CDLODQuadTree[] cube;

    /**
     * The mesh used to render every node of the 6 quadtree terrains that conform
     * the shape of the planet. (gridsize+1)^2 verts, gridsize^2 quads
     */
    private GridMesh gridMesh;
    private Sphere atmosphere;
    private Sphere clouds;

    /**
     * Render configuration of this CDLODQuadTree terrain
     */
    private CDLODSettings config = new CDLODSettings();
    private RenderPackage defaultPass;

    private int gridSize = 64;
    private float rootQuadScale = 100000;
    private int nLods = 6;
    private float yscale = 60000;
    public float terrainXZ;

    /**
     * The content at each position is the range distance
     * of that lod level.
     */
    private float[] rangeDistance;
    private float[] morphconstz;
    private float[] ranges;
    private final int rangeDistMax = 10;
    private final int rangeDistMin = 1;
    private final int rangeDistSteps = 10;//number of steps between rangeDistMin and rangeDistMax
    private static final float morphstartratio = .1f;

    public Color4f atmosphereColor = new Color4f(218, 220, 255, 1);
    private float atmosphereRadius,cloudlayerRadius;
    private float planetRadius;


    private static final String planetShaderID = "IDPlanetShader";
    private static final String planetShadowedID = "IDPlanetShadowedShader";

    public FlyAround camFly;

    static final String colormapUniformName="u_colorMap";
    static final String bumpMapUniformName="u_normalMap";
    static final String heightmapUniformName="u_heightMap";
    static final String splatmapUniformName="u_splatMap";
    static final String splatsheetUniformName="u_splatSheet";
    static final String splatarrayUniformName="u_splatArray";

    LineCube BoundingBoxGeometry;

    public void initialize(TerrainData data, Texture atmosphereGradient) {

        BoundingBoxGeometry=new LineCube();
        BoundingBoxGeometry.initializeVisuals();

        TimingHelper th=new TimingHelper("Planet initialization...");
        th.start();
        //Setup the 6 faces of the cube
        CDLODQuadTree planetChunkN;
        CDLODQuadTree planetChunkS;
        CDLODQuadTree planetChunkA;
        CDLODQuadTree planetChunkB;
        CDLODQuadTree planetChunkC;
        CDLODQuadTree planetChunkD;

        Material materialN = new Material();
        Material materialS = new Material();
        Material materialA = new Material();
        Material materialB = new Material();
        Material materialC = new Material();
        Material materialD = new Material();

        this.material = new Material();
        GLSLProgram myPlanetShader = PlanetShader.createInstance(planetShaderID,gameObject.engineManagers.sShaderSystem);
        material.shader = myPlanetShader;
        materialN.shader = myPlanetShader;
        materialS.shader = myPlanetShader;
        materialA.shader = myPlanetShader;
        materialB.shader = myPlanetShader;
        materialC.shader = myPlanetShader;
        materialD.shader = myPlanetShader;

        //texturing
        materialN.addTexture(data.TexColorMaps[0],colormapUniformName);
        materialS.addTexture(data.TexColorMaps[2],colormapUniformName);
        materialA.addTexture(data.TexColorMaps[3],colormapUniformName);
        materialB.addTexture(data.TexColorMaps[5],colormapUniformName);
        materialC.addTexture(data.TexColorMaps[1],colormapUniformName);
        materialD.addTexture(data.TexColorMaps[4],colormapUniformName);

        materialN.addTexture(data.TexNormalMaps[0],bumpMapUniformName);
        materialS.addTexture(data.TexNormalMaps[2],bumpMapUniformName);
        materialA.addTexture(data.TexNormalMaps[3],bumpMapUniformName);
        materialB.addTexture(data.TexNormalMaps[5],bumpMapUniformName);
        materialC.addTexture(data.TexNormalMaps[1],bumpMapUniformName);
        materialD.addTexture(data.TexNormalMaps[4],bumpMapUniformName);

        materialN.addTexture(data.TexDisplacementMaps[0],heightmapUniformName);
        materialS.addTexture(data.TexDisplacementMaps[2],heightmapUniformName);
        materialA.addTexture(data.TexDisplacementMaps[3],heightmapUniformName);
        materialB.addTexture(data.TexDisplacementMaps[5],heightmapUniformName);
        materialC.addTexture(data.TexDisplacementMaps[1],heightmapUniformName);
        materialD.addTexture(data.TexDisplacementMaps[4],heightmapUniformName);

        materialN.addTexture(data.TexSplatMaps[0],splatmapUniformName);
        materialS.addTexture(data.TexSplatMaps[2],splatmapUniformName);
        materialA.addTexture(data.TexSplatMaps[3],splatmapUniformName);
        materialB.addTexture(data.TexSplatMaps[5],splatmapUniformName);
        materialC.addTexture(data.TexSplatMaps[1],splatmapUniformName);
        materialD.addTexture(data.TexSplatMaps[4],splatmapUniformName);

        materialN.addTexture(data.TexArraySplat,splatarrayUniformName);
        materialS.addTexture(data.TexArraySplat,splatarrayUniformName);
        materialA.addTexture(data.TexArraySplat,splatarrayUniformName);
        materialB.addTexture(data.TexArraySplat,splatarrayUniformName);
        materialC.addTexture(data.TexArraySplat,splatarrayUniformName);
        materialD.addTexture(data.TexArraySplat,splatarrayUniformName);

        terrainXZ = rootQuadScale * gridSize;

        float maxverts = (float) Math.sqrt(Math.pow(4, nLods - 1) * (gridSize + 1) * (gridSize + 1));
        Logger.log("CDLOD planet: Mesh XZ " + terrainXZ + " mesh side (in verts) at max detail " + maxverts);
        ranges = new float[nLods];
        rangeDistance = new float[nLods];
        morphconstz = new float[nLods];
        setRangeDetail(rangeDistMin);

        Material boundingBoxMaterial = new Material();
        boundingBoxMaterial.shader = BoundingBoxProgram.createInstance("bbmat", gameObject.engineManagers.sShaderSystem);
        planetChunkN = new CDLODQuadTree(true, materialN, gridSize, rootQuadScale, nLods, yscale, ranges, morphconstz, rangeDistance, boundingBoxMaterial);
        planetChunkS = new CDLODQuadTree(true, materialS, gridSize, rootQuadScale, nLods, yscale, ranges, morphconstz, rangeDistance, boundingBoxMaterial);
        planetChunkA = new CDLODQuadTree(true, materialA, gridSize, rootQuadScale, nLods, yscale, ranges, morphconstz, rangeDistance, boundingBoxMaterial);
        planetChunkB = new CDLODQuadTree(true, materialB, gridSize, rootQuadScale, nLods, yscale, ranges, morphconstz, rangeDistance, boundingBoxMaterial);
        planetChunkC = new CDLODQuadTree(true, materialC, gridSize, rootQuadScale, nLods, yscale, ranges, morphconstz, rangeDistance, boundingBoxMaterial);
        planetChunkD = new CDLODQuadTree(true, materialD, gridSize, rootQuadScale, nLods, yscale, ranges, morphconstz, rangeDistance, boundingBoxMaterial);
        th.end();
        cube = new CDLODQuadTree[6];

        planetRadius = terrainXZ / 2f;
        atmosphereRadius = planetRadius * 1.05f;
        cloudlayerRadius=planetRadius*1.02f;

        //positioning of each cube face
        planetChunkN.transform.translate(0, planetRadius, 0);
        planetChunkS.transform.translate(0, -planetRadius, 0);
        planetChunkA.transform.translate(-planetRadius, 0, 0);
        planetChunkB.transform.translate(0, 0, planetRadius);
        planetChunkC.transform.translate(planetRadius, 0, 0);
        planetChunkD.transform.translate(0, 0, -planetRadius);

        //rotation of each cube face
        planetChunkS.transform.rotation.fromAngleNormalAxis(-MiniMath.PI, Vec3f.Zvector);
        planetChunkA.transform.rotation.fromAngleNormalAxis(MiniMath.H_PI, Vec3f.Zvector);
        planetChunkC.transform.rotation.fromAngleNormalAxis(-MiniMath.H_PI, Vec3f.Zvector);
        planetChunkB.transform.rotation.fromAngleNormalAxis(MiniMath.H_PI, Vec3f.Xvector);
        planetChunkD.transform.rotation.fromAngleNormalAxis(-MiniMath.H_PI, Vec3f.Xvector);

        gridMesh = new GridMesh(gridSize + 1);
        gridMesh.timeSystem=gameObject.engineManagers.sTime;
        gridMesh.GenBuffersAndSubmitToGL();

        //pack the 6 cube faces in an array
        cube[0] = planetChunkA;
        cube[1] = planetChunkB;
        cube[2] = planetChunkC;
        cube[3] = planetChunkD;
        cube[4] = planetChunkS;
        cube[5] = planetChunkN;

        //setup up the atmosphere (inverted sphere around the planet)
        atmosphere = new Sphere(
                AtmosphereProgram.createInstance("atmosphereShader",gameObject.engineManagers.sShaderSystem),
                atmosphereRadius, 64, 64);

        atmosphere.GenBuffersAndSubmitToGL();
        atmosphere.transform.position.add(planetRadius, 0, planetRadius);
        atmosphereColor.normalize_noalpha();
        atmosphere.material.addTexture(atmosphereGradient,"u_Texture");

        ShaderUniform3f atmosphereCol = (ShaderUniform3f) atmosphere.material.shader.getUniform("u_atmosphere_color");
        atmosphereCol.set(atmosphereColor);

        //setup up the cloud layer
        clouds = new Sphere(
                CloudProgram.createInstance("cloudShader",gameObject.engineManagers.sShaderSystem),
                cloudlayerRadius, 64, 64);

        clouds.GenBuffersAndSubmitToGL();
        clouds.transform.position.add(planetRadius, 0, planetRadius);
        clouds.material.addTexture(data.Clouds,"u_Texture");

        //Planet position and rotation
        this.transform.objectPivotPosition.set(planetRadius, 0, planetRadius);

        Quaternion rot = new Quaternion();
        rot.fromAngleNormalAxis(MiniMath.H_PI, Vec3f.Zvector);
        this.transform.rotation.multLocal(rot);
        rot.fromAngleNormalAxis(-MiniMath.H_PI, Vec3f.Yvector);
        this.transform.rotation.multLocal(rot);

        //update the node bounding boxes with the planet's world space transforms
        for (CDLODQuadTree chunk : cube)
            chunk.transformBoundingBoxes(transform);
    }

    private void LodSelect() {
        if (config.solid || config.wireframe) {
            transform.updateModelMatrix();

            int minLod = Integer.MAX_VALUE;
            int res;
            for (CDLODQuadTree chunk : cube) {
                res = chunk.LodSelect(gameObject.engineManagers.mainCamera);
                minLod = Math.min(minLod, res);
            }

            SelectionResults.drawnNodes=0;
            if (minLod != Integer.MAX_VALUE) {

                float factor = (minLod + 1) / ((float) cube[0].nLods + 1f);
                factor *= factor;

                if(camFly!=null)
                    camFly.allowedSpeed = camFly.maxSpeed * factor;
            }

        }
    }

    @Override
    public void update() {
        //run the node selection
        LodSelect();
        Quaternion rotation = SimpleQuaternionPool.create();
        rotation.fromAngleNormalAxis(0.0001f,Vec3f.Yvector);
        clouds.transform.rotation.multLocal(rotation);
    }


    @Override
    public void draw() {

        if (!gridMesh.uploadedVBO)
            gridMesh.GenBuffersAndSubmitToGL();

        if (!atmosphere.uploadedVBO)
            atmosphere.GenBuffersAndSubmitToGL();

        for (RenderPackage pass : renderPackages) {
            pass.bind();//binds the frame buffer
            GLSLProgram targetShader = pass.targetProgram;
            targetShader.useProgram();
            setRenderMode();
            gridMesh.bindAttributes(targetShader, false);
            bindPlanetInfo(targetShader);


            for (CDLODQuadTree chunk : cube) {
                chunk.draw(pass, gridMesh, transform);
            }

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
        }

        renderAtmosphere();
        renderClouds();

        if (config.debug) {
            GLES30.glDisable(GLES30.GL_DEPTH_TEST);
            for (CDLODQuadTree chunk : cube) {
                chunk.drawAABB(BoundingBoxGeometry);
            }
            GLES30.glEnable(GLES30.GL_DEPTH_TEST);
        }

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void renderClouds() {

        clouds.material.bindShader();

       
        ShaderUniform3f camPos = (ShaderUniform3f) clouds.material.shader.getUniform("camPos");
        camPos.set( gameObject.engineManagers.mainCamera.transform.position);
        camPos.bind();

        ShaderUniform3f lightPos = (ShaderUniform3f) clouds.material.shader.getUniform("lightPos");
        lightPos.set(gameObject.engineManagers.mainLight.transform.position);
        lightPos.bind();

        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE);

        if(clouds.isPointInside(gameObject.engineManagers.mainCamera.transform.position))
            GLES30.glCullFace(GLES30.GL_FRONT);
        clouds.draw();

        GLES30.glCullFace(GLES30.GL_BACK);
        GLES30.glDisable(GLES30.GL_BLEND);
    }
    private void renderAtmosphere() {

        atmosphere.material.bindShader();
        ShaderUniform3f atmosphereCol = (ShaderUniform3f) atmosphere.material.shader.getUniform("u_atmosphere_color");
        atmosphereCol.bind();

        ShaderUniform3f camPos = (ShaderUniform3f) atmosphere.material.shader.getUniform("camPos");
        camPos.set(gameObject.engineManagers.mainCamera.transform.position);
        camPos.bind();

        ShaderUniform3f lightPos = (ShaderUniform3f) atmosphere.material.shader.getUniform("lightPos");
        lightPos.set(gameObject.engineManagers.mainLight.transform.position);
        lightPos.bind();

        GLES30.glEnable(GLES30.GL_BLEND);
        GLES30.glBlendFunc(GLES30.GL_SRC_ALPHA, GLES30.GL_ONE);

        GLES30.glCullFace(GLES30.GL_FRONT);

        atmosphere.draw();

        GLES30.glCullFace(GLES30.GL_BACK);
        GLES30.glDisable(GLES30.GL_BLEND);
    }

    private void bindPlanetInfo(GLSLProgram shader) {
        ShaderUniform3f meshInfoUniform = (ShaderUniform3f) shader.getUniform("meshInfo");
        meshInfoUniform.set(terrainXZ, gridSize * rootQuadScale, yscale);
        meshInfoUniform.bind();

        ShaderUniform3f v = (ShaderUniform3f) shader.getUniform("u_Fogcolor");
        if (v != null) {
            Color4f fogColor = gameObject.engineManagers.mainLight.fogColor;
            v.set(fogColor.r, fogColor.g, fogColor.b);
            v.bind();
        }

        ShaderUniform1f zfarVar = (ShaderUniform1f) shader.getUniform("zfar");
        if (zfarVar != null) {
            zfarVar.v = gameObject.engineManagers.mainCamera.frustum.zfar;
            zfarVar.bind();
        }

        ShaderUniform3f camPosVar = (ShaderUniform3f) shader.getUniform("cameraPosition");
        if (camPosVar != null) {
            camPosVar.set(gameObject.engineManagers.mainCamera.transform.position);
            camPosVar.bind();
        }
        Light l = gameObject.engineManagers.mainLight;
        ShaderUniform3f lightpos = (ShaderUniform3f) shader.getUniform("u_LightPos");
        if (lightpos != null) {

            lightpos.set(l.mLightPosInEyeSpace[0],l.mLightPosInEyeSpace[1],l.mLightPosInEyeSpace[2]);
            lightpos.bind();
        }

        ShaderUniform3f ambientcolor = (ShaderUniform3f) shader.getUniform("ambientLight");
        if (ambientcolor != null) {
            ambientcolor.set(l.lightAmbient[0],l.lightAmbient[1],l.lightAmbient[2]);
            ambientcolor.bind();
        }
    }

    private void setRenderMode() {
        short solid = 0, wire = 0, texture = 0;

        if (config.solid)
            solid = 1;

        if (config.wireframe)
            wire = 2;

        if (config.texture)
            texture = 4;

        ShaderUniform1f meshMode = (ShaderUniform1f) material.shader.getUniform("mode");
        meshMode.v = (solid | wire | texture);
        meshMode.bind();
    }

    public void initializeRenderModes(FrameBufferInterface defaultFB, FrameBufferInterface shadowmapFB) {
        defaultPass = new DefaultRenderPackage(defaultFB, material.shader);
       // shadowPass = new ShadowmapRenderPackage(shadowmapFB, gameObject.engineManagers.sShaderSystem.shadowMapProgram);
      //  shadowedDefaultPass =
               // new DefaultRenderPackage(defaultFB, ShadowedTerrainShader.createInstance("shadowedterrain"));
        renderPackages.add(defaultPass);
    }

    public void setConfig(CDLODSettings _config) {
        config = _config;
    }

    @Override
    public void shadowMapMode(boolean enabled) {
        config.shadowmap = enabled;

        if (enabled) {
            GLSLProgram shadowed = gameObject.engineManagers.sShaderSystem.getProgram(planetShadowedID);
            if (shadowed != null) {
                material.shader = shadowed;
                ConfigShadowMapModeOn();
            }
        } else {
            material.shader = gameObject.engineManagers.sShaderSystem.getProgram(planetShaderID);
            ConfigShadowMapModeOff();
        }
    }

    @Override
    public void setRangeDetail(float distRange) {
        float f = .005f;
        float prevPos = distRange * terrainXZ * f;

        for (int i = 1; i < nLods + 1; i++) {
            ranges[i - 1] = prevPos + terrainXZ * f * (float) Math.pow(2.8f, i);
            prevPos = ranges[i - 1];
        }

        gameObject.engineManagers.mainCamera.frustum.change_zvalues(rootQuadScale / (float) Math.pow(4, nLods - 1), ranges[nLods - 1]);

        //generate morph constants
        for (int lod = 0; lod < nLods; lod++) {
            float morphend = ranges[lod];
            float morphstart = 0;

            if (lod > 0)
                morphstart = ranges[lod - 1];

            morphstart = MiniMath.lerp(morphend, morphstart, morphstartratio);
            rangeDistance[lod] = 1f / (morphend - morphstart); //y
            morphconstz[lod] = morphend / (morphend - morphstart); //x
        }
    }

    private void ConfigShadowMapModeOn() {

    }

    private void ConfigShadowMapModeOff() {

    }

    @Override
    public void wireframeMode(boolean enabled) {
        config.wireframe = enabled;
    }

    @Override
    public void textureMode(boolean enabled) {
        config.texture = enabled;
    }

    @Override
    public void solidMode(boolean enabled) {
        config.solid = enabled;
    }

    @Override
    public void drawAABBMode(boolean enabled) {
        config.debug = enabled;
    }

    @Override
    public void invalidateGLData() {
        if (gridMesh != null)
            gridMesh.invalidateVBO();
        if (atmosphere != null)
            atmosphere.invalidateVBO();
    }

    public void freeHeightmapPixels() {
        //TODO FIX
        for (CDLODQuadTree chunk : cube) {
           // chunk.material.displacementMap.freepixels();
        }
    }

    @Override
    public CDLODSettings getSettings() {
        return config;
    }

    @Override
    public int getRangeDistMax() {
        return rangeDistMax;
    }

    @Override
    public int getRangeDistMin() {
        return rangeDistMin;
    }

    @Override
    public int getRangeSteps() {
        return rangeDistSteps;
    }
}