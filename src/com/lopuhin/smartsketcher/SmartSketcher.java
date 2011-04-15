package com.lopuhin.smartsketcher;

import android.app.Activity;
import android.os.Bundle;


public class SmartSketcher extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MainSurfaceView sv = new MainSurfaceView(this);
        setContentView(sv);
        sv.resume();
    }
}

