package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;
import android.content.res.Resources;

public interface TextureInterface {
    int getGlID();
    int loadTexture(Resources res, AssetManager assetMngr);
}
