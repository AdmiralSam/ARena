package com.samuel.arena.framework.graphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Samuel on 3/15/2016.
 */
public class Animation {
    private final List<AnimationPose> frames;
    private final float framerate;
    private final boolean looping;
    private boolean running;
    private float localTime;

    public Animation(float framerate, boolean looping) {
        this.framerate = framerate;
        this.looping = looping;
        running = false;
        frames = new ArrayList<>();
    }

    public void start() {
        running = true;
    }

    public void stop() {
        running = false;
    }

    public void setTime(float time) {
        localTime = time;
    }

    public void addFrame(AnimationPose pose) {
        frames.add(pose);
    }

    public void update(float deltaTime) {
        if (running) {
            localTime += deltaTime;
        }
        if (looping && localTime > (frames.size() - 1) / framerate) {
            localTime -= (frames.size() - 1) / framerate;
        }
        if (!looping && localTime > (frames.size() - 1) / framerate) {
            localTime = (frames.size() - 1) / framerate - 0.001f;
        }
    }

    public AnimationPose getPose() {
        int frame = (int) (localTime * framerate);
        if (frame == frames.size() - 1) {
            return frames.get(frame);
        }
        return AnimationPose.interpolate(frames.get(frame), frames.get(frame + 1), localTime - frame / framerate);
    }

    public float getLocalTime() {
        return localTime;
    }
}
