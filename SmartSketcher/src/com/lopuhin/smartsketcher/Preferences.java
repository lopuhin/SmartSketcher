package com.lopuhin.smartsketcher;

import android.os.Bundle;
import android.preference.PreferenceActivity;


public class Preferences extends PreferenceActivity {
    public final static String TOOLBAL_VISIBLE = "TOOLBAL_VISIBLE";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

}
