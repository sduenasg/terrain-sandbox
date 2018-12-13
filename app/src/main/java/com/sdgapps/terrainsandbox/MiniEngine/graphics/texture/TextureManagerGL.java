package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.Resources;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.OpenGLChecks;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.utils.AndroidUtils;
import com.sdgapps.terrainsandbox.utils.Logger;

import java.util.HashMap;


public class TextureManagerGL {

    static boolean init = false;

    /**
     * Maps texture string names to texture ids
     */
    private static HashMap<String, Texture> texMap = new HashMap<String, Texture>();


    public static Texture2D getDummyTex(String name) {
        Texture2D t = new Texture2D(name, false, false, false, false, 1, false,true);
        return t;
    }


    /**
     * Adds a texture using a resource name and a resource ID (i.e. R.drawable.x or R.raw.x)
     */
    public static Texture addTexture(String name, int id, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode,boolean premultiplyAlpha, Resources res) {
        int resu;

        String[] aux = name.split("[.]+");

        if (texMap.containsKey(name)) {
            return texMap.get(name);
        }

        if (aux.length > 1) {
            String extension = aux[1];

            if (extension.equals("pkm")) {

                Texture2D t = new Texture2D(name, mipmapping, alpha, interpolation, wrapMode, id, false,premultiplyAlpha);
                t.compressedETC1 = true;
                resu = t.loadTexture(res);
                texMap.put(name, t);
                return t;

            } else {

                Texture2D t = new Texture2D(name, mipmapping, alpha, interpolation, wrapMode, id, false,premultiplyAlpha);
                resu = t.loadTexture(res);

                texMap.put(name, t);
                return t;
            }
        } else {
            Texture2D t = new Texture2D(name, mipmapping, alpha, interpolation, wrapMode, id, false,premultiplyAlpha);
            resu = t.loadTexture(res);

            texMap.put(name, t);
            return t;
        }
    }

    public static Texture addCubeTexture(String[] files, Resources res)
    {
        int[] resids=new int[files.length];

        int i=0;
        for(String s : files)
        {
            String[] aux = s.split("[.]+");
            resids[i]= AndroidUtils.getResId(aux[0],  R.drawable.class);
            i++;
        }

        CubeTexture t=new CubeTexture(files, resids);
        t.loadTexture(res);
        texMap.put(files[0], t);
        return t;
    }
    public static Texture addArrayTexture(String[] files, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode, Resources res)
    {
        int[] resids=new int[files.length];

        int i=0;
        for(String s : files)
        {
            String[] aux = s.split("[.]+");
            resids[i]= AndroidUtils.getResId(aux[0],  R.drawable.class);
            i++;
        }

        ArrayTexture t=new ArrayTexture(files, mipmapping, alpha, interpolation, wrapMode, resids);
        t.loadTexture(res);
        texMap.put(files[0], t);
        return t;
    }
    /**
     * Adds a texture using a resource name, if ETC1 textures are supported and an ETC1 version of the texture is present, it replaces the png by
     * the compressed ETC1 textures automatically. It falls back to using the png's otherwise
     */
    public static Texture addTexture(String name, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode, Resources res, boolean needsPixels, boolean premultiplyAlpha) {
        int resu;

        if (texMap.containsKey(name)) {
            Logger.log("Texture2D Manager: Warning: texture named: " + name + " already in texMap, no action taken");

            return texMap.get(name);
        }

        String[] aux = name.split("[.]+");

        if (aux.length > 1) {
            String withmip = aux[0] + "_mip_0";
            String compressed = withmip + ".pkm";
            //Check if it is compressed first
            int resid = AndroidUtils.getResId(withmip, R.raw.class);

            if (resid == -1 || !OpenGLChecks.etc1_texture_compression) {
                //use the png
                resid = AndroidUtils.getResId(aux[0], R.drawable.class);
                Texture2D t = new Texture2D(name, mipmapping, alpha, interpolation, wrapMode, resid, needsPixels,premultiplyAlpha);
                resu = t.loadTexture(res);

                texMap.put(name, t);
                // Logger.log("Texture2D Manager: New texture loaded " + name + " with Id " + resu);
                return t;
            } else {
                //use ETC1 compression
                Texture2D t = new Texture2D(compressed, mipmapping, alpha, interpolation, wrapMode, resid, needsPixels,premultiplyAlpha);
                t.compressedETC1 = true;
                resu = t.loadTexture(res);
                texMap.put(name, t);

                // Logger.log("Texture2D Manager: New ETC1 texture loaded " + name + " with Id " + resu);
                return t;
            }
        } else {
            int resid = AndroidUtils.getResId(aux[0], R.drawable.class);
            Texture2D t = new Texture2D(name, mipmapping, alpha, interpolation, wrapMode, resid, needsPixels,premultiplyAlpha);
            resu = t.loadTexture(res);

            texMap.put(name, t);
            // Logger.log("Texture2D Manager: New texture loaded " + name + " with Id " + resu);
            return t;
        }
    }


    public static void reuploadTextures(Resources res) {

        for (Texture t : texMap.values()) {
            if (t.glID != -1) t.loadTexture(res);
        }
    }

    public static void clearTM() {

        if (texMap != null) {
            texMap.clear();
        }
        init = false;
    }

    public static void reset() {

        if (texMap != null) texMap.clear();
        init = false;
    }
}
