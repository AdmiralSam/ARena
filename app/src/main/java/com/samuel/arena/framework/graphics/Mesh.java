package com.samuel.arena.framework.graphics;

import android.util.Log;

import com.samuel.arena.framework.core.Disposable;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_ELEMENT_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_FLOAT;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.GL_TRIANGLES;
import static android.opengl.GLES20.GL_UNSIGNED_SHORT;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glDeleteBuffers;
import static android.opengl.GLES20.glDrawElements;
import static android.opengl.GLES20.glGenBuffers;
import static android.opengl.GLES20.glVertexAttribPointer;

/**
 * Created by Samuel on 3/6/2016.
 */
public class Mesh implements Disposable {
    public static final String Tag = "Mesh";
    private static final Pattern vpPattern = Pattern.compile("vp ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+)");
    private static final Pattern vtPattern = Pattern.compile("\\tvt ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+)");
    private static final Pattern vnPattern = Pattern.compile("\\tvn ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+) ([-]?[0-9]+[.][0-9]+)");
    private static final Pattern fmPattern = Pattern.compile("fm ([0-9]+) ([0-9]+) ([0-9]+)");
    private final int positionBufferID;
    private final int uvBufferID;
    private final int normalBufferID;
    private final int indexBufferID;
    private final int vertexCount;
    private boolean disposed;

    public Mesh(float[] positions, float[] uvs, float[] normals, short[] indices) {
        vertexCount = indices.length;

        int[] bufferIDs = new int[4];
        glGenBuffers(4, bufferIDs, 0);
        positionBufferID = bufferIDs[0];
        uvBufferID = bufferIDs[1];
        normalBufferID = bufferIDs[2];
        indexBufferID = bufferIDs[3];

        glBindBuffer(GL_ARRAY_BUFFER, positionBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * positions.length, FloatBuffer.wrap(positions), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, uvBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * uvs.length, FloatBuffer.wrap(uvs), GL_STATIC_DRAW);
        glBindBuffer(GL_ARRAY_BUFFER, normalBufferID);
        glBufferData(GL_ARRAY_BUFFER, 4 * normals.length, FloatBuffer.wrap(normals), GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, 2 * indices.length, ShortBuffer.wrap(indices), GL_STATIC_DRAW);

        disposed = false;
    }

    public static Mesh parseMesh(String meshSource) {
        String[] meshLines = meshSource.split("\n");
        if (meshLines.length == 0 || !meshLines[0].equals("# Inter-Quake Export")) {
            Log.e(Tag, "Invalid mesh file format (must be IQE)");
            return null;
        }
        ArrayList<Float> positionList = new ArrayList<>();
        ArrayList<Float> uvList = new ArrayList<>();
        ArrayList<Float> normalList = new ArrayList<>();
        ArrayList<Short> indexList = new ArrayList<>();
        for (String line : meshLines) {
            if (line.matches(vpPattern.pattern())) {
                parseVertexPosition(positionList, line);
            } else if (line.matches(vtPattern.pattern())) {
                parseVertexUV(uvList, line);
            } else if (line.matches(vnPattern.pattern())) {
                parseVertexNormal(normalList, line);
            } else if (line.matches(fmPattern.pattern())) {
                parseFace(indexList, line);
            }
        }
        float[] positions = new float[positionList.size()];
        float[] uvs = new float[uvList.size()];
        float[] normals = new float[normalList.size()];
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
        for (int i = 0; i < indexList.size(); i++) {
            indices[i] = indexList.get(i);
        }
        return new Mesh(positions, uvs, normals, indices);
    }

    private static void parseFace(List<Short> indexList, String line) {
        Matcher fmMatch = fmPattern.matcher(line);
        fmMatch.find();
        short index1 = Short.parseShort(fmMatch.group(1));
        short index2 = Short.parseShort(fmMatch.group(2));
        short index3 = Short.parseShort(fmMatch.group(3));
        indexList.add(index1);
        indexList.add(index2);
        indexList.add(index3);
    }

    private static void parseVertexNormal(List<Float> normalList, String line) {
        Matcher vnMatch = vnPattern.matcher(line);
        vnMatch.find();
        float x = Float.parseFloat(vnMatch.group(1));
        float y = Float.parseFloat(vnMatch.group(2));
        float z = Float.parseFloat(vnMatch.group(3));
        normalList.add(x);
        normalList.add(y);
        normalList.add(z);
        normalList.add(0.0f);
    }

    private static void parseVertexUV(List<Float> uvList, String line) {
        Matcher vtMatch = vtPattern.matcher(line);
        vtMatch.find();
        float u = Float.parseFloat(vtMatch.group(1));
        float v = Float.parseFloat(vtMatch.group(2));
        uvList.add(u);
        uvList.add(v);
    }

    private static void parseVertexPosition(List<Float> positionList, String line) {
        Matcher vpMatch = vpPattern.matcher(line);
        vpMatch.find();
        float x = Float.parseFloat(vpMatch.group(1));
        float y = Float.parseFloat(vpMatch.group(2));
        float z = Float.parseFloat(vpMatch.group(3));
        positionList.add(x);
        positionList.add(y);
        positionList.add(z);
        positionList.add(1.0f);
    }

    public void draw(ShaderProgram shader) {
        if (disposed) {
            Log.e(Tag, "Cannot use a disposed mesh");
        } else {
            glBindBuffer(GL_ARRAY_BUFFER, positionBufferID);
            glVertexAttribPointer(shader.getAttributeLocation("position"), 4, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, uvBufferID);
            glVertexAttribPointer(shader.getAttributeLocation("uv"), 2, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ARRAY_BUFFER, normalBufferID);
            glVertexAttribPointer(shader.getAttributeLocation("normal"), 4, GL_FLOAT, false, 0, 0);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBufferID);
            glDrawElements(GL_TRIANGLES, vertexCount, GL_UNSIGNED_SHORT, 0);
        }
    }

    @Override
    public void dispose() {
        glDeleteBuffers(4, new int[]{positionBufferID, uvBufferID, normalBufferID, indexBufferID}, 0);
        disposed = true;
    }
}
