package com.samuel.arena.framework.graphics;

import android.util.Log;

import com.samuel.arena.framework.core.Rectangle;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_DYNAMIC_DRAW;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Samuel on 2/9/2016.
 */
public class SpriteBatch {
    public static final String Tag = "SpriteBatch";
    private final int positionBufferID;
    private final int uvBufferID;
    private final int indexBufferID;
    private final List<Texture> textures;
    private final List<Rectangle> sourceRectangles;
    private final List<Rectangle> destinationRectangles;
    private final ShaderProgram defaultShader;
    private final int virtualWidth;
    private final int virtualHeight;
    private boolean active;
    private ShaderProgram spriteShader;

    public SpriteBatch(int virtualWidth, int virtualHeight, ShaderProgram defaultShader) {
        this.virtualWidth = virtualWidth;
        this.virtualHeight = virtualHeight;
        this.defaultShader = defaultShader;
        active = false;
        textures = new ArrayList<>();
        sourceRectangles = new ArrayList<>();
        destinationRectangles = new ArrayList<>();
        int[] bufferIDs = new int[3];
        glGenBuffers(3, bufferIDs, 0);
        positionBufferID = bufferIDs[0];
        uvBufferID = bufferIDs[1];
        indexBufferID = bufferIDs[2];
    }

    public void drawTexture(Texture texture, Rectangle destinationRectangle) {
        drawTexture(texture, new Rectangle(0, 0, texture.getWidth(), texture.getHeight()), destinationRectangle);
    }

    public void drawTexture(Texture texture, Rectangle sourceRectangle, Rectangle destinationRectangle) {
        if (active) {
            textures.add(texture);
            sourceRectangles.add(sourceRectangle);
            destinationRectangles.add(destinationRectangle);
        } else {
            Log.e(Tag, "Cannot draw texture when sprite batch has not begun");
        }
    }

    public void begin() {
        begin(defaultShader);
    }

    public void begin(ShaderProgram spriteShader) {
        if (active) {
            Log.e(Tag, "Cannot begin when sprite batch has not ended");
        } else {
            active = true;
            this.spriteShader = spriteShader;
            spriteShader.begin();
        }
    }

    public void end() {
        if (active) {
            int drawIndex = 0;
            glUniform1i(spriteShader.getUniformLocation("texture"), 0);
            while (drawIndex < textures.size()) {
                Texture currentTexture = textures.get(drawIndex);
                int batchCount = 1;
                for (int i = drawIndex + 1; i < textures.size(); i++) {
                    if (textures.get(i) == currentTexture) {
                        batchCount++;
                    } else {
                        break;
                    }
                }
                currentTexture.bindToUnit(GL_TEXTURE0);
                float[] positions = new float[16 * batchCount];
                float[] uvs = new float[8 * batchCount];
                short[] indices = new short[6 * batchCount];
                for (int i = 0; i < batchCount; i++) {
                    Rectangle destination = destinationRectangles.get(i + drawIndex);
                    Rectangle source = sourceRectangles.get(i + drawIndex);
                    insertPositions(positions, i, destination);
                    insertUVS(currentTexture, uvs, i, source);
                    insertIndices(indices, i);
                }
                drawBatch(batchCount, positions, uvs, indices);
                drawIndex += batchCount;
            }
            spriteShader.end();
            spriteShader = null;
            textures.clear();
            sourceRectangles.clear();
            destinationRectangles.clear();
            active = false;
        } else {
            Log.e(Tag, "Cannot end when sprite batch has not begun");
        }
    }

    private void drawBatch(int batchCount, float[] positions, float[] uvs, short[] indices) {
        glBindBuffer(GL_ARRAY_BUFFER, positionBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * positions.length, FloatBuffer.wrap(positions), GL_DYNAMIC_DRAW);
        glVertexAttribPointer(spriteShader.getAttributeLocation("position"), 4, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ARRAY_BUFFER, uvBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * uvs.length, FloatBuffer.wrap(uvs), GL_DYNAMIC_DRAW);
        glVertexAttribPointer(spriteShader.getAttributeLocation("uv"), 2, GL_FLOAT, false, 0, 0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, 2 * indices.length, ShortBuffer.wrap(indices), GL_DYNAMIC_DRAW);
        glDrawElements(GL_TRIANGLES, 6 * batchCount, GL_UNSIGNED_SHORT, 0);
    }

    private void insertIndices(short[] indices, int offset) {
        indices[6 * offset] = (short) (4 * offset);
        indices[6 * offset + 1] = (short) (4 * offset + 1);
        indices[6 * offset + 2] = (short) (4 * offset + 2);
        indices[6 * offset + 3] = (short) (4 * offset + 2);
        indices[6 * offset + 4] = (short) (4 * offset + 3);
        indices[6 * offset + 5] = (short) (4 * offset);
    }

    private void insertUVS(Texture currentTexture, float[] uvs, int offset, Rectangle source) {
        uvs[8 * offset] = currentTexture.getUVX(source.x);
        uvs[8 * offset + 1] = currentTexture.getUVY(source.y);
        uvs[8 * offset + 2] = currentTexture.getUVX(source.x);
        uvs[8 * offset + 3] = currentTexture.getUVY(source.y + source.height);
        uvs[8 * offset + 4] = currentTexture.getUVX(source.x + source.width);
        uvs[8 * offset + 5] = currentTexture.getUVY(source.y + source.height);
        uvs[8 * offset + 6] = currentTexture.getUVX(source.x + source.width);
        uvs[8 * offset + 7] = currentTexture.getUVY(source.y);
    }

    private void insertPositions(float[] positions, int offset, Rectangle destination) {
        positions[16 * offset] = 2.0f * destination.x / virtualWidth - 1.0f;
        positions[16 * offset + 1] = 2.0f * (virtualHeight - destination.y) / virtualHeight - 1.0f;
        positions[16 * offset + 2] = 0.0f;
        positions[16 * offset + 3] = 1.0f;
        positions[16 * offset + 4] = 2.0f * destination.x / virtualWidth - 1.0f;
        positions[16 * offset + 5] = 2.0f * (virtualHeight - destination.y - destination.height) / virtualHeight - 1.0f;
        positions[16 * offset + 6] = 0.0f;
        positions[16 * offset + 7] = 1.0f;
        positions[16 * offset + 8] = 2.0f * (destination.x + destination.width) / virtualWidth - 1.0f;
        positions[16 * offset + 9] = 2.0f * (virtualHeight - destination.y - destination.height) / virtualHeight - 1.0f;
        positions[16 * offset + 10] = 0.0f;
        positions[16 * offset + 11] = 1.0f;
        positions[16 * offset + 12] = 2.0f * (destination.x + destination.width) / virtualWidth - 1.0f;
        positions[16 * offset + 13] = 2.0f * (virtualHeight - destination.y) / virtualHeight - 1.0f;
        positions[16 * offset + 14] = 0.0f;
        positions[16 * offset + 15] = 1.0f;
    }
}
