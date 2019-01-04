package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;

public class CubeTexture extends Texture {

    private int[] resIDs;
    private String[] files;

    public static final int IntBytes = Integer.SIZE / 8;

    CubeTexture(String[] _names, int[] _resIDs) {
        this.files = _names;
        this.resIDs=_resIDs;
    }
    @Override
    public int loadTexture(Resources res, AssetManager am) {

        this.glID = newTextureID();

        /*Disable android's drawable auto-scaling*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        int i=0;
        for(int resID:resIDs) {

            Bitmap temp = BitmapFactory.decodeResource(res, resID, opts);
            height = temp.getHeight();
            width = temp.getWidth();

            //get the pixel buffer
            /*ByteBuffer pixelbuf= ByteBuffer.allocateDirect(width * height * IntBytes);
            pixelbuf.order(ByteOrder.nativeOrder());
            temp.copyPixelsToBuffer(pixelbuf);
            pixelbuf.position(0);*/

            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, glID);

            GLUtils.texImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, temp, 0);
           /* GLES30.glTexImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i,
                    0, GLES30.GL_RGB, width, height, 0, GLES30.GL_RGB, GLES30.GL_UNSIGNED_BYTE, pixelbuf
            );*/

            temp.recycle();
            i++;
        }

        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MIN_FILTER,
                    GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_MAG_FILTER,
                GLES30.GL_LINEAR);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        GLES30.glTexParameteri(GLES30.GL_TEXTURE_CUBE_MAP, GLES30.GL_TEXTURE_WRAP_R, GLES30.GL_CLAMP_TO_EDGE);

        return glID;
    }
}
