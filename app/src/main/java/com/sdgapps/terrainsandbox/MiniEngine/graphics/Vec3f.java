
package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import com.sdgapps.terrainsandbox.SimpleVec3fPool;

public class Vec3f {

    public float x;
    public float y;
    public float z;

    /**
     * (0,1,0)
     */
    public static final Vec3f Yvector = new Vec3f(0f, 1f, 0f);
    /**
     * (1,0,0)
     */
    public static final Vec3f Xvector = new Vec3f(1f, 0f, 0f);
    /**
     * (0,0,1)
     */
    public static final Vec3f Zvector = new Vec3f(0f, 0f, 1f);
    /**
     * (0,0,0)
     */
    public static final Vec3f ZERO = new Vec3f(0f, 0f, 0f);

    public String toString() {
        return new String("x: " + Float.toString(x) + " y: " + Float.toString(y) + " z: "
                + Float.toString(z));
    }

    public Vec3f() {
        x = 0;
        y = 0;
        z = 0;
    }

    public Vec3f(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public Vec3f(Vec3f or) {

        x = or.x;
        y = or.y;
        z = or.z;
    }

    public Vec3f(int _x, int _y, int _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public void setZero() {
        x = 0;
        y = 0;
        z = 0;
    }

    public boolean equals(Vec3f in) {
        return (in.x == x && in.y == y && in.z == z);
    }


    public Vec3f calcCross(Vec3f v) {
        return SimpleVec3fPool.create(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - v.x * y);
    }

    public void calcCrossInside(Vec3f v) {
        set(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - v.x * y);
    }

    public final Vec3f normalizeNew() {
        final float magnitudesq = length2();
        final float invsqrt = MiniMath.fastInvSqrt(magnitudesq);
        return new Vec3f(x *= invsqrt, y *= invsqrt, z *= invsqrt);
    }


    public Vec3f normalize() {
        float magnitudesq = length2();
        float invsqrt = MiniMath.fastInvSqrt(magnitudesq);
        this.x *= invsqrt;
        this.y *= invsqrt;
        this.z *= invsqrt;

        return this;
    }

    @Deprecated
    public Vec3f normalize_SLOW() {

        float magnitude = length();

        if (magnitude != 0) {
            this.x /= magnitude;
            this.y /= magnitude;
            this.z /= magnitude;
        }

        return this;
    }

    public final Vec3f add(Vec3f other) {
        x += other.x;
        y += other.y;
        z += other.z;

        return this;
    }

    public void add(float tx, float ty, float tz) {
        x += tx;
        y += ty;
        z += tz;
    }

    public Vec3f add_return(Vec3f in) {
        return new Vec3f(x + in.x, y + in.y, z + in.z);
    }

    public void sub(Vec3f other) {
        x -= other.x;
        y -= other.y;
        z -= other.z;
    }

    public void sub(float _x, float _y, float _z) {
        x -= _x;
        y -= _y;
        z -= _z;
    }

    public final float length() {

        final float len = (float) Math.sqrt(length2());
        // float len=(float) 1f/MiniMath.fastInvSqrt(length2());

        return len;
    }

    public final float length2() {
        return (x * x) + (y * y) + (z * z);
    }

    public final float length4() {
        return (x * x * x * x) + (y * y * y * y) + (z * z * z * z);
    }

    public float calcDot(Vec3f other) {
        return (x * other.x) + (y * other.y) + (z * other.z);
    }

    public void scalarMul(float magnitude) {
        x *= magnitude;
        y *= magnitude;
        z *= magnitude;
    }

    /**
     * @param t linear interpolation factor
     */
    public void lerp(Vec3f b, float t) {
        //  return a + t * (b - a);
        x = x + t * (b.x - x);
        y = y + t * (b.y - y);
        z = z + t * (b.z - z);
    }

    public float calcAngle(Vec3f sv) {
        float angle = (float) Math.acos(calcDot(sv));
        return angle;
    }

    public final float distance2(Vec3f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (dx * dx) + (dy * dy) + (dz * dz);
    }

    public final float distance(Vec3f other) {
        float dx = x - other.x;
        float dy = y - other.y;
        float dz = z - other.z;
        return (float) Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }


    public final float distanceXZ(Vec3f other) {
        float dx = x - other.x;

        float dz = z - other.z;
        return (float) Math.sqrt((dx * dx) + (dz * dz));
    }

    public void rotatethisVectorAroundAxis(Vec3f ax, float rotateAngle) {

        float u = ax.x;
        float v = ax.y;
        float w = ax.z;

        /* Estas operaciones vienen de las operaciones con la matriz de rotacion */
        x = (float) ((u * x + v * y + w * z) + (x * (v * v + w * w) - u * (v * y + w * z))
                * Math.cos(rotateAngle) + (-(w * y) + v * z) * Math.sin(rotateAngle));
        y = (float) ((u * x + v * y + w * z) + (y * (u * u + w * w) - v * (u * x + w * z))
                * Math.cos(rotateAngle) + (w * x - u * z) * Math.sin(rotateAngle));
        z = (float) ((u * x + v * y + w * z) + (z * (u * u + v * v) - w * (u * x + v * y))
                * Math.cos(rotateAngle) + (-(v * x) + u * y) * Math.sin(rotateAngle));

        normalize();
    }

    public void set(float _x, float _y, float _z) {
        x = _x;
        y = _y;
        z = _z;
    }

    public void invert() {
        x = -x;
        y = -y;
        z = -z;
    }

    public Vec3f new_copy() {

        return new Vec3f(this.x, this.y, this.z);
    }

    public void set(Vec3f in) {
        this.x = in.x;
        this.y = in.y;
        this.z = in.z;
    }

    public void div(Vec3f den) {
        this.x *= 1f / den.x;
        this.y *= 1f / den.y;
        this.z *= 1f / den.z;
    }

    public void div(float den) {
        this.x *= 1f / den;
        this.y *= 1f / den;
        this.z *= 1f / den;
    }


    public float getValueFromIndex(int param) {

        if (param == 0) return x;
        else if (param == 1) return y;
        else if (param == 2) return z;

        return 0;
    }

    public void reflect(Vec3f normal) {
        Vec3f aux = SimpleVec3fPool.create(normal);
        float dot = this.calcDot(normal);
        aux.scalarMul(2 * dot);
        this.sub(aux);
    }

    public float project(Vec3f project_to) {
        float dot = this.calcDot(project_to);
        float proj = dot / (project_to.length());

        return proj;
    }

    /*Some color utilities*/

    /*As long the float packed with packColor is not in the [0, 1] range but in the [0, 16777215]
     range, you shouldn't have any problem with precision. But if you normalize the float in the
     [0,1] range, you'll have precision problems!*/
    public float packColor() {
        return x + y * 256.0f + z * 256.0f * 256.0f;
    }


    void unpackColor(float f) {
        x = (float) Math.floor(f / 256.0f / 256.0f);
        y = (float) Math.floor((f - z * 256.0f * 256.0f) / 256.0f);
        z = (float) Math.floor(f - z * 256.0f * 256.0f - y * 256.0f);

        this.div(255.0f);
        // now we have a vec3 with the 3 BEHAVIOURS in range [0..255]. Let's normalize it!
        //return color / 255.0;
    }

    public void abs()
    {
        x=Math.abs(x);
        y=Math.abs(y);
        z=Math.abs(z);
    }
}
