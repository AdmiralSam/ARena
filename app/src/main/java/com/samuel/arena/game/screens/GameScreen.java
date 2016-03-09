package com.samuel.arena.game.screens;

import android.opengl.Matrix;

import com.samuel.arena.framework.core.ContentManager;
import com.samuel.arena.framework.core.Screen;
import com.samuel.arena.framework.graphics.Mesh;
import com.samuel.arena.framework.graphics.ShaderProgram;
import com.samuel.arena.framework.graphics.SpriteBatch;
import com.samuel.arena.framework.graphics.Texture;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Samuel on 3/6/2016.
 */
public class GameScreen extends Screen {
    //Testing
    private float[] projectionMatrix;
    private float[] viewMatrix;
    private float[] modelMatrix;
    private ShaderProgram testShader;
    private Mesh arena;
    private Texture arenaUV;
    //Testing

    public GameScreen(ContentManager content) {
        super(content);
        projectionMatrix = new float[16];
        Matrix.perspectiveM(projectionMatrix, 0, 40.0f, 16.0f / 9.0f, 0.1f, 100.0f);
        viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 30.0f, 0.0f, 30.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
    }

    @Override
    public void loadContent() {
        testShader = content.loadShader("testShader");
        arena = content.loadMesh("Arena");
        arenaUV = content.loadTexture("Arena.png");
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        testShader.begin();
        glUniformMatrix4fv(testShader.getUniformLocation("projection"), 1, false, projectionMatrix, 0);
        glUniformMatrix4fv(testShader.getUniformLocation("view"), 1, false, viewMatrix, 0);
        glUniformMatrix4fv(testShader.getUniformLocation("model"), 1, false, modelMatrix, 0);
        glUniform1i(testShader.getUniformLocation("uvMap"), 0);
        arenaUV.bindToUnit(GL_TEXTURE0);
        arena.draw(testShader);
        testShader.end();
    }

    @Override
    public void update(float deltaTime) {

    }

    @Override
    public void start() {

    }

    @Override
    public void reset() {

    }
}
