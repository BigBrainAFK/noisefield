package com.android.noisefield;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class NoiseFieldWallpaper extends GLWallpaperService
{
    @Override
    public Engine onCreateEngine()
    {
        return new NoiseFieldEngine();
    }

    private class NoiseFieldEngine extends GLWallpaperService.GLEngine
    {
        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
        {
            super.onCreate(surfaceHolder);

            setTouchEventsEnabled(true);
            surfaceHolder.setSizeFromLayout();
            surfaceHolder.setFormat(PixelFormat.RGB_888);

            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x00020000;

            if (supportsEs2)
            {
                setEGLContextClientVersion(2);

                setPreserveEGLContextOnPause(true);

                setEGLConfigChooser(8, 8, 8, 8, 16, 0);

                renderer = new NoiseFieldRenderer(NoiseFieldWallpaper.this);
                setRenderer(renderer);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                {
                    renderer.setDensityDPI(getResources().getDisplayMetrics().densityDpi);
                }
                else
                {
                    DisplayMetrics metrics = new DisplayMetrics();
                    ((WindowManager) NoiseFieldWallpaper.this.getApplication().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
                    renderer.setDensityDPI(metrics.densityDpi);
                }
            }
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            super.onSurfaceChanged(holder,  format,  width,  height);
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder)
        {
            super.onSurfaceRedrawNeeded(holder);
        }

        @Override
        public void onTouchEvent(MotionEvent event)
        {
            super.onTouchEvent(event);

            if (renderer != null)
            {
                renderer.onTouch(event);
            }
        }
    }
}