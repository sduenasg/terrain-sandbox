package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.SimpleVec3fPool;

public class Transform {

    public Vec3f position = new Vec3f();
    public Quaternion rotation = new Quaternion();
    public Vec3f scale = new Vec3f();
    public float[] modelMatrix = new float[16];
    public Vec3f objectPivotPosition = new Vec3f();

    public Transform() {
        Matrix.setIdentityM(modelMatrix, 0);
        rotation.loadIdentity();
        scale.set(1, 1, 1);
    }

    public void translate(float x, float y, float z) {
        position.add(x, y, z);
    }

    public void updateModelMatrix() {
        Matrix.setIdentityM(modelMatrix, 0);

        //undo pivot translation
        Matrix.translateM(modelMatrix, 0, objectPivotPosition.x, objectPivotPosition.y, objectPivotPosition.z);

        //final translation
        Matrix.translateM(modelMatrix, 0, position.x, position.y, position.z);

        //rotation
        Vec3f axis = SimpleVec3fPool.create();
        float angle = rotation.toAngleAxis(axis);

        Matrix.rotateM(modelMatrix, 0, angle * (float) 180.0 / (float) Math.PI,
                axis.x, axis.y, axis.z);
        //scale
        Matrix.scaleM(modelMatrix, 0, scale.x, scale.x, scale.z);

        //center object's pivot on origin to perform rotation/scale
        Matrix.translateM(modelMatrix, 0, -objectPivotPosition.x, -objectPivotPosition.y, -objectPivotPosition.z);
    }

}
