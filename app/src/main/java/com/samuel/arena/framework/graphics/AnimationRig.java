package com.samuel.arena.framework.graphics;


import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samuel on 3/15/2016.
 */
public class AnimationRig {
    private static final Pattern jointPattern = Pattern.compile("joint \".*\" ([-]?[0-9]+)");
    private AnimationPose basePose;
    private int[] parents;
    private Map<Integer, List<Integer>> children;

    public AnimationRig(AnimationPose basePose, int[] parents) {
        this.basePose = basePose;
        this.parents = parents;
        children = new HashMap<>();
        for (int i = 0; i < parents.length; i++) {
            if (!children.containsKey(parents[i])) {
                children.put(parents[i], new ArrayList<Integer>());
            }
            children.get(parents[i]).add(i);
        }
    }

    public static AnimationRig parseRig(String rigSource) {
        String[] rigLines = rigSource.split("\n");
        List<Integer> parentList = new ArrayList<>();
        StringBuilder poseSource = new StringBuilder();
        for (String line : rigLines) {
            if (line.matches(jointPattern.pattern())) {
                Matcher jointMatcher = jointPattern.matcher(line);
                jointMatcher.find();
                parentList.add(Integer.parseInt(jointMatcher.group(1)));
            } else {
                poseSource.append(line);
                poseSource.append('\n');
            }
        }
        int[] parents = new int[parentList.size()];
        for (int i = 0; i < parentList.size(); i++) {
            parents[i] = parentList.get(i);
        }
        return new AnimationRig(AnimationPose.parsePose(poseSource.toString()), parents);
    }

    public float[] processPose(AnimationPose pose) {
        float[] processedPose = new float[16 * parents.length];
        flattenPose(processedPose, pose, 0);
        return processedPose;
    }

    public float[] processBasePose() {
        return processPose(basePose);
    }

    private void flattenPose(float[] result, AnimationPose pose, int index) {
        int parent = parents[index];
        float[] currentMatrix = translationRotationToMatrix(pose.getTranslation(index), pose.getRotation(index));
        if (parent == -1) {
            for (int i = 0; i < 16; i++) {
                result[16 * index + i] = currentMatrix[i];
            }
        } else {
            Matrix.multiplyMM(result, 16 * index, result, 16 * parent, currentMatrix, 0);
        }
        for (int child : children.get(index)) {
            flattenPose(result, pose, child);
        }
    }

    private float[] translationRotationToMatrix(float[] translation, float[] rotation) {
        float[] translationMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        Matrix.translateM(translationMatrix, 0, translation[0], translation[1], translation[2]);
        Matrix.setIdentityM(rotationMatrix, 0);
        float x = rotation[0];
        float y = rotation[1];
        float z = rotation[2];
        float w = rotation[3];
        rotationMatrix[0] = 1 - 2 * y * y - 2 * z * z;
        rotationMatrix[1] = 2 * x * y + 2 * z * w;
        rotationMatrix[2] = 2 * x * z - 2 * y * w;

        rotationMatrix[4] = 2 * x * y - 2 * z * w;
        rotationMatrix[5] = 1 - 2 * x * x - 2 * z * z;
        rotationMatrix[6] = 2 * y * z + 2 * x * w;

        rotationMatrix[8] = 2 * x * z + 2 * y * w;
        rotationMatrix[9] = 2 * y * z - 2 * x * w;
        rotationMatrix[10] = 1 - 2 * x * x - 2 * y * y;

        float[] resultMatrix = new float[16];
        Matrix.multiplyMM(resultMatrix, 0, translationMatrix, 0, rotationMatrix, 0);
        return resultMatrix;
    }

    public int size() {
        return parents.length;
    }
}
