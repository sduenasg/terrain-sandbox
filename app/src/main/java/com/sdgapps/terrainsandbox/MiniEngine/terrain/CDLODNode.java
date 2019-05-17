package com.sdgapps.terrainsandbox.MiniEngine.terrain;

import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Camera;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.BoundingBox;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Frustum;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.LineCube;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Transform;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec2f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform1f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniform2f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture2D;
import com.sdgapps.terrainsandbox.SimpleVec3fPool;

class CDLODNode extends SelectableNode {

    private CDLODNode[] children;
    short lod;
    private BoundingBox AABB;
    float xoffset;
    float zoffset;
    float quadScale;

    CDLODNode(boolean sphere, short _lod, float _quadScale, float _xoffset, float _yoffset, int gridsize,
              float fullWidth, Texture2D heightmap, CDLODQuadTree cdlodQuadTree) {
        quadScale = _quadScale;
        lod = _lod;
        xoffset = _xoffset;
        zoffset = _yoffset;
        float nodeSide = (float) gridsize * _quadScale;

        if (lod == 0) {

            /*
             * Only leaf nodes build the bounding box using the heightmap.
             * Other nodes can use their children's bounding boxes to create their own (faster)
             */
            if (!sphere)
                buildAABBfromData(heightmap, fullWidth, nodeSide);
            else
                buildAABBfromDataSphere(heightmap, fullWidth, nodeSide);
            //end recursion
        } else {
            //spawn child nodes
            children = new CDLODNode[4];
            Vec2f[] childrenOffsets = new Vec2f[4];

            childrenOffsets[0] = new Vec2f(xoffset, zoffset);
            childrenOffsets[1] = new Vec2f(xoffset + nodeSide / 2f, zoffset);
            childrenOffsets[2] = new Vec2f(xoffset, zoffset + nodeSide / 2f);
            childrenOffsets[3] = new Vec2f(xoffset + nodeSide / 2f, zoffset + nodeSide / 2f);

            for (int i = 0; i < 4; i++) {
                children[i] = new CDLODNode(sphere,
                        (short)(lod - 1),
                        _quadScale / 2f,
                        childrenOffsets[i].x,
                        childrenOffsets[i].y,
                        gridsize,
                        fullWidth,
                        heightmap,
                        cdlodQuadTree);
            }

            buildAABBfromChildren();
        }
    }


    private void SpherizePoint(Vec3f p, float sphereRadius) {
        p.x -= sphereRadius;
        p.z -= sphereRadius;
        p.y += sphereRadius;

        p = p.normalize();
        p.scalarMul(sphereRadius);

        p.y -= sphereRadius;
        p.x += sphereRadius;
        p.z += sphereRadius;
    }


    /*
     * Get 4 corners and center points, look for max and min xyz
     *
     * Faster than spherizing and displacing the points, less precise
     */
    private void calcBoxPoints(float sphereRadius,float nodeSide, Vec3f outMax,Vec3f outMin)
    {
        Vec3f[] points=new Vec3f[5];
        points[0]= SimpleVec3fPool.create(xoffset,0,zoffset); //bottom left point
        points[1]= SimpleVec3fPool.create(xoffset+nodeSide,0,zoffset);//bottom right point
        points[2]= SimpleVec3fPool.create(xoffset,0,zoffset+nodeSide);//top left point
        points[3]= SimpleVec3fPool.create(xoffset+nodeSide,0,zoffset+nodeSide);//top right point
        points[4]= SimpleVec3fPool.create(xoffset+nodeSide/2,0,zoffset+nodeSide/2);//center point

        outMax.set(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        outMin.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

        for(Vec3f point:points)
        {
            SpherizePoint(point,sphereRadius);

            if (point.y > outMax.y) {
                outMax.y = point.y;
            }
            if (point.y < outMin.y) {
                outMin.y = point.y;
            }

            if (point.x > outMax.x) {
                outMax.x = point.x;
            }
            if (point.x < outMin.x) {
                outMin.x = point.x;
            }

            if (point.z > outMax.z) {
                outMax.z = point.z;
            }
            if (point.z < outMin.z) {
                outMin.z = point.z;
            }
        }
    }

    /**
     * Bounding box initialization for planets
     */
    private void buildAABBfromDataSphere(Texture2D heightmap, float terrainWidth, float nodeSide) {
        //TimingHelper th=new TimingHelper("bb building");
        // th.start();
        AABB = new BoundingBox();

        Vec3f heightMapMax = SimpleVec3fPool.create();
        Vec3f heightMapMin = SimpleVec3fPool.create();

        //Fetch Y values for the area from the heightmap


        //spherize + displace all the vertices, very slow
        /*minMaxValAreaSphere(heightmap,
                xoffset / terrainWidth,
                zoffset / terrainWidth,
                nodeSide / terrainWidth,
                nodeSide / terrainWidth,
                heightMapMin,
                heightMapMax,
                terrainWidth * 0.5f,
                terrainWidth,
                CDLODQuadTree.yscale);*/

        //spherize only the corners and the center points, no displacement
        /*
        * At planetary scale, heightmap displacement seems negligible compared to the bounding
        * box that is created by the curvature of the mesh. This needs some more investigating
        * though, for when the node is so detailed (tiny part of the sphere) that the mesh is
        * almost flat.
        */
        calcBoxPoints(terrainWidth*0.5f,nodeSide,heightMapMax,heightMapMin);

        AABB.expand(heightMapMin);
        AABB.expand(heightMapMax);

        //th.end();
    }

    //TODO: Optimize, very slow as it goes through all the pixels on the heightmap
    private void minMaxValAreaSphere(Texture2D texture, float x, float z, float w, float h, Vec3f outMin, Vec3f outMax,
                                     float sphereRadius, float terrainWidth, float yscale) {

        outMax.set(-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE);
        outMin.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE);

        int width = texture.getWidth();
        int height = texture.getHeight();

        x *= (float) width;
        z *= (float) height;
        w *= (float) width;
        h *= (float) height;

        Vec3f radiusVector = new Vec3f();
        Vec3f newVec = new Vec3f();
        for (float i = x; i < x + w; i++)
            for (float j = z; j < z + h; j++) {
                newVec.set(terrainWidth * i / (float) width,
                        0,
                        terrainWidth * j / (float) height);
                //Spherize the flat base point
                SpherizePoint(newVec, sphereRadius);

                //NOTE: Disabled displacement mapping here for testing
                //Calculate displacement vector
               /* radiusVector.set(newVec);
                radiusVector.x -= sphereRadius;
                radiusVector.z -= sphereRadius;
                radiusVector.y += sphereRadius;
                radiusVector.normalize();
                radiusVector.scalarMul(texture.getVal((int)i,(int) j) * yscale);

                //apply heightmap displacement
                newVec.add(radiusVector);//for short: newvec = newvec + normal*getVal(i,j) * yscale
                */

                //compare values
                if (newVec.y > outMax.y) {
                    outMax.y = newVec.y;
                }
                if (newVec.y < outMin.y) {
                    outMin.y = newVec.y;
                }

                if (newVec.x > outMax.x) {
                    outMax.x = newVec.x;
                }
                if (newVec.x < outMin.x) {
                    outMin.x = newVec.x;
                }

                if (newVec.z > outMax.z) {
                    outMax.z = newVec.z;
                }
                if (newVec.z < outMin.z) {
                    outMin.z = newVec.z;
                }
            }
    }

    private void buildAABBfromChildren() {
        AABB = new BoundingBox();

        for (CDLODNode child : children) {
            AABB.expand(child.AABB.bMin);
            AABB.expand(child.AABB.bMax);
        }
    }

    private void buildAABBfromData(Texture2D heightmap, float terrainWidth, float nodeSide) {

        AABB = new BoundingBox();
        //Get the first and the last vertex positions to calculate the bounding box
        /*
         *                   *-----------*  <- last vert
         *                   |           |
         *                   |           |
         *                   |           |
         *                   |           |
         *     first vert -> *-----------*
         */
        AABB.expand(xoffset, 0, zoffset);
        AABB.expand(xoffset + nodeSide, 0, zoffset + nodeSide);

        //Fetch Y values for the area from the heightmap
        Vec2f maxmin = heightmap.minMaxValArea(
                xoffset / terrainWidth,
                zoffset / terrainWidth,
                nodeSide / terrainWidth,
                nodeSide / terrainWidth);
        maxmin.x *= CDLODQuadTree.yscale;
        maxmin.y *= CDLODQuadTree.yscale;

        AABB.bMax.y = maxmin.x;
        AABB.bMin.y = maxmin.y;
    }


    /**
     * @return true-> area handled by current node
     * false-> area not handled by current node (parent node should handle it)
     **/
    boolean LODSelect(Camera camera, SelectionResults selection, boolean parentCompletelyInFrustum,float[] ranges, float[] morphConsts, float terrainLenH) {
        ClearSelectionValues();
        Vec3f cameraPos = camera.gameObject.transform.position;

        if (!inSphereQRI(ranges[lod], cameraPos)) {
            // no node or child nodes were selected (out of range); return false so that our parent node handles our area
            return false;
        }

        if (horizonTestBoundingBox(camera,terrainLenH)) {
            return true;
        }

        Frustum f = camera.frustum;

        //if the parent is fully inside the frustum, this child is too
        int frustumTest = parentCompletelyInFrustum ? Frustum.INSIDE: f.testBoundingBox(AABB);
        if (frustumTest == Frustum.OUTSIDE) {

            //this node is out of frustum, select nothing and return true ,so that our
            //parent node does not select itself over our area
            return true;
        }

        if (lod == 0) {
            //AddWholeNodeToSelectionList( ) ;
            Select(4); //4 == whole node, 0-3 == cover-child-area nodes
            selection.add(this, lod);

            // we are in our LOD range at the last LOD level (leaf node)
            return true; // we have handled the area of our node
        } else {
            if (!inSphereQRI(ranges[lod - 1], cameraPos)) {
                // we cover the required lodLevel range
                //AddWholeNodeToSelectionList( ) ;

                Select(4);
                selection.add(this, lod);
            } else {
                /* We cover the more detailed lodLevel range: some or all of our four child nodes will
                have to be selected instead*/
                for (int i = 0; i < children.length; i++) {

                    if (!children[i].LODSelect(camera, selection,frustumTest==Frustum.INSIDE,ranges,morphConsts,terrainLenH)) {
                        // if a child node is outside of its LOD range, this node (parent) must handle it
                        // AddPartOfNodeToSelectionList( childNode.ParentSubArea ) ;

                        Select(i); //4 == whole node, 0-3 == loose nodes
                        selection.add(this, lod);
                    }
                }
            }

            return true;
        }
    }

    /**
     * Test if the bounding box is occluded by the planet itself
     */
    private boolean horizonTestBoundingBox(Camera camera, float terrainLenH) {

        /*
         * Same idea as frustum culling, we only test 2 of the 4 points of the bounding box
         * by choosing the corners that are closest/furthest along the direction of the normal
         * of the plane we want to test against
         *
         * More info here:
         *  https://cesium.com/blog/2013/04/25/horizon-culling/
         *  http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-testing-boxes-ii/
         *
         */
        float radius = terrainLenH;

        Vec3f viewPosition = camera.transform.position;

        Vec3f CV = SimpleVec3fPool.create(viewPosition);
        CV.sub(radius, 0, radius);//center of the sphere

        Vec3f normal = SimpleVec3fPool.create(CV);
        normal.normalize();

        Vec3f targetPosition = AABB.getN(normal);
        float vhMagnitudeSquared=CV.length2()-radius*radius;
        //plane test
        boolean behindPlane = testPointHorizon(CV, viewPosition, targetPosition, vhMagnitudeSquared);

        if (!behindPlane) return false;

        targetPosition = AABB.getP(normal);
        behindPlane = testPointHorizon(CV, viewPosition, targetPosition,vhMagnitudeSquared);

        return behindPlane;
    }

    private boolean testPointHorizon(Vec3f CV, Vec3f viewPosition, Vec3f targetPosition, float vhMagnitudeSquared) {
        Vec3f VT = SimpleVec3fPool.create(targetPosition);
        VT.sub(viewPosition);

        //float vtMagnitudeSquared=VT.length2();

        float dot = -VT.calcDot(CV);
        return dot > vhMagnitudeSquared;
            // && dot*dot/vtMagnitudeSquared>vhMagnitudeSquared; //cone test
    }

    void renderSelectedParts(GridMesh mesh, GLSLProgram shader,float[] rangeDistances, float[] morphconsts) {
        ShaderUniform2f range = (ShaderUniform2f) shader.getUniform("range");
        ShaderUniform1f qScale = (ShaderUniform1f) shader.getUniform("quad_scale");
        ShaderUniform1f gridDim = (ShaderUniform1f) shader.getUniform("gridDim");
        ShaderUniform1f lodLevel = (ShaderUniform1f) shader.getUniform("lodlevel");
        ShaderUniform2f offset = (ShaderUniform2f) shader.getUniform("nodeoffset");

        if (range != null) {
            range.v0 = morphconsts[lod];
            range.v1 = rangeDistances[lod];
            range.bind();
        }

        if (qScale != null) {
            qScale.v = quadScale;
            qScale.bind();
        }
        if (gridDim != null) {
            gridDim.v = 1;
            gridDim.bind();
        }

        if (lodLevel != null) {
            lodLevel.v = lod;
            lodLevel.bind();
        }

        if (offset != null) {
            offset.v0 = xoffset;
            offset.v1 = zoffset;
            offset.bind();
        }

        mesh.drawFromSelection(this.selection);
    }


    private boolean inSphereQRI(float radius, Vec3f center) {
        Vec3f min = AABB.bMin;
        Vec3f max = AABB.bMax;
        float d = 0;

        //X
        float e = Math.max(min.x - center.x, 0) + Math.max(center.x - max.x, 0);
        if (e >= radius) return false;
        d += e * e;

        //Y
        e = Math.max(min.y - center.y, 0) + Math.max(center.y - max.y, 0);
        if (e >= radius) return false;
        d += e * e;

        //Z
        e = Math.max(min.z - center.z, 0) + Math.max(center.z - max.z, 0);
        if (e >= radius) return false;
        d += e * e;

        return d <= radius * radius;
    }

    void renderBox(Material boundingBoxMaterial,LineCube geometry) {

        AABB.draw(boundingBoxMaterial.shader,geometry);
        /*if (selection[4])
            AABB.drawFromSelection(boundingBoxMaterial.shader);
        else//drawFromSelection the boxes of the children we are covering for
            for (int i = 0; i < 4; i++)
                if (selection[i])
                    children[i].AABB.drawFromSelection(boundingBoxMaterial.shader);*/

    }

    void transformBoundingBoxRecursive(Transform t, Transform planetTransform) {

        if (lod != 0)
            for (CDLODNode child : children) {
                child.transformBoundingBoxRecursive(t, planetTransform);
            }

        AABB.updateBoxValues(t, planetTransform);
    }
}
