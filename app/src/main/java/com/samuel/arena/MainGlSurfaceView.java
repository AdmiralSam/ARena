package com.samuel.arena;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by Samuel on 2/25/2016.
 */
public class MainGlSurfaceView extends GLSurfaceView{
    private final Renderer renderer;
    public MainGlSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
        renderer = new MainGLRenderer();
        setRenderer(renderer);
    }
}
