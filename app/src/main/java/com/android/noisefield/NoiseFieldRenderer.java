package com.android.noisefield;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NoiseFieldRenderer implements GLSurfaceView.Renderer
{
    //region Data
        private final Context context;
        private final ParticleManager particleManager = new ParticleManager();
        private int densityDPI;
        private long startTime;
    //endregion

    //region OpenGL ES2.0 Data
        // Shader program ID
        private int backgroundProgramId;
        private int particleProgramId;

        // Vertex Buffer Object (VBO)
        private int backgroundVboId;
        private int particleVboId;

        // Texture id for particles
        private int particleTextureId;

        // Projection matrix
        private final float[] mvpMatrix = new float[16];

        // Scaling factor
        private float scaleSize;
    //endregion

    public NoiseFieldRenderer(Context context)
    {
        this.context = context;
    }

    //region Surface handling
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config)
        {
            // Set the clear color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            // Enable blending for transparency
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

            try {
                backgroundProgramId = setupProgram(R.raw.bg_vs, R.raw.bg_fs);

                // Create VBO and upload vertex data
                int[] buffers = new int[2];
                GLES20.glGenBuffers(2, buffers, 0);
                backgroundVboId = buffers[0];

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundVboId);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, BackgroundManager.vertexData.length * 4,
                        FloatBuffer.wrap(BackgroundManager.vertexData), GLES20.GL_STATIC_DRAW);

                particleProgramId = setupProgram(R.raw.noisefield_vs, R.raw.noisefield_fs);

                // Load particle texture
                particleTextureId = loadTexture(R.drawable.dot);

                // Create particle VBO
                particleVboId = buffers[1];

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVboId);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, particleManager.getParticleData().length * 4,
                        FloatBuffer.wrap(particleManager.getParticleData()), GLES20.GL_DYNAMIC_DRAW);

            }
            catch (Exception ignored) {}
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height)
        {
            GLES20.glViewport(0, 0, width, height);
            setupProjectionMatrix(width, height);
            scaleSize = densityDPI / 240.0f;
            particleManager.setDimensions(width, height);
        }
    //endregion

    //region Setters
        public void setDensityDPI(int densityDPI)
        {
            this.densityDPI = densityDPI;
        }
    //endregion

    //region Draw handling
        @Override
        public void onDrawFrame(GL10 gl)
        {
            // Some older Android images don't limit to 60FPS themselves
            long endTime = System.currentTimeMillis();
            long dt = endTime - startTime;
            if (dt < 17 && dt > 0)
            {
                try {
                    Thread.sleep(17 - dt);
                }
                catch (Exception ignored) {}
            }
            startTime = System.currentTimeMillis();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            try {
                drawBackground();

                // Update and draw particles
                particleManager.updateParticles();

                drawParticles();
            }
            catch (Exception ignored) {}
        }
    //endregion

    //region Draw handlers
        private void drawBackground()
        {
            GLES20.glUseProgram(backgroundProgramId);

            // Bind VBO and enable vertex attributes
            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundVboId);

            // position x, y
            GLES20.glEnableVertexAttribArray(0);
            GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 20, 0);

            // color r, g, b
            GLES20.glEnableVertexAttribArray(1);
            GLES20.glVertexAttribPointer(1, 3, GLES20.GL_FLOAT, false, 20, 8);

            // Draw the vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, BackgroundManager.vertexCount);
        }

        public void drawParticles()
        {
            // Render particles
            GLES20.glUseProgram(particleProgramId);

            GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVboId);
            GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, particleManager.getParticleArrayDataLength(), FloatBuffer.wrap(particleManager.getParticleData()));

            // Pass float x, y and z
            GLES20.glEnableVertexAttribArray(0);
            GLES20.glVertexAttribPointer(0, 3, GLES20.GL_FLOAT, false, 36, 0);

            // Pass float speed
            GLES20.glEnableVertexAttribArray(1);
            GLES20.glVertexAttribPointer(1, 1, GLES20.GL_FLOAT, false, 36, 12);

            // Pass float alpha
            GLES20.glEnableVertexAttribArray(2);
            GLES20.glVertexAttribPointer(2, 1, GLES20.GL_FLOAT, false, 36, 24);

            // Pass MVP matrix
            GLES20.glUniformMatrix4fv(0, 1, false, mvpMatrix, 0);

            // Pass Scale size
            GLES20.glUniform1f(1, scaleSize);

            // Bind particle texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, particleTextureId);
            GLES20.glUniform1i(2, 0);

            GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleManager.getParticleCount());
        }
    //endregion

    //region OpenGL helper functions
        private int setupProgram(int vertexShaderResourceId, int fragmentShaderResourceId1)
        {
            String vertexShaderSource = loadShaderSource(context.getResources(), vertexShaderResourceId);
            String fragmentShaderSource = loadShaderSource(context.getResources(), fragmentShaderResourceId1);

            int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
            int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

            int tempStore = GLES20.glCreateProgram();
            GLES20.glAttachShader(tempStore, vertexShader);
            GLES20.glAttachShader(tempStore, fragmentShader);
            GLES20.glLinkProgram(tempStore);

            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(tempStore, GLES20.GL_LINK_STATUS, linkStatus, 0);
            if (linkStatus[0] == 0)
            {
                String error = GLES20.glGetProgramInfoLog(tempStore);
                throw new RuntimeException("Program linking failed: " + error);
            }

            return tempStore;
        }

        private int compileShader(int type, String source)
        {
            int shader = GLES20.glCreateShader(type);
            GLES20.glShaderSource(shader, source);
            GLES20.glCompileShader(shader);

            int[] compiled = new int[1];
            GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
            if (compiled[0] == 0)
            {
                String error = GLES20.glGetShaderInfoLog(shader);
                GLES20.glDeleteShader(shader);
                throw new RuntimeException("Shader compilation failed: " + error);
            }

            return shader;
        }

        private String loadShaderSource(Resources resources, int resourceId)
        {
            InputStream inputStream = resources.openRawResource(resourceId);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder shaderSource = new StringBuilder();

            try
            {
                String line;
                while ((line = reader.readLine()) != null)
                {
                    shaderSource.append(line).append("\n");
                }
            }
            catch (IOException e)
            {
                throw new RuntimeException("Failed to read shader source: " + e.getMessage());
            }
            finally
            {
                try
                {
                    inputStream.close();
                }
                catch (IOException ignored) {}
            }

            return shaderSource.toString();
        }

        private int loadTexture(int resourceId)
        {
            // Generate a texture ID
            final int[] textureHandle = new int[1];
            GLES20.glGenTextures(1, textureHandle, 0);

            if (textureHandle[0] != 0)
            {
                // Load the texture resource as a Bitmap
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inScaled = false; // No pre-scaling
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

                // Bind to the texture ID
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

                // Set texture parameters
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

                // Load the bitmap into the bound texture
                GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

                // Recycle the bitmap, as it's no longer needed
                bitmap.recycle();
            }
            else
            {
                throw new RuntimeException("Error generating texture handle.");
            }

            return textureHandle[0];
        }

        private void setupProjectionMatrix(int width, int height)
        {
            if (width > height)
            {
                float aspectRatio = (float) width / height;
                Matrix.frustumM(mvpMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, 1.0f, 100.0f);
            }
            else
            {
                float aspectRatio = (float) height / width;
                Matrix.frustumM(mvpMatrix, 0, -1.0f, 1.0f, -aspectRatio, aspectRatio, 1.0f, 100.0f);
            }

            // Apply additional transformations like the original code
            Matrix.rotateM(mvpMatrix, 0, 180.0f, 0.0f, 1.0f, 0.0f);
            Matrix.scaleM(mvpMatrix, 0, -1.0f, 1.0f, 1.0f);
            Matrix.translateM(mvpMatrix, 0, 0.0f, 0.0f, 1.0f);
        }
    //endregion

    //region Touch passthrough
        public void onTouch(MotionEvent event)
        {
            particleManager.onTouch(event);
        }
    //endregion
}
