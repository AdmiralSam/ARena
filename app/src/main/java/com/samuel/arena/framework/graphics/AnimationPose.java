package com.samuel.arena.framework.graphics;

import android.opengl.Matrix;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samuel on 3/15/2016.
 */
public class AnimationPose {
    private static final Pattern pqPattern = Pattern.compile("\\tpq ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+)");
    private final float[][] translations;
    private final float[][] rotations;

    public AnimationPose(float[][] translations, float[][] rotations) {
        this.translations = new float[translations.length][3];
        this.rotations = new float[rotations.length][4];
        for (int i = 0; i < translations.length; i++) {
            for (int j = 0; j < translations[i].length; j++) {
                this.translations[i][j] = translations[i][j];
            }
        }
        for (int i = 0; i < rotations.length; i++) {
            for (int j = 0; j < rotations[i].length; j++) {
                this.rotations[i][j] = rotations[i][j];
            }
        }
    }

    public static AnimationPose parsePose(String poseSource) {
        List<float[]> translationList = new ArrayList<>();
        List<float[]> rotationList = new ArrayList<>();
        String[] poseLines = poseSource.split("\n");
        for (String line : poseLines) {
            Matcher pqMatcher = pqPattern.matcher(line);
            pqMatcher.find();
            float[] translation = new float[3];
            float[] rotation = new float[4];
            translation[0] = Float.parseFloat(pqMatcher.group(1));
            translation[1] = Float.parseFloat(pqMatcher.group(2));
            translation[2] = Float.parseFloat(pqMatcher.group(3));
            rotation[0] = Float.parseFloat(pqMatcher.group(4));
            rotation[1] = Float.parseFloat(pqMatcher.group(5));
            rotation[2] = Float.parseFloat(pqMatcher.group(6));
            rotation[3] = Float.parseFloat(pqMatcher.group(7));
            translationList.add(translation);
            rotationList.add(rotation);
        }
        float[][] translations = new float[translationList.size()][];
        float[][] rotations = new float[rotationList.size()][];
        for (int i = 0; i < translationList.size(); i++) {
            translations[i] = translationList.get(i);
            rotations[i] = rotationList.get(i);
        }
        return new AnimationPose(translations, rotations);
    }

    public static AnimationPose interpolate(AnimationPose zero, AnimationPose one, float parameter) {
        float[][] translations = new float[zero.translations.length][3];
        float[][] rotations = new float[zero.rotations.length][4];
        for (int i = 0; i < translations.length; i++) {
            for (int j = 0; j < 3; j++) {
                translations[i][j] = parameter * (one.translations[i][j] - zero.translations[i][j]) + zero.translations[i][j];
            }
            for (int j = 0; j < 4; j++) {
                rotations[i][j] = parameter * (one.rotations[i][j] - zero.rotations[i][j]) + zero.rotations[i][j];
            }
        }
        return new AnimationPose(translations, rotations);
    }

    public float[] getTranslation(int index) {
        return translations[index];
    }

    public float[] getRotation(int index) {
        return rotations[index];
    }

    public float[] getMatrix(int index) {
        float[] translationMatrix = new float[16];
        float[] rotationMatrix = new float[16];
        Matrix.translateM(translationMatrix, 0, translations[index][0], translations[index][1], translations[index][2]);
        Matrix.setIdentityM(rotationMatrix, 0);
        float x = rotations[index][0];
        float y = rotations[index][1];
        float z = rotations[index][2];
        float w = rotations[index][3];
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
}
