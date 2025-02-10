package com.android.noisefield;

import android.app.Activity;
import android.os.Bundle;

public class NoiseField extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        NoiseFieldView mView = new NoiseFieldView(this);
        setContentView(mView);
    }
}
