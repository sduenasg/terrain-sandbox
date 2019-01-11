package com.sdgapps.terrainsandbox.MiniEngine.terrain;

import android.content.res.AssetManager;
import android.content.res.Resources;

import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture2D;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.TextureManager;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.utils.AndroidUtils;
import com.sdgapps.terrainsandbox.utils.StringFileReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Class that reads a terrain configuration from a plain text file
 * <p>
 * The file contains the lists of color maps, normal maps, and height maps (displacement)
 * for a certain terrain.
 */
public class TerrainData {

    private boolean planetaryScene = false;

    private ArrayList<String> colorMaps;
    private ArrayList<String> displacementMaps;
    private ArrayList<String> normalMaps;
    private ArrayList<String> splatMaps;
    private String textureArraySplat;

    Texture[] TexColorMaps;
    Texture[] TexDisplacementMaps;
    Texture[] TexNormalMaps;
    Texture[] TexSplatMaps;

    Texture TexArraySplat;
    Texture Clouds;

    public TerrainData(String file, AssetManager assetMngr) {
        String data= null;
        try {
            data = StringFileReader.readTextFromInputStream(assetMngr.open(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
        String[] lines = data.split("\n");
        List<String> lst = new ArrayList<String>(Arrays.asList(lines));

        String ln;
        String[] values;

        colorMaps = new ArrayList<String>();
        displacementMaps = new ArrayList<String>();
        normalMaps = new ArrayList<String>();
        splatMaps = new ArrayList<String>();


        while (!lst.isEmpty()) {
            ln = lst.get(0);
            lst.remove(0);
            values = ln.split(" ");

            if (ln.contains("#col")) {

                if (values.length > 2)
                    planetaryScene = true;

                colorMaps.addAll(Arrays.asList(values).subList(1, values.length));
            } else if (ln.contains("#norm")) {
                if (values.length > 2)
                    planetaryScene = true;


                normalMaps.addAll(Arrays.asList(values).subList(1, values.length));
            } else if (ln.contains("#disp")) {
                if (values.length > 2)
                    planetaryScene = true;

                displacementMaps.addAll(Arrays.asList(values).subList(1, values.length));
            }
            else if (ln.contains("#splat")) {
                if (values.length > 2)
                    planetaryScene = true;

                splatMaps.addAll(Arrays.asList(values).subList(1, values.length));
            }
            else if (ln.contains("#ssheet")) {
                    textureArraySplat=values[1].trim();
            }
        }
    }

    public void LoadTextures() {
        TexColorMaps = new Texture[colorMaps.size()];
        for (int i = 0; i < colorMaps.size(); i++) {
            TexColorMaps[i] = TextureManager.getInstance().add2DTexture(colorMaps.get(i), true, false, Texture2D.FILTER_LINEAR, Texture2D.WRAP_CLAMP,false,true);
        }

        TexNormalMaps = new Texture2D[normalMaps.size()];
        for (int i = 0; i < normalMaps.size(); i++) {
            TexNormalMaps[i] = TextureManager.getInstance().add2DTexture(normalMaps.get(i), true, false, Texture2D.FILTER_LINEAR, Texture2D.WRAP_CLAMP,false,true);
        }

        TexDisplacementMaps = new Texture2D[displacementMaps.size()];
        for (int i = 0; i < displacementMaps.size(); i++) {
            TexDisplacementMaps[i] = TextureManager.getInstance().add2DTexture(displacementMaps.get(i), false, false, Texture2D.FILTER_NEAREST, Texture2D.WRAP_CLAMP,true,true);
        }

        TexSplatMaps = new Texture2D[splatMaps.size()];
        for (int i = 0; i < splatMaps.size(); i++) {
            TexSplatMaps[i] = TextureManager.getInstance().add2DTexture(splatMaps.get(i), true, false, Texture2D.FILTER_LINEAR, Texture2D.WRAP_CLAMP,false,false);
        }

        TexArraySplat=TextureManager.getInstance().addArrayTexture(textureArraySplat,true,false,Texture.FILTER_LINEAR,Texture.WRAP_REPEAT);
        Clouds=TextureManager.getInstance().add2DTexture("textures/cloudswithmips",true,false,true, Texture.WRAP_CLAMP,false,true);
    }

    public boolean isPlanetaryScene() {
        return planetaryScene;
    }

}
