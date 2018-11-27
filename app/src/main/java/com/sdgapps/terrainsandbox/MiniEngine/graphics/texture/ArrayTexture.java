package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**Warning: only supported from OpenGL ES 3.0 and up*/
public class ArrayTexture extends Texture {

    private int[] resIDs;
    private String[] files;
    private int layerCount;
    public static final int IntBytes = Integer.SIZE / 8;
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
    public int loadTexture(Resources res) {

        this.glID = newTextureID();

        /*Disable android's drawable auto-scaling*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        int[] pixels;
        int i=0;
        for(int resID:resIDs) {
            Bitmap temp = BitmapFactory.decodeResource(res, resID, opts);

            height = temp.getHeight();
            width = temp.getWidth();

            //get the pixel buffer
            ByteBuffer pixelbuf= ByteBuffer.allocateDirect(width * height * IntBytes);
            pixelbuf.order(ByteOrder.nativeOrder());
            temp.copyPixelsToBuffer(pixelbuf);
            pixelbuf.position(0);

            GLES30.glBindTexture(GLES30.GL_TEXTURE_2D_ARRAY, glID);
            GLES30.glTexStorage3D(GLES30.GL_TEXTURE_2D_ARRAY, mipmaplevels, GLES30.GL_RGBA8, width, height, layerCount);
// Upload pixel data.
// The first 0 refers to the mipmap level (level 0, since there's only 1)
// The following 2 zeroes refers to the x and y offsets in case you only want to specify a subrectangle.
// The i refers to the layer index offset (we start from index i and have 1 levels).
// Altogether you can specify a 3D box subset of the overall texture, but only one mip level at a time.
            GLES30.glTexSubImage3D(GLES30.GL_TEXTURE_2D_ARRAY, 0, 0, 0, i, width, height, 1, GLES30.GL_RGBA, GLES30.GL_UNSIGNED_BYTE, pixelbuf);
//https://www.khronos.org/opengl/wiki/Array_Texture
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D_ARRAY);
            temp.recycle();
            i++;
        }

        // Texture2D parameters
        if (interpolation == FILTER_LINEAR)
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_LINEAR_MIPMAP_LINEAR);
        else
            GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_LINEAR_MIPMAP_NEAREST);//bilineal

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_2D_ARRAY, GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR);//mag filter

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
