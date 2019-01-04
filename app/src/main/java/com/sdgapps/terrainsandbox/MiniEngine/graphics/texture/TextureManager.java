package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.utils.AndroidUtils;
import com.sdgapps.terrainsandbox.utils.Logger;

import java.util.HashMap;


public class TextureManager {

    private static TextureManager instance;

    private Resources resources;
    private AssetManager assetMngr;

    public void setAssets(AssetManager am, Resources r)
    {
        resources=r;
        assetMngr=am;
    }
    public static TextureManager getInstance() {
        if (instance == null) {
            instance = new TextureManager();
        }
        return instance;
    }

    /**
     * Maps texture string names to texture ids
     */
    private HashMap<String, Texture> texMap = new HashMap<String, Texture>();

    public Texture addCubeTexture(String[] files)
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
        t.loadTexture(resources,assetMngr);
        texMap.put(files[0], t);
        return t;
    }

    public Texture addArrayTexture(String[] files, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode)
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
        t.loadTexture(resources,assetMngr);
        texMap.put(files[0], t);
        return t;
    }

    /** Note: ETC2 texture mipmappping requires mip level images to be included. Generate them using Mali texture compression tool
     * and call add2DTexture for the mip0 file
     *
     * Note2: these paths don't come from the user so they should require minimal error checking
     * */
    public  Texture add2DTexture(String path, boolean mipmapping, boolean alpha, boolean interpolation, boolean wrapMode, boolean needsPixels, boolean premultiplyAlpha) {
        path=path.trim();
        while(path.endsWith("/") ||path.endsWith("\\"))
            path=path.substring(0,path.length()-1);
            path=path.trim();

        if (texMap.containsKey(path)) {
            Logger.log("Texture2D Manager: Warning: texture: " + path + " already in texMap, no action taken");
            return texMap.get(path);
        }

        Texture2D t = new Texture2D(path, mipmapping, alpha, interpolation, wrapMode, needsPixels,premultiplyAlpha);
        t.loadTexture(resources,assetMngr);

        texMap.put(path, t);
        // Logger.log("Texture2D Manager: New texture loaded: "+path);
        return t;
    }


    public void reuploadTextures() {
        for (Texture t : texMap.values()) {
            if (t.glID != -1) t.loadTexture(resources,assetMngr);
        }
    }

    public  void clearTM() {

        if (texMap != null) {
            texMap.clear();
        }
    }

    public  void reset() {

        if (texMap != null) texMap.clear();
    }
}
