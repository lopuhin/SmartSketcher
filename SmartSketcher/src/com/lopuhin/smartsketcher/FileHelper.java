package com.lopuhin.smartsketcher;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Canvas;


public class FileHelper {
    private static final String
        PREVIEW_FILENAME_RE = "preview-(\\d+).png",
        PREVIEW_FILENAME_PATTERN = "preview-%d.png";
    
    private final MainSurfaceView mainSurfaceView;
    private final Context context;
    private final static String TAG = "FileHelper";

    FileHelper(MainSurfaceView mainSurfaceView) {
        this.mainSurfaceView = mainSurfaceView;
        this.context = mainSurfaceView.getContext();
    }

    public static Long getSheetIdByPreviewPath(String previewPath) {
        /**
         * Return sheet id, given a path to preview, or null - if filename is invalid.
         */
        String parent = getSDDir().getAbsolutePath();
        String expectedPath = parent + "/" + PREVIEW_FILENAME_RE;
        Log.d(TAG, "preview " + previewPath + " " + expectedPath);
        if (previewPath.matches(expectedPath)) {
            Pattern p = Pattern.compile(expectedPath);
            Matcher m = p.matcher(previewPath);
            if (m.find()) {
                return Long.parseLong(m.group(1));
            }
        }
        return null;
    }

    public static File getPreviewFileBySheetId(long sheetId) {
        /**
         * Return file, where preview should live
         */
        return new File(getSDDir(), String.format(PREVIEW_FILENAME_PATTERN, sheetId));
    }
    
    public void savePreview() {
        /**
         * Spawn a thread to save preview
         */
        new SavePreviewThread().start();
    }
    
    private static File getSDDir() {
        String path = Environment.getExternalStorageDirectory()
            .getAbsolutePath() + "/android/com.lopuhin.smartsketcher/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
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

    private void notifyMediaScanner(File file) {
        Uri uri = Uri.fromFile(file);
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
    }

    private class SavePreviewThread extends Thread {
        @Override
        public void run() {
            /**
             * Save preview to file (use current sheet id as postfix)
             */
            Sheet sheet = mainSurfaceView.getSheet();
            File file = getPreviewFileBySheetId(sheet.getId());
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(file);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Bitmap bitmap = mainSurfaceView.makeScreenshot();
            if (bitmap != null) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                notifyMediaScanner(file);
            }
        }
    }
}
