package com.samuel.arena.framework.messaging;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by SamuelDong on 2/10/16.
 */
public class MessageCenter {
    private final HashMap<String, ArrayList<Object>> listeners;
    private final ArrayList<Object> toRemove;

    public MessageCenter() {
        listeners = new HashMap<>();
        toRemove = new ArrayList<>();
    }

    public void addListener(String message, Object callback) {
        if (!listeners.containsKey(message)) {
            listeners.put(message, new ArrayList<Object>());
        }
        listeners.get(message).add(callback);
    }

    public void broadcast(String message) {
        if (listeners.containsKey(message)) {
            cleanup();
            for (Object listener : listeners.get(message)) {
                if (listener instanceof Callback0) {
                    ((Callback0) listener).callback();
                }
            }
            cleanup();
        }
    }

    public <A> void broadcast(String message, A parameter1) {
        if (listeners.containsKey(message)) {
            cleanup();
            for (Object listener : listeners.get(message)) {
                if (listener instanceof Callback1) {
                    try {
                        ((Callback1<A>) listener).callback(parameter1);
                    } catch (ClassCastException e) {
                    }
                }
            }
            cleanup();
        }
    }

    public <A, B> void broadcast(String message, A parameter1, B parameter2) {
        if (listeners.containsKey(message)) {
            cleanup();
            for (Object listener : listeners.get(message)) {
                if (listener instanceof Callback2) {
                    try {
                        ((Callback2<A, B>) listener).callback(parameter1, parameter2);
                    } catch (ClassCastException e) {
                    }
                }
            }
            cleanup();
        }
    }

    public <A, B, C> void broadcast(String message, A parameter1, B parameter2, C parameter3) {
        if (listeners.containsKey(message)) {
            cleanup();
            for (Object listener : listeners.get(message)) {
                if (listener instanceof Callback3) {
                    try {
                        ((Callback3<A, B, C>) listener).callback(parameter1, parameter2, parameter3);
                    } catch (ClassCastException e) {
                    }
                }
            }
            cleanup();
        }
    }

    public void removeListener(Object listener) {
        toRemove.add(listener);
    }

    private void cleanup() {
        for (String message : listeners.keySet()) {
            for (Object listener : toRemove) {
                listeners.get(message).remove(listener);
            }
        }
        toRemove.clear();
    }
}
