package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import com.sdgapps.terrainsandbox.SimpleVec3fPool;

/**
 * Class that represents a 3d plane using point and a normal vector.
 */
public class Plane {

    public Vec3f normal;
    public Vec3f point;
    float d = 1;

    public Plane() {
    }

    public Plane(Vec3f _normal, Vec3f _P) {
        normal = _normal;
        point = _P;
        d = -(normal.calcDot(point));
    }

    public void set(Vec3f _normal, Vec3f _P) {
        normal = _normal;
        point = _P;
        d = -(normal.calcDot(point));
    }

    public int testBoundingBoxAgainstPlane(Vec3f bbmax, Vec3f bbmin) {
        /*Find points neg and pos, box's minimum and maximum points relative to the plane normal
          this saves us from testing all 8 points from the box against the plane, only having to test 2*/

        //max point relative to the plane
        Vec3f positive = SimpleVec3fPool.create(bbmin);
        if (normal.x >= 0)
            positive.x = bbmax.x;

        if (normal.y >= 0)
            positive.y = bbmax.y;

        if (normal.z >= 0)
            positive.z = bbmax.z;

        //min point relative to the plane
        Vec3f negative = SimpleVec3fPool.create(bbmax);
        if (normal.x >= 0)
            negative.x = bbmin.x;

        if (normal.y >= 0)
            negative.y = bbmin.y;

        if (normal.z >= 0)
            negative.z = bbmin.z;

        if (distance(positive) < 0)
            return Frustum.OUTSIDE; //OUTSIDE
        else if (distance(negative) < 0)
            return Frustum.INTERSECT;//INTERSECT

        return Frustum.OUTSIDE;
    }

    public float testPointAgainstPlane(Vec3f testPoint) {
        Vec3f p = SimpleVec3fPool.create(testPoint);
        p.sub(point);
        return p.calcDot(normal);
    }


    float distance(Vec3f p) {
        return (d + normal.calcDot(p));
    }
}
