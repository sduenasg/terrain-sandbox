package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.sdgapps.terrainsandbox.utils.Logger;

import java.io.IOException;

public class CubeTexture extends Texture {

    private String[] files;

    public static final int IntBytes = Integer.SIZE / 8;

    CubeTexture(String[] _names) {
        this.files = _names;
    }

    public int loadTextureInternal(Resources res, AssetManager am)
    {
        this.glID = newTextureID();

        /*Disable android's drawable auto-scaling*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        Bitmap temp=null;
        for(int i=0;i<6;i++) {
            try {
                temp = BitmapFactory.decodeStream(am.open(files[i]),null,opts);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //Bitmap temp = BitmapFactory.decodeResource(res, resID, opts);
            height = temp.getHeight();
            width = temp.getWidth();

            GLES30.glBindTexture(GLES30.GL_TEXTURE_CUBE_MAP, glID);

            GLUtils.texImage2D(GLES30.GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, temp, 0);
            temp.recycle();
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

    @Override
    public int loadTexture(Resources res, AssetManager am) {
        return loadTextureInternal(res,am);
    }
}
