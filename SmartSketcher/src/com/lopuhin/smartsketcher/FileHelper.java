package com.lopuhin.smartsketcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


public class FileHelper {
	private static final String 
		PREVIEW_FILENAME_RE = "sketch-preview-(\\d+).png",
		PREVIEW_FILENAME_PATTERN = "sketch-preview-%d.png",
		FILENAME_PATTERN = "sketch-%d.xml";
	
	private final MainSurfaceView mainSurfaceView;
	private final Context context;
	private final String TAG = "FileHelper";

	private boolean isSaved = false;
	
	FileHelper(MainSurfaceView mainSurfaceView) {
		this.mainSurfaceView = mainSurfaceView;
		this.context = mainSurfaceView.getContext();
	}

	public Sheet getSavedSheet() {
		if (!isStorageAvailable()) {
			return null;
		}
		File lastFile = getLastFile(getSDDir());
		if (lastFile == null) {
			return null;
		}
		return getSavedSheet(lastFile);
	}

	public Sheet getSavedSheetByPreviewPath(String previewPath) {
		String parent = getSDDir().getAbsolutePath();
		String expectedPath = parent + "/" + PREVIEW_FILENAME_RE;
		Log.d(TAG, "preview " + previewPath + " " + expectedPath);
    	if (previewPath.matches(expectedPath)) {
    		Pattern p = Pattern.compile(expectedPath);
    		Matcher m = p.matcher(previewPath);
    		if (m.find()) {
    			int sketchNumber = Integer.parseInt(m.group(1));
    			File file = new File(
    				getSDDir(), String.format(FILENAME_PATTERN, sketchNumber));
    			Log.d(TAG, "file " + file);
    			return getSavedSheet(file);
    		}
    	}
    	return null;
	}
	
	public void saveToSD() {
		if (!isStorageAvailable()) {
			return;
		}
		new SaveTask().execute();
	}

	private Sheet getSavedSheet(File file) {
		Sheet savedSheet = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			savedSheet = Sheet.loadFromFile(fis);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		return savedSheet;
	}

	private File getSDDir() {
		String path = Environment.getExternalStorageDirectory()
				.getAbsolutePath() + "/smartsketcher/";

		File file = new File(path);
		if (!file.exists()) {
			file.mkdirs();
		}

		return file;
	}

	private File getLastFile(File dir) {
		int suffix = 1;
		File newFile = null;
		File file = null;
		do {
			file = newFile;
			newFile = new File(dir, String.format(FILENAME_PATTERN, suffix));
			suffix++;
		} while (newFile.exists());
		return file;
	}

	private File[] getUniqueFilePath(File dir) {
		// return pair [sketch filename, preview filename]
		int suffix = 1;
		while (new File(dir, String.format(FILENAME_PATTERN, suffix)).exists()) {
			suffix++;
		}
		return new File[]{
				new File(dir, String.format(FILENAME_PATTERN, suffix)),
				new File(dir, String.format(PREVIEW_FILENAME_PATTERN, suffix)),
		};
	}

	private void saveSheet(File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			Sheet sheet = mainSurfaceView.getSheet();
			sheet.saveToFile(fos);
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private void savePreview(File file) {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			Sheet sheet = mainSurfaceView.getSheet();
			sheet.savePreviewToFile(
					fos,
					mainSurfaceView.getMeasuredWidth(),
					mainSurfaceView.getMeasuredHeight());
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean isStorageAvailable() {
		String externalStorageState = Environment.getExternalStorageState();
		if (!externalStorageState.equals(Environment.MEDIA_MOUNTED)) {
			Toast.makeText(context, R.string.sd_card_is_not_available,
					Toast.LENGTH_SHORT).show();
			return false;
		}
		return true;
	}

	void share() {
		// TODO  - test
		if (!isStorageAvailable()) {
			return;
		}
		new SaveTask() {
			protected void onPostExecute(File file) {
				isSaved = true;
				Uri uri = Uri.fromFile(file);
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/xml");
				i.putExtra(Intent.EXTRA_STREAM, uri);
				context.startActivity(Intent.createChooser(
						i, context.getString(R.string.send_image_to)));

				super.onPostExecute(file);
			}
		}.execute();
	}

	private File saveSheet() {
		File[] files = getUniqueFilePath(getSDDir());
		saveSheet(files[0]);
		savePreview(files[1]);
		notifyMediaScanner(files[1]);
		return files[0];
	}

	private void notifyMediaScanner(File file) {
		Uri uri = Uri.fromFile(file);
		context.sendBroadcast(
				new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
	}

	private class SaveTask extends AsyncTask<Void, Void, File> {
		private ProgressDialog dialog = ProgressDialog.show(context, "",
				context.getString(R.string.saving_to_sd_please_wait), true);

		protected File doInBackground(Void... none) {
			// TODO - pause?
			//mainSurfaceView.pause();
			return saveSheet();
		}

		protected void onPostExecute(File file) {
			dialog.hide();
			//mainSurfaceView.resume();
		}
	}

}
