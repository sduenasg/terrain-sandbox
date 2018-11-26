package com.sdgapps.terrainsandbox.MiniEngine.behaviours;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.RenderPackage;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.FrameBufferInterface;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.Material;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class CircleBillboard extends Renderer {
    private FloatBuffer mLineVerts;
    private boolean renderable=false;
    private int scale = 1;
    private int vertexCount;

    public CircleBillboard(int verts, FrameBufferInterface fb, Material _material, int _scale)
    {
        vertexCount=verts;
        this.material=_material;
        RenderPackage pass = new RenderPackage(fb, material.shader);
        renderPackages.add(pass);
        scale=_scale;
    }

    @Override
    public void draw() {
        if (!renderable)
            prepareForRendering();

        if (renderPackages.size() > 0) {
            RenderPackage pass = renderPackages.get(0);
            pass.targetFB.bind();
            pass.targetProgram.useProgram();

            material.bindShader();
            int positionHandle=material.shader.getAttributeGLid("a_Position");
            GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, mLineVerts);
            GLES20.glEnableVertexAttribArray(positionHandle);

            sendMatrices();
            //Draw
            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, vertexCount);
        }
    }

    private void sendMatrices()
    {
        Matrix.setIdentityM(MatrixManager.modelMatrix, 0);

        // Multiplies model matrix with view matrix
        Matrix.multiplyMM(MatrixManager.modelViewMatrix, 0, MatrixManager.viewMatrix, 0,
                transform.modelMatrix, 0);

        ShaderUniformMatrix4fv MVMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_MVMatrix");
        ShaderUniformMatrix4fv ProjectionMatrix= (ShaderUniformMatrix4fv) material.shader.getUniform("u_Projectionmatrix");

        MVMatrix.array=MatrixManager.modelViewMatrix;
        ProjectionMatrix.array=MatrixManager.projectionMatrix;
        MVMatrix.bind();
        ProjectionMatrix.bind();
    }


    public void prepareForRendering() {
        /*Build a triangle fan (circle shape)*/
        float radius = scale;
        float center_x = 0.0f;
        float center_y = 0.0f;
        float center_z = 0.0f;

        float lineVertData[] = new float[vertexCount * 3]; // (x,y) for each vertex
        int idx = 0;

        // Center vertex for triangle fan
        lineVertData[idx++] = center_x;
        lineVertData[idx++] = center_y;
        lineVertData[idx++] = center_z;

        // Outer vertices of the circle
        int outerVertexCount = vertexCount - 1;

        for (int i = 0; i < outerVertexCount; ++i) {
            float percent = (i / (float) (outerVertexCount - 1));
            float rad = (float) (percent * 2 * Math.PI);

            //Vertex position
            float outer_x = (float) (center_x + radius * Math.cos(rad));
            float outer_y = (float) (center_y + radius * Math.sin(rad));
            float outer_z = 0;
            lineVertData[idx++] = outer_x;
            lineVertData[idx++] = outer_y;
            lineVertData[idx++] = outer_z;
        }

        /* end CIRCLE*/
        mLineVerts = ByteBuffer.allocateDirect(lineVertData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mLineVerts.put(lineVertData).position(0);
        renderable = true;
    }
}
