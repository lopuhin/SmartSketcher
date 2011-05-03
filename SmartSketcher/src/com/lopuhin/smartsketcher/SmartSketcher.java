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
    	SAVE_ITEM = Menu.FIRST + 2, // TODO - only new, not save!
    	OPEN_ITEM = Menu.FIRST + 3,
    	CLEAR_ITEM = Menu.FIRST + 4;
    
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
    	menu.add(0, CLEAR_ITEM, Menu.NONE, R.string.clear);
    	menu.add(0, OPEN_ITEM, Menu.NONE, R.string.open);
    	menu.add(0, SAVE_ITEM, Menu.NONE, R.string.save).setIcon(
				android.R.drawable.ic_menu_save);
    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	super.onOptionsItemSelected(item);
    	switch (item.getItemId()) {
    	case (UNDO_ITEM) : 
    		// TODO
    		return true;
    	case (REDO_ITEM) :
    		// TODO
    		return true;
    	case (CLEAR_ITEM) :
    		mainSurfaceView.clearSheet();
    		return true;
    	case (SAVE_ITEM) :
    		fileHelper.saveToSD();
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

