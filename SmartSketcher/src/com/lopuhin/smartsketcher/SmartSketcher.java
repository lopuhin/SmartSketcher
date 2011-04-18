package com.lopuhin.smartsketcher;

import android.app.Activity;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class SmartSketcher extends Activity {
	private MainSurfaceView mainView;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainView = new MainSurfaceView(this);
        setContentView(mainView);
        mainView.resume();
        registerForContextMenu(mainView);
    }
    
    static final private int
    	UNDO_ITEM = Menu.FIRST, 
    	REDO_ITEM = Menu.FIRST + 1;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	super.onCreateOptionsMenu(menu);

    	menu.add(0, UNDO_ITEM, Menu.NONE, R.string.undo);
    	menu.add(0, REDO_ITEM, Menu.NONE, R.string.redo);

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
    	}
    	return false;
    }

    // FIXME - not working - should detect still long presses in onTouchEvent
    @Override
    public void onCreateContextMenu(
    		ContextMenu menu, View v,
    		ContextMenu.ContextMenuInfo menuInfo) {
    	super.onCreateContextMenu(menu, v, menuInfo);
    	menu.setHeaderTitle(R.string.app_name);
    	menu.add(0, UNDO_ITEM, Menu.NONE, R.string.undo);
    	menu.add(0, REDO_ITEM, Menu.NONE, R.string.redo);
    }

}

