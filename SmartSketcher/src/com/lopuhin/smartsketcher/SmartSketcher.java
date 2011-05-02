package com.lopuhin.smartsketcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SmartSketcher extends Activity {
	private MainSurfaceView mainSurfaceView;
	private FileHelper fileHelper;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainSurfaceView = new MainSurfaceView(this);
        setContentView(mainSurfaceView);
        fileHelper = new FileHelper(mainSurfaceView);
        mainSurfaceView.resume();
        registerForContextMenu(mainSurfaceView);
    }
    
    static final private int
    	UNDO_ITEM = Menu.FIRST, 
    	REDO_ITEM = Menu.FIRST + 1,
    	SAVE_ITEM = Menu.FIRST + 2,
    	OPEN_ITEM = Menu.FIRST + 3;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	menu.add(0, UNDO_ITEM, Menu.NONE, R.string.undo);
    	menu.add(0, REDO_ITEM, Menu.NONE, R.string.redo);
    	menu.add(0, SAVE_ITEM, 0, R.string.save).setIcon(
				android.R.drawable.ic_menu_save);
    	menu.add(0, OPEN_ITEM, 0, R.string.open);
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
    	case (SAVE_ITEM) :
    		fileHelper.saveToSD();
    		return true;
    	case (OPEN_ITEM) :
    		mainSurfaceView.setSheet(fileHelper.getSavedSheet());
    		return true;
    	
    	}
    	return false;
    }

}

