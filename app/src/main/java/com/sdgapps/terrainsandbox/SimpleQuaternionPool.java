package com.sdgapps.terrainsandbox;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Quaternion;

/**
 * Use this pool VERY carefully. It is very simple and fast, but
 * it doesn't track which pool elements are being used/stop being used. These are for very short
 * immediate usages only.**
 **/
public class SimpleQuaternionPool {

    private static final int pool_size = 10;
    private static Quaternion[] pool = new Quaternion[pool_size];
    private static int current = 0;

    public static void init() {
        for (int i = 0; i < pool_size; i++) {
            pool[i] = new Quaternion();
        }
    }

    public static Quaternion create() {

        Quaternion ret = pool[current];
        current = (current + 1) % pool_size;
        ret.loadIdentity();
        return ret;
    }

    public static Quaternion create(Quaternion in) {
        Quaternion ret = pool[current];
        ret.x = in.x;
        ret.y = in.y;
        ret.z = in.z;
        ret.w = in.w;

        current = (current + 1) % pool_size;
        return ret;
    }
}
