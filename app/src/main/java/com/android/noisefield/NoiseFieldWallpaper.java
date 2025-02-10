package com.android.noisefield;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.SurfaceHolder;

public class NoiseFieldWallpaper extends GLWallpaperService {
    private static final String TAG = "NoiseFieldWallpaper";

    NoiseFieldRenderer renderer;

    @Override
    public Engine onCreateEngine() {
        Log.d(TAG, "onCreateEngine: Wallpaper engine created");
        return new NoiseFieldEngine();
    }

    private class NoiseFieldEngine extends GLWallpaperService.GLEngine {

        @Override
        public void onCreate(SurfaceHolder surfaceHolder) {
            super.onCreate(surfaceHolder);
            Log.d(TAG, "onCreate: Surface created");

            final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
            final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
            final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

            if (supportsEs2)
            {
                setEGLContextClientVersion(2);

                setPreserveEGLContextOnPause(true);

                renderer = new NoiseFieldRenderer(NoiseFieldWallpaper.this);

                setRenderer(renderer);

                //set up preference listener
            }

            Log.d(TAG, "onCreate: Renderer and GLSurfaceView initialized");
        }

        @Override
        public void onSurfaceChanged(SurfaceHolder holder, int format, int width, int height)
        {
            super.onSurfaceChanged( holder,  format,  width,  height);
            Log.d(TAG, "GLES2 onSurfaceChanged the surface was changed");
        }

        @Override
        public void onSurfaceRedrawNeeded(SurfaceHolder holder)
        {
            super.onSurfaceRedrawNeeded(holder);
            Log.d(TAG, "GLES2 RedrawNeeded the surface was redrawn");

        }
    }

    GLSurfaceView.Renderer getNewRenderer()
    {
        return renderer = new NoiseFieldRenderer(NoiseFieldWallpaper.this);
    }
}