package com.sdgapps.terrainsandbox.MiniEngine.graphics.texture;

import android.content.res.AssetManager;

public interface TextureInterface {
    int getGlID();
    int loadTexture(AssetManager assetMngr);
}
