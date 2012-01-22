package com.lopuhin.smartsketcher;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.LinearLayout;


public class SmartSketcher extends Activity 
    implements OnSharedPreferenceChangeListener
{
    /**
     * Main activity. Handles menu, opening activity
     */
    private MainSurfaceView mainSurfaceView;
    private DBAdapter dbAdapter;
    private FileHelper fileHelper;
    
    private static String TAG = "SmartSketcher";

    // activity results
    static final private int
        OPEN_SHEET_RESULT = 1,
        SHOW_PREFERENCES_RESULT = 2;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        mainSurfaceView = new MainSurfaceView(this, dbAdapter);
        ((LinearLayout)findViewById(R.id.sketchContainer)).addView(mainSurfaceView);
        ((ImageButton)findViewById(R.id.marker)).setEnabled(false);

        SharedPreferences prefs =
            PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);

        fileHelper = new FileHelper(mainSurfaceView);
        setupToolbar();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mainSurfaceView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mainSurfaceView.onPause();
        fileHelper.savePreview();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /**
         * Disable or enable menu items
         */
        super.onPrepareOptionsMenu(menu);

        SharedPreferences prefs = PreferenceManager
            .getDefaultSharedPreferences(getApplicationContext());

        final boolean isToolbarVisible = prefs.getBoolean(Preferences.TOOLBAL_VISIBLE, true);
        menu.findItem(R.id.undo)
            .setEnabled(mainSurfaceView.getSheet().canUndo())
            .setVisible(!isToolbarVisible);
        menu.findItem(R.id.redo)
            .setEnabled(mainSurfaceView.getSheet().canRedo())
            .setVisible(!isToolbarVisible);
            
        final int instrument = mainSurfaceView.getInstrument();
        menu.findItem(R.id.draw)
            .setEnabled(instrument != MainSurfaceView.DRAW_INSTRUMENT)
            .setVisible(!isToolbarVisible);
        menu.findItem(R.id.erase)
            .setEnabled(instrument != MainSurfaceView.ERASE_INSTRUMENT)
            .setVisible(!isToolbarVisible);
        menu.findItem(R.id.hand).setVisible(!isToolbarVisible);
        menu.findItem(R.id.palette).setVisible(!isToolbarVisible);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Handle menu items
         */
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case (R.id.undo) : 
            undo_pressed();
            return true;
        case (R.id.redo) :
            redo_pressed();
            return true;
        case (R.id.draw) :
            draw_pressed();
            return true;
        case (R.id.erase) :
            erase_pressed();
            return true;
        case (R.id.clear) :
            mainSurfaceView.clearSheet();
            return true;
        case (R.id.newitem) :
            fileHelper.savePreview();
            mainSurfaceView.clearSheet();	
            return true;
        case (R.id.open) :
            fileHelper.savePreview();
            Intent intent = new Intent(SmartSketcher.this, OpenSheetActivity.class);
            startActivityForResult(intent, OPEN_SHEET_RESULT);
            return true;
        case (R.id.preferences) :
            startActivityForResult(new Intent(this, Preferences.class),
                                   SHOW_PREFERENCES_RESULT);
            return true;
        }
        return false;
    }

    private void setupToolbar() {
        ((ImageButton)findViewById(R.id.undo))
            .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View unused) {
                        undo_pressed();
                    }});
        ((ImageButton)findViewById(R.id.redo))
            .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View unused) {
                        redo_pressed();
                    }});
        ((ImageButton)findViewById(R.id.marker))
            .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View unused) {
                        draw_pressed();
                    }});
        ((ImageButton)findViewById(R.id.eraser))
            .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View unused) {
                        erase_pressed();
                    }});
        ((ImageButton)findViewById(R.id.hand))
            .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View unused) {
                        // TODO
                    }});
        ((ImageButton)findViewById(R.id.palette))
            .setOnClickListener(new View.OnClickListener() {
                    public void onClick(View unused) {
                        // TODO
                    }});
    }

    private void hideToolbar() {
    	int animId;
    	final View buttonContainer = findViewById(R.id.buttonContainer);
    	if (buttonContainer.getVisibility() == View.INVISIBLE)
            return;
        if (getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_PORTRAIT)
            animId = R.drawable.move_toolbar_out_top;
        else
            animId = R.drawable.move_toolbar_out_left;
        Animation animation = AnimationUtils.loadAnimation(this, animId);
        animation.setAnimationListener(new AnimationListener() {
                public void onAnimationEnd(Animation _animation) {
                    buttonContainer.setVisibility(View.INVISIBLE);
                }
                public void onAnimationRepeat(Animation _animation) {}
                public void onAnimationStart(Animation _animation) {}
            });
        buttonContainer.startAnimation(animation);
    }

    private void showToolbar() {
    	int animId;
    	final View buttonContainer = findViewById(R.id.buttonContainer);
    	if (buttonContainer.getVisibility() == View.VISIBLE)
            return;
        if (getResources().getConfiguration().orientation ==
            Configuration.ORIENTATION_PORTRAIT)
            animId = R.drawable.move_toolbar_in_top;
        else
            animId = R.drawable.move_toolbar_in_left;
        buttonContainer.setVisibility(View.VISIBLE);
        buttonContainer.startAnimation(AnimationUtils.loadAnimation(this, animId));
    }
    
    private void undo_pressed () {
        mainSurfaceView.getSheet().undo();
    }

    private void redo_pressed () {
        mainSurfaceView.getSheet().redo();
    }

    private void draw_pressed() {
        mainSurfaceView.setInstrument(MainSurfaceView.DRAW_INSTRUMENT);
        ((ImageButton)findViewById(R.id.marker)).setEnabled(false);
        ((ImageButton)findViewById(R.id.eraser)).setEnabled(true);
    }

    private void erase_pressed() {
        mainSurfaceView.setInstrument(MainSurfaceView.ERASE_INSTRUMENT);
        ((ImageButton)findViewById(R.id.eraser)).setEnabled(false);
        ((ImageButton)findViewById(R.id.marker)).setEnabled(true);
    }
    
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        /**
         * Handle opening of saved sheet via OpenSheetActivity
         */
        if (resultCode == RESULT_OK && requestCode == OPEN_SHEET_RESULT) {
            long sheetId = data.getLongExtra("sheetId", -1);
            Log.d(TAG, "opening sheetId " + sheetId);
            if (sheetId != -1) {
                mainSurfaceView.setSheet(dbAdapter.loadSheet(sheetId));
            }
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(Preferences.TOOLBAL_VISIBLE)) {
            if (prefs.getBoolean(key, true))
                showToolbar();
            else
                hideToolbar();
        }
    }


}

