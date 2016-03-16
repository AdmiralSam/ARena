package com.samuel.arena.framework.core;

import android.opengl.Matrix;

/**
 * Created by Samuel on 3/15/2016.
 */
public class Transform {
    public float x, y, z;
    public float scale;
    public float rotation;

    public Transform() {
        this(0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
    }

    public Transform(float x, float y, float z, float scale, float rotation) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.scale = scale;
        this.rotation = rotation;
    }

    public float[] getModelMatrix() {
        float[] translate = new float[16];
        float[] scale = new float[16];
        float[] rotate = new float[16];
        Matrix.setIdentityM(translate, 0);
        Matrix.setIdentityM(scale, 0);
        Matrix.setIdentityM(rotate, 0);
        Matrix.translateM(translate, 0, x, y, z);
        Matrix.scaleM(scale, 0, this.scale, this.scale, this.scale);
        Matrix.rotateM(rotate, 0, rotation, 0.0f, 0.0f, 1.0f);
        float[] scaleRotate = new float[16];
        float[] result = new float[16];
        Matrix.multiplyMM(scaleRotate, 0, scale, 0, rotate, 0);
        Matrix.multiplyMM(result, 0, translate, 0, scaleRotate, 0);
        return result;
    }

    public void translate(float dx, float dy, float dz) {
        x += dx;
        y += dy;
        z += dz;
    }
}
