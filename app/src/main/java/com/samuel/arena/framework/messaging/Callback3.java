package com.samuel.arena.framework.messaging;

/**
 * Created by SamuelDong on 2/10/16.
 */
public interface Callback3<A, B, C> {
    void callback(A parameter1, B parameter2, C parameter3);
}
