package com.samuel.arena.game.systems;

import android.opengl.Matrix;

import com.samuel.arena.framework.core.Screen;
import com.samuel.arena.framework.core.System;
import com.samuel.arena.framework.graphics.ShaderProgram;
import com.samuel.arena.framework.messaging.Callback1;
import com.samuel.arena.framework.messaging.MessageCenter;

import static android.opengl.GLES20.glUniform4f;
import static android.opengl.GLES20.glUniformMatrix4fv;

/**
 * Created by Samuel on 3/15/2016.
 */
public class CameraSystem implements System {
    private final Callback1<MessageCenter> getProjectionMatrix, getViewMatrix, getPosition;
    private final Callback1<ShaderProgram> bindCamera;

    public CameraSystem() {
        getProjectionMatrix = new Callback1<MessageCenter>() {
            @Override
            public void callback(MessageCenter parameter1) {
                parameter1.broadcast("Projection Matrix", getProjectionMatrix());
            }
        };
        getViewMatrix = new Callback1<MessageCenter>() {
            @Override
            public void callback(MessageCenter parameter1) {
                parameter1.broadcast("View Matrix", getViewMatrix());
            }
        };
        getPosition = new Callback1<MessageCenter>() {
            @Override
            public void callback(MessageCenter parameter1) {
                parameter1.broadcast("Position", getPosition());
            }
        };
        bindCamera = new Callback1<ShaderProgram>() {
            @Override
            public void callback(ShaderProgram parameter1) {
                glUniformMatrix4fv(parameter1.getUniformLocation("projection"), 1, false, getProjectionMatrix(), 0);
                glUniformMatrix4fv(parameter1.getUniformLocation("view"), 1, false, getViewMatrix(), 0);
                float[] position = getPosition();
                glUniform4f(parameter1.getUniformLocation("cameraPosition"), position[0], position[1], position[2], position[3]);
            }
        };
    }

    public float[] getProjectionMatrix() {
        float[] projectionMatrix = new float[16];
        Matrix.perspectiveM(projectionMatrix, 0, 40.0f, 16.0f / 9.0f, 1.0f, 50.0f);
        return projectionMatrix;
    }

    public float[] getViewMatrix() {
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 0.0f, 15.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);
        return viewMatrix;
    }

    public float[] getPosition() {
        float[] inverseView = new float[16];
        float[] position = new float[4];
        Matrix.invertM(inverseView, 0, getViewMatrix(), 0);
        position[0] = 0.0f;
        position[1] = 0.0f;
        position[2] = 0.0f;
        position[3] = 1.0f;
        float[] result = new float[4];
        Matrix.multiplyMV(result, 0, inverseView, 0, position, 0);
        return result;
    }

    @Override
    public void attachScreenListeners(Screen screen) {
        screen.messageCenter.addListener("Get Projection Matrix", getProjectionMatrix);
        screen.messageCenter.addListener("Get View Matrix", getViewMatrix);
        screen.messageCenter.addListener("Get Position", getPosition);
        screen.messageCenter.addListener("Bind Camera", bindCamera);
    }

    @Override
    public void detachScreenListeners(Screen screen) {
        screen.messageCenter.removeListener(getProjectionMatrix);
        screen.messageCenter.removeListener(getViewMatrix);
        screen.messageCenter.removeListener(getPosition);
        screen.messageCenter.removeListener(bindCamera);
    }

    @Override
    public void update(float deltaTime) {

    }
}
