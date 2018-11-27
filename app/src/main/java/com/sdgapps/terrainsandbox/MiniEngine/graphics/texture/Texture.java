package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.Resources;
import android.opengl.GLES30;

public class Texture implements TextureInterface{
    public int glID = -1;
    public static final boolean FILTER_LINEAR = true;
    public static final boolean FILTER_NEAREST = false;
    public static final boolean WRAP_REPEAT = true;
    public static final boolean WRAP_CLAMP = false;


    boolean interpolation = FILTER_LINEAR;
    boolean wrapMode = WRAP_CLAMP;
    boolean alpha;
    boolean mipmapping;

    int height = 0;
    int width = 0;
    final int mipmaplevels = 6;
    @Override
    public int getGlID() {
        return this.glID;
    }

    @Override
    public int loadTexture(Resources res) {
        return 0;
    }

    int newTextureID() {
        int[] temp = new int[1];
        GLES30.glGenTextures(1, temp, 0);
        return temp[0];
    }
}
