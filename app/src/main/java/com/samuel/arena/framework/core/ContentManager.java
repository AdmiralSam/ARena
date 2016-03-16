package com.samuel.arena.framework.core;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.samuel.arena.framework.graphics.AnimatedMesh;
import com.samuel.arena.framework.graphics.Mesh;
import com.samuel.arena.framework.graphics.ShaderProgram;
import com.samuel.arena.framework.graphics.Texture;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Samuel on 2/7/2016.
 */
public class ContentManager implements Disposable {
    public static final String Tag = "Content";
    private final Map<String, Disposable> content;
    private final Context context;

    public ContentManager(Context context) {
        this.context = context;
        content = new HashMap<>();
    }

    public AnimatedMesh loadAnimatedMesh(String filename) {
        if (!content.containsKey(filename) || !(content.get(filename) instanceof AnimatedMesh)) {
            String meshSource = getFileAsString(filename + ".iqe");
            content.put(filename, AnimatedMesh.parseAnimatedMesh(meshSource));
        }
        return (AnimatedMesh) content.get(filename);
    }

    public Mesh loadMesh(String filename) {
        if (!content.containsKey(filename) || !(content.get(filename) instanceof Mesh)) {
            String meshSource = getFileAsString(filename + ".iqe");
            content.put(filename, Mesh.parseMesh(meshSource));
        }
        return (Mesh) content.get(filename);
    }

    public ShaderProgram loadShader(String filename) {
        if (!content.containsKey(filename) || !(content.get(filename) instanceof ShaderProgram)) {
            String vertexShaderSource = getFileAsString(filename + ".vsf");
            String fragmentShaderSource = getFileAsString(filename + ".fsf");
            if (vertexShaderSource.length() == 0 || fragmentShaderSource.length() == 0) {
                if (vertexShaderSource.length() == 0) {
                    Log.e(Tag, "Vertex shader file missing");
                }
                if (fragmentShaderSource.length() == 0) {
                    Log.e(Tag, "Fragment shader file missing");
                }
                return null;
            } else {
                content.put(filename, new ShaderProgram(vertexShaderSource, fragmentShaderSource));
            }
        }
        return (ShaderProgram) content.get(filename);
    }

    public Texture loadTexture(String filename) {
        if (!content.containsKey(filename) || !(content.get(filename) instanceof Texture)) {
            content.put(filename, new Texture(getBitmap(filename)));
        }
        return (Texture) content.get(filename);
    }

    public void dispose() {
        for (Disposable disposable : content.values()) {
            disposable.dispose();
        }
    }

    private Bitmap getBitmap(String filename) {
        InputStream fileStream;
        Bitmap bitmap = null;
        try {
            fileStream = context.getAssets().open(filename);
            bitmap = BitmapFactory.decodeStream(fileStream);
        } catch (IOException e) {
            Log.e(Tag, "File \"" + filename + "\" does not exist");
        }
        return bitmap;
    }


    private String getFileAsString(String filename) {
        BufferedReader fileReader = null;
        StringBuilder fileStringBuilder = new StringBuilder();
        try {
            fileReader = new BufferedReader(new InputStreamReader(context.getAssets().open(filename)));
            String line = fileReader.readLine();
            while (line != null) {
                fileStringBuilder.append(line);
                fileStringBuilder.append('\n');
                line = fileReader.readLine();
            }
        } catch (IOException e) {
            Log.e(Tag, "File \"" + filename + "\" does not exist");
            return "";
        } finally {
            if (fileReader != null) {
                try {
                    fileReader.close();
                } catch (IOException e) {
                    Log.e(Tag, "Failed to close buffered reader");
                }
            }
        }
        return fileStringBuilder.toString();
    }
}
