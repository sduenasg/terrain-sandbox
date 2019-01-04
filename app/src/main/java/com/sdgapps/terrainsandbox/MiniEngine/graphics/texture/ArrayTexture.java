package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**Warning: only supported from OpenGL ES 3.0 and up*/
public class ArrayTexture extends Texture {

    private int[] resIDs;
    private String[] files;
    private int layerCount;

    ArrayTexture(String[] _names, boolean mipmap, boolean alpha, boolean _interpolation, boolean _wrapMode, int[] _resIDs) {
        this.files = _names;
        this.resIDs=_resIDs;
        this.mipmapping = mipmap;
        this.alpha = alpha;
        this.interpolation = _interpolation;
        this.wrapMode = _wrapMode;
        layerCount=resIDs.length;
    }

    @Override
    public int loadTexture(Resources res, AssetManager am) {

        this.glID = newTextureID();

        /*Disable android's drawable auto-scaling*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, glID);
        Bitmap temp = BitmapFactory.decodeResource(res, resIDs[0], opts);
        height = temp.getHeight();
        width = temp.getWidth();
        GLES30.glTexStorage3D(GLES30.GL_TEXTURE_2D_ARRAY, mipmaplevels, GLES30.GL_RGBA8, width, height, layerCount);

        int i=0;
        for(int resID:resIDs) {
            temp = BitmapFactory.decodeResource(res, resID, opts);

            height = temp.getHeight();
            width = temp.getWidth();

            //get the pixel buffer
            ByteBuffer pixelbuf= ByteBuffer.allocateDirect(width * height *IntBytes);
            pixelbuf.order(ByteOrder.nativeOrder());
            temp.copyPixelsToBuffer(pixelbuf);
            pixelbuf.position(0);

            int internalFormat=GLUtils.getInternalFormat(temp); //like GLES30.RGBA
            int type=GLUtils.getType(temp); //i.e GLES30.UNSIGNED_BYTE

            // Upload pixel data.
            GLES30.glTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, width, height, 1, internalFormat, type, pixelbuf);
//https://www.khronos.org/opengl/wiki/Array_Texture
//https://www.khronos.org/registry/OpenGL-Refpages/es3.0/
            temp.recycle();
            i++;
        }
        GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D_ARRAY);

        // Texture2D parameters
        if (interpolation == FILTER_LINEAR) {
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
        }
        else {
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);//bilinear
        }

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);//mag filter

        if (wrapMode == WRAP_CLAMP) {
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        } else {
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        }

        return glID;
    }
}
