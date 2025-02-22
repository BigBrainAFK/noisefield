package com.android.noisefield;

import android.view.MotionEvent;

public class ParticleManager
{
    public static class Particle
    {
        float x;
        float y;
        float speed;
        float wander;
        float alphaStart;
        float alpha;
        float life;
        float death;

        Particle(float[] data)
        {
            x = data[0];
            y = data[1];
            speed = data[3];
            wander = data[4];
            alphaStart = data[5];
            alpha = data[6];
            life = data[7];
            death = data[8];
        }

        public float[] toFloatArray()
        {
            return new float[]{x, y, 0, speed, wander, alphaStart, alpha, life, death};
        }
    }

    Noise noise = new Noise();

    //region Particle data
        private final int particleCount = 83;
        private final int particlePropertyCount = 9;
        private final int particleArrayLength = particleCount * particlePropertyCount;
        private final int particleArrayDataSize = particleArrayLength * 4;
        private final float[] particleData = new float[particleArrayLength];
    //endregion

    //region Touch data
        private boolean touchDown = false;
        private float touchX = 0.0f;
        private float touchY = 0.0f;
        private float touchInfluence = 0.0f;
    //endregion

    //region Dimensional data
        private int width;
        private int height;
        private boolean landscape;
    //endregion

    ParticleManager()
    {
        initializeParticles();
    }

    public int getParticleArrayDataSize()
    {
        return this.particleArrayDataSize;
    }

    public float[] getParticleData()
    {
        return particleData;
    }

    public int getParticleCount()
    {
        return particleCount;
    }

    public void setDimensions(int width, int height)
    {
        this.width = width;
        this.height = height;
        landscape = width > height;
    }

    private void initializeParticles()
    {
        for (int i = 0; i < particleCount; i++)
        {
            int index = i * particlePropertyCount;

            Particle particle = new Particle(new float[particlePropertyCount]);

            // Initialize position
            particle.x = noise.boundRandom(-1.0f, 1.0f);
            particle.y = noise.boundRandom(-1.0f, 1.0f);

            // Initialize rest
            particle.speed = noise.boundRandom(0.0002f, 0.02f);
            particle.wander = noise.boundRandom(0.50f, 1.5f);
            particle.death = 0;
            particle.life = noise.boundRandom(300, 800);
            particle.alphaStart = noise.boundRandom(0.01f, 1.0f);
            particle.alpha = particle.alphaStart;

            float[] newParticle = particle.toFloatArray();
            System.arraycopy(newParticle, 0, particleData, index, particlePropertyCount);
        }
    }

    public void updateParticles(long deltaTime)
    {
        float rads, speed;
        float deltaTimeFactor = deltaTime / 33.0f; // This adjusts it to the designed ~30FPS

        for (int i = 0; i < particleCount; i++)
        {
            int index = i * particlePropertyCount;

            float[] initialRawData = new float[particlePropertyCount];

            System.arraycopy(particleData, index, initialRawData, 0, particlePropertyCount);

            Particle particle = new Particle(initialRawData);

            if (particle.life < 0.0f || particle.x < -1.2f ||
                    particle.x > 1.2f || particle.y < -1.7f ||
                    particle.y > 1.7f) {
                particle.x = noise.boundRandom(-1.0f, 1.0f);
                particle.y = noise.boundRandom(-1.0f, 1.0f);
                particle.speed = noise.boundRandom(0.0002f, 0.02f);
                particle.wander = noise.boundRandom(0.50f, 1.5f);
                particle.death = 0;
                particle.life = noise.boundRandom(300, 800);
                particle.alphaStart = noise.boundRandom(0.01f, 1.0f);
                particle.alpha = particle.alphaStart;
            }

            float touchDist = (float) Math.sqrt(Math.pow(touchX - particle.x, 2) +
                    Math.pow(touchY - particle.y, 2));

            float noiseValue = noise.getNoiseFloat2(particle.x, particle.y);

            if (touchDown || touchInfluence > 0.0f)
            {
                if (touchDown)
                {
                    touchInfluence = 1.0f;
                }

                rads = (float) Math.atan2(touchX - particle.x + noiseValue,
                        touchY - particle.y + noiseValue);

                if (touchDist > 0.0f)
                {
                    speed = (0.25f + (noiseValue * particle.speed + 0.01f)) / touchDist * 0.3f;
                    speed *= touchInfluence;
                }
                else
                {
                    speed = 0.3f;
                }

                speed *= deltaTimeFactor;

                particle.x += (float) Math.cos(rads) * speed * 0.2f;
                particle.y += (float) Math.sin(rads) * speed * 0.2f;
            }

            speed = noiseValue * particle.speed + 0.01f;
            speed *= deltaTimeFactor;
            rads = (360 * noiseValue * particle.wander) * (float) Math.PI / 180.0f;

            particle.x += (float) Math.cos(rads) * speed * 0.24f;
            particle.y += (float) Math.sin(rads) * speed * 0.24f;

            particle.life -= deltaTimeFactor;
            particle.death += deltaTimeFactor;

            float dist = (float) Math.sqrt(Math.pow(particle.x, 2) + Math.pow(particle.y, 2));

            if (dist >= 0.95f) {
                if (particle.alphaStart < 1.0f)
                {
                    particle.alphaStart += 0.01f * deltaTimeFactor;
                    particle.alphaStart *= 1 - (dist - 0.95f);
                }
            }

            if (particle.death < 101.0f)
            {
                particle.alpha = particle.alphaStart * particle.death / 100.0f;
            }
            else if (particle.life < 101.0f)
            {
                particle.alpha = particle.alpha * particle.life / 100.0f;
            }
            else
            {
                particle.alpha = particle.alphaStart;
            }

            float[] updatedRawData = particle.toFloatArray();
            System.arraycopy(updatedRawData, 0, particleData, index, particlePropertyCount);
        }

        if (touchInfluence > 0)
        {
            touchInfluence -= 0.01f * deltaTimeFactor;
        }
    }

    public void onTouch(MotionEvent event)
    {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_POINTER_UP) {
            touchDown = false;
        }
        else if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE || action == MotionEvent.ACTION_POINTER_DOWN)
        {
            int pointerCount = event.getPointerCount();

            if (!touchDown)
            {
                touchDown = true;
            }

            if (pointerCount > 0)
            {
                float wRatio = 1.0f;
                float hRatio = 1.0f;

                if (!landscape)
                {
                    hRatio = (float) height / width;
                }
                else
                {
                    wRatio = (float) width / height;
                }

                touchInfluence = 1.0f;

                touchX = event.getX(0) / width * wRatio * 2 - wRatio;
                touchY = -(event.getY(0) / height * hRatio * 2 - hRatio);
            }
        }
    }
}
