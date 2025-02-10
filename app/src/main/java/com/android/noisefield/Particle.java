package com.android.noisefield;

public class Particle {
    float x;
    float y;
    float speed;
    float wander;
    float alphaStart;
    float alpha;
    int life;
    int death;

    Particle(float[] data) {
        x = data[0];
        y = data[1];
        speed = data[3];
        wander = data[4];
        alphaStart = data[5];
        alpha = data[6];
        life = Math.round(data[7]);
        death = Math.round(data[8]);
    }

    public float[] toFloatArray() {
        return new float[]{x, y, 0, speed, wander, alphaStart, alpha, (float)life, (float)death};
    }
}
