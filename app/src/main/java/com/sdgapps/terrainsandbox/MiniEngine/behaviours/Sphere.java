package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.MiniMath;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.Transform;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.MiniEngine.terrain.GridMesh;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class Sphere extends Renderer {

    public boolean uploadedVBO = false;

    private static final int FloatBytes = Float.SIZE / 8;
    private static final int ShortBytes = Short.SIZE / 8;
    private int indexArraySize;
    private float verts[];
    private short[] tris;
    private int buffers[];
    private int vertCount;

    public Sphere(GLSLProgram shader, float radius, int longSegs, int latSegs) {
        material = new Material();
        material.shader = shader;
        generatePositionData(radius, longSegs, latSegs);
        transform = new Transform();
    }

    private void generatePositionData(float radius, int longSegs, int latSegs) {

        int longVerts = longSegs + 1;
        int latVerts = latSegs + 1;

        vertCount = longVerts * latVerts;
        verts = new float[longVerts * latVerts * 8];

        //pack positions, normals and texture coords into the same array
        for (int i = 0; i < longVerts; i++) {
            float iDivLong = (float) i / (float) longSegs;
            float theta = i == 0 || i == longSegs ? 0.0f : iDivLong * 2.0f * MiniMath.PI;
            for (int j = 0; j < latVerts; j++) {
                float jDivLat = (float) j / (float) latSegs;
                float phi = jDivLat * MiniMath.PI;

                // Normal
                verts[i * latVerts * 8 + j * 8 + 3] = (float) Math.cos(theta) * (float) Math.sin(phi);
                verts[i * latVerts * 8 + j * 8 + 4] = (float) Math.cos(phi);
                verts[i * latVerts * 8 + j * 8 + 5] = (float) Math.sin(theta) * (float) Math.sin(phi);

                // Position
                verts[i * latVerts * 8 + j * 8] = verts[i * latVerts * 8 + j * 8 + 3] * radius;
                verts[i * latVerts * 8 + j * 8 + 1] = verts[i * latVerts * 8 + j * 8 + 4] * radius;
                verts[i * latVerts * 8 + j * 8 + 2] = verts[i * latVerts * 8 + j * 8 + 5] * radius;

                // Texture2D coordinates
                verts[i * latVerts * 8 + j * 8 + 6] = iDivLong;
                verts[i * latVerts * 8 + j * 8 + 7] = jDivLat;
            }
        }

        // Build triangles
        int triCount = longSegs * latSegs * 2;
        int elementCount = triCount * 3;
        tris = new short[elementCount];
        int index = 0;
        for (int i = 0; i < longSegs; i++)
            for (int j = 0; j < latSegs; j++) {
                // Vertex indices
                int v0 = j + latVerts * i;
                int v1 = j + latVerts * (i + 1);
                int v2 = v1 + 1;
                int v3 = v0 + 1;

                // First triangle
                tris[index++] = (short) v0;
                tris[index++] = (short) v1;
                tris[index++] = (short) v2;

                // Second triangle
                tris[index++] = (short) v0;
                tris[index++] = (short) v2;
                tris[index++] = (short) v3;
            }
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
            indexBuffer.clear();
            indexBuffer = null;

            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, posBuffer.capacity() * GridMesh.FloatBytes, posBuffer,
                    GLES30.GL_STATIC_DRAW);
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

            posBuffer = null;
            indexBuffer = null;
            uploadedVBO = true;
            indexArraySize = tris.length;
        }
    }


    private void bindAttribs(GLSLProgram shader) {
        //glVertexAttribPointer(int indx, int size, int type, boolean normalized, int stride, int offset)

        int positionHandle=shader.getAttributeGLid("a_Position");
        int normalHandle=shader.getAttributeGLid("a_Normal");
        int texcoordHandle=shader.getAttributeGLid("a_TexCoordinate");

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);

        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, FloatBytes * 8, 0);
        GLES30.glEnableVertexAttribArray(positionHandle);

        GLES30.glVertexAttribPointer(normalHandle,   3, GLES30.GL_FLOAT, false, FloatBytes * 8, FloatBytes * 3);
        GLES30.glEnableVertexAttribArray(normalHandle);

        GLES30.glVertexAttribPointer(texcoordHandle, 2, GLES30.GL_FLOAT, false, FloatBytes * 8, FloatBytes * 6);
        GLES30.glEnableVertexAttribArray(texcoordHandle);

        //index buffer
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);
    }

    @Override
    public void draw() {
        transform.updateModelMatrix();
        material.bindTextures();
        sendMatrices();
        bindAttribs(material.shader);

        GLES30.glDrawElements(GLES30.GL_TRIANGLES, indexArraySize, GLES30.GL_UNSIGNED_SHORT, 0);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    private void sendMatrices() {
        Matrix.multiplyMM(MatrixManager.modelViewMatrix, 0, MatrixManager.viewMatrix, 0,
                transform.modelMatrix, 0);

        Matrix.multiplyMM(MatrixManager.MVPMatrix, 0, MatrixManager.projectionMatrix, 0,
                MatrixManager.modelViewMatrix, 0);

        ShaderUniformMatrix4fv ModelMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_Modelmatrix");
        ShaderUniformMatrix4fv MVPMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_MVPMatrix");

        ModelMatrix.array=transform.modelMatrix;
        MVPMatrix.array=MatrixManager.MVPMatrix;

        ModelMatrix.bind();
        MVPMatrix.bind();
    }

    public void invalidateVBO() {
        uploadedVBO = false;
    }
}
