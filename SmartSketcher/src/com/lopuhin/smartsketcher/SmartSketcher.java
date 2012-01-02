package com.lopuhin.smartsketcher;

import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;


public class SmartSketcher extends Activity {
    private MainSurfaceView mainSurfaceView;
    private DBAdapter dbAdapter;
    private FileHelper fileHelper;
    
    private static String TAG = "SmartSketcher";
        
    static final private int
        UNDO_ITEM = Menu.FIRST, 
        REDO_ITEM = Menu.FIRST + 1,
        NEW_ITEM = Menu.FIRST + 2,
        OPEN_ITEM = Menu.FIRST + 3,
        CLEAR_ITEM = Menu.FIRST + 4,
        ERASE_ITEM = Menu.FIRST + 5,
        DRAW_ITEM = Menu.FIRST + 6,
        BRUSH_ITEM = Menu.FIRST + 7;

    static final private int OPEN_SHEET_RESULT = 1;
    
    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.main);
        LinearLayout layout = (LinearLayout)findViewById(R.id.mainLayout);
        
        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        Sheet sheet = dbAdapter.loadLastSheet();

        mainSurfaceView = new MainSurfaceView(this, dbAdapter, sheet);
        layout.addView(mainSurfaceView);

        fileHelper = new FileHelper(mainSurfaceView);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mainSurfaceView.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
        fileHelper.savePreview();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        menu.add(0, UNDO_ITEM, Menu.NONE, R.string.undo);
        menu.add(0, REDO_ITEM, Menu.NONE, R.string.redo);
        menu.add(0, BRUSH_ITEM, Menu.NONE, R.string.brush);
        menu.add(0, DRAW_ITEM, Menu.NONE, R.string.draw);
        menu.add(0, ERASE_ITEM, Menu.NONE, R.string.erase);
        menu.add(0, OPEN_ITEM, Menu.NONE, R.string.open);
        menu.add(0, CLEAR_ITEM, Menu.NONE, R.string.clear);
        menu.add(0, NEW_ITEM, Menu.NONE, R.string.newitem);
            
        return true;
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        MenuItem undoItem = menu.findItem(UNDO_ITEM);
        undoItem.setEnabled(mainSurfaceView.getSheet().canUndo());
        MenuItem redoItem = menu.findItem(REDO_ITEM);
        redoItem.setEnabled(mainSurfaceView.getSheet().canRedo());
            
        final int instrument = mainSurfaceView.getInstrument();
        MenuItem drawItem = menu.findItem(DRAW_ITEM);
        drawItem.setEnabled(instrument != MainSurfaceView.DRAW_INSTRUMENT);
        MenuItem eraseItem = menu.findItem(ERASE_ITEM);
        eraseItem.setEnabled(instrument != MainSurfaceView.ERASE_INSTRUMENT);

        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
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
            mainSurfaceView.setDefaultInstrument();
            mainSurfaceView.clearSheet();
            return true;
        case (NEW_ITEM) :
            mainSurfaceView.setDefaultInstrument();
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
        if (resultCode == RESULT_OK && requestCode == OPEN_SHEET_RESULT) {
            long sheetId = data.getLongExtra("sheetId", -1);
            Log.d(TAG, "sheetId " + sheetId);
            if (sheetId != -1) {
                mainSurfaceView.setSheet(dbAdapter.loadSheet(sheetId));
            }
        }
        mainSurfaceView.setDefaultInstrument();
        mainSurfaceView.getSheet().setDirty();
    }
        
    /*private String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
        }*/

}

