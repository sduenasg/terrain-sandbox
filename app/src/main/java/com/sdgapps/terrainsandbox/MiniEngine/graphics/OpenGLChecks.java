package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.ETC1Util;
import android.opengl.GLES30;

import com.sdgapps.terrainsandbox.utils.Logger;

public class OpenGLChecks {

    public static boolean oes_depth_texture = false;
    public static boolean etc1_texture_compression = false;
    public static boolean standard_derivatives = false;

    //TODO CODE these requirements to throw errors during app init
    /**
     * Required for texture fetches on the vertex shader
     */
    public static boolean vertex_shader_texture_fetch_enabled = true;

    /**
     * Required for UNSIGNED_INT index arrays on glDrawElements calls
     */
    public static boolean GL_UINT_INDEX = false;
    private static int MAX_VERTEX_UNIFORM_VECTORS;
    private static int MAX_VERTEX_TEXTURE_IMAGE_UNITS;
    private static int GL_MAX_TEXTURE_IMAGE_UNITS;
    public static int GL_MAX_TEXTURE_SIZE;
    public static int GL_DEPTH_BITS;
    public static String GL_RENDERER;
    public static String GL_VENDOR;
    public static String GL_VERSION;

    private static boolean done = false;

    public static void runChecks() {

        if (!done) {
            done = true;
            String extensions = GLES30.glGetString(GLES30.GL_EXTENSIONS);

            int[] res = new int[5];

        /*the maximum number of four-element floating-point, integer, or boolean vectors that can be
         held in uniform variable storage for a vertex shader. The value must be at least 128."*/
            GLES30.glGetIntegerv(GLES30.GL_MAX_VERTEX_UNIFORM_VECTORS, res, 0);
            MAX_VERTEX_UNIFORM_VECTORS = res[0];

            GLES30.glGetIntegerv(GLES30.GL_MAX_VERTEX_TEXTURE_IMAGE_UNITS, res, 1);
            MAX_VERTEX_TEXTURE_IMAGE_UNITS = res[1];

            GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_IMAGE_UNITS, res, 2);
            GL_MAX_TEXTURE_IMAGE_UNITS = res[2];


            GLES30.glGetIntegerv(GLES30.GL_MAX_TEXTURE_SIZE, res, 3);
            GL_MAX_TEXTURE_SIZE = res[3];

            GLES30.glGetIntegerv(GLES30.GL_DEPTH_BITS, res, 4);
            GL_DEPTH_BITS = res[4];

            vertex_shader_texture_fetch_enabled = (MAX_VERTEX_TEXTURE_IMAGE_UNITS > 0);
            // vertex_shader_texture_fetch_enabled=false;

            oes_depth_texture = extensions.contains("OES_depth_texture");
            etc1_texture_compression = ETC1Util.isETC1Supported();

            GL_UINT_INDEX = extensions.contains("GL_OES_element_index_uint");
            standard_derivatives = extensions.contains("GL_OES_standard_derivatives");
            Logger.log("EXTENSIONS " + extensions);

            GL_RENDERER = GLES30.glGetString(GLES30.GL_RENDERER);
            GL_VENDOR = GLES30.glGetString(GLES30.GL_VENDOR);
            GL_VERSION = GLES30.glGetString(GLES30.GL_VERSION);

            //logDeviceGLinfo();
            //log();
        }
    }

    public static void log() {
        Logger.log("GL_OES_ELEMENT_INDEX_UINT " + GL_UINT_INDEX); //required
        Logger.log("GL_OES_DEPTH_TEXTURE extension -> " + oes_depth_texture); //required
        Logger.log("GL_OES_standard_derivatives " + standard_derivatives); //optional
        Logger.log("GL_MAX_TEXTURE_SIZE " + GL_MAX_TEXTURE_SIZE);
        Logger.log("GL_MAX TEXTURE_IMAGE_UNITS " + GL_MAX_TEXTURE_IMAGE_UNITS);
        Logger.log("GL_DEPTH_BITS " + GL_DEPTH_BITS); //optional
    }

    public static void logDeviceGLinfo() {
        Logger.log("GL_RENDERER = " + GL_RENDERER);
        Logger.log("GL_VENDOR = " + GL_VENDOR);
        Logger.log("GL_VERSION = " + GL_VERSION);
        Logger.log("GL_EXTENSIONS = " + GLES30.glGetString(GLES30.GL_EXTENSIONS));
    }
}
