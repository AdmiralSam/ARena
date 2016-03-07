package com.samuel.arena.framework.graphics;

import android.util.Log;

import com.samuel.arena.framework.core.Disposable;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Samuel on 3/6/2016.
 */
public class Mesh implements Disposable {
    public static final String Tag = "Mesh";
    private final int positionBufferID;
    private final int uvBufferID;
    private final int normalBufferID;
    private final int indexBufferID;
    private final int vertexCount;
    private boolean disposed;

    public Mesh(float[] positions, float[] uvs, float[] normals, short[] indices) {
        vertexCount = indices.length;

        int[] bufferIDs = new int[4];
        glGenBuffers(4, bufferIDs, 0);
        positionBufferID = bufferIDs[0];
        uvBufferID = bufferIDs[1];
        normalBufferID = bufferIDs[2];
        indexBufferID = bufferIDs[3];

        glBindBuffer(GL_ARRAY_BUFFER, positionBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * positions.length, FloatBuffer.wrap(positions), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, uvBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * uvs.length, FloatBuffer.wrap(uvs), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, normalBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * normals.length, FloatBuffer.wrap(normals), GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, 2 * indices.length, ShortBuffer.wrap(indices), GL_STATIC_DRAW);

        disposed = false;
    }

    public void draw(ShaderProgram shader) {
        if (disposed) {
            Log.e(Tag, "Cannot use a disposed mesh");
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, positionBufferID);
            glVertexAttribPointer(shader.getAttributeLocation("position"), 4, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, uvBufferID);
            glVertexAttribPointer(shader.getAttributeLocation("uv"), 4, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, normalBufferID);
            glVertexAttribPointer(shader.getAttributeLocation("normal"), 4, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
            glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_SHORT, 0);
        }
    }

    @Override
    public void dispose() {
        glDeleteBuffers(4, new int[]{positionBufferID, uvBufferID, normalBufferID, indexBufferID}, 0);
        disposed = true;
    }
}
