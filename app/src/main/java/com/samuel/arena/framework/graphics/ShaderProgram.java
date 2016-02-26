package com.samuel.arena.framework.graphics;

import android.util.Log;

import com.samuel.arena.framework.core.Disposable;

import java.util.HashMap;

import static android.opengl.GLES20.GL_FRAGMENT_SHADER;
import static android.opengl.GLES20.GL_VERTEX_SHADER;
import static android.opengl.GLES20.glAttachShader;
import static android.opengl.GLES20.glCompileShader;
import static android.opengl.GLES20.glCreateProgram;
import static android.opengl.GLES20.glCreateShader;
import static android.opengl.GLES20.glDeleteProgram;
import static android.opengl.GLES20.glDeleteShader;
import static android.opengl.GLES20.glDisableVertexAttribArray;
import static android.opengl.GLES20.glEnableVertexAttribArray;
import static android.opengl.GLES20.glGetAttribLocation;
import static android.opengl.GLES20.glGetProgramInfoLog;
import static android.opengl.GLES20.glGetShaderInfoLog;
import static android.opengl.GLES20.glGetUniformLocation;
import static android.opengl.GLES20.glLinkProgram;
import static android.opengl.GLES20.glShaderSource;
import static android.opengl.GLES20.glUseProgram;

/**
 * Created by Samuel on 2/7/2016.
 */
public class ShaderProgram implements Disposable {
    public static final String Tag = "Shader";
    private static ShaderProgram activeShaderProgram;
    private final HashMap<String, Integer> attributes;
    private final HashMap<String, Integer> uniforms;
    private int programID;
    private boolean disposed;

    public ShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        attributes = new HashMap<>();
        uniforms = new HashMap<>();
        disposed = false;
        createShaderProgram(vertexShaderSource, fragmentShaderSource);
        getAttributeUniformLocations(vertexShaderSource, fragmentShaderSource);
    }

    public void begin() {
        if (disposed) {
            Log.e(Tag, "Cannot begin a disposed shader program");
        } else {
            if (activeShaderProgram == null) {
                glUseProgram(programID);
                for (int attributeLocation : attributes.values()) {
                    glEnableVertexAttribArray(attributeLocation);
                }
                activeShaderProgram = this;
            } else {
                Log.e(Tag, "Previous shader program has not ended");
            }
        }
    }

    public void end() {
        if (!disposed) {
            if (activeShaderProgram == this) {
                for (int attributeLocation : attributes.values()) {
                    glDisableVertexAttribArray(attributeLocation);
                }
                activeShaderProgram = null;
            } else {
                Log.e(Tag, "This shader program is not active");
            }
        } else {
            Log.e(Tag, "Cannot end a disposed shader program");
        }
    }

    public int getAttributeLocation(String name) {
        if (attributes.containsKey(name)) {
            return attributes.get(name);
        } else {
            Log.e(Tag, "Attribute \"" + name + "\" does not exist");
            return -1;
        }
    }

    public int getUniformLocation(String name) {
        if (uniforms.containsKey(name)) {
            return uniforms.get(name);
        } else {
            Log.e(Tag, "Uniform \"" + name + "\" does not exist");
            return -1;
        }
    }

    public void dispose() {
        glDeleteProgram(programID);
        disposed = true;
    }

    private void createShaderProgram(String vertexShaderSource, String fragmentShaderSource) {
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexShaderSource);
        glCompileShader(vertexShader);
        if (glGetShaderInfoLog(vertexShader).length() != 0) {
            Log.e(Tag, "Vertex Shader Compilation Log:\n" + glGetShaderInfoLog(vertexShader));
        }

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentShaderSource);
        glCompileShader(fragmentShader);
        if (glGetShaderInfoLog(fragmentShader).length() != 0) {
            Log.e(Tag, "Fragment Shader Compilation Log:\n" + glGetShaderInfoLog(fragmentShader));
        }

        programID = glCreateProgram();
        glAttachShader(programID, vertexShader);
        glAttachShader(programID, fragmentShader);
        glLinkProgram(programID);
        if (glGetProgramInfoLog(programID).length() != 0) {
            Log.e(Tag, "Program Linking Log:\n" + glGetProgramInfoLog(programID));
        }

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    private void getAttributeUniformLocations(String vertexShaderSource, String fragmentShaderSource) {
        for (String line : vertexShaderSource.split("\n")) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals("attribute")) {
                String name = tokens[tokens.length - 1];
                name = name.substring(0, name.length() - 1);
                attributes.put(name, glGetAttribLocation(programID, name));
            }
            if (tokens[0].equals("uniform")) {
                String name = tokens[tokens.length - 1];
                name = name.substring(0, name.length() - 1);
                uniforms.put(name, glGetUniformLocation(programID, name));
            }
        }
        for (String line : fragmentShaderSource.split("\n")) {
            String[] tokens = line.split(" ");
            if (tokens[0].equals("uniform")) {
                String name = tokens[tokens.length - 1];
                name = name.substring(0, name.length() - 1);
                uniforms.put(name, glGetUniformLocation(programID, name));
            }
        }
    }
}
