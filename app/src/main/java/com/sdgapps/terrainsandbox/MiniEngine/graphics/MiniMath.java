
package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import java.util.Random;

public class MiniMath {


    public static final float PI = (float) Math.PI;
    public static final float Q_PI = (float) (PI / 4);
    public static final float D_PI = (float) (PI * 2);
    public static final float H_PI = (float) (PI / 2);
    public static final float FLT_EPSILON = 1.1920928955078125E-7f;
    private static final float defaultframetime = 16f;
    public static final float defaultframetimeInverted = 1f / defaultframetime;
    public static final float inv_D_PI = 1f / D_PI;
    public static final float inv_360 = 1f / 360f;
    public static final int FLOAT_SIZE_BYTES = 4;

    public static Random random = new Random(System.currentTimeMillis());

    /**
     * A value to multiply a degree value by, to convert it to radians.
     */
    public static final float DEG_TO_RAD = PI / 180.0f;
    /**
     * A value to multiply a radian value by, to convert it to degrees.
     */
    public static final float RAD_TO_DEG = 180.0f / PI;

    @Deprecated
    public static float invSqrt(float n) {

        return (float) (1.0f / Math.sqrt(n));
    }

    /**
     * fastInvSQRT, faster inverse square root
     *
     * @param x value to calculate fastInvSQRT
     * @returns 1/sqrt(x) Very fast for vector normalization without divisions and without square
     * roots
     */
    public static float fastInvSqrt(float x) {
        float xhalf = 0.5f * x;
        int i = Float.floatToIntBits(x); // get bits for floating value
        i = 0x5f375a86 - (i >> 1); // gives initial guess y0
        x = Float.intBitsToFloat(i); // convert bits back to float
        x = x * (1.5f - xhalf * x * x); // Newton step, repeating increases accuracy
        return x;
    }

    public static float Remap(float value, float from1, float to1, float from2, float to2) {
        return (value - from1) / (to1 - from1) * (to2 - from2) + from2;
    }

    public static float clampf(float value, float from, float to) {

        if (value > to) return to;
        if (value < from) return from;
        return value;
    }

    public static float fract(float val) {

        return val - (float) Math.floor(val);
    }

    public static float lerp(float a, float b, float t) {
        return a + t * (b - a);
    }

    public static float reduceAngle(float rad_angle) {
        int laps = (int) (rad_angle * inv_D_PI);
        float res = rad_angle - MiniMath.D_PI * 2 * laps;
        return res;

    }

    public static float reduceAngleDeg(float deg_angle) {
        int laps = (int) (deg_angle * inv_360);
        float res = deg_angle - 360 * laps;
        return res;

    }

    public static float abs(float floatValue) {
        if (floatValue < 0) {
            return -floatValue;
        }
        return floatValue;
    }

    public static float nextRandomFloat() {

        return random.nextFloat();
    }

    public static int nextRandomInt(int min, int max) {
        return (int) (nextRandomFloat() * (max - min + 1)) + min;
    }

}
