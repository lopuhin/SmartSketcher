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


public class FileHelper {
    private static final String
        PREVIEW_FILENAME_PATTERN = "preview-full-%d.png",
        SMALL_PREVIEW_FILENAME_PATTERN = "preview-small-%d.png";
    
    private final MainSurfaceView mainSurfaceView;
    private final Context context;
    private final static String TAG = "FileHelper";

    FileHelper(MainSurfaceView mainSurfaceView) {
        this.mainSurfaceView = mainSurfaceView;
        this.context = mainSurfaceView.getContext();
    }

    public static File getFullPreviewFileBySheetId(long sheetId) {
        /**
         * Return file, where preview should reside
         */
        return new File(getSDDir(), String.format(PREVIEW_FILENAME_PATTERN, sheetId));
    }

    public static File getSmallPreviewFileBySheetId(long sheetId) {
        /**
         * Return file, where small preview should reside
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
            .getAbsolutePath() + "/android/data/com.lopuhin.smartsketcher/";
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
             * Save small and full previews to files (use current sheet id as postfix)
             */
            Sheet sheet = mainSurfaceView.getSheet();
            long sheetId = sheet.getId();
            final File fileFull = getFullPreviewFileBySheetId(sheetId),
                  fileSmall = getSmallPreviewFileBySheetId(sheetId);
            FileOutputStream fosFull, fosSmall;
            try {
                fosFull = new FileOutputStream(fileFull);
                fosSmall = new FileOutputStream(fileSmall);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Bitmap bitmapFull = mainSurfaceView.makeScreenshot();
            if (bitmapFull != null) {
                final int width = bitmapFull.getWidth(), height = bitmapFull.getHeight();
                int smallWidth, smallHeight;
                // TODO - load size from resources
                int maxSize = 150; 
                if (width > height) {
                    smallWidth = maxSize;
                    smallHeight = (int)((float)height * maxSize / width);
                } else {
                    smallHeight = maxSize;
                    smallWidth = (int)((float)width * maxSize / height);
                }
                Bitmap bitmapSmall = Bitmap.createScaledBitmap(
                        bitmapFull, smallWidth, smallHeight, true);
                bitmapSmall.compress(Bitmap.CompressFormat.PNG, 100, fosSmall);
                bitmapFull.compress(Bitmap.CompressFormat.PNG, 100, fosFull);
                notifyMediaScanner(fileSmall);
                notifyMediaScanner(fileFull);
            }
        }
    }
}
