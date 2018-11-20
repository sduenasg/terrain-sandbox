package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.Resources;

public class AppTextureManager extends TextureManagerGL {

    static boolean init = false;

    public static Texture terrainDetailTexture;
    public static Texture terrainDetailNormalmap;

    public static Texture atmosphereGradient;
    public static Resources res;

    public static Texture shadowmap;

    public static void initialize(Resources _res) {
        reset();
        shadowmap = getDummyTex("trebuchet");
        res = _res;
        load_terrain_detail_texture("detail2", "detail2nmap");
        init = true;
    }

    public static void load_terrain_detail_texture(String texturename, String normalmapname) {

        atmosphereGradient = addTexture("atmogradient" + ".png", true, false, Texture.FILTER_LINEAR, Texture.WRAP_REPEAT, res, false);
        terrainDetailTexture = addTexture(texturename + ".png", true, true, Texture.FILTER_LINEAR, Texture.WRAP_CLAMP, res, false);
        terrainDetailNormalmap = addTexture(normalmapname + ".png", true, true, Texture.FILTER_LINEAR, Texture.WRAP_CLAMP, res, false);
    }
}
