package com.lopuhin.smartsketcher;

import android.app.Activity;
import android.view.View;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Gallery;
import android.widget.Toast;

public class PreviewGallery extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_gallery);
        
        Gallery g = (Gallery) findViewById(R.id.preview_gallery);
        g.setAdapter(new PreviewImageAdapter(this));

        g.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                Toast.makeText(PreviewGallery.this, "" + position, Toast.LENGTH_SHORT).show();
            }
        });
	}	
}
