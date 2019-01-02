package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.GLES30;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

public class LineCube {

    public static final int FloatBytes = Float.SIZE / 8;
    public static final int IntBytes = Integer.SIZE / 8;
    public static final int ShortBytes = Short.SIZE / 8;

    boolean uploadedVBO =false;
    private int buffers[];

    private int indexArraySize=0;

    public void bindAttributes(GLSLProgram shader) {

        int positionHandle=shader.getAttributeGLid("a_Position");

        //position buffer
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
        GLES30.glVertexAttribPointer(positionHandle, 3, GLES30.GL_FLOAT, false, 0, 0);
        GLES30.glEnableVertexAttribArray(positionHandle);

        //index buffer
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);

        GLES30.glLineWidth(6);
    }

    public void draw(GLSLProgram shader, Vec3f position, Vec3f scale) {

        if (uploadedVBO) {

            sendMatrix(shader,position,scale);
            GLES30.glDrawElements(GLES30.GL_LINES, indexArraySize, GLES30.GL_UNSIGNED_INT, 0);
        }
    }

    private void sendMatrix(GLSLProgram shader, Vec3f position, Vec3f scale)
    {
        Matrix.setIdentityM(MatrixManager.modelMatrix, 0);
        Matrix.translateM(MatrixManager.modelMatrix,0,position.x,position.y,position.z);
        Matrix.scaleM(MatrixManager.modelMatrix,0, scale.x,scale.y,scale.z);

        Matrix.multiplyMM(MatrixManager.modelViewMatrix, 0, MatrixManager.viewMatrix, 0,
                MatrixManager.modelMatrix, 0);

        Matrix.multiplyMM(MatrixManager.MVPMatrix, 0, MatrixManager.projectionMatrix, 0,
                MatrixManager.modelViewMatrix, 0);

        ShaderUniformMatrix4fv MVPMatrix= (ShaderUniformMatrix4fv) shader.getUniform("u_MVPMatrix");
        MVPMatrix.array=MatrixManager.MVPMatrix;
        MVPMatrix.bind();
    }

    public void initializeVisuals() {

        final float[] lineVertData = {
                -1,-1,-1, //0
                -1,-1, 1, //1
                -1, 1,-1, //2
                -1, 1, 1, //3

                 1, 1, 1, //4
                 1, 1,-1, //5
                 1,-1,-1, //6
                 1,-1, 1  //7
        };

        final int[] index_array = {
                    0,1,
                    0,2,
                    0,6,

                    1,3,
                    1,7,

                    2,5,
                    2,3,

                    6,7,
                    6,5,

                    7,4,

                    4,5,
                    4,3
        };
        indexArraySize = index_array.length;

        IntBuffer indexBuffer = ByteBuffer.allocateDirect(index_array.length * IntBytes)
                .order(ByteOrder.nativeOrder()).asIntBuffer();
        indexBuffer.put(index_array);
        indexBuffer.position(0);

        FloatBuffer mLineVertices = ByteBuffer.allocateDirect(lineVertData.length * FloatBytes)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mLineVertices.put(lineVertData).position(0);

        buffers = new int[2];
        GLES30.glGenBuffers(2, buffers, 0);

        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, buffers[0]);
        GLES30.glBufferData(GLES30.GL_ELEMENT_ARRAY_BUFFER,
                indexBuffer.capacity() * IntBytes, indexBuffer,
                GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ELEMENT_ARRAY_BUFFER, 0);

        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, buffers[1]);
        GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER,  mLineVertices.capacity() * FloatBytes, mLineVertices,
                GLES30.GL_STATIC_DRAW);
        GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);

        uploadedVBO =true;
    }
}
