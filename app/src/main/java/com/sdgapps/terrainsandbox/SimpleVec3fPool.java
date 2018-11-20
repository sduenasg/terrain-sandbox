package com.sdgapps.terrainsandbox;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec3f;

/**
 * Use this pool VERY carefully. It is very simple and fast, but
 * it doesn't track which pool elements are being used/stop being used. These are for very short
 * immediate usages only.**
 **/
public class SimpleVec3fPool {

    private static final int pool_size = 50;
    private static Vec3f[] pool = new Vec3f[pool_size];
    private static int current = 0;

    public static void init() {
        for (int i = 0; i < pool_size; i++) {
            pool[i] = new Vec3f();
        }
    }

    public static Vec3f create() {

        Vec3f ret = pool[current];
        current = (current + 1) % pool_size;
        ret.setZero();
        return ret;
    }

    public static Vec3f create(Vec3f in) {
        Vec3f ret = pool[current];
        ret.x = in.x;
        ret.y = in.y;
        ret.z = in.z;

        current = (current + 1) % pool_size;
        return ret;
    }

    public static Vec3f create(float f, float g, float h) {
        Vec3f ret = pool[current];
        ret.x = f;
        ret.y = g;
        ret.z = h;

        current = (current + 1) % pool_size;
        return ret;
    }
}
