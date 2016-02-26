package com.samuel.arena;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.samuel.arena.game.Game;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static android.opengl.GLES20.GL_COLOR_BUFFER_BIT;
import static android.opengl.GLES20.GL_DEPTH_BUFFER_BIT;
import static android.opengl.GLES20.glClear;
import static android.opengl.GLES20.glClearColor;
import static android.opengl.GLES20.glViewport;

/**
 * Created by Samuel on 2/25/2016.
 */
public class MainGLRenderer implements GLSurfaceView.Renderer {
    private final Game game;
    private long previousTime;

    public MainGLRenderer(Context context) {
        game = new Game(context);
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        glClearColor(1.0f, 0.0f, 1.0f, 1.0f);
        game.initialize();
        game.loadContent();
        previousTime = System.nanoTime();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        game.draw();
        game.update((float) (System.nanoTime() - previousTime) / 1000000000.0f);
        previousTime = System.nanoTime();
    }

    public void onTouchEvent(MotionEvent event) {
        game.onTouchEvent(event);
    }
}
