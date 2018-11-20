package com.sdgapps.terrainsandbox.MiniEngine.graphics;

public class Vec2f {
    public float x, y;
    public static final Vec2f yvector = new Vec2f(0f, 1f);

    public Vec2f() {
        x = 0;
        y = 0;
    }

    public Vec2f(Vec2f in) {
        x = in.x;
        y = in.y;
    }

    public Vec2f(float _x, float _y) {
        x = _x;
        y = _y;
    }

    public void set(Vec2f in) {
        x = in.x;
        y = in.y;
    }

    public float len() {
        return (float) Math.sqrt(x * x + y * y);
    }

    public void normalize() {
        float l = len();
        if (l > 0) {
            x /= l;
            y /= l;
        }
    }

    public void scalarMul(float in) {
        x *= in;
        y *= in;
    }

    public void sub(Vec2f in) {
        x -= in.x;
        y -= in.y;
    }

    public void add(Vec2f in) {
        x += in.x;
        y += in.y;
    }

    //signed angle
    public float getAngle(Vec2f in) {
        return (float) Math.toDegrees(Math.atan((x * in.y - y * in.x) / (x * in.x + y * in.y)));
    }

    public String toString() {
        return new String("x: " + x + " y: " + y);
    }

    public float dot(Vec2f in) {
        return x * in.x + y * in.y;
    }
}