
package com.sdgapps.terrainsandbox.MiniEngine.graphics;

/**
 * RGBA Color class
 */
public class Color4f implements java.io.Serializable {
    public float r, g, b, a;

    public Color4f(float _r, float _g, float _b, float _a) {
        r = _r;
        g = _g;
        b = _b;
        a = _a;
    }

    /**
     * Converts a RGB color with values ranging [0,255] to the [0,1] range (OpenGL's standard color format)
     */
    public void normalize_noalpha() {
        r = r / 255f;
        g = g / 255f;
        b = b / 255f;
    }

    /**
     * Converts a RGBA color with values ranging [0,255] to the [0,1] range (OpenGL's standard color format)
     */
    public void normalize_alpha() {
        r = r / 255f;
        g = g / 255f;
        b = b / 255f;
        a = a / 255f;
    }

    public String toString() {
        String str = new String();
        str += "R:" + r + " G:" + g + " B:" + b;
        return str;
    }
}
