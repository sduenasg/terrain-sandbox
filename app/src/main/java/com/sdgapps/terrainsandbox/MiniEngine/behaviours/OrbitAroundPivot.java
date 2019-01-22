package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import com.sdgapps.terrainsandbox.MiniEngine.Behaviour;
import com.sdgapps.terrainsandbox.MiniEngine.TimeSystem;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.MiniMath;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Quaternion;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;
import com.sdgapps.terrainsandbox.SimpleVec3fPool;

public class OrbitAroundPivot extends Behaviour {

    private float horAngle = 0;
    private float verAngle = 0;
    private Vec3f initialPosition = new Vec3f();
    private Vec3f pivot = new Vec3f();

    private boolean orbit = false;
    private float pivotRadius;

    public void setOrbitEnabled(boolean enabled) {
        this.orbit = enabled;
    }

    public void setPivot(Vec3f _pivot, float radius) {
        pivot.set(_pivot);
        pivotRadius = radius;
        transform.position.set(pivot);
        transform.position.z -= radius;
        transform.position.x -= radius;
        initialPosition.set(transform.position);
        transform.rotation.fromAxes(Vec3f.Xvector, Vec3f.Yvector, Vec3f.Zvector);
        rotateAroundPivotXYangle(0);
        rotateAroundPivotXZangle(0);
    }

    public void rotateAroundPivotXZincrement(float increment) {
        rotateAroundPivotXZangle(MiniMath.reduceAngleDeg(increment + horAngle));
    }

    public void rotateAroundPivotXYincrement(float increment) {
        rotateAroundPivotXYangle(MiniMath.reduceAngleDeg(increment + verAngle));
    }

    private void rotateAroundPivotXZangle(float angle) {
        float rotation = horAngle - angle;
        Quaternion rot = new Quaternion();
        rot = rot.fromAngleNormalAxis((float) Math.toRadians(rotation), Vec3f.Yvector);
        rot.mult(transform.rotation, transform.rotation);
        horAngle -= rotation;

        Vec3f aux = SimpleVec3fPool.create(initialPosition);
        aux.sub(pivot);
        aux = transform.rotation.multLocal(aux);
        aux.add(pivot);
        gameObject.transform.position.set(aux);
    }

    private void rotateAroundPivotXYangle(float angle) {
        float rotation = verAngle - angle;
        Quaternion rot = new Quaternion();
        rot = rot.fromAngleNormalAxis((float) Math.toRadians(rotation), transform.rotation.getXAxis());
        rot.mult(transform.rotation, transform.rotation);
        verAngle -= rotation;

        Vec3f aux = SimpleVec3fPool.create(initialPosition);
        aux.sub(pivot);
        aux = transform.rotation.multLocal(aux);
        aux.add(pivot);
        gameObject.transform.position.set(aux);
        gameObject.transform.position.set(aux);
    }

    public void rotateElevation(float angle) {
        rotateAroundPivotXYangle(angle);
    }

    public void rotateAzimuth(float angle) {
        rotateAroundPivotXZangle(angle);
    }


    @Override
    public void update() {
        if (orbit) {
            int timeDelta = (int) gameObject.engineManagers.sTime.deltaTime;
            float timefactor = timeDelta * TimeSystem.defaultframetimeInverted;
            rotateAroundPivotXZincrement(0.2f * timefactor);
        }
    }
}
