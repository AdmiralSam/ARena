package com.samuel.arena.framework.graphics;

import android.util.Log;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_INT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glUniformMatrix4fv;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Samuel on 3/15/2016.
 */
public class AnimatedMesh extends Mesh {
    private static final Pattern vbPattern = Pattern.compile("\\tvb ([0-9]+) [-]?[0-9]+[.][0-9]+");
    private static final Pattern animationPattern = Pattern.compile("animation \"(.*)\" (true|false) ([-]?[0-9]+[.][0-9]+)");
    private final AnimationSystem animationSystem;
    private final AnimationRig animationRig;
    private final int boneIndexBufferID;

    public AnimatedMesh(AnimationSystem animationSystem, AnimationRig animationRig, float[] positions, float[] uvs, float[] normals, int[] boneIndices, short[] indices) {
        super(positions, uvs, normals, indices);
        this.animationSystem = animationSystem;
        this.animationRig = animationRig;

        int[] boneIndexBufferID = new int[1];
        glGenBuffers(1, boneIndexBufferID, 0);
        this.boneIndexBufferID = boneIndexBufferID[0];

        glBindBuffer(GL_ARRAY_BUFFER, this.boneIndexBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * boneIndices.length, IntBuffer.wrap(boneIndices), GL_STATIC_DRAW);
    }

    public static AnimatedMesh parseAnimatedMesh(String meshSource) {
        String[] meshLines = meshSource.split("\n");
        if (meshLines.length == 0 || !meshLines[0].equals("# Inter-Quake Export")) {
            Log.e(Tag, "Invalid mesh file format (must be IQE)");
            return null;
        }
        List<Float> positionList = new ArrayList<>();
        List<Float> uvList = new ArrayList<>();
        List<Float> normalList = new ArrayList<>();
        List<Integer> boneIndexList = new ArrayList<>();
        List<Short> indexList = new ArrayList<>();
        StringBuilder rigSource = new StringBuilder();
        boolean readingRig = false;
        AnimationSystem animationSystem = new AnimationSystem();
        Animation currentAnimation = null;
        StringBuilder frameSource = null;
        for (String line : meshLines) {
            if (!readingRig && line.contains("joint")) {
                readingRig = true;
            }
            if (readingRig && line.equals("")) {
                readingRig = false;
            }
            if (readingRig) {
                rigSource.append(line);
                rigSource.append('\n');
                continue;
            }
            if (line.matches(vpPattern.pattern())) {
                parseVertexPosition(positionList, line);
            } else if (line.matches(vtPattern.pattern())) {
                parseVertexUV(uvList, line);
            } else if (line.matches(vnPattern.pattern())) {
                parseVertexNormal(normalList, line);
            } else if (line.matches(vbPattern.pattern())) {
                Matcher vbMatcher = vbPattern.matcher(line);
                vbMatcher.find();
                boneIndexList.add(Integer.parseInt(vbMatcher.group(1)));
            } else if (line.matches(fmPattern.pattern())) {
                parseFace(indexList, line);
            } else if (line.matches(animationPattern.pattern())) {
                Matcher animationMatcher = animationPattern.matcher(line);
                animationMatcher.find();
                String animationName = animationMatcher.group(1);
                boolean looping = Boolean.parseBoolean(animationMatcher.group(2));
                float framerate = Float.parseFloat(animationMatcher.group(3));
                currentAnimation = new Animation(framerate, looping);
                animationSystem.addAnimation(animationName, currentAnimation);
            } else if (frameSource != null && line.equals("")) {
                currentAnimation.addFrame(AnimationPose.parsePose(frameSource.toString()));
                frameSource = null;
            } else if (line.equals("frame")) {
                frameSource = new StringBuilder();
            } else if (frameSource != null && line.matches(AnimationPose.pqPattern.pattern())) {
                frameSource.append(line);
                frameSource.append('\n');
            }
        }
        float[] positions = new float[positionList.size()];
        float[] uvs = new float[uvList.size()];
        float[] normals = new float[normalList.size()];
        int[] boneIndices = new int[boneIndexList.size()];
        short[] indices = new short[indexList.size()];
        for (int i = 0; i < positionList.size(); i++) {
            positions[i] = positionList.get(i);
        }
        for (int i = 0; i < uvList.size(); i++) {
            uvs[i] = uvList.get(i);
        }
        for (int i = 0; i < normalList.size(); i++) {
            normals[i] = normalList.get(i);
        }
        for (int i = 0; i < boneIndexList.size(); i++) {
            boneIndices[i] = boneIndexList.get(i);
        }
        for (int i = 0; i < indexList.size(); i++) {
            indices[i] = indexList.get(i);
        }
        return new AnimatedMesh(animationSystem, AnimationRig.parseRig(rigSource.toString()), positions, uvs, normals, boneIndices, indices);
    }

    public void update(float deltaTime) {
        animationSystem.update(deltaTime);
    }

    public void draw(ShaderProgram shader) {
        glBindBuffer(GL_ARRAY_BUFFER, boneIndexBufferID);
        glVertexAttribPointer(shader.getAttributeLocation("boneIndex"), 1, GL_INT, false, 0, 0);
        glUniformMatrix4fv(shader.getUniformLocation("bind"), animationRig.size(), false, animationRig.processBasePose(), 0);
        glUniformMatrix4fv(shader.getUniformLocation("pose"), animationRig.size(), false, animationRig.processPose(animationSystem.getPose()), 0);
        super.draw(shader);
    }

    public void transition(String name, float transitionTime) {
        animationSystem.transition(name, transitionTime);
    }

    public String getCurrentAnimation() {
        return animationSystem.getCurrentAnimation();
    }

    public float getCurrentAnimationTime() {
        return animationSystem.getCurrentAnimationTime();
    }
}
