
package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.Behaviour;
import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Color4f;

/**
 * Basic light class
 */
public class Light extends Behaviour {

    private float[] mLightModelMatrix = new float[16];

    public float[] lightAmbient;
    public float[] lightDiffuse;
    public float[] lightSpecular;
    public Color4f fogColor = new Color4f(1, 1, 1, 1);


    private final float[] mLightPosInModelSpace = new float[]{
            0.0f, 0.0f, 0.0f, 1.0f
    };

    private final float[] mLightPosInWorldSpace = new float[4];
    public final float[] mLightPosInEyeSpace = new float[4];

    public Light() {
        lightAmbient = new float[4];
        lightDiffuse = new float[4];
        lightSpecular = new float[4];
    }

    public void initData(Color4f ambient, Color4f diffuse, Color4f specular, Color4f _fogcolor) {

        fogColor = _fogcolor;

        lightAmbient[0] = ambient.r;
        lightAmbient[1] = ambient.g;
        lightAmbient[2] = ambient.b;
        lightAmbient[3] = ambient.a;

        lightDiffuse[0] = diffuse.r;
        lightDiffuse[1] = diffuse.g;
        lightDiffuse[2] = diffuse.b;
        lightDiffuse[3] = diffuse.a;

        lightSpecular[0] = specular.r;
        lightSpecular[1] = specular.g;
        lightSpecular[2] = specular.b;
        lightSpecular[3] = specular.a;

        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, gameObject.transform.position.x, gameObject.transform.position.y,
                gameObject.transform.position.z);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, MatrixManager.viewMatrix, 0,
                mLightPosInWorldSpace, 0);

        GLES30.glClearColor(fogColor.r, fogColor.g, fogColor.b, 1f);
    }

    //TODO use the transform matrices
    private void updateMatrices() {
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.translateM(mLightModelMatrix, 0, transform.position.x, transform.position.y,
                transform.position.z);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, MatrixManager.viewMatrix, 0,
                mLightPosInWorldSpace, 0);
    }

    @Override
    public void update() {
        updateMatrices();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onEnable() {

    }

    @Override
    public void onDisable() {

    }
}
