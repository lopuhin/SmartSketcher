package com.lopuhin.smartsketcher;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class SmartSketcher extends Activity {
	private MainSurfaceView mainSurfaceView;
	private FileHelper fileHelper;
	
	private static final int SELECT_PICTURE = 1;
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
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainSurfaceView = new MainSurfaceView(this);
        setContentView(mainSurfaceView);
        fileHelper = new FileHelper(mainSurfaceView);
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	mainSurfaceView.resume();
    	mainSurfaceView.setSheet(fileHelper.getSavedSheet());
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	fileHelper.saveToSD();
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
    	
    	final int submode = mainSurfaceView.getSubmode();
    	MenuItem drawItem = menu.findItem(DRAW_ITEM);
    	drawItem.setEnabled(submode != MainSurfaceView.DRAW_SUBMODE);
    	MenuItem eraseItem = menu.findItem(ERASE_ITEM);
    	eraseItem.setEnabled(submode != MainSurfaceView.ERASE_SUBMODE);

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
    		mainSurfaceView.setSubmode(MainSurfaceView.DRAW_SUBMODE);
    		return true;
    	case (ERASE_ITEM) :
    		mainSurfaceView.setSubmode(MainSurfaceView.ERASE_SUBMODE);
    		return true;
    	case (CLEAR_ITEM) :
    		mainSurfaceView.clearSheet();
    		return true;
    	case (NEW_ITEM) :
    		fileHelper.saveToSD();
    		mainSurfaceView.clearSheet();
    		return true;
    	case (OPEN_ITEM) :
    		Intent intent = new Intent();
        	intent.setType("image/png");
        	intent.setAction(Intent.ACTION_GET_CONTENT);
        	startActivityForResult(Intent.createChooser(
        			intent, "Open"), SELECT_PICTURE);
    		return true;
    	}
    	return false;
    }

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
	    	Uri selectedImageUri = data.getData();
	    	String selectedImagePath = getPath(selectedImageUri);
	    	mainSurfaceView.setSheet(
	    			fileHelper.getSavedSheetByPreviewPath(selectedImagePath));
	    }
	    mainSurfaceView.getSheet().setDirty();
	}
	
	private String getPath(Uri uri) {
	    String[] projection = {MediaStore.Images.Media.DATA};
	    Cursor cursor = managedQuery(uri, projection, null, null, null);
	    int column_index = cursor.getColumnIndexOrThrow(
	    		MediaStore.Images.Media.DATA);
	    cursor.moveToFirst();
	    return cursor.getString(column_index);
	}

}

