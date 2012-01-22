package com.lopuhin.smartsketcher;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class SmartSketcher extends Activity {
    /**
     * Main activity. Handles menu, opening activity
     */
    private MainSurfaceView mainSurfaceView;
    private DBAdapter dbAdapter;
    private FileHelper fileHelper;
    
    private static String TAG = "SmartSketcher";
        
    static final private int
        UNDO_ITEM = Menu.FIRST + 1, 
        REDO_ITEM = Menu.FIRST + 2,
        NEW_ITEM = Menu.FIRST + 3,
        OPEN_ITEM = Menu.FIRST + 4,
        CLEAR_ITEM = Menu.FIRST + 5,
        ERASE_ITEM = Menu.FIRST + 6,
        DRAW_ITEM = Menu.FIRST + 7,
        HAND_ITEM = Menu.FIRST + 8,
        SHOW_TOOLBAR_ITEM = Menu.FIRST + 9,
        HIDE_TOOLBAR_ITEM = Menu.FIRST + 10,
        PALETTE_ITEM = Menu.FIRST + 11,
        DELETE_ITEM = Menu.FIRST + 12;

    static final private int OPEN_SHEET_RESULT = 1;
    private boolean isToolbarVisible;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        isToolbarVisible = true; // TODO - load from resources
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();

        mainSurfaceView = new MainSurfaceView(this, dbAdapter);
        ((LinearLayout)findViewById(R.id.sketchContainer)).addView(mainSurfaceView);

        fileHelper = new FileHelper(mainSurfaceView);
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

        menu.add(0, HIDE_TOOLBAR_ITEM, Menu.NONE, R.string.hide_toolbar);
      	menu.add(0, SHOW_TOOLBAR_ITEM, Menu.NONE, R.string.show_toolbar);
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
            
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        /**
         * Disable or enable menu items
         */
        super.onPrepareOptionsMenu(menu);
        
        menu.findItem(HIDE_TOOLBAR_ITEM).setVisible(isToolbarVisible);
        menu.findItem(SHOW_TOOLBAR_ITEM).setVisible(!isToolbarVisible);
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
        final View buttonContainer = findViewById(R.id.buttonContainer);
        switch (item.getItemId()) {
        case (SHOW_TOOLBAR_ITEM) :
            int animId;
            if (getResources().getConfiguration().orientation ==
                Configuration.ORIENTATION_PORTRAIT)
                animId = R.drawable.move_toolbar_in_top;
            else
                animId = R.drawable.move_toolbar_in_left;
            buttonContainer.setVisibility(View.VISIBLE);
            buttonContainer.startAnimation(AnimationUtils.loadAnimation(this, animId));
            isToolbarVisible = true;
            return true;
        case (HIDE_TOOLBAR_ITEM) :
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
            isToolbarVisible = false;
            return true;
        case (UNDO_ITEM) : 
            mainSurfaceView.getSheet().undo();
            return true;
        case (REDO_ITEM) :
            mainSurfaceView.getSheet().redo();
            return true;
        case (DRAW_ITEM) :
            mainSurfaceView.setInstrument(MainSurfaceView.DRAW_INSTRUMENT);
            return true;
        case (ERASE_ITEM) :
            mainSurfaceView.setInstrument(MainSurfaceView.ERASE_INSTRUMENT);
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
        }
        return false;
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

}

