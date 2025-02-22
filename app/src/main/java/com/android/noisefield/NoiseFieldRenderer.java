package com.android.noisefield;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.Matrix;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class NoiseFieldRenderer implements GLSurfaceView.Renderer
{
    private String TAG = "Noisefield Renderer";

    //region Data
        private final Context context;
        private final ParticleManager particleManager = new ParticleManager();
        private int densityDPI;
        private long startTime = SystemClock.uptimeMillis();
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
        // XOffset
        private float xOffset = 0.0f;


        // Background program locations
        private int aBackgroundPositionLocation;
        private int aBackgroundColorLocation;
        private int uBackgroundXOffsetLocation;

        // Particle program locations
        private int aParticlePositionLocation;
        private int aParticleSpeedLocation;
        private int aParticleAlphaLocation;
        private int uParticleMVPMatrixLocation;
        private int uParticleScaleLocation;
        private int uParticleTextureLocaiton;
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

            setupBackground();

            setupParticles();
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

        public void setOffset(float xOffset, float yOffset, int xPixels, int yPixels)
        {
            this.xOffset = xOffset;
        }
    //endregion

    //region Draw handling
        @Override
        public void onDrawFrame(GL10 gl)
        {
            long endTime = SystemClock.uptimeMillis();
            long deltaTime = endTime - startTime;
            startTime = SystemClock.uptimeMillis();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);


            drawBackground();

            // Update and draw particles
            particleManager.updateParticles(deltaTime);

            drawParticles();
        }
    //endregion

    //region Draw handlers
        private void setupBackground()
        {
            backgroundProgramId = setupProgram(R.raw.bg_vs, R.raw.bg_fs);

            // Fetch shader locations as Mali/Adreno sort these differently
            aBackgroundPositionLocation = GLES20.glGetAttribLocation(backgroundProgramId, "aPosition");
            aBackgroundColorLocation = GLES20.glGetAttribLocation(backgroundProgramId, "aColor");
            uBackgroundXOffsetLocation = GLES20.glGetUniformLocation(backgroundProgramId, "uXOffset");

            Log.d(TAG, "background position location: " + aBackgroundPositionLocation);
            Log.d(TAG, "background color location: " + aBackgroundColorLocation);
            Log.d(TAG, "background offset location: " + uBackgroundXOffsetLocation);

            // Create VBO and upload vertex data
            int[] buffers = new int[1];
            GLES20.glGenBuffers(1, buffers, 0);
            backgroundVboId = buffers[0];

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundVboId);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, BackgroundManager.vertexDataSize,
                        FloatBuffer.wrap(BackgroundManager.vertexData), GLES20.GL_STATIC_DRAW);
            }

            // position x, y
            GLES20.glEnableVertexAttribArray(aBackgroundPositionLocation);

            // color r, g, b
            GLES20.glEnableVertexAttribArray(aBackgroundColorLocation);
        }

        private void setupParticles()
        {
            particleProgramId = setupProgram(R.raw.noisefield_vs, R.raw.noisefield_fs);

            // Load particle texture
            particleTextureId = loadTexture(R.drawable.dot);

            // Fetch shader locations as Mali/Adreno sort these differently
            aParticlePositionLocation = GLES20.glGetAttribLocation(particleProgramId, "aPosition");
            aParticleSpeedLocation = GLES20.glGetAttribLocation(particleProgramId, "aSpeed");
            aParticleAlphaLocation = GLES20.glGetAttribLocation(particleProgramId, "aAlpha");
            uParticleMVPMatrixLocation = GLES20.glGetUniformLocation(particleProgramId, "uMVPMatrix");
            uParticleScaleLocation = GLES20.glGetUniformLocation(particleProgramId, "uScaleSize");
            uParticleTextureLocaiton = GLES20.glGetUniformLocation(particleProgramId, "uTexture");

            Log.d(TAG, "particle position location: " + aParticlePositionLocation);
            Log.d(TAG, "particle speed location: " + aParticleSpeedLocation);
            Log.d(TAG, "particle alpha location: " + aParticleAlphaLocation);
            Log.d(TAG, "particle matrix location: " + uParticleMVPMatrixLocation);
            Log.d(TAG, "particle scale location: " + uParticleScaleLocation);
            Log.d(TAG, "particle texture location: " + uParticleTextureLocaiton);

            // Create particle VBO
            int[] buffers = new int[1];
            GLES20.glGenBuffers(1, buffers, 0);
            particleVboId = buffers[0];

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVboId);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, particleManager.getParticleArrayDataSize(),
                        FloatBuffer.wrap(particleManager.getParticleData()), GLES20.GL_DYNAMIC_DRAW);
            }

            // Pass float x, y and z
            GLES20.glEnableVertexAttribArray(aParticlePositionLocation);

            // Pass float speed
            GLES20.glEnableVertexAttribArray(aParticleSpeedLocation);

            // Pass float alpha
            GLES20.glEnableVertexAttribArray(aParticleAlphaLocation);

            // Bind particle texture
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, particleTextureId);
        }

        private void drawBackground()
        {
            GLES20.glUseProgram(backgroundProgramId);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, backgroundVboId);

                int[] bufferSize = new int[1];
                GLES20.glGetBufferParameteriv(GLES20.GL_ARRAY_BUFFER, GLES20.GL_BUFFER_SIZE, bufferSize, 0);

                if (bufferSize[0] == 0) {
                    ByteBuffer nativeByteBuffer = ByteBuffer.allocateDirect(BackgroundManager.vertexDataSize);
                    nativeByteBuffer.order(ByteOrder.nativeOrder());

                    FloatBuffer backgroundVertexDataBuffer = nativeByteBuffer.asFloatBuffer();
                    backgroundVertexDataBuffer.put(BackgroundManager.vertexData).position(0);

                    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, BackgroundManager.vertexDataSize,
                            backgroundVertexDataBuffer, GLES20.GL_STATIC_DRAW);
                }

                // position x, y
                GLES20.glVertexAttribPointer(aBackgroundPositionLocation, 2, GLES20.GL_FLOAT, false, 20, 0);

                // color r, g, b
                GLES20.glVertexAttribPointer(aBackgroundColorLocation, 3, GLES20.GL_FLOAT, false, 20, 8);
            }
            else
            {
                ByteBuffer nativeByteBuffer = ByteBuffer.allocateDirect(BackgroundManager.vertexDataSize);
                nativeByteBuffer.order(ByteOrder.nativeOrder());

                FloatBuffer backgroundVertexDataBuffer = nativeByteBuffer.asFloatBuffer();
                backgroundVertexDataBuffer.put(BackgroundManager.vertexData).position(0);

                // position x, y
                GLES20.glVertexAttribPointer(aBackgroundPositionLocation, 2, GLES20.GL_FLOAT, false, 20, backgroundVertexDataBuffer);

                // color r, g, b
                GLES20.glVertexAttribPointer(aBackgroundColorLocation, 3, GLES20.GL_FLOAT, false, 20, backgroundVertexDataBuffer.position(2));
            }

            GLES20.glUniform1f(uBackgroundXOffsetLocation, xOffset);

            // Draw the vertices
            GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, BackgroundManager.vertexCount);
        }

        public void drawParticles()
        {
            // Render particles
            GLES20.glUseProgram(particleProgramId);

            ByteBuffer nativeByteBuffer = ByteBuffer.allocateDirect(particleManager.getParticleArrayDataSize());
            nativeByteBuffer.order(ByteOrder.nativeOrder());

            FloatBuffer particleVertexDataBuffer = nativeByteBuffer.asFloatBuffer();
            particleVertexDataBuffer.put(particleManager.getParticleData()).position(0);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
            {
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVboId);

                int[] bufferSize = new int[1];
                GLES20.glGetBufferParameteriv(GLES20.GL_ARRAY_BUFFER, GLES20.GL_BUFFER_SIZE, bufferSize, 0);

                if (bufferSize[0] == 0 || bufferSize[0] < particleManager.getParticleArrayDataSize())
                {
                    GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, particleManager.getParticleArrayDataSize(),
                            particleVertexDataBuffer, GLES20.GL_STATIC_DRAW);
                }
                else
                {
                    GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, particleManager.getParticleArrayDataSize(), particleVertexDataBuffer);
                }

                // Pass float x, y and z
                GLES20.glVertexAttribPointer(aParticlePositionLocation, 3, GLES20.GL_FLOAT, false, 36, 0);

                // Pass float speed
                GLES20.glVertexAttribPointer(aParticleSpeedLocation, 1, GLES20.GL_FLOAT, false, 36, 12);

                // Pass float alpha
                GLES20.glVertexAttribPointer(aParticleAlphaLocation, 1, GLES20.GL_FLOAT, false, 36, 24);
            }
            else
            {
                // Pass float x, y and z
                GLES20.glVertexAttribPointer(aParticlePositionLocation, 3, GLES20.GL_FLOAT, false, 36, particleVertexDataBuffer);

                // Pass float speed
                GLES20.glVertexAttribPointer(aParticleSpeedLocation, 1, GLES20.GL_FLOAT, false, 36, particleVertexDataBuffer.position(3));

                // Pass float alpha
                GLES20.glVertexAttribPointer(aParticleAlphaLocation, 1, GLES20.GL_FLOAT, false, 36, particleVertexDataBuffer.position(6));
            }

            // Pass MVP matrix
            GLES20.glUniformMatrix4fv(uParticleMVPMatrixLocation, 1, false, mvpMatrix, 0);

            // Pass Scale size
            GLES20.glUniform1f(uParticleScaleLocation, scaleSize);

            // Bind particle texture
            GLES20.glUniform1i(uParticleTextureLocaiton, 0);

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

                if (aspectRatio > 1.7f)
                {
                    aspectRatio = 1.7f;
                }

                Matrix.frustumM(mvpMatrix, 0, -aspectRatio, aspectRatio, -1.0f, 1.0f, 1.0f, 100.0f);
            }
            else
            {
                float aspectRatio = (float) height / width;

                if (aspectRatio > 1.7f)
                {
                    aspectRatio = 1.7f;
                }

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
