package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.opengl.ETC1;
import android.opengl.ETC1Util;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.util.Log;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec2f;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.utils.AndroidUtils;

import java.io.IOException;
import java.nio.Buffer;

public class Texture {

    public String name;

    /**
     * Android resource ID
     */
    public int resID;

    /**
     * OpenGL texture handle
     */
    public int glID = -1;
    protected boolean alpha;
    protected boolean mipmapping;

    boolean compressedETC1 = false;
    private boolean interpolation = FILTER_LINEAR;
    private boolean wrapMode = WRAP_CLAMP;

    public static boolean FILTER_LINEAR = true;
    public static boolean FILTER_NEAREST = false;
    public static boolean WRAP_REPEAT = true;
    public static boolean WRAP_CLAMP = false;
    private boolean needsPixels = false;
    private int[] pixels;

    private int height = 0;
    private int width = 0;

    final int mipmaplevels = 11;
    private int[] mipmapresids = new int[mipmaplevels];
    private boolean loadedMippampsresids = false;

    Texture(String name, boolean mipmap, boolean alpha, boolean _interpolation, boolean _wrapMode, int resID, boolean _needsPixels) {
        this.name = name;
        this.mipmapping = mipmap;
        this.alpha = alpha;
        this.resID = resID;
        this.interpolation = _interpolation;
        this.wrapMode = _wrapMode;
        needsPixels = _needsPixels;
    }

    public Texture(int glid) {
        this.glID = glid;
    }

    public int loadTexture(Resources res) {
        if (compressedETC1) {
            getETC1_mipmap_resids();
            return loadCompressedTexturePKM(res);
        } else {
            if (this.mipmapping)
                return loadTexture_mipmapping(res);
            else
                return loadTexture_nomipmapping(res);
        }
    }


    private void getETC1_mipmap_resids() {

        if (!loadedMippampsresids) {


            String[] splitname = this.name.split("_mip");
            String base = new String();
            for (int i = 0; i < splitname.length - 1; i++) {

                base += splitname[i];
            }
            base = base + "_mip_";

            int id = -1;
            mipmapresids[0] = this.resID;//mip0 es la base de esta textura
            for (int i = 1; i < mipmaplevels; i++) {

                String file = base + Integer.toString(i);
                file = file.trim();
                id = AndroidUtils.getResId(file, R.raw.class);
                //Logger.log("kk " +  file +" " + actorId);
                mipmapresids[i] = id;
            }

            loadedMippampsresids = true;
        }
    }

    /**
     * Mali texture compression tool outputs ETC1 compressed textures
     */
    private int loadCompressedTexturePKM(Resources res) {
        this.glID = newTextureID();

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glID);

       /* GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);*/

        if (wrapMode == WRAP_CLAMP) {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        } else {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        }

        if (interpolation == Texture.FILTER_LINEAR)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_LINEAR);//bilinear
        else
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_NEAREST);

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);//mag filter

        try {

            for (int i = 0; i < mipmapresids.length; i++) {
                int id = mipmapresids[i];
                if (id >= 0) {
                    ETC1Util.ETC1Texture etctex = ETC1Util.createTexture(res.openRawResource(id));
                    width = etctex.getWidth();
                    height = etctex.getHeight();


                    Buffer data = etctex.getData();
                    int imageSize = data.remaining();
                    GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, i, ETC1.ETC1_RGB8_OES, etctex.getWidth(), etctex.getHeight(),
                            0, imageSize, data);
                }
            }

            /*
             * NOTE
             * After some testing on my devices, only the Nvidia shield tablet autogenerates mipmaps
             * for ETC1 textures
             **/

            //GLES20.glCompressedTexImage2D(GLES20.GL_TEXTURE_2D, 0, ETC1.ETC1_RGB8_OES, etctex.getWidth(),etctex.getHeight(),
            //  0, imageSize, data);

            // GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D); // no sirve con ETC1, en nvidia shield si, imagino que el driver de nvidia es mejor que el resto

            // bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
            Log.w("ETC1 error", " " + e.getMessage());
        }

        return glID;
    }

    private int loadTexture_nomipmapping(Resources res) {

        this.glID = newTextureID();

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false; //Disables android's automatic scaling of images

        Bitmap temp = BitmapFactory.decodeResource(res, this.resID, opts);

        height = temp.getHeight();
        width = temp.getWidth();

        if (needsPixels) {

            pixels = new int[width * height];
            temp.getPixels(pixels, 0, width, 0, 0, width, height);
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glID);


        if (interpolation == FILTER_LINEAR) {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        } else {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
        }

        if (wrapMode == WRAP_CLAMP) {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        } else {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, temp, 0);

        temp.recycle();

        return glID;
    }

    private int loadTexture_mipmapping(Resources res) {
        this.glID = newTextureID();

        /*Disable android's drawable auto-scaling*/
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScaled = false;

        Bitmap temp = BitmapFactory.decodeResource(res, this.resID, opts);

        height = temp.getHeight();
        width = temp.getWidth();

        if (needsPixels) {

            pixels = new int[width * height];
            temp.getPixels(pixels, 0, width, 0, 0, width, height);
        }

        //  Bitmap bmp = Bitmap.createBitmap(temp, 0, 0, temp.getWidth(), temp.getHeight(), flip, true);
        
        /*
        Android treats image coordinates differently than OpenGL. Android considers Y=0 to be at the top of the image, while OpenGL's interpretation of Y=0 is the bottom.
        We can invert the images here, but it is less costly to just input images with the Y inverted in an image editor.
        */

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, glID);


        // Texture parameters
        if (interpolation == Texture.FILTER_LINEAR)
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_LINEAR);
        else
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                    GLES20.GL_LINEAR_MIPMAP_NEAREST);//bilineal

        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                GLES20.GL_LINEAR);//mag filter

        if (wrapMode == WRAP_CLAMP) {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        } else {
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);
        }

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, temp, 0);
//GLUtils.texImage2D(GLES20.GL_TEXTURE_2D,0,GLES20.GL_RGBA,temp,0);
        //Generate the mipmaps
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        temp.recycle();

        return glID;
    }

    private int newTextureID() {
        int[] temp = new int[1];
        GLES20.glGenTextures(1, temp, 0);
        return temp[0];
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
}
