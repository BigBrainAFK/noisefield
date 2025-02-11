package com.android.noisefield;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class NoiseFieldView extends GLSurfaceView {
    private NoiseFieldRenderer renderer;

    public NoiseFieldView(Context context) {
        super(context);

        final ActivityManager activityManager = (ActivityManager) getContext().getSystemService(Context.ACTIVITY_SERVICE);
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x00020000;

        if (supportsEs2)
        {
            setEGLContextClientVersion(2);

            setPreserveEGLContextOnPause(true);

            renderer = new NoiseFieldRenderer(getContext());
            setRenderer(renderer);

            DisplayMetrics metrics = new DisplayMetrics();
            ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
            renderer.setDensityDPI(metrics.densityDpi);
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        super.surfaceCreated(surfaceHolder);

        surfaceHolder.setSizeFromLayout();
        surfaceHolder.setFormat(PixelFormat.RGB_888);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (renderer != null) {
            renderer.onTouch(event);
        }

        return super.onTouchEvent(event);
    }
}
