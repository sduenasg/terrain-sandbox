package com.sdgapps.terrainsandbox.MiniEngine.graphics;

import com.sdgapps.terrainsandbox.SimpleVec3fPool;

public final class Quaternion {
    public float x;
    public float y;
    public float z;
    public float w;

    // Identity Quaternion
    public Quaternion() {
        x = 0;
        y = 0;
        z = 0;
        w = 1;
    }

    public float[] getQuaternionAsArray() {
        float[] res = new float[4];
        res[0] = x;
        res[1] = y;
        res[2] = z;
        res[3] = w;
        return res;
    }

    public void setFromArray(float[] fromArray) {
        if (fromArray != null) {
            x = fromArray[0];
            y = fromArray[1];
            z = fromArray[2];
            w = fromArray[3];
        }
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public float getW() {
        return w;
    }

    public Quaternion set(Quaternion other) {
        this.x = other.x;
        this.y = other.y;
        this.z = other.z;
        this.w = other.w;
        return this;
    }

    public Quaternion set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
        return this;
    }

    /**
     * Sets this Quaternion to {0, 0, 0, 1}
     */
    public void loadIdentity() {
        x = y = z = 0;
        w = 1;
    }

    public boolean isIdentity() {
        if (x == 0 && y == 0 && z == 0 && w == 1)
            return true;
        else
            return false;
    }

    /**
     * <code>fromRotationMatrix</code> sets this quaternion from a supplied
     * matrix. This matrix is assumed to be a rotational matrix.
     */
    public Quaternion fromRotationMatrix(float m00, float m01, float m02, float m10, float m11,
                                         float m12, float m20, float m21, float m22) {

        // first normalize the forward (F), up (U) and side (S) vectors of the rotation matrix
        // so that the scale does not affect the rotation
        float lengthSquared = m00 * m00 + m10 * m10 + m20 * m20;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = (float) (1.0f / Math.sqrt(lengthSquared));
            m00 *= lengthSquared;
            m10 *= lengthSquared;
            m20 *= lengthSquared;
        }
        lengthSquared = m01 * m01 + m11 * m11 + m21 * m21;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = (float) (1.0f / Math.sqrt(lengthSquared));
            m01 *= lengthSquared;
            m11 *= lengthSquared;
            m21 *= lengthSquared;
        }
        lengthSquared = m02 * m02 + m12 * m12 + m22 * m22;
        if (lengthSquared != 1f && lengthSquared != 0f) {
            lengthSquared = (float) (1.0f / Math.sqrt(lengthSquared));
            m02 *= lengthSquared;
            m12 *= lengthSquared;
            m22 *= lengthSquared;
        }
        // the trace is the sum of the diagonal elements; see
        // http://mathworld.wolfram.com/MatrixTrace.html

        float t = m00 + m11 + m22; // diagonal

        // we protect the division by s by ensuring that s>=1
        if (t >= 0) { // |w| >= .5
            float s = (float) Math.sqrt(t + 1); // |s|>=1 ...
            w = 0.5f * s;
            s = 0.5f / s;
            x = (m21 - m12) * s;
            y = (m02 - m20) * s;
            z = (m10 - m01) * s;
        } else if ((m00 > m11) && (m00 > m22)) {
            float s = (float) Math.sqrt(1.0f + m00 - m11 - m22); // |s|>=1
            x = s * 0.5f; // |x| >= .5
            s = 0.5f / s;
            y = (m10 + m01) * s;
            z = (m02 + m20) * s;
            w = (m21 - m12) * s;
        } else if (m11 > m22) {
            float s = (float) Math.sqrt(1.0f + m11 - m00 - m22); // |s|>=1
            y = s * 0.5f; // |y| >= .5
            s = 0.5f / s;
            x = (m10 + m01) * s;
            z = (m21 + m12) * s;
            w = (m02 - m20) * s;
        } else {
            float s = (float) Math.sqrt(1.0f + m22 - m00 - m11); // |s|>=1
            z = s * 0.5f; // |z| >= .5
            s = 0.5f / s;
            x = (m02 + m20) * s;
            y = (m21 + m12) * s;
            w = (m10 - m01) * s;
        }

        return this;
    }

    /**
     * <code>fromAngleNormalAxis</code> sets this quaternion to the values specified by an angle and
     * a normalized axis of rotation.
     *
     * @param angle the angle to rotate (in radians).
     * @param axis  the axis of rotation (already normalized).
     */
    public Quaternion fromAngleNormalAxis(float angle, Vec3f axis) {
        if (axis.x == 0 && axis.y == 0 && axis.z == 0) {
            loadIdentity();
        } else {
            float halfAngle = 0.5f * angle;
            float sin = (float) Math.sin(halfAngle);
            w = (float) Math.cos(halfAngle);
            x = sin * axis.x;
            y = sin * axis.y;
            z = sin * axis.z;
        }
        return this;
    }

    /**
     * <code>toAngleAxis</code> sets a given angle and axis to that represented by the current
     * quaternion. The values are stored as following: The axis is provided as a parameter and built
     * by the method, the angle is returned as a float.
     *
     * @param axisStore the object we'll store the computed axis in.
     * @return the angle of rotation in radians.
     */
    public float toAngleAxis(Vec3f axisStore) {
        float sqrLength = x * x + y * y + z * z;
        float angle;
        if (sqrLength == 0.0f) {
            angle = 0.0f;
            if (axisStore != null) {
                axisStore.x = 1.0f;
                axisStore.y = 0.0f;
                axisStore.z = 0.0f;
            }
        } else {
            angle = (float) (2.0f * Math.acos(w));
            if (axisStore != null) {
                float invLength = (float) (1.0f / Math.sqrt(sqrLength));
                axisStore.x = x * invLength;
                axisStore.y = y * invLength;
                axisStore.z = z * invLength;
            }
        }

        return angle;
    }

    /**
     * <code>mult</code> multiplies this quaternion by a parameter quaternion. The result is
     * returned as a new quaternion. It should be noted that quaternion multiplication is not
     * commutative so q * p != p * q. It IS safe for q and res to be the same object. It IS safe for
     * this and res to be the same object.
     *
     * @param q   the quaternion to multiply this quaternion by.
     * @param res the quaternion to store the result in.
     * @return the new quaternion.
     */
    public Quaternion mult(Quaternion q, Quaternion res) {
        if (res == null)
            res = new Quaternion();
        float qw = q.w, qx = q.x, qy = q.y, qz = q.z;
        res.x = x * qw + y * qz - z * qy + w * qx;
        res.y = -x * qz + y * qw + z * qx + w * qy;
        res.z = x * qy - y * qx + z * qw + w * qz;
        res.w = -x * qx - y * qy - z * qz + w * qw;
        return res;
    }

    public void multLocal(Quaternion q) {

        float ox, oy, oz, ow;
        ox = x;
        oy = y;
        oz = z;
        ow = w;
        float qw = q.w, qx = q.x, qy = q.y, qz = q.z;
        x = ox * qw + oy * qz - oz * qy + ow * qx;
        y = -ox * qz + oy * qw + oz * qx + ow * qy;
        z = ox * qy - oy * qx + oz * qw + ow * qz;
        w = -ox * qx - oy * qy - oz * qz + ow * qw;
    }

    /**
     * <code>fromAxes</code> creates a <code>Quaternion</code> that represents the coordinate system
     * defined by three axes. These axes are assumed to be orthogonal and no error checking is
     * applied. Thus, the user must insure that the three axes being provided indeed represents a
     * proper right handed coordinate system.
     *
     * @param xAxis vector representing the x-axis of the coordinate system.
     * @param yAxis vector representing the y-axis of the coordinate system.
     * @param zAxis vector representing the z-axis of the coordinate system.
     */
    public Quaternion fromAxes(Vec3f xAxis, Vec3f yAxis, Vec3f zAxis) {
        return fromRotationMatrix(xAxis.x, yAxis.x, zAxis.x, xAxis.y, yAxis.y, zAxis.y, xAxis.z,
                yAxis.z, zAxis.z);
    }

    public Vec3f getZAxis() {
        return getColumn2();
    }

    public Vec3f getYAxis() {
        return getColumn1();
    }

    public Vec3f getXAxis() {
        return getColumn0();
    }


    public Vec3f getColumn2() {
        Vec3f result = SimpleVec3fPool.create();
        float norm = norm();
        // we explicitly test norm against one here, saving a division
        // at the cost of a test and branch. Is it worth it?
        float s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;

        // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        float xx = x * xs;
        float xz = x * zs;
        float xw = w * xs;
        float yy = y * ys;
        float yz = y * zs;
        float yw = w * ys;

        result.x = (xz + yw);
        result.y = (yz - xw);
        result.z = 1 - (xx + yy);

        return result;
    }

    public Vec3f getColumn1() {
        Vec3f result = SimpleVec3fPool.create();
        float norm = norm();
        // we explicitly test norm against one here, saving a division
        // at the cost of a test and branch. Is it worth it?
        float s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;

        // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        float xs = x * s;
        float ys = y * s;
        float zs = z * s;
        float xx = x * xs;
        float xy = x * ys;
        float xw = w * xs;
        float yz = y * zs;
        float zz = z * zs;
        float zw = w * zs;

        // using s=2/norm (instead of 1/norm) saves 9 multiplications by 2 here

        result.x = (xy - zw);
        result.y = 1 - (xx + zz);
        result.z = (yz + xw);
        return result;
    }

    public Vec3f getColumn0() {
        Vec3f result = SimpleVec3fPool.create();
        float norm = norm();
        // we explicitly test norm against one here, saving a division
        // at the cost of a test and branch. Is it worth it?
        float s = (norm == 1f) ? 2f : (norm > 0f) ? 2f / norm : 0;

        // compute xs/ys/zs first to save 6 multiplications, since xs/ys/zs
        // will be used 2-4 times each.
        float ys = y * s;
        float zs = z * s;
        float xy = x * ys;
        float xz = x * zs;
        float yy = y * ys;
        float yw = w * ys;
        float zz = z * zs;
        float zw = w * zs;

        // using s=2/norm (instead of 1/norm) saves 9 multiplications by 2 here
        result.x = 1 - (yy + zz);
        result.y = (xy + zw);
        result.z = (xz - yw);
        return result;
    }

    /**
     * <code>mult</code> multiplies this quaternion by a parameter vector. The result is stored in
     * the supplied vector
     *
     * @param v the vector to multiply this quaternion by.
     * @return v
     */
    public Vec3f multLocal(Vec3f v) {
        float tempX, tempY;
        tempX = w * w * v.x + 2 * y * w * v.z - 2 * z * w * v.y + x * x * v.x + 2 * y * x * v.y + 2
                * z * x * v.z - z * z * v.x - y * y * v.x;
        tempY = 2 * x * y * v.x + y * y * v.y + 2 * z * y * v.z + 2 * w * z * v.x - z * z * v.y + w
                * w * v.y - 2 * x * w * v.z - x * x * v.y;
        v.z = 2 * x * z * v.x + 2 * y * z * v.y + z * z * v.z - 2 * w * y * v.x - y * y * v.z + 2
                * w * x * v.y - x * x * v.z + w * w * v.z;
        v.x = tempX;
        v.y = tempY;
        return v;
    }

    /**
     * <code>norm</code> returns the norm of this quaternion. This is the dot product of this
     * quaternion with itself.
     *
     * @return the norm of the quaternion.
     */
    public float norm() {
        return w * w + x * x + y * y + z * z;
    }

    /**
     * <code>normalize</code> normalizes the current <code>Quaternion</code>
     */
    public void normalize() {
        float n = MiniMath.fastInvSqrt(norm());
        x *= n;
        y *= n;
        z *= n;
        w *= n;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ", " + z + ", " + w + ")";
    }

    /**
     * <code>lookAt</code> is a convienence method for auto-setting the quaternion based on a
     * direction and an up vector. It computes the rotation to transform the z-axis to point into
     * 'direction' and the y-axis to 'up'.
     *
     * @param direction where to look at in terms of local coordinates
     * @param up        a vector indicating the local up direction. (typically {0, 1, 0} in jME.)
     */

    public void lookAt(Vec3f direction, Vec3f up) {
        Vec3f newDirection = SimpleVec3fPool.create(direction);
        newDirection.normalize();

        up = up.calcCross(newDirection);
        up.normalize();

        Vec3f right = SimpleVec3fPool.create(newDirection);
        right = right.calcCross(up);
        right.normalize();

        fromAxes(up, right, newDirection);
    }


}
