package com.lopuhin.smartsketcher;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class Preferences extends PreferenceActivity {
    public final static String TOOLBAL_VISIBLE = "TOOLBAL_VISIBLE";
    public final static boolean TOOLBAL_VISIBLE_DEFAULT = true;
    public final static String THICKNESS = "THICKNESS";
    public final static float THICKNESS_DEFAULT = 3.0f;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
