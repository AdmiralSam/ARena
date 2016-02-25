package com.samuel.arena;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by Samuel on 2/25/2016.
 */
public class MainGlSurfaceView extends GLSurfaceView{
    private final MainGLRenderer renderer;
    public MainGlSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new MainGLRenderer();
        setRenderer(renderer);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        renderer.onTouchEvent(event);
        return true;
    }
}
