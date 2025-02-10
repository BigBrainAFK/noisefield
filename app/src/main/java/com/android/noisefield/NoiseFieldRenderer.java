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

public class NoiseFieldRenderer implements GLSurfaceView.Renderer {
    //region General data
        private final Context context;
        private final Noise noise = new Noise();
        private int densityDPI;
        private int width;
        private int height;
        private boolean landscape;
    //endregion

    //region OpenGL ES2.0 Data
        // Shader program ID
        private int backgroundProgramId;
        private int particleProgramId;

        // Vertex Buffer Object (VBO)
        private int vboId;
        private int particleVboId;

        // Texture id for particles
        private int particleTextureId;

        // Projection matrix
        private final float[] mvpMatrix = new float[16];

        // Scaling factor
        private float scaleSize;
    //endregion

    //region Background Vertex setup and data
        private final float[] vertexData = new float[]{
        //       X          Y        R          G         B
                -1.50f,     1.0f,    0.080f,    0.335f,   0.406f,
                -1.50f,    -0.2f,    0.137f,    0.176f,   0.225f,
                -1.05f,     0.3f,    0.000f,    0.088f,   0.135f,
                -1.50f,     1.0f,    0.080f,    0.335f,   0.406f,
                -1.05f,     0.3f,    0.000f,    0.088f,   0.135f,
                -0.60f,     0.4f,    0.000f,    0.184f,   0.233f,
                -1.50f,     1.0f,    0.080f,    0.335f,   0.406f,
                -0.60f,     0.4f,    0.000f,    0.184f,   0.233f,
                +0.00f,     1.0f,    0.133f,    0.404f,   0.478f,
                +0.00f,     1.0f,    0.133f,    0.404f,   0.478f,
                -0.60f,     0.4f,    0.000f,    0.184f,   0.233f,
                +0.30f,     0.4f,    0.000f,    0.124f,   0.178f,
                +0.00f,     1.0f,    0.133f,    0.404f,   0.478f,
                +0.30f,     0.4f,    0.000f,    0.124f,   0.178f,
                +1.50f,     1.0f,    0.002f,    0.173f,   0.231f,
                +1.50f,     1.0f,    0.002f,    0.173f,   0.231f,
                +0.30f,     0.4f,    0.000f,    0.124f,   0.178f,
                +1.50f,    -1.0f,    0.000f,    0.088f,   0.135f,
                +0.30f,     0.4f,    0.000f,    0.124f,   0.178f,
                -0.60f,     0.4f,    0.000f,    0.184f,   0.233f,
                +0.00f,     0.2f,    0.000f,    0.088f,   0.135f,
                +0.30f,     0.4f,    0.000f,    0.124f,   0.178f,
                +0.00f,     0.2f,    0.000f,    0.088f,   0.135f,
                +1.50f,    -1.0f,    0.000f,    0.088f,   0.135f,
                +0.00f,     0.2f,    0.000f,    0.088f,   0.135f,
                -0.60f,     0.4f,    0.000f,    0.184f,   0.233f,
                -0.60f,     0.1f,    0.002f,    0.196f,   0.233f,
                -0.60f,     0.1f,    0.002f,    0.196f,   0.233f,
                -0.60f,     0.4f,    0.000f,    0.184f,   0.233f,
                -1.05f,     0.3f,    0.000f,    0.088f,   0.135f,
                -1.05f,     0.3f,    0.000f,    0.088f,   0.135f,
                -1.50f,    -0.2f,    0.137f,    0.176f,   0.225f,
                -0.60f,     0.1f,    0.002f,    0.196f,   0.233f,
                -0.45f,    -0.3f,    0.002f,    0.059f,   0.090f,
                -0.60f,     0.1f,    0.002f,    0.196f,   0.233f,
                -1.50f,    -0.2f,    0.137f,    0.176f,   0.225f,
                -0.45f,    -0.3f,    0.002f,    0.059f,   0.090f,
                -1.50f,    -0.2f,    0.137f,    0.176f,   0.225f,
                -1.50f,    -1.0f,    0.204f,    0.212f,   0.218f,
                +1.50f,    -1.0f,    0.000f,    0.088f,   0.135f,
                -0.45f,    -0.3f,    0.002f,    0.059f,   0.090f,
                -1.50f,    -1.0f,    0.204f,    0.212f,   0.218f,
                +0.00f,     0.2f,    0.000f,    0.088f,   0.135f,
                -0.60f,     0.1f,    0.002f,    0.196f,   0.233f,
                -0.45f,    -0.3f,    0.002f,    0.059f,   0.090f,
                +1.50f,    -1.0f,    0.000f,    0.088f,   0.135f,
                +0.00f,     0.2f,    0.000f,    0.088f,   0.135f,
                -0.45f,    -0.3f,    0.002f,    0.059f,   0.090f
        };
        private final int vertexCount = vertexData.length / 5;
    //endregion

    //region Particle data
        private float[] particleData;
        private final int particleDataSize = 9; // because we store 9 attributes
        private final int particleCount = 83;
    //endregion

    //region Touch event data
        private boolean touchDown;
        private float touchX;
        private float touchY;
        private float touchInfluence;
    //endregion

    public NoiseFieldRenderer(Context context) {
        this.context = context;
    }

    //region Surface handling
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            // Set the clear color
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

            // Initialize particles
            initializeParticles();

            // Enable blending for transparency
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE);

            try {

                // Compile shaders and link the program
                String vertexShaderSource = loadShaderSource(context.getResources(), R.raw.bg_vs);
                String fragmentShaderSource = loadShaderSource(context.getResources(), R.raw.bg_fs);

                int vertexShader = compileShader(GLES20.GL_VERTEX_SHADER, vertexShaderSource);
                int fragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderSource);

                backgroundProgramId = GLES20.glCreateProgram();
                GLES20.glAttachShader(backgroundProgramId, vertexShader);
                GLES20.glAttachShader(backgroundProgramId, fragmentShader);
                GLES20.glLinkProgram(backgroundProgramId);

                int[] linkStatus = new int[1];
                GLES20.glGetProgramiv(backgroundProgramId, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] == 0) {
                    String error = GLES20.glGetProgramInfoLog(backgroundProgramId);
                    throw new RuntimeException("Program linking failed: " + error);
                }

                // Create VBO and upload vertex data
                int[] buffers = new int[1];
                GLES20.glGenBuffers(1, buffers, 0);
                vboId = buffers[0];

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, vertexData.length * 4,
                        FloatBuffer.wrap(vertexData), GLES20.GL_STATIC_DRAW);

                // Compile particle shaders and initialize particle program
                String particleVertexShaderSource = loadShaderSource(context.getResources(), R.raw.noisefield_vs);
                String particleFragmentShaderSource = loadShaderSource(context.getResources(), R.raw.noisefield_fs);

                int particleVertexShader = compileShader(GLES20.GL_VERTEX_SHADER, particleVertexShaderSource);
                int particleFragmentShader = compileShader(GLES20.GL_FRAGMENT_SHADER, particleFragmentShaderSource);

                particleProgramId = GLES20.glCreateProgram();
                GLES20.glAttachShader(particleProgramId, particleVertexShader);
                GLES20.glAttachShader(particleProgramId, particleFragmentShader);
                GLES20.glLinkProgram(particleProgramId);

                GLES20.glGetProgramiv(particleProgramId, GLES20.GL_LINK_STATUS, linkStatus, 0);
                if (linkStatus[0] == 0) {
                    String error = GLES20.glGetProgramInfoLog(particleProgramId);
                    throw new RuntimeException("Particle program linking failed: " + error);
                }

                // Load particle texture
                particleTextureId = loadTexture(R.drawable.dot);

                // Create particle VBO
                GLES20.glGenBuffers(1, buffers, 0);
                particleVboId = buffers[0];

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVboId);
                GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, particleData.length * 4,
                        FloatBuffer.wrap(particleData), GLES20.GL_DYNAMIC_DRAW);

            } catch (Exception ignored) {
            }
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            setupProjectionMatrix(width, height);
            scaleSize = densityDPI / 240.0f;
            this.width = width;
            this.height = height;
            this.landscape = width > height;
        }
    //endregion

    //region Setters
        public void setDensityDPI(int densityDPI) {
            this.densityDPI = densityDPI;
        }
    //endregion

    //region Draw handling
        @Override
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

            try {
                GLES20.glUseProgram(backgroundProgramId);

                // Bind VBO and enable vertex attributes
                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, vboId);

                // position
                GLES20.glEnableVertexAttribArray(0);
                GLES20.glVertexAttribPointer(0, 2, GLES20.GL_FLOAT, false, 20, 0);

                // color
                GLES20.glEnableVertexAttribArray(1);
                GLES20.glVertexAttribPointer(1, 3, GLES20.GL_FLOAT, false, 20, 8);

                // Draw the vertices
                GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexCount);

                // Update and draw particles
                updateParticles();

                // Render particles
                GLES20.glUseProgram(particleProgramId);

                GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, particleVboId);
                GLES20.glBufferSubData(GLES20.GL_ARRAY_BUFFER, 0, particleData.length * 4, FloatBuffer.wrap(particleData));

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


                GLES20.glDrawArrays(GLES20.GL_POINTS, 0, particleCount);
            } catch (Exception ignored) {
            }
        }
    //endregion

    //region OpenGL helper functions
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

        private int loadTexture(int resourceId) {
            // Generate a texture ID
            final int[] textureHandle = new int[1];
            GLES20.glGenTextures(1, textureHandle, 0);

            if (textureHandle[0] != 0) {
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
            } else {
                throw new RuntimeException("Error generating texture handle.");
            }

            return textureHandle[0];
        }

        private void setupProjectionMatrix(int width, int height) {
            float aspectRatio = (float) width / height;

            if (width > height) {
                Matrix.frustumM(mvpMatrix, 0, -aspectRatio, aspectRatio, -1, 1, 1, 100);
            } else {
                Matrix.frustumM(mvpMatrix, 0, -1, 1, -1 / aspectRatio, 1 / aspectRatio, 1, 100);
            }

            // Apply additional transformations like the original code
            Matrix.rotateM(mvpMatrix, 0, 180, 0, 1, 0);
            Matrix.scaleM(mvpMatrix, 0, -1, 1, 1);
            Matrix.translateM(mvpMatrix, 0, 0, 0, 1);
        }
    //endregion

    //region Particle handling
        private void initializeParticles() {
            particleData = new float[particleCount * particleDataSize];

            for (int i = 0; i < particleCount; i++) {
                int index = i * particleDataSize;

                Particle particle = new Particle(new float[]{0f,0f,0f,0f,0f,0f,0f,0f,0f});

                // Initialize position
                particle.x = noise.rsRand(-1.0f, 1.0f);
                particle.y = noise.rsRand(-1.0f, 1.0f);

                // Initialize rest
                particle.speed = noise.rsRand(0.0002f, 0.02f);
                particle.wander = noise.rsRand(0.50f, 1.5f);
                particle.death = 0;
                particle.life = noise.rsRand(300, 800);
                particle.alphaStart = noise.rsRand(0.01f, 1.0f);
                particle.alpha = particle.alphaStart;

                float[] newParticle = particle.toFloatArray();
                System.arraycopy(newParticle, 0, particleData, index, particleDataSize);
            }
        }

        private void updateParticles() {
            float rads, speed;

            for (int i = 0; i < particleCount; i++) {
                int index = i * particleDataSize;

                float[] initialRawData = new float[particleDataSize];

                System.arraycopy(particleData, index, initialRawData, 0, particleDataSize);

                Particle particle = new Particle(initialRawData);

                if (particle.life < 0 || particle.x < -1.2 ||
                        particle.x > 1.2 || particle.y < -1.7 ||
                        particle.y > 1.7) {
                    particle.x = noise.rsRand(-1.0f, 1.0f);
                    particle.y = noise.rsRand(-1.0f, 1.0f);
                    particle.speed = noise.rsRand(0.0002f, 0.02f);
                    particle.wander = noise.rsRand(0.50f, 1.5f);
                    particle.death = 0;
                    particle.life = noise.rsRand(300, 800);
                    particle.alphaStart = noise.rsRand(0.01f, 1.0f);
                    particle.alpha = particle.alphaStart;
                }

                float touchDist = (float) Math.sqrt(Math.pow(touchX - particle.x, 2) +
                                                    Math.pow(touchY - particle.y, 2));

                float noiseval = noise.noisef2(particle.x, particle.y);

                if (touchDown || touchInfluence > 0.0f) {
                    if (touchDown) {
                        touchInfluence = 1.0f;
                    }

                    rads = (float) Math.atan2(touchX - particle.x + noiseval,
                                                    touchY - particle.y + noiseval);

                    if (touchDist > 0.0f) {
                        speed = (0.25f + (noiseval * particle.speed + 0.01f)) / touchDist * 0.3f;
                        speed = speed * touchInfluence;
                    } else {
                        speed = 0.3f;
                    }

                    particle.x += (float) Math.cos(rads) * speed * 0.2f;
                    particle.y += (float) Math.sin(rads) * speed * 0.2f;
                }

                float angle = 360 * noiseval * particle.wander;
                speed = noiseval * particle.speed + 0.01f;
                rads = angle * (float) Math.PI / 180.0f;

                particle.x += (float) Math.cos(rads) * speed * 0.24f;
                particle.y += (float) Math.sin(rads) * speed * 0.24f;

                particle.life--;
                particle.death++;

                float dist = (float) Math.sqrt(particle.x * particle.x +
                                               particle.y * particle.y);
                
                if (dist < 0.95f) {
                    dist = 0.0f;
                    particle.alphaStart *= 1 - dist;
                } else {
                    dist = dist - 0.95f;
                    if (particle.alphaStart < 1.0f) {
                        particle.alphaStart += 0.01f;
                        particle.alphaStart *= 1 - dist;
                    }
                }

                if (particle.death < 101) {
                    particle.alpha = (particle.alphaStart) * (particle.death) / 100.0f;
                } else if (particle.life < 101) {
                    particle.alpha = particle.alpha * particle.life / 100.0f;
                } else {
                    particle.alpha = particle.alphaStart;
                }

                float[] updatedRawData = particle.toFloatArray();
                System.arraycopy(updatedRawData, 0, particleData, index, particleDataSize);
            }

            // Reduce touch influence over time
            if (touchInfluence > 0) {
                touchInfluence -= 0.01f;
            }
    }
    //endregion

    //region Touch handling
        public void onTouch(MotionEvent event) {
            int action = event.getActionMasked();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP){
                touchDown = false;
            } else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_POINTER_DOWN) {
                int pointerCount = event.getPointerCount();

                if (!touchDown) {
                    touchDown = true;
                }

                if (pointerCount > 0) {
                    float wRatio = 1.0f;
                    float hRatio = 1.0f;

                    if (!landscape) {
                        hRatio = (float) height / width;
                    } else {
                        wRatio = (float) width / height;
                    }

                    touchInfluence = 1.0f;

                    touchX = event.getX(0) / width * wRatio * 2 - wRatio;
                    touchY = -(event.getY(0) / height * hRatio * 2 - hRatio);
                }
            }
        }
    //endregion
}
