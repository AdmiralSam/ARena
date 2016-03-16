package com.samuel.arena.framework.graphics;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Samuel on 3/15/2016.
 */
public class AnimationSystem {
    private final Map<String, Animation> animations;
    private String currentAnimation;
    private String transitioningAnimation;
    private AnimationPose transitionFrom;
    private float transitionLength;
    private float transitionTime;

    public AnimationSystem() {
        animations = new HashMap<>();
    }

    public void addAnimation(String name, Animation animation) {
        animations.put(name, animation);
        if (currentAnimation == null) {
            currentAnimation = name;
            animation.start();
        }
    }

    public void update(float deltaTime) {
        if (transitioningAnimation != null) {
            animations.get(transitioningAnimation).update(deltaTime);
            transitionTime += deltaTime;
            if (transitionTime > transitionLength) {
                currentAnimation = transitioningAnimation;
                transitioningAnimation = null;
            }
        }
        animations.get(currentAnimation).update(deltaTime);
    }

    public void transition(String name, float length) {
        transitionFrom = getPose();
        transitionLength = length;
        transitionTime = 0.0f;
        transitioningAnimation = name;
        animations.get(currentAnimation).stop();
        animations.get(currentAnimation).setTime(0.0f);
        animations.get(transitioningAnimation).setTime(0.0f);
        animations.get(transitioningAnimation).start();
    }

    public AnimationPose getPose() {
        if (transitioningAnimation == null) {
            return animations.get(currentAnimation).getPose();
        } else {
            AnimationPose transition = animations.get(transitioningAnimation).getPose();
            return AnimationPose.interpolate(transitionFrom, transition, transitionTime / transitionLength);
        }
    }

    public String getCurrentAnimation() {
        return transitioningAnimation == null ? currentAnimation : transitioningAnimation;
    }

    public float getCurrentAnimationTime() {
        return animations.get(currentAnimation).getLocalTime();
    }
}
