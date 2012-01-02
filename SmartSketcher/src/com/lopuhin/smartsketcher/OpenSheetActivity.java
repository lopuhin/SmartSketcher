package com.lopuhin.smartsketcher;

import java.util.ArrayList;
import java.io.File;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.widget.BaseAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.AdapterView.OnItemClickListener;
import android.util.Log;
import android.net.Uri;


public class OpenSheetActivity extends Activity {
    private DBAdapter dbAdapter;
    private ArrayList<Long> sheetIds;        

    private final static String TAG = "OpenSheetActivity";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        sheetIds = dbAdapter.getSheetIds();
        
        Gallery gallery = (Gallery) findViewById(R.id.gallery);
        gallery.setAdapter(new ImageAdapter(this));

        gallery.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    Intent result = new Intent();
                    Log.d(TAG, "sheetId " + id);
                    result.putExtra("sheetId", id);
                    setResult(RESULT_OK, result);
                    finish();
                }
            });
    }

    public class ImageAdapter extends BaseAdapter {
    
        int mGalleryItemBackground;
        private Context mContext;
        
        public ImageAdapter(Context c) {
            mContext = c;
            TypedArray attr = mContext.obtainStyledAttributes(R.styleable.OpenSheetActivity);
            mGalleryItemBackground = attr.getResourceId(
                 R.styleable.OpenSheetActivity_android_galleryItemBackground, 0);
            attr.recycle();
        }

        public int getCount() {
            return sheetIds.size();
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView = new ImageView(mContext);

            File previewFile = FileHelper.getPreviewFileBySheetId(sheetIds.get(position));
            if (previewFile.exists()) {
                imageView.setImageURI(Uri.fromFile(previewFile));
            } else {
                imageView.setImageResource(R.drawable.no_preview);
            }
            //imageView.setLayoutParams(new Gallery.LayoutParams(300, 200));
            imageView.setLayoutParams(
                new Gallery.LayoutParams(Gallery.LayoutParams.FILL_PARENT,
                                         Gallery.LayoutParams.FILL_PARENT));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setBackgroundResource(mGalleryItemBackground);

            return imageView;
        }
    }
    
}
