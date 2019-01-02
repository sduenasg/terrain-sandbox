package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.Matrix;
import com.sdgapps.terrainsandbox.SimpleVec3fPool;

/**
 * Class that represents a view Frustum. Provides frustum culling functionality.
 */
public class Frustum {
    private float top;
    private float bottom;
    private float left;
    private float right;
    private float aspectRatio;

    public float[] projectionMatrix = new float[16];

    public float znear = DEFAULT_ZNEAR;
    public float zfar = DEFAULT_ZFAR;

    //half of the horizontal field of view
    private float horizontalFov_H = DEFAULT_H_FOV;

    private static final float DEFAULT_H_FOV = 42f;
    public static final float DEFAULT_ZNEAR = 1f;
    public static final float DEFAULT_ZFAR = 13000000f;
    public static final int INTERSECT = 1;
    public static final int OUTSIDE = -1;
    public static final int INSIDE = 2;

    //near plane
    private Vec3f nearCenter = new Vec3f();
    private Vec3f nearNormal = new Vec3f();

    //far plane
    private Vec3f farNormal = new Vec3f();
    private Vec3f farCenter = new Vec3f();

    //left plane
    private Vec3f leftNormal = new Vec3f();

    //right plane
    private Vec3f rightNormal = new Vec3f();

    //top plane
    private Vec3f topNormal = new Vec3f();

    //bottom plane
    private Vec3f botNormal = new Vec3f();
    private Plane pbottom = new Plane();
    private Plane ptop = new Plane();
    private Plane pnear = new Plane();
    private Plane pfar = new Plane();
    private Plane pleft = new Plane();
    private Plane pright = new Plane();
    private Plane[] frustumPlanes = new Plane[6];

    public Frustum() {
        frustumPlanes[0] = pnear;
        frustumPlanes[1] = pfar;
        frustumPlanes[2] = pleft;
        frustumPlanes[3] = pright;
        frustumPlanes[4] = ptop;
        frustumPlanes[5] = pbottom;
    }

    public void calcClippingPlanes(Vec3f cameraPosition, Vec3f viewVec, Vec3f up, Vec3f right) {

        /*
        Theory: http://www.lighthouse3d.com/tutorials/view-frustum-culling/geometric-approach-extracting-the-planes/
        */

        float Hnear = (float) Math.tan(Math.toRadians(horizontalFov_H*aspectRatio*.5f)) * znear;
        float Wnear = Hnear * aspectRatio;

        viewVec.normalize();
        up.normalize();
        right.normalize();

        //far plane
        farCenter.set(viewVec);
        farCenter.scalarMul(zfar);
        farCenter.add(cameraPosition); //far plane center point
        farNormal.set(viewVec);
        farNormal.invert();

        //near plane
        nearCenter.set(viewVec);
        nearCenter.scalarMul(znear);
        nearCenter.add(cameraPosition);
        nearNormal.set(viewVec);

        //right plane
        Vec3f a = SimpleVec3fPool.create(right);
        a.scalarMul(Wnear);
        a.add(nearCenter);
        a.sub(cameraPosition);
        a.normalize();
        rightNormal.set(a.calcCross(up));
        rightNormal.normalize();

        //left plane
        a.set(right);
        a.invert();//left
        a.scalarMul(Wnear);
        a.add(nearCenter);
        a.sub(cameraPosition);
        a.normalize();
        leftNormal.set(up.calcCross(a));
        leftNormal.normalize();

        //top plane
        a.set(up);
        a.scalarMul(Hnear);
        a.add(nearCenter);
        a.sub(cameraPosition);
        a.normalize();
        topNormal.set(right.calcCross(a));
        topNormal.normalize();

        //bottom plane
        a.set(up);
        a.invert();
        a.scalarMul(Hnear);
        a.add(nearCenter);
        a.sub(cameraPosition);
        a.normalize();
        botNormal.set(a.calcCross(right));
        botNormal.normalize();

        pbottom.set(botNormal,  cameraPosition);
        ptop.set(topNormal,     cameraPosition);
        pnear.set(nearNormal,   nearCenter);
        pfar.set(farNormal,     farCenter);
        pleft.set(leftNormal,   cameraPosition);
        pright.set(rightNormal, cameraPosition);
    }

    public boolean testPoint(Vec3f inpoint) {
        boolean res = false;
        for (int i = 0; i < frustumPlanes.length; i++) {

            res = (frustumPlanes[i].testPointAgainstPlane(inpoint) >= 0);
            if (!res) return res;
        }

        return true;
    }

    public int testSphere(Vec3f center, float r) {
        int result = INSIDE;
        float distance;

        for(Plane p: frustumPlanes) {
            distance = p.distance(center);
            if (distance < -r)
                return OUTSIDE;
            else if (distance < r)
                result =  INTERSECT;
        }
        return(result);
    }

    public int testBoundingBox(BoundingBox inbb) {
        int result = INSIDE;

        for (Plane p:frustumPlanes) {
            if (p.distance(inbb.getP(p.normal)) < 0)
                return OUTSIDE;
            else if (p.distance(inbb.getN(p.normal)) < 0)
                result = INTERSECT;
        }

        return result;
    }

    public void change_fov(float _fov) {

        horizontalFov_H = _fov;
        computeProjectionMatrix();
    }

    public void computeProjectionMatrix() {
        float verticalFovHalf = horizontalFov_H * 1f/aspectRatio;
        top = (float) (Math.tan(Math.toRadians(verticalFovHalf)) * znear);
        bottom = -top;
        left = aspectRatio * bottom;
        right = -left;
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, znear, zfar);
    }

    public void change_zvalues(float znearin, float zfarin) {
        znear = znearin;
        zfar = zfarin;
        change_fov(DEFAULT_H_FOV);//current horizontalFov_H
    }

    public void setDefaultNearFar() {
        znear = DEFAULT_ZNEAR;
        zfar = DEFAULT_ZFAR;
        change_fov(horizontalFov_H);
    }

    public void setDefault() {
        znear = DEFAULT_ZNEAR;
        zfar = DEFAULT_ZFAR;
        change_fov(DEFAULT_H_FOV);
    }

    public void setup(float _top, float _bottom, float _left, float _right, float _near, float _far, float _aspectRatio)
    {
        top=_top;
        bottom=_bottom;
        left=_left;
        right=_right;
        znear=_near;
        zfar=_far;
        aspectRatio=_aspectRatio;
    }

    public float getAspectRatio() {
        return aspectRatio;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
    }
}
