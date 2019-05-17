package com.sdgapps.terrainsandbox.MiniEngine.terrain;

import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.RenderPackage;
import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Camera;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.LineCube;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Transform;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture2D;

public class CDLODQuadTree {

    static float yscale;

    /**
     * Number of LOD levels
     */
    short nLods;

    /**
     * gridSize: Determines the number of Quads for every node (gridSize * gridSize)
     * <p>
     * Example: nLods = 5, gridSize = 32 (usual values are 8, 16, 32, 64, 128...)
     * <p>
     * LOD 0 -> 32x32  Maximum detail, 4^(nLods-1) of these nodes cover the whole area of the heightmap
     * LOD 1 -> 32x32 ->covers the same area that lod 0 would need 64x64 quads to cover
     * LOD 2 -> 32x32 ->covers the same area that lod 0 would need 128x128 quads to cover
     * LOD 3 -> 32x32 ->covers the same area that lod 0 would need 256x256 quads to cover
     * LOD 4 -> 32x32 ->covers the same area that lod 0 would need 512x512 quads to cover
     * (This is the whole terrain at lowest detail)
     * <p>
     * Detail ranges, resolution and number of lods are the more important parameters to
     * tweak and balance performance and quality.
     */
    private int gridSize;

    /**
     * root -> the lod node that covers the whole terrain at lowest detail. Scale is the distance
     * between two adjacent vertices in this mesh. When this is smaller, it makes the mesh denser.
     */
    private float rootQuadScale;

    /**
     * Size of the side of the terrain
     */
    public float terrainXZ;

    /**
     * Center point of the terrain
     */
    public Vec3f middlePoint;

    /**
     * Root node of the quad tree
     */
    private CDLODNode root;
    //private float[] shadowMapMVPMatrix = new float[16];
    private boolean initialized;
    private SelectionResults selection = new SelectionResults();

    private Material boundingBoxMaterial;

    private float[] rangeDistance;
    private float[] morphconstz;
    private float[] ranges;

    public Material material;
    public Transform transform;

    CDLODQuadTree(Material mat, int _gridSize, float _rootQuadScale, short _nLods, float _yscale,
                  float[] _ranges, float[] _morphconstz, float[] _rangeDistance, Material _boundingBoxMaterial) {
        super();
        gridSize = _gridSize;
        rootQuadScale = _rootQuadScale;
        terrainXZ = rootQuadScale * gridSize;
        middlePoint = new Vec3f(terrainXZ / 2f, 0, terrainXZ / 2);
        nLods = _nLods;
        yscale = _yscale;
        ranges = _ranges;
        morphconstz = _morphconstz;
        rangeDistance = _rangeDistance;

        transform = new Transform();
        transform.objectPivotPosition.set(terrainXZ / 2f, 0, terrainXZ / 2f);

        material = mat;
        root = new CDLODNode((short)(nLods - 1), rootQuadScale, 0, 0, gridSize, gridSize * rootQuadScale, (Texture2D)material.getTexture(Planet.heightmapUniformName));
        boundingBoxMaterial = _boundingBoxMaterial;
        initialized = true;
    }

    /**
     * CDLOD node selection
     * @param mainCamera
     */
    int LodSelect(Camera mainCamera) {
        selection.clear();


        if (initialized) {
            root.LODSelect(mainCamera, selection, false,ranges,morphconstz,terrainXZ * 0.5f);
        }

        transform.updateModelMatrix();
        return selection.getLowestLodReached();
    }

    void draw(RenderPackage pass, GridMesh gridMesh, Transform planetTransform) {
        if (selection.getSelectionList().size() > 0) {
            if (initialized) {

                GLSLProgram targetShader = pass.targetProgram;

                Matrix.multiplyMM(MatrixManager.modelMatrix, 0, planetTransform.modelMatrix, 0, transform.modelMatrix, 0);

               // pass.setupForRendering(MatrixManager.modelMatrix, shadowMapMVPMatrix, material, targetShader);
                material.bindTextures();
                sendMatrices();
                //selection.renderSelectionInstanced(gridMesh,targetShader);
                selection.renderSelection(gridMesh,targetShader,rangeDistance,morphconstz);
                Matrix.setIdentityM(MatrixManager.modelMatrix, 0);
            }
        }
    }

    private void sendMatrices() {

        Matrix.multiplyMM(MatrixManager.modelViewMatrix, 0, MatrixManager.viewMatrix, 0,
                MatrixManager.modelMatrix, 0);

        Matrix.multiplyMM(MatrixManager.MVPMatrix, 0, MatrixManager.projectionMatrix, 0,
                MatrixManager.modelViewMatrix, 0);

        ShaderUniformMatrix4fv MVMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_MVMatrix");
        ShaderUniformMatrix4fv MVPMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_MVPMatrix");

        MVMatrix.array=MatrixManager.modelViewMatrix;
        MVPMatrix.array=MatrixManager.MVPMatrix;

        MVMatrix.bind();
        MVPMatrix.bind();
    }

    void drawAABB(LineCube geometry) {
        boundingBoxMaterial.shader.useProgram();
        geometry.bindAttributes(boundingBoxMaterial.shader);

        for (SelectableNode snode : selection.getSelectionList()) {
            ((CDLODNode) snode).renderBox(this.boundingBoxMaterial,geometry);
        }

    }

    void transformBoundingBoxes(Transform planetTransform) {
        root.transformBoundingBoxRecursive(transform, planetTransform);
    }
}
