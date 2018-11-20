package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.Behaviour;
import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Frustum;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;

public class Camera extends Behaviour {

    private Vec3f lookat = new Vec3f();
    public Frustum frustum = new Frustum();
    private Frustum lightFrustum;
    public Light shadowmapCaster;
    private Vec3f lightLookAt=new Vec3f();

    public Camera() {
        super();
        MatrixManager.projectionMatrix = frustum.projectionMatrix;
    }

    public void updateShadowMapCamera() {

        if (shadowmapCaster != null) {
            Vec3f lightpos = shadowmapCaster.gameObject.transform.position;

            Matrix.setLookAtM(MatrixManager.shadowmapViewMatrix, 0,
                    lightpos.x, lightpos.y, lightpos.z,
                    lightLookAt.x, lightLookAt.y, lightLookAt.z,
                    0, 1, 0);
        }
    }

    public void setupShadowMapCamera(float gridRadius) {

        lightFrustum = new Frustum();
        MatrixManager.shadowmapProjectionMatrix = lightFrustum.projectionMatrix;

        float near = 1f;
        float far = gridRadius * 2;
        float bottom = -1.0f;
        float top = 1.0f;
        float ratio = 7500000;

        lightFrustum.setup(1.1f * top,1.1f * bottom,-1.1f * ratio,1.1f * ratio,near,far,-1.1f * ratio);
        Matrix.orthoM(MatrixManager.shadowmapProjectionMatrix, 0, -1.1f * ratio, 1.1f * ratio, 1.1f * bottom * ratio, 1.1f * top * ratio, near, far);
    }

    public void setAspectRatio(float aspectRatio) {
        if (aspectRatio != frustum.getAspectRatio()) {
            frustum.setAspectRatio(aspectRatio);
            frustum.computeProjectionMatrix();
        }
    }

    private void executeLookAt() {

        lookat.set(transform.rotation.getZAxis());
        lookat.scalarMul(10000);
        lookat.add(transform.position);

        Vec3f up = transform.rotation.getYAxis();
        Matrix.setLookAtM(MatrixManager.viewMatrix, 0, transform.position.x, transform.position.y, transform.position.z,
                lookat.x, lookat.y, lookat.z, up.x, up.y, up.z);
    }

    @Override
    public void update() {
        frustum.calcClippingPlanes(transform.position, transform.rotation.getZAxis(),
                transform.rotation.getYAxis(),
                transform.rotation.getXAxis());
        executeLookAt();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
