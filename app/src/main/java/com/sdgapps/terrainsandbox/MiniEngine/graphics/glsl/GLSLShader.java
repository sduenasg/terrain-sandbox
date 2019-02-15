package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.content.res.AssetManager;
import android.opengl.GLES30;

import com.sdgapps.terrainsandbox.utils.Logger;
import com.sdgapps.terrainsandbox.utils.StringFileReader;

import java.io.IOException;

public class GLSLShader {

    public int glHandle;
    private boolean isFragment;
    private String path;
    private String programID;

    /**
     * A native method that is implemented by the 'jni-optimizer' native library,
     * which is packaged with this application.
     */
    public native String jnioptimize(String shaderCode,boolean isFragment);


    GLSLShader(String _path, AssetManager assetMngr, boolean isFragment, String _programID, boolean optimize) {
        programID=_programID;
        path=_path;
        this.isFragment = isFragment;

        String code= getCode(assetMngr);

        if(optimize)
        {
            String optcode=jnioptimize(code,isFragment);
            if(optcode!=null)
                code=optcode;
        }

        if (isFragment)
            glHandle = compileShader(GLES30.GL_FRAGMENT_SHADER, code);
        else
            glHandle = compileShader(GLES30.GL_VERTEX_SHADER, code);
    }

    void reloadShader(AssetManager assetMngr) {

        if (isFragment)
            glHandle = compileShader(GLES30.GL_FRAGMENT_SHADER, getCode(assetMngr));
        else
            glHandle = compileShader(GLES30.GL_VERTEX_SHADER, getCode(assetMngr));
    }

    private String getCode(AssetManager am) {
        try {
            return StringFileReader.readTextFromInputStream(am.open(path));

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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
                Logger.err("Error compiling "+(isFragment?"fragment":"vertex")+" shader in "+programID+" : "   + GLES30.glGetShaderInfoLog(shaderHandle));
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
