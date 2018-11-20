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

    public static int getGLid(String s) {

        if (texMap == null)
            return -1;
        return ((Integer) texMap.get(s).glID).intValue();
    }

    public static Texture getTexture(String s) {
        if (texMap == null)
            return null;

        return texMap.get(s);
    }

    public static Texture getDummyTex(String name) {
        Texture t = new Texture(name, false, false, false, false, 1, false);
        return t;
    }


    /**
     * Adds a texture using a resource name and a resource ID (i.e. R.drawable.x or R.raw.x)
     */
    public static Texture addTexture(String name, int id, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode, Resources res) {
        int resu;

        String[] aux = name.split("[.]+");

        if (texMap.containsKey(name)) {
            return texMap.get(name);
        }

        if (aux.length > 1) {
            String extension = aux[1];

            if (extension.equals("pkm")) {

                Texture t = new Texture(name, mipmapping, alpha, interpolation, wrapMode, id, false);
                t.compressedETC1 = true;
                resu = t.loadTexture(res);
                texMap.put(name, t);
                return t;

            } else {

                Texture t = new Texture(name, mipmapping, alpha, interpolation, wrapMode, id, false);
                resu = t.loadTexture(res);

                texMap.put(name, t);
                return t;
            }
        } else {
            Texture t = new Texture(name, mipmapping, alpha, interpolation, wrapMode, id, false);
            resu = t.loadTexture(res);

            texMap.put(name, t);
            return t;
        }
    }


    /**
     * Adds a texture using a resource name, if ETC1 textures are supported and an ETC1 version of the texture is present, it replaces the png by
     * the compressed ETC1 textures automatically. It falls back to using the png's otherwise
     */
    public static Texture addTexture(String name, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode, Resources res, boolean needsPixels) {
        int resu;

        if (texMap.containsKey(name)) {
            Logger.log("Texture Manager: Warning: texture named: " + name + " already in texMap, no action taken");

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
                Texture t = new Texture(name, mipmapping, alpha, interpolation, wrapMode, resid, needsPixels);
                resu = t.loadTexture(res);

                texMap.put(name, t);
                // Logger.log("Texture Manager: New texture loaded " + name + " with Id " + resu);
                return t;
            } else {
                //use ETC1 compression
                Texture t = new Texture(compressed, mipmapping, alpha, interpolation, wrapMode, resid, needsPixels);
                t.compressedETC1 = true;
                resu = t.loadTexture(res);
                texMap.put(name, t);

                // Logger.log("Texture Manager: New ETC1 texture loaded " + name + " with Id " + resu);
                return t;
            }
        } else {
            int resid = AndroidUtils.getResId(aux[0], R.drawable.class);
            Texture t = new Texture(name, mipmapping, alpha, interpolation, wrapMode, resid, needsPixels);
            resu = t.loadTexture(res);

            texMap.put(name, t);
            // Logger.log("Texture Manager: New texture loaded " + name + " with Id " + resu);
            return t;
        }
    }

    public static void addDummy(String name) {
        Texture t = new Texture(name, false, false, false, false, -1, false);
        texMap.put(name, t);
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
