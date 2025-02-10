package com.android.noisefield;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NoiseFieldRenderer implements GLSurfaceView.Renderer {

    private static final String TAG = "NoiseFieldRenderer";

    private final Context context;

    // Shader program ID
    private int programId;

    // Vertex Buffer Object (VBO)
    private int vboId;

    // Vertex count
    private int vertexCount;

    public NoiseFieldRenderer(Context context) {
        this.context = context;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "Created surface setting up GLES");
        // Set the clear color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        try {
            // Compile shaders and link the program
            String vertexShaderSource = loadShaderSource(context.getResources(), R.raw.bg_vs);
            String fragmentShaderSource = loadShaderSource(context.getResources(), R.raw.bg_fs);

            Log.d(TAG, vertexShaderSource);

            Log.d(TAG, fragmentShaderSource);

            int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
            int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

            programId = GLES20.glCreateProgram();
            GLES20.glAttachShader(programId, vertexShader);
            GLES20.glAttachShader(programId, fragmentShader);
            GLES20.glLinkProgram(programId);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programId, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0) {
                String error = GLES20.glGetProgramInfoLog(programId);
                Log.e(TAG, "Program linking failed: " + error);
                throw new RuntimeException("Program linking failed: " + error);
            }

            // Load vertex data from a resource file
            float[] vertexData = loadVertexData(R.raw.bgmesh);

            // Calculate vertex count
            vertexCount = vertexData.length / 5; // 5 elements per vertex (x, y, r, g, b)

            // Create VBO and upload vertex data
            int[] buffers = new int[1];
            GLES20.glGenBuffers(1, buffers, 0);
            vboId = buffers[0];

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
            GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4,
                    FloatBuffer.wrap(vertexData), GLES20.GL_STATIC_DRAW);

            Log.d(TAG, "Surface created successfully.");

        } catch (Exception e) {
            Log.e(TAG, "Error during onSurfaceCreated: " + e.getMessage(), e);
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        try {
            GLES20.glUseProgram(programId);

            // Bind VBO and enable vertex attributes
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

            GLES20.glEnableVertexAttribArray(0);
            GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 20, 0);

            GLES20.glEnableVertexAttribArray(1);
            GLES20.glVertexAttribPointer(1, 3, GLES20.GL_FLOAT, false, 20, 8);

            // Draw the vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

            Log.d(TAG, "Frame drawn successfully.");

        } catch (Exception e) {
            Log.e(TAG, "Error during onDrawFrame: " + e.getMessage(), e);
        }
    }

    private int compileShader(int type, String source) {
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);

        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if (compiled[0] == 0) {
            String error = GLES20.glGetShaderInfoLog(shader);
            GLES20.glDeleteShader(shader);
            throw new RuntimeException("Shader compilation failed: " + error);
        }

        return shader;
    }

    private String loadShaderSource(Resources resources, int resourceId) {
        InputStream inputStream = resources.openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder shaderSource = new StringBuilder();

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                shaderSource.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read shader source: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        return shaderSource.toString();
    }

    private float[] loadVertexData(int resourceId) {
        ArrayList<Float> vertexList = new ArrayList<>();
        InputStream inputStream = context.getResources().openRawResource(resourceId);
        BufferedReader reader = new BufferedReader(new InputStreamReader((inputStream)));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                for (String token : tokens) {
                    vertexList.add(Float.parseFloat(token));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load vertex data: " + e.getMessage());
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                // Ignore
            }
        }

        // Convert to float array
        float[] vertexData = new float[vertexList.size()];
        for (int i = 0; i < vertexList.size(); i++) {
            vertexData[i] = vertexList.get(i);
        }

        return vertexData;
    }
}
