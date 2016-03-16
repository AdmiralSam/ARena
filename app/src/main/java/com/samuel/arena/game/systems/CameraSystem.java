package com.samuel.arena.game.systems;

import android.content.Context;
import android.opengl.Matrix;
import android.view.Display;
import android.view.WindowManager;

import com.qualcomm.vuforia.CameraCalibration;
import com.qualcomm.vuforia.CameraDevice;
import com.qualcomm.vuforia.Matrix44F;
import com.qualcomm.vuforia.ObjectTracker;
import com.qualcomm.vuforia.PIXEL_FORMAT;
import com.qualcomm.vuforia.Renderer;
import com.qualcomm.vuforia.Tool;
import com.qualcomm.vuforia.TrackerManager;
import com.qualcomm.vuforia.Vec2I;
import com.qualcomm.vuforia.VideoBackgroundConfig;
import com.qualcomm.vuforia.VideoMode;
import com.qualcomm.vuforia.Vuforia;
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
    private final Callback1<Matrix44F> updateView;
    private final float[] projectionMatrix;
    private float[] viewMatrix;

    public CameraSystem(Context context) {
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
        updateView = new Callback1<Matrix44F>() {
            @Override
            public void callback(Matrix44F parameter1) {
                viewMatrix = parameter1.getData();
                //Matrix.rotateM(viewMatrix, 0, 90.0f, 1.0f, 0.0f, 0.0f);
                Matrix.scaleM(viewMatrix, 0, 5.0f, 5.0f, 5.0f);
            }
        };

        CameraDevice.getInstance().init(CameraDevice.CAMERA.CAMERA_DEFAULT);
        CameraDevice.getInstance().selectVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        configureVideoBackground(context);
        if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO)) {
            if (!CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO))
                CameraDevice.getInstance().setFocusMode(CameraDevice.FOCUS_MODE.FOCUS_MODE_NORMAL);
        }
        CameraDevice.getInstance().start();
        Vuforia.setFrameFormat(PIXEL_FORMAT.RGB888, true);
        TrackerManager.getInstance().getTracker(ObjectTracker.getClassType()).start();

        viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 0.0f, 15.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);

        CameraCalibration camCal = CameraDevice.getInstance()
                .getCameraCalibration();
        projectionMatrix = Tool.getProjectionGL(camCal, 10.0f, 5000.0f).getData();
    }

    private void configureVideoBackground(Context context) {
        CameraDevice cameraDevice = CameraDevice.getInstance();
        VideoMode videoMode = cameraDevice.getVideoMode(CameraDevice.MODE.MODE_DEFAULT);
        VideoBackgroundConfig config = new VideoBackgroundConfig();
        config.setEnabled(true);
        config.setPosition(new Vec2I(0, 0));
        int xSize, ySize;
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        int mScreenWidth = size.x;
        int mScreenHeight = size.y;
        xSize = mScreenWidth;
        ySize = (int) (videoMode.getHeight() * (mScreenWidth / (float) videoMode
                .getWidth()));

        if (ySize < mScreenHeight) {
            xSize = (int) (mScreenHeight * (videoMode.getWidth() / (float) videoMode
                    .getHeight()));
            ySize = mScreenHeight;
        }
        config.setSize(new Vec2I(xSize, ySize));
        Renderer.getInstance().setVideoBackgroundConfig(config);
    }

    public float[] getProjectionMatrix() {
        return projectionMatrix;
    }

    public float[] getViewMatrix() {/*
        float[] viewMatrix = new float[16];
        Matrix.setLookAtM(viewMatrix, 0, 0.0f, 15.0f, 15.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f);*/
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
        screen.messageCenter.addListener("Update View", updateView);
    }

    @Override
    public void detachScreenListeners(Screen screen) {
        screen.messageCenter.removeListener(getProjectionMatrix);
        screen.messageCenter.removeListener(getViewMatrix);
        screen.messageCenter.removeListener(getPosition);
        screen.messageCenter.removeListener(bindCamera);
        screen.messageCenter.removeListener(updateView);
    }

    @Override
    public void update(float deltaTime) {

    }
}
