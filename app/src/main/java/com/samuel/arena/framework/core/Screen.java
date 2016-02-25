package com.samuel.arena.framework.core;

import com.samuel.arena.framework.graphics.SpriteBatch;
import com.samuel.arena.framework.messaging.MessageCenter;

/**
 * Created by SamuelDong on 2/10/16.
 */
public abstract class Screen {
    public MessageCenter messageCenter;
    protected final ContentManager content;
    protected ScreenState screenState;

    protected Screen(ContentManager content) {
        this.content = content;
        messageCenter = new MessageCenter();
        screenState = ScreenState.Inactive;
    }

    public abstract void loadContent();

    public void unloadContent() {
        content.dispose();
    }

    public abstract void draw(SpriteBatch spriteBatch);

    public abstract void update(float deltaTime);

    public void start() {
        screenState = ScreenState.Active;
    }

    public void reset() {
        screenState = ScreenState.Inactive;
    }

    protected enum ScreenState {Inactive, Loading, Active, Unloading}
}
