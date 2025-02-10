package com.android.noisefield;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class NoiseFieldView extends GLSurfaceView {
    private final NoiseFieldRenderer renderer;

    public NoiseFieldView(Context context) {
        super(context);

        // Set OpenGL ES version
        setEGLContextClientVersion(2);

        // Initialize the custom renderer
        renderer = new NoiseFieldRenderer(context);

        // Set the renderer
        setRenderer(renderer);

        // Continuous rendering mode
        setRenderMode(RENDERMODE_CONTINUOUSLY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Pass touch events to the renderer
        //renderer.onTouchEvent(event);
        return true;
    }
}
