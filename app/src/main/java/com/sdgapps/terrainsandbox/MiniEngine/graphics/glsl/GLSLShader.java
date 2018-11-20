package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.sdgapps.terrainsandbox.utils.Logger;
import com.sdgapps.terrainsandbox.utils.RawResourceReader;


public class GLSLShader {

    public int glHandle;
    public boolean isFragment;

    //shader code resource id
    public int resid;

    public GLSLShader(int resid, Resources res, boolean isFragment) {

        if (isFragment)
            glHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getCode(resid, res));
        else
            glHandle = compileShader(GLES20.GL_VERTEX_SHADER, getCode(resid, res));

        this.isFragment = isFragment;
        this.resid = resid;
    }

    protected void reloadShader(Resources res) {

        if (isFragment)
            glHandle = compileShader(GLES20.GL_FRAGMENT_SHADER, getCode(resid, res));
        else
            glHandle = compileShader(GLES20.GL_VERTEX_SHADER, getCode(resid, res));
    }

    private static String getCode(int resid, Resources res) {
        return RawResourceReader.readTextFileFromRawResource(resid, res);
    }

    /**
     * Compiles a shader.
     *
     * @param shaderType   type of shader (vertex or fragment)
     * @param shaderSource string that contains the shader's code
     * @return OpenGL handle for the shader
     */
    public static int compileShader(final int shaderType, final String shaderSource) {
        int shaderHandle = GLES20.glCreateShader(shaderType);

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES20.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES20.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
                Logger.log("Error compiling shader: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }

        if (shaderHandle == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

}
