package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.opengl.GLES30;

import java.io.IOException;

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
    int mipmaplevels = 6;
    byte compressionType=0;
    byte pathType=0;

    @Deprecated
    static final byte compression_ETC1 =1;

    static final byte compression_ETC2 =2;
    static final byte compression_NONE =0;

    static final byte isdirectory=1;
    static final byte isfile = 0;

    public static final int IntBytes = Integer.SIZE / 8;
    @Override
    public int getGlID() {
        return this.glID;
    }

    @Override
    public int loadTexture(Resources res, AssetManager assetMngr) {
        return 0;
    }

    int newTextureID() {
        int[] temp = new int[1];
        GLES30.glGenTextures(1, temp, 0);
        return temp[0];
    }

    void setWrapMode(int target)
    {
        if (wrapMode == WRAP_CLAMP) {
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        } else {
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
            GLES30.glTexParameteri(target, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        }
    }

    void setFiltering(int target)
    {
        if(!mipmapping) {
            if (interpolation == FILTER_LINEAR) {
                GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            } else {
                GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
                GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            }
        }
        else
        {
            if (interpolation == FILTER_LINEAR) {
                GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            } else {
                GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);//bilineal
            }

            GLES30.glTexParameterf(target, GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR);
        }
    }


}
