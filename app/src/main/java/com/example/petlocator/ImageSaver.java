package com.example.petlocator;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class ImageSaver {

    private static final String TAG = "ImageSaver";

    public interface OnImageSavedListener {
        void onImageSaved(Uri imageUri);
    }

    public static void saveImage(Context context, Bitmap bitmap, String fileName, OnImageSavedListener listener) {
        File imageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
        try (FileOutputStream outputStream = new FileOutputStream(imageFile)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
            outputStream.flush();
        } catch (IOException e) {
            Log.e(TAG, "Failed to save bitmap to file", e);
            return;
        }
        MediaScannerConnection.scanFile(context, new String[]{imageFile.toString()}, null, new MediaScannerConnection.OnScanCompletedListener() {
            @Override
            public void onScanCompleted(String path, Uri uri) {
                if (listener != null) {
                    listener.onImageSaved(uri);
                }
            }
        });
    }

}