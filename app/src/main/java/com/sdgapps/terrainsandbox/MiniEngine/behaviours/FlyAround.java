package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import com.sdgapps.terrainsandbox.MiniEngine.Behaviour;
import com.sdgapps.terrainsandbox.MiniEngine.TimeSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.MiniMath;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Quaternion;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;
import com.sdgapps.terrainsandbox.SimpleVec3fPool;
import com.sdgapps.terrainsandbox.Singleton;

public class FlyAround extends Behaviour {

    private float verticalTranslation = 0;
    private float horizontalTranslation = 0;

    private float xRotateValue = 0;
    private float yRotateValue = 0;
    private float zRotateValue = 0;
    Vec3f initialPosition = new Vec3f();
    Quaternion initialRotation=new Quaternion();

    public final float maxSpeed = 20000;
    public float allowedSpeed = maxSpeed;
    private float walkSpeed = 0;
    private float verticalSpeed = 0;
    private float horizontalSpeed = 0;

    float walk;
    public void onAddedToEntity()
    {
        initialPosition.set(transform.position);
        initialRotation.set(transform.rotation);
    }

    public void reset() {
        transform.position.set(initialPosition);
        transform.rotation.set(initialRotation);
        xRotateValue = 0;
        yRotateValue = 0;
    }

    private void rotateX(float angle) {
        Quaternion rot = new Quaternion();
        rot = rot.fromAngleNormalAxis(angle, transform.rotation.getXAxis());
        rot.mult(transform.rotation, transform.rotation);
    }

    private void rotateZ(float angle) {
        Quaternion rot = new Quaternion();
        rot = rot.fromAngleNormalAxis(angle, transform.rotation.getZAxis());
        rot.mult(transform.rotation, transform.rotation);
    }

    private void rotateY(float angle) {
        Quaternion rot = new Quaternion();
        rot = rot.fromAngleNormalAxis(angle, transform.rotation.getYAxis());
        rot.mult(transform.rotation, transform.rotation);
    }


    private void executeUIrotation(float timefactor) {
        if (xRotateValue != 0) {
            rotateY(xRotateValue * 0.003f * timefactor);
            //xRotateValue = 0;
        }

        if (yRotateValue != 0) {
            rotateX(yRotateValue * 0.003f * timefactor);
            //yRotateValue = 0;
        }

        if (zRotateValue != 0) {
            rotateZ(zRotateValue * 0.003f * timefactor);
            //yRotateValue = 0;
        }
    }

    public void updateDirection(float x, float y, float z) {
        xRotateValue = x;
        yRotateValue = -y;
        zRotateValue = z;
    }



    private void setCamera(float timefactor) {

        executeUIrotation(timefactor);
        Vec3f zAx = transform.rotation.getZAxis();
        zAx.normalize();

        if (walk != 0) {
            walkSpeed = MiniMath.lerp(walkSpeed, Math.signum(walk) * allowedSpeed, 0.05f * timefactor);

        } else if (walkSpeed != 0) {
            walkSpeed = MiniMath.lerp(walkSpeed, 0, 0.05f * timefactor);
            if (walkSpeed < 0.01f)
                walkSpeed = 0;
        }

        if (verticalTranslation != 0) {
            verticalSpeed = MiniMath.lerp(verticalSpeed, Math.signum(verticalTranslation) * allowedSpeed, 0.05f * timefactor);
        } else if (verticalSpeed != 0) {
            verticalSpeed = MiniMath.lerp(verticalSpeed, 0, 0.05f * timefactor);
            if (verticalSpeed < 0.06f && verticalSpeed > -0.06f)
                verticalSpeed = 0;
        }

        if (horizontalTranslation != 0) {
            horizontalSpeed = MiniMath.lerp(horizontalSpeed, Math.signum(horizontalTranslation) * allowedSpeed, 0.05f * timefactor);
        } else if (horizontalSpeed != 0) {
            horizontalSpeed = MiniMath.lerp(horizontalSpeed, 0, 0.05f * timefactor);
            if (horizontalSpeed < 0.06f && horizontalSpeed > -0.06f)
                horizontalSpeed = 0;
        }

        transform.position.x += walkSpeed * zAx.x * timefactor;
        transform.position.y += walkSpeed * zAx.y * timefactor;
        transform.position.z += walkSpeed * zAx.z * timefactor;

        Vec3f up = SimpleVec3fPool.create(transform.rotation.getYAxis());
        up.scalarMul(verticalSpeed * timefactor);
        transform.position.add(up);

        Vec3f right = SimpleVec3fPool.create(transform.rotation.getXAxis());
        right.scalarMul(horizontalSpeed * timefactor);
        transform.position.add(right);
    }


    public void setVerticalTranslation(float verticalTranslation) {
        this.verticalTranslation = verticalTranslation;
    }

    public void setHorizontalTranslation(float horizontalTranslation) {
        this.horizontalTranslation = horizontalTranslation;
    }


    public void setWalk(float walk) {
        this.walk = walk;
    }

    @Override
    public void update() {
        int timeDelta = (int) Singleton.systems.sTime.deltaTime;
        float timefactor = timeDelta * TimeSystem.defaultframetimeInverted;
        setCamera(timefactor);
    }

}
