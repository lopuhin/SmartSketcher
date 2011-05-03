package com.lopuhin.smartsketcher;

import android.content.Context;
import android.content.res.TypedArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

public class PreviewImageAdapter extends BaseAdapter {
	int mGalleryItemBackground;
	private Context mContext;

	public PreviewImageAdapter(Context c) {
        mContext = c;
        TypedArray a = mContext.getTheme().obtainStyledAttributes(R.styleable.PreviewGallery);
        mGalleryItemBackground = a.getResourceId(
                R.styleable.PreviewGallery_android_galleryItemBackground, 0);
        a.recycle();
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return null;
	}

}
