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
import android.view.Window;
import android.widget.AdapterView;
import android.widget.GridView;
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
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.open);

        dbAdapter = new DBAdapter(this);
        dbAdapter.open();
        sheetIds = dbAdapter.getSheetIds();
        
        GridView gridView = (GridView) findViewById(R.id.openview);
        gridView.setAdapter(new ImageAdapter(this));

        gridView.setOnItemClickListener(new OnItemClickListener() {
                public void onItemClick(AdapterView parent, View v, int position, long id) {
                    Intent result = new Intent();
                    Long sheetId = sheetIds.get(position);
                    Log.d(TAG, "sheetId " + sheetId);
                    result.putExtra("sheetId", sheetId);
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
            ImageView imageView;
            final int size = 150, padding = 8;
            if (convertView == null) {
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(padding, padding, padding, padding);
            } else {
                imageView = (ImageView) convertView;
            }
            File previewFile = FileHelper.getSmallPreviewFileBySheetId(sheetIds.get(position));
            if (previewFile.exists()) {
                imageView.setImageURI(Uri.fromFile(previewFile));
            } else {
                //imageView.setImageResource(R.drawable.no_preview_small);
            }
            imageView.setBackgroundResource(mGalleryItemBackground);
            return imageView;
        }
    }
    
}
