package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.content.res.Resources;
import android.opengl.GLES30;

import com.sdgapps.terrainsandbox.utils.Logger;
import com.sdgapps.terrainsandbox.utils.RawResourceReader;


public class GLSLShader {

    public int glHandle;
    public boolean isFragment;

    //shader code resource id
    public int resid;

    String programID;
    public GLSLShader(int resid, Resources res, boolean isFragment, String _programID) {
        programID=_programID;
        this.isFragment = isFragment;
        if (isFragment)
            glHandle = compileShader(GLES30.GL_FRAGMENT_SHADER, getCode(resid, res));
        else
            glHandle = compileShader(GLES30.GL_VERTEX_SHADER, getCode(resid, res));



        this.resid = resid;
    }

    protected void reloadShader(Resources res) {

        if (isFragment)
            glHandle = compileShader(GLES30.GL_FRAGMENT_SHADER, getCode(resid, res));
        else
            glHandle = compileShader(GLES30.GL_VERTEX_SHADER, getCode(resid, res));
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
    private int compileShader(final int shaderType, final String shaderSource) {
        int shaderHandle = GLES30.glCreateShader(shaderType);

        if (shaderHandle != 0) {
            // Pass in the shader source.
            GLES30.glShaderSource(shaderHandle, shaderSource);

            // Compile the shader.
            GLES30.glCompileShader(shaderHandle);

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES30.glGetShaderiv(shaderHandle, GLES30.GL_COMPILE_STATUS, compileStatus, 0);

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0) {
               // Logger.err("Error compiling "+(isFragment?"fragment":"vertex")+" shader in "+programID+" : "   + GLES30.glGetShaderInfoLog(shaderHandle));
                GLES30.glDeleteShader(shaderHandle);
                shaderHandle = 0;
                throw new RuntimeException("Error compiling "+(isFragment?"fragment":"vertex")+" shader in "+programID+" : "   + GLES30.glGetShaderInfoLog(shaderHandle));
            }
            else
                Logger.warning("GLes log for " + (isFragment ? "fragment" : "vertex") + " shader in " + programID + " : " + GLES30.glGetShaderInfoLog(shaderHandle));


        }

        if (shaderHandle == 0) {
            throw new RuntimeException("Error creating shader.");
        }

        return shaderHandle;
    }

}
