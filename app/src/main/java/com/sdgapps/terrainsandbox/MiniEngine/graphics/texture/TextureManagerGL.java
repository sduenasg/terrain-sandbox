package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.Resources;

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
     * Adds a texture using a resource name, if compression_ETC1 textures are supported and an compression_ETC1 version of the texture is present, it replaces the png by
     * the compressed compression_ETC1 textures. It falls back to using the png's otherwise
     */
    private static byte getTextureType(String name)
    {
        byte imageType=0;
        String[] aux = name.split("[.]+");
        if (aux.length > 1 && aux[1].trim().toLowerCase().equals("pkm"))
        {
            imageType = Texture.compression_ETC2;
        }
        else
            imageType= Texture.compression_NONE;

        return imageType;
    }
    /** Note: ETC2 texture mipmappping requires mip level images to be included. Generate them using Mali texture compression tool
     * and call addTexture for the mip0 file*/
    public static Texture addTexture(String name, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode, Resources res, boolean needsPixels, boolean premultiplyAlpha) {
        if (texMap.containsKey(name)) {
            Logger.log("Texture2D Manager: Warning: texture named: " + name + " already in texMap, no action taken");
            return texMap.get(name);
        }

        byte imgType=getTextureType(name);

        String[] aux = name.split("[.]+");

        int resid;
        if(imgType==Texture.compression_ETC2) {
            resid = AndroidUtils.getResId(aux[0], R.raw.class);
        }
        else {//no compression
             resid = AndroidUtils.getResId(aux[0], R.drawable.class);
        }

        Texture2D t = new Texture2D(name, mipmapping, alpha, interpolation, wrapMode, resid, needsPixels,premultiplyAlpha,imgType);

        t.loadTexture(res);

        texMap.put(name, t);
        // Logger.log("Texture2D Manager: New texture loaded: "+name);
        return t;
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
