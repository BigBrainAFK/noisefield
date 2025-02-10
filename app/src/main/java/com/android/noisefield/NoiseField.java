package com.android.noisefield;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class NoiseField extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MAIN", "Creating view");
        NoiseFieldView mView = new NoiseFieldView(this);
        Log.d("MAIN", "View created");
        setContentView(mView);
        Log.d("MAIN", "Content set to view");
    }
}
