package com.android.noisefield;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.service.wallpaper.WallpaperService;
import android.util.Log;
import android.view.SurfaceHolder;

public abstract class GLWallpaperService extends WallpaperService {
    private static final String TAG = "GLWallpaperService";

    public class GLEngine extends Engine{
        private WallpaperGLSurfaceView glSurfaceView;
        private boolean rendererHasBeenSet;
        NoiseFieldRenderer renderer;

        class WallpaperGLSurfaceView extends GLSurfaceView {
            private static final String TAG = "WallpaperGLSurfaceView";

            WallpaperGLSurfaceView(Context context)
            {
                super(context);
            }

            public SurfaceHolder getHolder()
            {
                return getSurfaceHolder();
            }

            public void onDestroy()
            {
                super.onDetachedFromWindow();
            }
        }

        @Override
        public void onCreate(SurfaceHolder surfaceHolder)
        {
            super.onCreate(surfaceHolder);

            glSurfaceView = new WallpaperGLSurfaceView(GLWallpaperService.this);

            Log.d(TAG, "onCreate was called");
        }

        @Override
        public void onVisibilityChanged(boolean visible)
        {
            super.onVisibilityChanged(visible);
            if(rendererHasBeenSet)
            {
                if (visible)
                {
                    glSurfaceView.onResume();
                    //glSurfaceView.requestRender();
                    Log.d(TAG, "onResume was called");
                } else
                {
                    glSurfaceView.onPause();
                    Log.d(TAG, "onPause was called");
                }
            }
        }

        @Override
        public void onDestroy()
        {
            super.onDestroy();
            glSurfaceView.onDestroy();
            Log.d(TAG, "onDestroy was called");
        }

        protected void setRenderer(GLSurfaceView.Renderer renderer)
        {
            this.renderer = (NoiseFieldRenderer)renderer;
            glSurfaceView.setRenderer(renderer);
            rendererHasBeenSet = true;
            Log.d(TAG, "setRenderer was called");
        }

        protected void setPreserveEGLContextOnPause(boolean preserve)
        {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
            {
                glSurfaceView.setPreserveEGLContextOnPause(preserve);
            }
        }

        protected void setEGLContextClientVersion(int version)
        {
            glSurfaceView.setEGLContextClientVersion(version);
        }
    }
}
