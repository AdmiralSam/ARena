package com.samuel.arena.framework.graphics;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Samuel on 3/15/2016.
 */
public class AnimationPose {
    private static final Pattern pqPattern = Pattern.compile("\\tpq ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+)");
    private float[][] translations;
    private float[][] rotations;

    public AnimationPose(float[][] translations, float[][] rotations) {
        this.translations = translations;
        this.rotations = rotations;
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

    public float[] getTranslation(int index) {
        return translations[index];
    }

    public float[] getRotation(int index) {
        return rotations[index];
    }
}
