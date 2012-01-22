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

    // menu buttons
    static final private int
        UNDO_ITEM = Menu.FIRST + 1, 
        REDO_ITEM = Menu.FIRST + 2,
        NEW_ITEM = Menu.FIRST + 3,
        OPEN_ITEM = Menu.FIRST + 4,
        CLEAR_ITEM = Menu.FIRST + 5,
        ERASE_ITEM = Menu.FIRST + 6,
        DRAW_ITEM = Menu.FIRST + 7,
        HAND_ITEM = Menu.FIRST + 8,
        PALETTE_ITEM = Menu.FIRST + 11,
        DELETE_ITEM = Menu.FIRST + 12,
        PREFERENCES_ITEM = Menu.FIRST + 13;

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

        menu.add(0, UNDO_ITEM, Menu.NONE, R.string.undo).setIcon(R.drawable.undo);
        menu.add(0, REDO_ITEM, Menu.NONE, R.string.redo).setIcon(R.drawable.redo);
        menu.add(0, DRAW_ITEM, Menu.NONE, R.string.draw).setIcon(R.drawable.marker);
        menu.add(0, ERASE_ITEM, Menu.NONE, R.string.erase).setIcon(R.drawable.eraser);
        menu.add(0, HAND_ITEM, Menu.NONE, R.string.hand).setIcon(R.drawable.hand);
        menu.add(0, PALETTE_ITEM, Menu.NONE, R.string.palette).setIcon(R.drawable.palette);
        menu.add(0, CLEAR_ITEM, Menu.NONE, R.string.clear);
        menu.add(0, NEW_ITEM, Menu.NONE, R.string.newitem);
        menu.add(0, OPEN_ITEM, Menu.NONE, R.string.open);
        menu.add(0, DELETE_ITEM, Menu.NONE, R.string.delete);
        menu.add(0, PREFERENCES_ITEM, Menu.NONE, R.string.preferences);
            
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
        menu.findItem(UNDO_ITEM)
            .setEnabled(mainSurfaceView.getSheet().canUndo())
            .setVisible(!isToolbarVisible);
        menu.findItem(REDO_ITEM)
            .setEnabled(mainSurfaceView.getSheet().canRedo())
            .setVisible(!isToolbarVisible);
            
        final int instrument = mainSurfaceView.getInstrument();
        menu.findItem(DRAW_ITEM)
            .setEnabled(instrument != MainSurfaceView.DRAW_INSTRUMENT)
            .setVisible(!isToolbarVisible);
        menu.findItem(ERASE_ITEM)
            .setEnabled(instrument != MainSurfaceView.ERASE_INSTRUMENT)
            .setVisible(!isToolbarVisible);
        menu.findItem(HAND_ITEM).setVisible(!isToolbarVisible);
        menu.findItem(PALETTE_ITEM).setVisible(!isToolbarVisible);

        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        /**
         * Handle menu items
         */
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
        case (UNDO_ITEM) : 
            undo_pressed();
            return true;
        case (REDO_ITEM) :
            redo_pressed();
            return true;
        case (DRAW_ITEM) :
            draw_pressed();
            return true;
        case (ERASE_ITEM) :
            erase_pressed();
            return true;
        case (CLEAR_ITEM) :
            mainSurfaceView.clearSheet();
            return true;
        case (NEW_ITEM) :
            fileHelper.savePreview();
            mainSurfaceView.clearSheet();	
            return true;
        case (OPEN_ITEM) :
            fileHelper.savePreview();
            Intent intent = new Intent(SmartSketcher.this, OpenSheetActivity.class);
            startActivityForResult(intent, OPEN_SHEET_RESULT);
            return true;
        case (PREFERENCES_ITEM) :
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

