package com.samuel.arena.game;

import android.content.Context;
import android.view.MotionEvent;

import com.samuel.arena.framework.core.ContentManager;
import com.samuel.arena.framework.core.Screen;
import com.samuel.arena.framework.core.System;
import com.samuel.arena.framework.graphics.ShaderProgram;
import com.samuel.arena.framework.graphics.SpriteBatch;
import com.samuel.arena.framework.messaging.Callback1;
import com.samuel.arena.game.screens.GameScreen;
import com.samuel.arena.game.systems.TouchSystem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Samuel on 2/25/2016.
 */
public class Game {
    public static final int VirtualWidth = 1920, VirtualHeight = 1080;
    private final Map<String, Screen> screens;
    private final Context context;
    private final ContentManager content;
    private final List<System> systems;
    private final Callback1<String> switchScreens;
    private Screen currentScreen;
    private SpriteBatch spriteBatch;
    private TouchSystem touchSystem;

    public Game(Context context) {
        this.context = context;
        content = new ContentManager(context);
        screens = new HashMap<>();
        systems = new ArrayList<>();
        switchScreens = new Callback1<String>() {
            @Override
            public void callback(String screenName) {
                switchScreens(screenName);
            }
        };
    }

    public void initialize() {
        initializeSystems();
        initializeScreens();
    }

    private void initializeSystems() {
        touchSystem = new TouchSystem();
        systems.add(touchSystem);
    }

    private void initializeScreens() {
        screens.put("Game Screen", new GameScreen(createContentManager()));
        switchScreens("Game Screen");
    }

    public void loadContent() {
        ShaderProgram defaultShader = content.loadShader("Shaders/BasicShader");
        spriteBatch = new SpriteBatch(Game.VirtualWidth, Game.VirtualHeight, defaultShader);
    }

    public void draw() {
        if (currentScreen != null) {
            currentScreen.draw(spriteBatch);
        }
    }

    public void update(float deltaTime) {
        if (currentScreen != null) {
            currentScreen.update(deltaTime);
        }
    }

    private ContentManager createContentManager() {
        return new ContentManager(context);
    }

    private void switchScreens(String screenName) {
        Screen oldScreen = currentScreen;
        Screen newScreen = screens.get(screenName);
        if (newScreen != null) {
            attachScreenListeners(newScreen);
            newScreen.loadContent();
            newScreen.start();
            currentScreen = newScreen;
            if (oldScreen != null) {
                detachScreenListeners(oldScreen);
                oldScreen.reset();
                oldScreen.unloadContent();
            }
        }
    }

    private void attachScreenListeners(Screen screen) {
        screen.messageCenter.addListener("Switch Screens", switchScreens);
        for (System system : systems) {
            system.attachScreenListeners(screen);
        }
    }

    private void detachScreenListeners(Screen screen) {
        screen.messageCenter.removeListener(switchScreens);
        for (System system : systems) {
            system.detachScreenListeners(screen);
        }
    }

    public void onTouchEvent(MotionEvent event) {
        if (currentScreen != null) {
            touchSystem.onTouchEvent(event, currentScreen.messageCenter);
        }
    }
}
