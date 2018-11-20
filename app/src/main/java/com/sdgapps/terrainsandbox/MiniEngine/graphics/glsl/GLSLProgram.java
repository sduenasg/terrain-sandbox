package com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl;

import android.content.res.Resources;
import android.opengl.GLES20;

import com.sdgapps.terrainsandbox.MiniEngine.behaviours.Light;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.texture.Texture;
import com.sdgapps.terrainsandbox.Singleton;
import com.sdgapps.terrainsandbox.utils.Logger;

import java.util.HashMap;


/**
 * GLSL Program with Vertex and Fragment shaders.
 */
public class GLSLProgram {

    private HashMap<String, ShaderVariable> uniforms = new HashMap<>();

    public int glHandle = -1;
    public GLSLShader vertex, fragment;

    public int positionHandle = -1;
    public int normalHandle = -1;

    public int tangentHandle = -1;
    public int bitangentHandle = -1;

    public int blendIndicesHandle = -1;
    public int blendWeightsHandle = -1;
    public int texcoordHandle = -1;

    public int MVPMatrixHandle = -1;
    public int MVMatrixHandle = -1;

    public int ModelMatrixHandle = -1;
    public int ViewMatrixHandle = -1;
    public int ProjMatrixHandle = -1;

    public int shadowmapMVPmatrixHandle = -1;

    public int gridPositionHandle = -1;
    public int barycentricHandle = -1;
    public int morphTargetHandle = -1;

    public String shaderID = "";


    public Texture texture2;
    public Texture texture3;
    public Texture texture4;

    public int ntextures = 1;

    public int shadowMapTextureUniformHandle;
    public int TextureUniformHandle;
    public int Texture2UniformHandle;
    public int Texture3UniformHandle;
    public int Texture4UniformHandle;
    public int BumpMapTextureUniformHandle;
    public int SpecularMapTextureUniformHandle;
    public int DisplacementMapTextureUniformHandle;

    //Luz : Handlers
    public int LightPosHandle;
    public int ambientColorHandle;
    public int diffuseColorHandle;

    public boolean usesLight = false;
    public boolean usesBumpMap = false;
    public boolean usesSpecularMap = false;
    public boolean usesShadowmapMVP = false;

    //en todos se usa la MVP
    public static final short USES_MVMATRIX = 0x0;
    public static final short USES_MODEL_MATRIX = 0x1;
    public static final short USES_VIEW_MATRIX = 0x2;
    public static final short USES_NONE = 0x3;
    public static final short USES_VIEW_AND_PROJ = 0x4;

    /**
     * Indica si se le pasa al shader la matriz ModelView (true) o solo la view (ya que ya le dan
     * las coordenadas en coordenadas de mundo)
     */
    public short MVMatrixUsage = USES_MVMATRIX;

    public void addTexture(Texture t) {
        ntextures++;

        if (ntextures == 2) texture2 = t;
        else if (ntextures == 3) texture3 = t;
        else if (ntextures == 4) texture4 = t;
        else {
            ntextures--;
            Logger.log("Shaders: No more textures supported by the engine");
        }
    }


    public GLSLProgram(String id, int vertexid, int fragmentid, boolean usesLight, boolean usesBumpMap, boolean usesSpecularMap, short matrixusage,
                       boolean uses_shadowmapMVP) {
        Resources res=Singleton.systems.sShaderSystem.res;
        this.shaderID = id;
        this.ntextures = 1;
        vertex = new GLSLShader(vertexid, res, false);
        fragment = new GLSLShader(fragmentid, res, true);

        glHandle = createAndLinkProgram(vertex.glHandle,
                fragment.glHandle, new String[]{
                        "a_Position", "a_Normal"
                });
        this.usesLight = usesLight;
        this.usesSpecularMap = usesSpecularMap;
        this.usesBumpMap = usesBumpMap;
        this.MVMatrixUsage = matrixusage;
        this.usesShadowmapMVP = uses_shadowmapMVP;
        buildVariables();
    }

    /**
     * Useful for context changes where android destroys the egl context (shaders, textures etc. are
     * removed from GPU memory)
     */
    public void reloadToGPU(Resources res) {
        this.vertex.reloadShader(res);
        this.fragment.reloadShader(res);
        int vertexhandle = this.vertex.glHandle;
        int fragmenthandle = this.fragment.glHandle;

        glHandle = createAndLinkProgram(vertexhandle,
                fragmenthandle, new String[]{
                        "a_Position", "a_Normal"
                });
        buildVariables();
        rebuildUserVariables();
    }


    private void rebuildUserVariables() {

        this.buildVariables();

        for (ShaderVariable sv : uniforms.values()) {
            sv.glHandle = GLES20.glGetUniformLocation(glHandle, sv.name);
        }
    }


    /**
     * Obtain basic uniform and attribute handlers for the program
     */
    void buildVariables() {
        /**NOTE: glGetUniformLocation is allowed to return -1 if the uniform is not used by the shader*/

        positionHandle = GLES20.glGetAttribLocation(glHandle, "a_Position");
        normalHandle = GLES20.glGetAttribLocation(glHandle, "a_Normal");
        tangentHandle = GLES20.glGetAttribLocation(glHandle, "a_Tangent");
        bitangentHandle = GLES20.glGetAttribLocation(glHandle, "a_Bitangent");
        texcoordHandle = GLES20.glGetAttribLocation(glHandle, "a_TexCoordinate");

        MVPMatrixHandle = GLES20.glGetUniformLocation(glHandle, "u_MVPMatrix");
        MVMatrixHandle = GLES20.glGetUniformLocation(glHandle, "u_MVMatrix");
        ModelMatrixHandle = GLES20.glGetUniformLocation(glHandle, "u_Modelmatrix");
        ProjMatrixHandle = GLES20.glGetUniformLocation(glHandle, "u_Projectionmatrix");
        ViewMatrixHandle = GLES20.glGetUniformLocation(glHandle, "u_Viewmatrix");

        TextureUniformHandle = GLES20.glGetUniformLocation(glHandle, "u_Texture");
        Texture2UniformHandle = GLES20.glGetUniformLocation(glHandle, "u_Texture2");
        Texture3UniformHandle = GLES20.glGetUniformLocation(glHandle, "u_Texture3");
        Texture4UniformHandle = GLES20.glGetUniformLocation(glHandle, "u_Texture4");

        if (usesBumpMap)
            BumpMapTextureUniformHandle = GLES20.glGetUniformLocation(glHandle, "u_BumpMap");

        if (usesSpecularMap)
            SpecularMapTextureUniformHandle = GLES20.glGetUniformLocation(glHandle, "u_SpecularMap");

        DisplacementMapTextureUniformHandle = GLES20.glGetUniformLocation(glHandle, "u_heightmap");

        if (usesLight) {
            LightPosHandle = GLES20.glGetUniformLocation(glHandle, "u_LightPos");
            ambientColorHandle = GLES20.glGetUniformLocation(glHandle, "ambientLight");
            diffuseColorHandle = GLES20.glGetUniformLocation(glHandle, "diffuseLight");
        }

        blendIndicesHandle = GLES20.glGetAttribLocation(glHandle, "a_blendIndices");
        blendWeightsHandle = GLES20.glGetAttribLocation(glHandle, "a_blendWeights");

        gridPositionHandle = GLES20.glGetAttribLocation(glHandle, "a_gridPosition");
        barycentricHandle = GLES20.glGetAttribLocation(glHandle, "a_barycentric");
        morphTargetHandle = GLES20.glGetAttribLocation(glHandle, "a_morphTarget");

        if (usesShadowmapMVP) {
            shadowmapMVPmatrixHandle = GLES20.glGetUniformLocation(glHandle, "u_shadowmapMVP");
            shadowMapTextureUniformHandle = GLES20.glGetUniformLocation(glHandle, "u_Shadowmaptex");
        }
    }

    public void useProgram(Light light) {

        GLES20.glUseProgram(glHandle);

        if (this.usesLight && light != null) {
            GLES20.glUniform3f(LightPosHandle, light.mLightPosInEyeSpace[0],
                    light.mLightPosInEyeSpace[1], light.mLightPosInEyeSpace[2]);

            GLES20.glUniform3f(ambientColorHandle, light.lightAmbient[0],
                    light.lightAmbient[1], light.lightAmbient[2]);

            GLES20.glUniform3f(diffuseColorHandle, light.lightDiffuse[0],
                    light.lightDiffuse[1], light.lightDiffuse[2]);
        }

        if (this.ntextures > 1) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.texture2.glID);
            GLES20.glUniform1i(Texture2UniformHandle, 1);
        }
        if (this.ntextures > 2) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.texture3.glID);
            GLES20.glUniform1i(Texture3UniformHandle, 2);
        }

        if (this.ntextures > 3) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.texture4.glID);
            GLES20.glUniform1i(Texture4UniformHandle, 3);
        }

        for (ShaderVariable sv : uniforms.values()) {
            if (sv.autobind)
                sv.bind();
        }
    }

    public void addUniform(ShaderVariable sv) {
        sv.glHandle = GLES20.glGetUniformLocation(glHandle, sv.name);
        uniforms.put(sv.name, sv);
    }

    public ShaderVariable getUniform(String uname) {

        ShaderVariable res = uniforms.get(uname);

        //if(res==null)
        //   Logger.log("Requested uniform "+ uname + " on shader " + shaderID + " with a null result");
        return res;
    }

    /**
     * Compiles and links a Shader Program
     *
     * @param vertexShaderHandle   Vertex shader OpenGL Handle
     * @param fragmentShaderHandle Fragment shader OpenGL Handle
     * @param attributes           Program attributes
     * @return OpenGL handle of the program
     */
    private static int createAndLinkProgram(final int vertexShaderHandle,
                                            final int fragmentShaderHandle, final String[] attributes) {
        int programHandle = GLES20.glCreateProgram();

        if (programHandle != 0) {
            // bindGridMesh the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // bindGridMesh the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // bindGridMesh attributes
            if (attributes != null) {
                final int size = attributes.length;
                for (int i = 0; i < size; i++) {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == 0) {
                Logger.log("Error compiling program: " + GLES20.glGetProgramInfoLog(programHandle));
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0) {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }

    public String toString() {
        return " vertexHandle: " + this.vertex.glHandle + " fragmentHandle: " + this.fragment.glHandle;
    }
}
