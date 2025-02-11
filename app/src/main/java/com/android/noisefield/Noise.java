package com.android.noisefield;

import java.util.Random;

public class Noise
{
    private final Random random = new Random();

    private static final int B = 0x100;
    private static final int BM = 0xff;
    private static final int N = 0x1000;

    private final int[] p = new int[B + B + 2];
    private final float[][] g3 = new float[B + B + 2][3];
    private final float[][] g2 = new float[B + B + 2][2];

    Noise()
    {
        initNoise();
    }

    private void initNoise()
    {
        int i, j, k;
        float[] g1 = new float[B + B + 2];

        for (i = 0; i < B; i++)
        {
            p[i] = i;

            g1[i] = (float) (boundRandom(B * 2) - B) / B;

            for (j = 0; j < 2; j++)
            {
                g2[i][j] = (float) (boundRandom(B * 2) - B) / B;
            }
            normalizeVec2(g2[i]);

            for (j = 0; j < 3; j++)
            {
                g3[i][j] = (float) (boundRandom(B * 2) - B) / B;
            }
            normalizeVec3(g3[i]);
        }

        for (i = B - 1; i >= 0; i--)
        {
            k = p[i];
            p[i] = p[j = boundRandom(B)];
            p[j] = k;
        }

        for (i = 0; i < B + 2; i++)
        {
            p[B + i] = p[i];
            g1[B + i] = g1[i];
            for (j = 0; j < 2; j++)
            {
                g2[B + i][j] = g2[i][j];
            }
            for (j = 0; j < 3; j++) {
                g3[B + i][j] = g3[i][j];
            }
        }
    }

    private float noise_sCurve(float t)
    {
        return t * t * (3.0f - 2.0f * t);
    }

    private void normalizeVec2(float[] v)
    {
        float s = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1]);
        v[0] /= s;
        v[1] /= s;
    }

    private void normalizeVec3(float[] v)
    {
        float s = (float) Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);
        v[0] /= s;
        v[1] /= s;
        v[2] /= s;
    }

    public float getNoiseFromVec2(float x, float y)
    {
        int bx0, bx1, by0, by1, b00, b10, b01, b11;
        float rx0, rx1, ry0, ry1, sx, sy, a, b, t, u, v;
        float[] q;
        int i, j;

        t = x + N;
        bx0 = ((int) t) & BM;
        bx1 = (bx0 + 1) & BM;
        rx0 = t - (int) t;
        rx1 = rx0 - 1.0f;

        t = y + N;
        by0 = ((int) t) & BM;
        by1 = (by0 + 1) & BM;
        ry0 = t - (int) t;
        ry1 = ry0 - 1.0f;

        i = p[bx0];
        j = p[bx1];

        b00 = p[i + by0];
        b10 = p[j + by0];
        b01 = p[i + by1];
        b11 = p[j + by1];

        sx = noise_sCurve(rx0);
        sy = noise_sCurve(ry0);

        q = g2[b00];
        u = rx0 * q[0] + ry0 * q[1];
        q = g2[b10];
        v = rx1 * q[0] + ry0 * q[1];
        a = linearInterpolate(u, v, sx);

        q = g2[b01];
        u = rx0 * q[0] + ry1 * q[1];
        q = g2[b11];
        v = rx1 * q[0] + ry1 * q[1];
        b = linearInterpolate(u, v, sx);

        return 1.5f * linearInterpolate(a, b, sy);
    }

    private float linearInterpolate(float a, float b, float f)
    {
        return a * (1.0f - f) + (b * f);
    }

    private int boundRandom(int max)
    {
        return random.nextInt(max + 1);
    }

    public float boundRandom(float min, float max)
    {
        return min + random.nextFloat() * (max - min);
    }

    public int boundRandom(int min, int max)
    {
        return random.nextInt(max + 1 - min) + min;
    }
}
