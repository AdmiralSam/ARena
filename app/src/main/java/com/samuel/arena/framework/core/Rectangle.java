package com.samuel.arena.framework.core;

/**
 * Created by Samuel on 2/9/2016.
 */
public class Rectangle {
    public int x;
    public int y;
    public int width;
    public int height;

    public Rectangle(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean contains(Point p) {
        return p.x >= x && p.x <= x + width && p.y >= y && p.y <= y + height;
    }
}
