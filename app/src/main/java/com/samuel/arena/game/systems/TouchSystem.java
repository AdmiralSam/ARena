package com.samuel.arena.game.systems;

import android.content.Context;
import android.view.Display;
import android.view.MotionEvent;
import android.view.WindowManager;

import com.samuel.arena.framework.core.Point;
import com.samuel.arena.framework.core.Screen;
import com.samuel.arena.framework.core.System;
import com.samuel.arena.framework.messaging.Callback2;
import com.samuel.arena.framework.messaging.MessageCenter;
import com.samuel.arena.game.Game;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Samuel on 2/10/2016.
 */
public class TouchSystem implements System {
    private final float xScale, yScale;
    private final Map<Integer, Point> pointers;
    private final Callback2<Integer, MessageCenter> isDown, getLocation;

    public TouchSystem(Context context) {
        pointers = new HashMap<>();
        isDown = new Callback2<Integer, MessageCenter>() {
            @Override
            public void callback(Integer pointerID, MessageCenter messageCenter) {
                messageCenter.broadcast("Pointer Down", isDown(pointerID));
            }
        };
        getLocation = new Callback2<Integer, MessageCenter>() {
            @Override
            public void callback(Integer pointerID, MessageCenter messageCenter) {
                messageCenter.broadcast("Pointer Location", getLocation(pointerID));
            }
        };
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        android.graphics.Point size = new android.graphics.Point();
        display.getSize(size);
        xScale = (float) Game.VirtualWidth / size.x;
        yScale = (float) Game.VirtualHeight / size.y;
    }

    public void onTouchEvent(MotionEvent event, MessageCenter messageCenter) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: {
                int newPointerIndex = event.getActionIndex();
                int newPointerID = event.getPointerId(newPointerIndex);
                float newX = event.getX(newPointerIndex) * xScale;
                float newY = event.getY(newPointerIndex) * yScale;
                pointers.put(newPointerID, new Point(newX, newY));
                messageCenter.broadcast("Touch Down", newPointerID, pointers.get(newPointerID));
            }
            break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: {
                int pointerIndex = event.getActionIndex();
                int pointerID = event.getPointerId(pointerIndex);
                messageCenter.broadcast("Touch Up", pointerID, pointers.get(pointerID));
                pointers.remove(pointerID);
            }
            break;
            case MotionEvent.ACTION_MOVE: {
                for (Integer pointerID : pointers.keySet()) {
                    int pointerIndex = event.findPointerIndex(pointerID);
                    float newX = event.getX(pointerIndex) * xScale;
                    float newY = event.getY(pointerIndex) * yScale;
                    pointers.get(pointerID).x = newX;
                    pointers.get(pointerID).y = newY;
                    messageCenter.broadcast("Touch Moved", pointerID, pointers.get(pointerID));
                }
            }
            break;
            default:
                break;
        }
    }

    public boolean isDown(int pointerID) {
        return pointers.containsKey(pointerID);
    }

    public Point getLocation(int pointerID) {
        if (isDown(pointerID)) {
            return pointers.get(pointerID);
        }
        return new Point(-1.0f, -1.0f);
    }

    @Override
    public void attachScreenListeners(Screen screen) {
        screen.messageCenter.addListener("Is Pointer Down", isDown);
        screen.messageCenter.addListener("Get Pointer Location", getLocation);
    }

    @Override
    public void detachScreenListeners(Screen screen) {
        screen.messageCenter.removeListener(isDown);
        screen.messageCenter.removeListener(getLocation);
    }

    @Override
    public void update(float deltaTime) {

    }
}
