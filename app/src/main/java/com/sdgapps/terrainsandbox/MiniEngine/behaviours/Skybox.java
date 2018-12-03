package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Transform;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Vec2f;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.GridMesh;
import com.sdgapps.terrainsandbox.utils.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Skybox: Send a clipping space quad filling the viewport. The shader unprojects it to generate
 * the cubemap uvs.
 */
public class Skybox extends Renderer {
    public boolean uploadedVBO = false;

    private static final int FloatBytes = Float.SIZE / 8;
    private static final int ShortBytes = Short.SIZE / 8;
    private int indexArraySize;

    private float verts[];
    private short[] tris; //index array
    private int buffers[];//data buffer ids

    private int vertCount;

    private class SkyboxFace {
        private Vec2f[] vertices;
        SkyboxFace(Vec2f a, Vec2f  b, Vec2f  c, Vec2f  d)
        {
           vertices=new Vec2f[]{a,b,c,d};
        }
    }

    public Skybox() {
        material = new Material();
        generatePositionData();
        transform = new Transform();
        GenBuffersAndSubmitToGL();
    }

    private void generatePositionData() {

        vertCount = 4 ;
        verts = new float[vertCount * 2];

        SkyboxFace face = new SkyboxFace(
                new Vec2f(-1, -1),
                new Vec2f(1, -1),
                new Vec2f(1, 1),
                new Vec2f(-1, 1));

        int triCount = 2;
        int elementCount = triCount * 3;//3 vertices per tri
        tris = new short[elementCount];

        int index=0;
        for(int j=0;j<4;j++) {
            // Position
            verts[j*2] = face.vertices[j].x;
            verts[j*2+1] = face.vertices[j].y;
        }

        // First triangle
        tris[index++] = 0;
        tris[index++] = 1;
        tris[index++] = 2;

        // Second triangle
        tris[index++] = 0;
        tris[index++] = 2;
        tris[index++] = 3;

    }

    public void GenBuffersAndSubmitToGL() {

        if (!uploadedVBO) {
            //gen buffers
            ByteBuffer fBufW = ByteBuffer.allocateDirect(tris.length * ShortBytes);
            fBufW.order(ByteOrder.nativeOrder());
            ShortBuffer indexBuffer = fBufW.asShortBuffer();
            indexBuffer.put(tris);
            indexBuffer.position(0);

            ByteBuffer aBuf = ByteBuffer.allocateDirect(verts.length * FloatBytes);
            aBuf.order(ByteOrder.nativeOrder());
            FloatBuffer posBuffer = aBuf.asFloatBuffer();
            posBuffer.put(verts);
            posBuffer.position(0);
            //gridPositions_array = null;

            buffers = new int[2];

            //submit to opengl
            GLES30.glGenBuffers(2, buffers, 0);

            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);
            GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,
                    indexBuffer.capacity() * GridMesh.ShortBytes, indexBuffer,
                    GLES30.GL_STATIC_DRAW);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, posBuffer.capacity() * GridMesh.FloatBytes, posBuffer,
                    GLES30.GL_STATIC_DRAW);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);


            uploadedVBO = true;
            indexArraySize = tris.length;
        }
    }


    private void bindAttribs(GLSLProgram shader) {
        int positionHandle=shader.getAttributeGLid("a_Position");

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
        GLES30.glVertexAttribPointer(positionHandle, 2, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(positionHandle);



    }

    @Override
    public void draw() {
        material.bindShader();
        material.bindTextures();
        sendMatrices();
        bindAttribs(material.shader);

        //bind index buffer
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        GLES30.glDepthMask(false);
            GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexArraySize, GLES30.GL_UNSIGNED_SHORT, 0);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
            GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
        GLES30.glDepthMask(true);
    }

    private void sendMatrices() {

        ShaderUniformMatrix4fv ViewMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_Viewatrix");
        ShaderUniformMatrix4fv ProjectionMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_Projectionmatrix");

        ViewMatrix.array=MatrixManager.viewMatrix;
        ProjectionMatrix.array=MatrixManager.projectionMatrix;

        ViewMatrix.bind();
        ProjectionMatrix.bind();
    }

    public void invalidateVBO() {
        uploadedVBO = false;
    }
}
