package com.samuel.arena.game.screens;

import android.opengl.Matrix;

import com.samuel.arena.framework.core.ContentManager;
import com.samuel.arena.framework.core.Point;
import com.samuel.arena.framework.core.Screen;
import com.samuel.arena.framework.core.Transform;
import com.samuel.arena.framework.graphics.AnimatedMesh;
import com.samuel.arena.framework.graphics.Mesh;
import com.samuel.arena.framework.graphics.ShaderProgram;
import com.samuel.arena.framework.graphics.SpriteBatch;
import com.samuel.arena.framework.graphics.Texture;
import com.samuel.arena.framework.messaging.Callback1;
import com.samuel.arena.framework.messaging.Callback2;

import static android.opengl.GLES20.GL_TEXTURE0;
import static android.opengl.GLES20.glUniform1f;
import static android.opengl.GLES20.glUniform1i;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Samuel on 3/6/2016.
 */
public class GameScreen extends Screen {
    public static final float ArenaRadius = 9.0f;
    public static final float CharacterRadius = 1.0f;
    public static final float CharacterSpeed = 3.0f;
    private final Callback1<float[]> setProjectionMatrix, setViewMatrix, setCameraPosition;
    private final Callback2<Integer, Point> onTouchDown, onTouchUp, onTouchMoved;
    private float[] projectionMatrix;
    private float[] viewMatrix;
    private float[] cameraPosition;
    private ShaderProgram blinnPhong;
    private ShaderProgram animatedBlinnPhong;
    private Mesh arena;
    private Transform arenaTransform;
    private AnimatedMesh character1;
    private AnimatedMesh character2;
    private Transform character1Transform;
    private Transform character2Transform;
    private Texture arenaUV;
    private int control = 1;
    private int joystickPointerID;
    private float joystickX, joystickY;

    public GameScreen(ContentManager content) {
        super(content);
        setProjectionMatrix = new Callback1<float[]>() {
            @Override
            public void callback(float[] parameter1) {
                setProjectionMatrix(parameter1);
            }
        };
        setViewMatrix = new Callback1<float[]>() {
            @Override
            public void callback(float[] parameter1) {
                setViewMatrix(parameter1);
            }
        };
        setCameraPosition = new Callback1<float[]>() {
            @Override
            public void callback(float[] parameter1) {
                setCameraPosition(parameter1);
            }
        };
        onTouchDown = new Callback2<Integer, Point>() {
            @Override
            public void callback(Integer parameter1, Point parameter2) {
                onTouchDown(parameter1, parameter2);
            }
        };
        onTouchUp = new Callback2<Integer, Point>() {
            @Override
            public void callback(Integer parameter1, Point parameter2) {
                onTouchUp(parameter1, parameter2);
            }
        };
        onTouchMoved = new Callback2<Integer, Point>() {
            @Override
            public void callback(Integer parameter1, Point parameter2) {
                onTouchMoved(parameter1, parameter2);
            }
        };
        joystickPointerID = -1;
    }

    @Override
    public void loadContent() {
        blinnPhong = content.loadShader("BlinnPhong");
        animatedBlinnPhong = content.loadShader("AnimatedBlinnPhong");
        arena = content.loadMesh("Arena");
        character1 = content.loadAnimatedMesh("Character");
        character2 = content.loadAnimatedMesh("Character2");
        arenaUV = content.loadTexture("Arena.png");
    }

    @Override
    public void draw(SpriteBatch spriteBatch) {
        blinnPhong.begin();
        messageCenter.broadcast("Bind Camera", blinnPhong);
        glUniformMatrix4fv(blinnPhong.getUniformLocation("model"), 1, false, arenaTransform.getModelMatrix(), 0);
        glUniform1i(blinnPhong.getUniformLocation("uvMap"), 0);
        glUniform1f(blinnPhong.getUniformLocation("specularExponent"), 50.0f);
        arenaUV.bindToUnit(GL_TEXTURE0);
        arena.draw(blinnPhong);
        blinnPhong.end();

        animatedBlinnPhong.begin();
        messageCenter.broadcast("Bind Camera", animatedBlinnPhong);
        glUniformMatrix4fv(animatedBlinnPhong.getUniformLocation("model"), 1, false, character1Transform.getModelMatrix(), 0);
        glUniform1i(animatedBlinnPhong.getUniformLocation("uvMap"), 0);
        glUniform1f(animatedBlinnPhong.getUniformLocation("specularExponent"), 50.0f);
        arenaUV.bindToUnit(GL_TEXTURE0);
        character1.draw(animatedBlinnPhong);
        glUniformMatrix4fv(animatedBlinnPhong.getUniformLocation("model"), 1, false, character2Transform.getModelMatrix(), 0);
        character2.draw(animatedBlinnPhong);
        animatedBlinnPhong.end();
    }

    private void setProjectionMatrix(float[] projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    private void setViewMatrix(float[] viewMatrix) {
        this.viewMatrix = viewMatrix;
    }

    private void setCameraPosition(float[] position) {
        cameraPosition = position;
    }

    private void onTouchDown(int pointerID, Point touchLocation) {
        if (touchLocation.x > 0 && touchLocation.x < 400 && touchLocation.y > 680 && touchLocation.y < 1080) {
            joystickPointerID = pointerID;
            joystickX = (touchLocation.x - 200.0f) / 200.0f;
            joystickY = -(touchLocation.y - 880.0f) / 200.0f;
        }
    }

    private void onTouchUp(int pointerID, Point touchLocation) {
        if (pointerID == joystickPointerID) {
            joystickPointerID = -1;
        }
    }

    private void onTouchMoved(int pointerID, Point touchLocation) {
        if (pointerID == joystickPointerID) {
            if (touchLocation.x > 0 && touchLocation.x < 400 && touchLocation.y > 680 && touchLocation.y < 1080) {
                joystickX = (touchLocation.x - 200.0f) / 200.0f;
                joystickY = -(touchLocation.y - 880.0f) / 200.0f;
            } else {
                joystickPointerID = -1;
                joystickX = 0.0f;
                joystickY = 0.0f;
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        character1.update(deltaTime);
        character2.update(deltaTime);
        if (control == 1) {
            if (joystickPointerID != -1) {
                float jx = getPerspectiveJoystickX();
                float jy = getPerspectiveJoystickY();
                character1Transform.translate(CharacterSpeed * jx * deltaTime, CharacterSpeed * jy * deltaTime, 0.0f);
                character1Transform.rotation = (float) Math.toDegrees(Math.atan2(jy, jx)) + 180;
            }
            if (joystickPointerID != -1 && character1.getCurrentAnimation().equals("Idle")) {
                character1.transition("Moving", 0.25f);
            }
            if (joystickPointerID == -1 && character1.getCurrentAnimation().equals("Moving")) {
                character1.transition("Idle", 0.25f);
            }
            float distanceFromCenter = (float) Math.sqrt(Math.pow(character1Transform.x, 2) + Math.pow(character1Transform.y, 2));
            float distanceFromOther = (float) Math.sqrt(Math.pow(character1Transform.x - character2Transform.x, 2) + Math.pow(character1Transform.y - character2Transform.y, 2));
            if (distanceFromCenter > ArenaRadius) {
                float extra = distanceFromCenter - ArenaRadius;
                character1Transform.translate(-character1Transform.x * extra / distanceFromCenter, -character1Transform.y * extra / distanceFromCenter, 0.0f);
            }
            if (distanceFromOther < 2 * CharacterRadius) {
                float extra = 2 * CharacterRadius - distanceFromOther;
                character1Transform.translate((character1Transform.x - character2Transform.x) * extra / distanceFromOther, (character1Transform.y - character2Transform.y) * extra / distanceFromOther, 0.0f);
            }
        }
        messageCenter.broadcast("Get Projection Matrix", messageCenter);
        messageCenter.broadcast("Get View Matrix", messageCenter);
        messageCenter.broadcast("Get Position", messageCenter);
    }

    private float getPerspectiveJoystickX() {
        float[] xAxis = new float[4];
        float[] yAxis = new float[4];
        xAxis[0] = 1.0f;
        xAxis[1] = 0.0f;
        xAxis[2] = 0.0f;
        xAxis[3] = 0.0f;

        yAxis[0] = 0.0f;
        yAxis[1] = 1.0f;
        yAxis[2] = 0.0f;
        yAxis[3] = 0.0f;

        float[] worldSpaceXAxis = new float[4];
        float[] worldSpaceYAxis = new float[4];
        float[] inverseView = new float[16];
        Matrix.invertM(inverseView, 0, viewMatrix, 0);
        Matrix.multiplyMV(worldSpaceXAxis, 0, inverseView, 0, xAxis, 0);
        Matrix.multiplyMV(worldSpaceYAxis, 0, inverseView, 0, yAxis, 0);

        float worldYAxisX = worldSpaceYAxis[0];
        float worldYAxisY = worldSpaceYAxis[1];
        float yAxisLength = Matrix.length(worldYAxisX, worldYAxisY, 0.0f);
        worldYAxisX /= yAxisLength;
        worldYAxisY /= yAxisLength;

        float worldXAxisX = worldSpaceXAxis[0];
        float worldXAxisY = worldSpaceXAxis[1];
        float xAxisLength = Matrix.length(worldXAxisX, worldXAxisY, 0.0f);
        worldXAxisX /= xAxisLength;
        worldXAxisY /= xAxisLength;

        return worldYAxisX * joystickY + worldXAxisX * joystickX;
    }

    private float getPerspectiveJoystickY() {
        float[] xAxis = new float[4];
        float[] yAxis = new float[4];
        xAxis[0] = 1.0f;
        xAxis[1] = 0.0f;
        xAxis[2] = 0.0f;
        xAxis[3] = 0.0f;

        yAxis[0] = 0.0f;
        yAxis[1] = 1.0f;
        yAxis[2] = 0.0f;
        yAxis[3] = 0.0f;

        float[] worldSpaceXAxis = new float[4];
        float[] worldSpaceYAxis = new float[4];
        float[] inverseView = new float[16];
        Matrix.invertM(inverseView, 0, viewMatrix, 0);
        Matrix.multiplyMV(worldSpaceXAxis, 0, inverseView, 0, xAxis, 0);
        Matrix.multiplyMV(worldSpaceYAxis, 0, inverseView, 0, yAxis, 0);

        float worldYAxisX = worldSpaceYAxis[0];
        float worldYAxisY = worldSpaceYAxis[1];
        float yAxisLength = Matrix.length(worldYAxisX, worldYAxisY, 0.0f);
        worldYAxisX /= yAxisLength;
        worldYAxisY /= yAxisLength;

        float worldXAxisX = worldSpaceXAxis[0];
        float worldXAxisY = worldSpaceXAxis[1];
        float xAxisLength = Matrix.length(worldXAxisX, worldXAxisY, 0.0f);
        worldXAxisX /= xAxisLength;
        worldXAxisY /= xAxisLength;

        return worldYAxisY * joystickY + worldXAxisY * joystickX;
    }

    @Override
    public void start() {
        arenaTransform = new Transform();
        character2Transform = new Transform(-5.0f, 0.0f, 0.5f, 3.0f, 180.0f);
        character1Transform = new Transform(5.0f, 0.0f, 0.5f, 3.0f, 0.0f);
        messageCenter.addListener("Projection Matrix", setProjectionMatrix);
        messageCenter.addListener("View Matrix", setViewMatrix);
        messageCenter.addListener("Position", setCameraPosition);
        messageCenter.addListener("Touch Down", onTouchDown);
        messageCenter.addListener("Touch Up", onTouchUp);
        messageCenter.addListener("Touch Moved", onTouchMoved);
    }

    @Override
    public void reset() {
        messageCenter.removeListener(setProjectionMatrix);
        messageCenter.removeListener(setViewMatrix);
        messageCenter.removeListener(setCameraPosition);
        messageCenter.removeListener(onTouchDown);
        messageCenter.removeListener(onTouchMoved);
        messageCenter.removeListener(onTouchUp);

    }
}
