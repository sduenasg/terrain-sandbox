
package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.sdgapps.terrainsandbox.MiniEngine.MatrixManager;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.GLSLProgram;
import com.sdgapps.terrainsandbox.MiniEngine.graphics.glsl.ShaderUniformMatrix4fv;
import com.sdgapps.terrainsandbox.SimpleVec3fPool;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Simple bounding box class
 */
public class BoundingBox {

    public Vec3f bMin;
    public Vec3f bMax;
    private boolean renderable = false;

    public BoundingBox() {
    }

    public BoundingBox(float minx, float miny, float minz, float maxx, float maxy, float maxz) {
        bMin = new Vec3f();
        bMax = new Vec3f();
        bMin.x = minx;
        bMin.y = miny;
        bMin.z = minz;

        bMax.x = maxx;
        bMax.y = maxy;
        bMax.z = maxz;
        calcCenter();
    }

    public Vec3f getP(Vec3f normal) {
        Vec3f positive = SimpleVec3fPool.create(bMin);
        if (normal.x > 0)
            positive.x = bMax.x;

        if (normal.y > 0)
            positive.y = bMax.y;

        if (normal.z > 0)
            positive.z = bMax.z;

        return positive;
    }

    public Vec3f getN(Vec3f normal) {
        Vec3f negative = SimpleVec3fPool.create(bMin);
        if (normal.x < 0)
            negative.x = bMax.x;

        if (normal.y < 0)
            negative.y = bMax.y;

        if (normal.z < 0)
            negative.z = bMax.z;

        return negative;
    }

    /**
     * Expands the box to include p
     *
     * @param p point to be wrapped by the box
     */
    public void expand(Vec3f p) {
        if (this.bMin == null) {
            this.bMin = new Vec3f(p);
            this.bMax = new Vec3f(p);
        } else {
            if (p.x > bMax.x)
                bMax.x = p.x;
            else if (p.x < bMin.x)
                bMin.x = p.x;

            if (p.y > bMax.y)
                bMax.y = p.y;
            else if (p.y < bMin.y)
                bMin.y = p.y;

            if (p.z > bMax.z)
                bMax.z = p.z;
            else if (p.z < bMin.z)
                bMin.z = p.z;
        }
    }

    public void expand(float x, float y, float z) {

        if (this.bMin == null) {
            this.bMin = new Vec3f(x, y, z);
            this.bMax = new Vec3f(x, y, z);
        } else {
            if (x > bMax.x)
                bMax.x = x;
            else if (x < bMin.x)
                bMin.x = x;

            if (y > bMax.y)
                bMax.y = y;
            else if (y < bMin.y)
                bMin.y = y;

            if (z > bMax.z)
                bMax.z = z;
            else if (z < bMin.z)
                bMin.z = z;
        }
    }


    /**
     * Checks wether point p is inside the bounding box
     *
     * @param p point
     * @return true-> inside, false->outside
     */
    public boolean isPointInside(Vec3f p) {
        if (bMin.x <= p.x && p.x <= bMax.x && bMin.y <= p.y && p.y <= bMax.y && bMin.z <= p.z
                && p.z <= bMax.z)
            return true;

        return false;
    }

    /**
     * Checks wether point p is inside the bounding box
     *
     * @param p point
     * @return true-> inside, false->outside
     */
    private boolean isPointInside_OPT(Vec3f p) {
        if (bMin.x > p.x || p.x > bMax.x || bMin.y > p.y || p.y > bMax.y || bMin.z > p.z
                || p.z > bMax.z)
            return false;

        return true;

    }

    public boolean isPointInside(float x, float y, float z) {
        return bMin.x <= x && x <= bMax.x && bMin.y <= y && y <= bMax.y && bMin.z <= z && z <= bMax.z;

    }

    private Vec3f calcCenter() {
        Vec3f center = new Vec3f();
        center.x = (bMax.x + bMin.x) / 2f;
        center.y = (bMax.y + bMin.y) / 2f;
        center.z = (bMax.z + bMin.z) / 2f;

        return center;
    }

    /**
     * Divides the box in 8 sub-boxes
     */
    public BoundingBox[] subdivide8() {
        BoundingBox[] result = new BoundingBox[8];

        Vec3f center = calcCenter();

        result[0] = new BoundingBox(bMin.x, center.y, bMin.z, center.x, bMax.y, center.z); // A
        result[1] = new BoundingBox(center.x, center.y, bMin.z, bMax.x, bMax.y, center.z); // B
        result[2] = new BoundingBox(bMin.x, bMin.y, bMin.z, center.x, center.y, center.z); // C
        result[3] = new BoundingBox(center.x, bMin.y, bMin.z, bMax.x, center.y, center.z); // D

        result[4] = new BoundingBox(bMin.x, center.y, center.z, center.x, bMax.y, bMax.z); // e
        result[5] = new BoundingBox(center.x, center.y, center.z, bMax.x, bMax.y, bMax.z); // f
        result[6] = new BoundingBox(bMin.x, bMin.y, center.z, center.x, center.y, bMax.z); // g
        result[7] = new BoundingBox(center.x, bMin.y, center.z, bMax.x, center.y, bMax.z); // h

        return result;
    }

    /**
     * Divides the box in 4 sub-boxes
     */
    public BoundingBox[] subdivide4() {
        BoundingBox[] result = new BoundingBox[4];
        Vec3f center = calcCenter();

        result[0] = new BoundingBox(bMin.x, bMin.y, bMin.z, center.x, bMax.y, center.z);
        result[1] = new BoundingBox(center.x, bMin.y, bMin.z, bMax.x, bMax.y, center.z);
        result[2] = new BoundingBox(bMin.x, bMin.y, center.z, center.x, bMax.y, bMax.z);
        result[3] = new BoundingBox(center.x, bMin.y, center.z, bMax.x, bMax.y, bMax.z);
        return result;
    }

    /**
     * Checks for intersection between a sphere and this box
     *
     * @param scenter sphere center
     * @param rad     sphere radius
     * @returns true->intersects, false-> doesn't intersect
     */

    public boolean intersectsSphere(Vec3f scenter, float rad) {
        if (this.isPointInside_OPT(scenter))
            return true;

        Vec3f center = calcCenter();
        Vec3f boxRadius = new Vec3f();
        boxRadius.set(bMax);
        boxRadius.sub(center);

        final float sqrad = rad * rad;
        Vec3f boxToSphere = new Vec3f(scenter);
        boxToSphere.sub(center);

        // Look for the closest point from the box to the sphere
        // X axis
        Vec3f boxPoint = new Vec3f();
        if (boxToSphere.x < -boxRadius.x)
            boxPoint.x = -boxRadius.x;
        else if (boxToSphere.x > boxRadius.x)
            boxPoint.x = boxRadius.x;
        else
            boxPoint.x = boxToSphere.x;

        /* Bail early */
        float xsq = boxPoint.x * boxPoint.x;
        if (xsq >= sqrad)
            return false;

        // Y axis
        if (boxToSphere.y < -boxRadius.y)
            boxPoint.y = -boxRadius.y;
        else if (boxToSphere.y > boxRadius.y)
            boxPoint.y = boxRadius.y;
        else
            boxPoint.y = boxToSphere.y;

        /* Bail early*/
        float xysq = boxPoint.y * boxPoint.y + xsq;
        if (xysq >= sqrad)
            return false;

        // Z Axis
        if (boxToSphere.z < -boxRadius.z)
            boxPoint.z = -boxRadius.z;
        else if (boxToSphere.z > boxRadius.z)
            boxPoint.z = boxRadius.z;
        else
            boxPoint.z = boxToSphere.z;

        float zsq = boxPoint.z * boxPoint.z;

        /*boxpoint is the box point that is closest to the sphere. If the distance is shorter than
        the sphere radius then there is an intersection.
        */
        if (xysq + zsq < sqrad)
            return true;
        return false;
    }


    public String toString() {
        return "bMin: " + bMin.toString() + " bMax: " + bMax.toString();
    }

    /**
     * Box rendering
     */
    private FloatBuffer mLineVertices;

    public void draw(GLSLProgram shader) {

        if (renderable) {
            GLES20.glVertexAttribPointer(shader.positionHandle, 3, GLES20.GL_FLOAT, false, 0, mLineVertices);
            GLES20.glEnableVertexAttribArray(shader.positionHandle);

            GLES20.glLineWidth(6);

            sendMatrix(shader);

            GLES20.glDrawArrays(GLES20.GL_LINES, 0, 24);
        }
    }

    private void sendMatrix(GLSLProgram shader)
    {
        Matrix.setIdentityM(MatrixManager.modelMatrix, 0);
        Matrix.multiplyMM(MatrixManager.modelViewMatrix, 0, MatrixManager.viewMatrix, 0,
                MatrixManager.modelMatrix, 0);

        Matrix.multiplyMM(MatrixManager.MVPMatrix, 0, MatrixManager.projectionMatrix, 0,
                MatrixManager.modelViewMatrix, 0);

        ShaderUniformMatrix4fv MVPMatrix= (ShaderUniformMatrix4fv) shader.getUniform("u_MVPMatrix");
        MVPMatrix.array=MatrixManager.MVPMatrix;
        MVPMatrix.bind();
    }

    public void prepareForRendering() {

        final float[] lineVertData = {
                // X, Y, Z,
                bMin.x, bMin.y, bMin.z, bMin.x, bMin.y, bMax.z,
                bMin.x, bMin.y, bMin.z, bMin.x, bMax.y, bMin.z,
                bMin.x, bMin.y, bMin.z, bMax.x, bMin.y, bMin.z,
                //
                bMin.x, bMin.y, bMax.z, bMin.x, bMax.y, bMax.z,
                bMin.x, bMin.y, bMax.z, bMax.x, bMin.y, bMax.z,
                //
                bMin.x, bMax.y, bMin.z, bMax.x, bMax.y, bMin.z,
                bMin.x, bMax.y, bMin.z, bMin.x, bMax.y, bMax.z,
                //
                bMax.x, bMin.y, bMin.z, bMax.x, bMin.y, bMax.z,
                bMax.x, bMin.y, bMin.z, bMax.x, bMax.y, bMin.z,
                //
                bMax.x, bMin.y, bMax.z, bMax.x, bMax.y, bMax.z,
                //
                bMax.x, bMax.y, bMax.z, bMax.x, bMax.y, bMin.z,
                //
                bMax.x, bMax.y, bMax.z, bMin.x, bMax.y, bMax.z,
        };

        mLineVertices = ByteBuffer.allocateDirect(lineVertData.length * 4)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        mLineVertices.put(lineVertData).position(0);
        mLineVertices.position(0);
        renderable = true;
    }

    public void updateBoxValues(Transform transform, Transform planetTransform) {

        //translate the box to the terrain's object space
        bMax.sub(transform.objectPivotPosition);
        bMin.sub(transform.objectPivotPosition);

        //rotate the max and min points
        transform.rotation.multLocal(bMax);
        transform.rotation.multLocal(bMin);

        //translate the box back to it's original position
        bMax.add(transform.objectPivotPosition);
        bMin.add(transform.objectPivotPosition);

        //translate it to the final terrain position
        bMax.add(transform.position);
        bMin.add(transform.position);

        //Apply planet transform
        //translate the box to the terrain's object space
        bMax.sub(planetTransform.objectPivotPosition);
        bMin.sub(planetTransform.objectPivotPosition);

        //apply planet rotation
        planetTransform.rotation.multLocal(bMax);
        planetTransform.rotation.multLocal(bMin);

        bMax.add(planetTransform.objectPivotPosition);
        bMin.add(planetTransform.objectPivotPosition);

        //the box rotation might mess up max/min values, swap them if necessary
        rearrangeMaxMin();

        //regenerate rendering data for the box
        prepareForRendering();
    }

    private void rearrangeMaxMin() {
        float aux;
        if (bMax.x < bMin.x) {
            aux = bMax.x;
            bMax.x = bMin.x;
            bMin.x = aux;
        }
        if (bMax.y < bMin.y) {
            aux = bMax.y;
            bMax.y = bMin.y;
            bMin.y = aux;
        }
        if (bMax.z < bMin.z) {
            aux = bMax.z;
            bMax.z = bMin.z;
            bMin.z = aux;
        }
    }
}
