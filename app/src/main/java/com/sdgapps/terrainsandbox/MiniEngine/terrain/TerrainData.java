package com.sdgapps.terrainsandbox.MiniEngine.terrain;

import android.content.res.Resources;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.TextureManagerGL;
import com.sdgapps.terrainsandbox.R;
import com.sdgapps.terrainsandbox.utils.AndroidUtils;
import com.sdgapps.terrainsandbox.utils.RawResourceReader;

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

    public Texture[] TexColorMaps;
    public Texture[] TexDisplacementMaps;
    public Texture[] TexNormalMaps;

    public TerrainData(String file, Resources res) {
        int resid = AndroidUtils.getResId(file, R.raw.class);
        String data = RawResourceReader.readTextFileFromRawResource(resid, res);
        String[] lines = data.split("\n");
        List<String> lst = new ArrayList<String>(Arrays.asList(lines));

        String ln;
        String[] values;

        colorMaps = new ArrayList<String>();
        displacementMaps = new ArrayList<String>();
        normalMaps = new ArrayList<String>();

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
        }
    }

    public void LoadTextures(Resources res) {
        TexColorMaps = new Texture[colorMaps.size()];
        for (int i = 0; i < colorMaps.size(); i++) {
            TexColorMaps[i] = TextureManagerGL.addTexture(colorMaps.get(i), true, false, Texture.FILTER_LINEAR, Texture.WRAP_CLAMP, res, false);
        }

        TexNormalMaps = new Texture[normalMaps.size()];
        for (int i = 0; i < normalMaps.size(); i++) {
            TexNormalMaps[i] = TextureManagerGL.addTexture(normalMaps.get(i), true, false, Texture.FILTER_LINEAR, Texture.WRAP_CLAMP, res, false);
        }

        TexDisplacementMaps = new Texture[displacementMaps.size()];
        for (int i = 0; i < displacementMaps.size(); i++) {
            TexDisplacementMaps[i] = TextureManagerGL.addTexture(displacementMaps.get(i), false, false, Texture.FILTER_NEAREST, Texture.WRAP_CLAMP, res, true);
        }
    }

    public boolean isPlanetaryScene() {
        return planetaryScene;
    }

}
