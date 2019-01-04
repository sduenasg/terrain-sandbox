package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.GLES30;
import android.opengl.GLUtils;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec2f;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.utils.AndroidUtils;
import com.sdgapps.terrainsandbox.utils.Logger;

import java.io.IOException;

public class Texture2D extends Texture{

    public String name;

    /**
     * Android resource ID
     */
    private int resID;
    private boolean needsPixels;
    private int[] pixels;
    private int[] mipmapresids;
    private boolean loadedMippampsresids = false;
    private boolean preMultiplyAlpha = true;

    Texture2D(String name, boolean mipmap, boolean alpha, boolean _interpolation, boolean _wrapMode, int resID, boolean _needsPixels,boolean _premultiplyAlpha, byte compression) {
        this.name = name;
        this.mipmapping = mipmap;
        this.alpha = alpha;
        this.resID = resID;
        this.interpolation = _interpolation;
        this.wrapMode = _wrapMode;
        needsPixels = _needsPixels;
        preMultiplyAlpha=_premultiplyAlpha;
        this.compressionType=compression;
    }

    public int loadTexture(Resources res) {
        if(compressionType==compression_ETC2 ||compressionType==compression_ETC1) {
            mipmaplevels=12;
            getETC1MipmapResids();
            return loadCompressedETC2(res);
        }
        else {
            return loadTextureInternalUncompressed(res);
        }
    }

    /**grabs mipmaps for the current texture*/
    private void getETC1MipmapResids() {
        if (!loadedMippampsresids) {

            if(mipmapping) {
                mipmapresids = new int[mipmaplevels];
                String[] splitname = this.name.split("_mip");
                String base = new String();
                for (int i = 0; i < splitname.length - 1; i++) {

                    base += splitname[i];
                }
                base = base + "_mip_";

                int id = -1;
                mipmapresids[0] = this.resID;
                for (int i = 1; i < mipmaplevels; i++) {

                    String file = base + Integer.toString(i);
                    file = file.trim();
                    Logger.log("ETC2 print "+file);
                    id = AndroidUtils.getResId(file, R.raw.class);
                    mipmapresids[i] = id;
                }
            }
            else
            {
                mipmapresids = new int[1];
                mipmapresids[0]=resID;
            }
            loadedMippampsresids = true;
        }
    }

    /**
     * Mali texture compression tool outputs ETC1/ETC2 compressed textures
     */
    private int loadCompressedETC2(Resources res) {
        this.glID = newTextureID();


        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, glID);

        if(!mipmapping) {
            if (interpolation == FILTER_LINEAR) {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            } else {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            }
        }
        else
        {
            if (interpolation == FILTER_LINEAR) {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            } else {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);
            }

            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR);
        }

        ETC2Util.ETC2Texture etctex=null;

        for (int i = 0; i < mipmaplevels; i++) {
            int id = mipmapresids[i];
            try {
                etctex = ETC2Util.createTexture(res.openRawResource(id));
            } catch (IOException e) {
                e.printStackTrace();
                Logger.err(e.toString());
            }
            width = etctex.getWidth();
            height = etctex.getHeight();
            int datasize=etctex.getData().remaining();

            GLES30.glCompressedTexImage2D(GLES30.GL_TEXTURE_2D, i, etctex.getCompressionFormat(), etctex.getWidth(), etctex.getHeight(),
                    0, etctex.getData().remaining(), etctex.getData());
        }
        return glID;
    }

    private int loadTextureInternalUncompressed(Resources res) {

        this.glID = newTextureID();

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false; //Disables android's automatic scaling of images

        /*
        * Disables bitmapfactory alpha channel premultiplication:
        * Useful for images with an alpha channel that aren't used as actual images but as data of
        * some kind (like a splatmap)
        */
        if(!preMultiplyAlpha)
            opts.inPremultiplied = false;

        Bitmap temp = BitmapFactory.decodeResource(res, this.resID, opts);
        temp.setPremultiplied(false);

        height = temp.getHeight();
        width = temp.getWidth();

        if (needsPixels) {

            pixels = new int[width * height];
            temp.getPixels(pixels, 0, width, 0, 0, width, height);
        }

        GLES30.glBindTexture(GLES30.GL_TEXTURE_2D, glID);


        if(!mipmapping) {
            if (interpolation == FILTER_LINEAR) {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_LINEAR);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR);
            } else {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER, GLES30.GL_NEAREST);
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_NEAREST);
            }
        }
        else
        {
            if (interpolation == FILTER_LINEAR) {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_LINEAR);
            } else {
                GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MIN_FILTER, GLES30.GL_LINEAR_MIPMAP_NEAREST);//bilineal
            }

            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_MAG_FILTER,GLES30.GL_LINEAR);
        }

        if (wrapMode == WRAP_CLAMP) {
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_CLAMP_TO_EDGE);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_CLAMP_TO_EDGE);
        } else {
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_S, GLES30.GL_REPEAT);
            GLES30.glTexParameterf(GLES30.GL_TEXTURE_2D, GLES30.GL_TEXTURE_WRAP_T, GLES30.GL_REPEAT);
        }

        GLUtils.texImage2D(GLES30.GL_TEXTURE_2D, 0, temp, 0);


        if(mipmapping)
            GLES30.glGenerateMipmap(GLES30.GL_TEXTURE_2D);

        temp.recycle();
        return glID;
    }

    public float getVal(int x, int y) {
        int color = Color.red(pixels[x + y * width]);
        return (float) color / 255f;
    }

    public Vec2f minMaxValArea(float x, float z, float w, float h) {
        float maxVal = -Float.MAX_VALUE;
        float minVal = Float.MAX_VALUE;
        //convert world normalized coords to local texture coords

        x *= width;
        z *= height;
        w *= width;
        h *= height;

        for (float i = x; i < x + w; i++)
            for (float j = z; j < z + h; j++) {

                float newVal = getVal((int)i, (int)j);

                if (newVal > maxVal) {
                    maxVal = newVal;
                }
                if (newVal < minVal) {
                    minVal = newVal;
                }
            }
        return new Vec2f(maxVal, minVal);
    }

    public void freepixels() {
        pixels = null;
    }

    public int getHeight() {
        return height;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public int getGlID() {
        return glID;
    }
}
