package com.samuel.arena.game.screens;

import android.opengl.Matrix;
import android.util.Log;

import com.samuel.arena.framework.core.ContentManager;
import com.samuel.arena.framework.core.Screen;
import com.samuel.arena.framework.graphics.AnimatedMesh;
import com.samuel.arena.framework.graphics.Mesh;
import com.samuel.arena.framework.graphics.ShaderProgram;
import com.samuel.arena.framework.graphics.SpriteBatch;
import com.samuel.arena.framework.graphics.Texture;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Samuel on 3/6/2016.
 */
public class GameScreen extends Screen {
    //Testing
    private final float[] projectionMatrix;
    private final float[] viewMatrix;
    private final float[] modelMatrix;
    private final float[] characterMatrix;
    private ShaderProgram testShader;
    private ShaderProgram animShader;
    private Mesh arena;
    private AnimatedMesh character;
    private Texture arenaUV;
    private float timePassed;
    private boolean moving;
    //Testing

    public GameScreen(ContentManager content) {
        super(content);
        projectionMatrix = new float[16];
        Matrix.perspectiveM(projectionMatrix, 0, 40.0f, 16.0f / 9.0f, 1.0f, 50.0f);
        viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 15.0f, 0.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        characterMatrix = new float[16];
        Matrix.setIdentityM(characterMatrix, 0);
        Matrix.scaleM(characterMatrix, 0, 3.0f, 3.0f, 3.0f);
        Matrix.translateM(characterMatrix, 0, 0.0f, 0.0f, 1.0f);
    }

    @Override
    public void loadContent() {
        testShader = content.loadShader("BlinnPhong");
        animShader = content.loadShader("AnimatedBlinnPhong");
        arena = content.loadMesh("Arena");
        character = content.loadAniamtedMesh("Character");
        arenaUV = content.loadTexture("Arena.png");
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        testShader.begin();
        glUniformMatrix4fv(testShader.getUniformLocation("projection"), 1, false, projectionMatrix, 0);
        glUniformMatrix4fv(testShader.getUniformLocation("view"), 1, false, viewMatrix, 0);
        glUniformMatrix4fv(testShader.getUniformLocation("model"), 1, false, modelMatrix, 0);
        glUniform1i(testShader.getUniformLocation("uvMap"), 0);
        glUniform1f(testShader.getUniformLocation("specularExponent"), 50.0f);
        glUniform4f(testShader.getUniformLocation("cameraPosition"), 15.0f, 0.0f, 15.0f, 1.0f);
        arenaUV.bindToUnit(GL_TEXTURE0);
        arena.draw(testShader);
        testShader.end();
        animShader.begin();
        glUniformMatrix4fv(animShader.getUniformLocation("projection"), 1, false, projectionMatrix, 0);
        glUniformMatrix4fv(animShader.getUniformLocation("view"), 1, false, viewMatrix, 0);
        glUniformMatrix4fv(animShader.getUniformLocation("model"), 1, false, characterMatrix, 0);
        glUniform1i(animShader.getUniformLocation("uvMap"), 0);
        glUniform1f(animShader.getUniformLocation("specularExponent"), 50.0f);
        glUniform4f(animShader.getUniformLocation("cameraPosition"), 15.0f, 0.0f, 15.0f, 1.0f);
        arenaUV.bindToUnit(GL_TEXTURE0);
        character.draw(animShader);
        animShader.end();
    }

    @Override
    public void update(float deltaTime) {
        Log.d("Delta", deltaTime + "s");
        character.update(deltaTime);
        timePassed += deltaTime;
        if (moving) {
            if (timePassed > 3.0f) {
                timePassed = 0.0f;
                moving = false;
                character.transition("Idle", 0.25f);
            }
        } else {
            if (timePassed > 17.0f) {
                timePassed = 0.0f;
                moving = true;
                character.transition("Moving", 0.25f);
            }
        }
    }

    @Override
    public void start() {
        //TODO
    }

    @Override
    public void reset() {
        //TODO
    }
}
