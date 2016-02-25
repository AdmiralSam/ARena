package com.samuel.arena.framework.graphics;

import android.graphics.Bitmap;
import android.opengl.GLUtils;
import android.util.Log;

import com.samuel.arena.framework.core.Disposable;

import static android.opengl.GLES20.GL_LINEAR;
import static android.opengl.GLES20.GL_TEXTURE_2D;
import static android.opengl.GLES20.GL_TEXTURE_MAG_FILTER;
import static android.opengl.GLES20.GL_TEXTURE_MIN_FILTER;
import static android.opengl.GLES20.glActiveTexture;
import static android.opengl.GLES20.glBindTexture;
import static android.opengl.GLES20.glDeleteTextures;
import static android.opengl.GLES20.glGenTextures;
import static android.opengl.GLES20.glTexParameteri;

/**
 * Created by SamuelDong on 2/9/16.
 */
public class Texture implements Disposable {
    private int textureID;
    private int width;
    private int height;
    private boolean disposed;

    public Texture(Bitmap texture) {
        width = texture.getWidth();
        height = texture.getHeight();

        int[] textureID = new int[1];
        glGenTextures(1, textureID, 0);
        glBindTexture(GL_TEXTURE_2D, textureID[0]);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        GLUtils.texImage2D(GL_TEXTURE_2D, 0, texture, 0);
        texture.recycle();

        this.textureID = textureID[0];
        disposed = false;
    }

    public void bindToUnit(int activeTextureUnit) {
        if (!disposed) {
            glActiveTexture(activeTextureUnit);
            glBindTexture(GL_TEXTURE_2D, textureID);
        } else {
            Log.e("Texture", "Cannot use a disposed texture");
        }
    }

    public float getUVX(int pixelX) {
        return (float) pixelX / width;
    }

    public float getUVY(int pixelY) {
        return (float) pixelY / height;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    @Override
    public void dispose() {
        glDeleteTextures(1, new int[]{textureID}, 0);
        disposed = true;
    }
}
