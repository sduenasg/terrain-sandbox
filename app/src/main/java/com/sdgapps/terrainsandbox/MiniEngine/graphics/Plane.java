package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import com.sdgapps.terrainsandbox.SimpleVec3fPool;

/**
 * Class that represents a 3d plane using point and a normal vector.
 */
public class Plane {

    public Vec3f normal=new Vec3f();
    public Vec3f point=new Vec3f();
    private float d = 1;

    public Plane(Vec3f _normal, Vec3f _P) {
        normal = _normal;
        point = _P;
        d = -(normal.calcDot(point));
    }

    public Plane() {

    }

    public void set(Vec3f _normal, Vec3f _P) {

        normal.set(_normal);
        point.set(_P);
        d = -(normal.calcDot(point));
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
