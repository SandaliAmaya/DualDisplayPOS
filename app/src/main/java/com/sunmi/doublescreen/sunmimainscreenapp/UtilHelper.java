package com.sunmi.doublescreen.sunmimainscreenapp;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UtilHelper {

    private static final String TAG = "UTIL_HELPER";

    public static File createImage(Bitmap bitmap) {
        try {
            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "QR_CODES");

            // Create the storage directory if it does not exist
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d(TAG, "Oops! Failed create directory");
                    return null;
                }
            }
            File qrFile = new File(mediaStorageDir.getPath()
                    + File.separator
                    + new SimpleDateFormat("yyyy-MM-dd hh_mm_ss",
                    Locale.getDefault()).format(new Date()) + "_QR.png");
            Log.i(TAG, "createImage: File created[" + qrFile.getAbsolutePath() + "]");
            FileOutputStream os = new FileOutputStream(qrFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

            os.flush();
            os.close();
            return qrFile;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static boolean saveImage(Bitmap bitmap, String name) {
        try {
            File mediaStorageDir = new File(
                    Environment
                            .getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
                    "QR");

            File filePath = new File(mediaStorageDir.getPath()
                    + File.separator
                    + new SimpleDateFormat("yyyy-MM-dd hh_mm_ss",
                    Locale.getDefault()).format(new Date()) + ".png");

            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.e(TAG, "saveImage: failed to create directory!");
                }
            }

//            File filePath =  new File( Environment.getExternalStorageDirectory() + File.separator +"qr_pay");
            boolean isSuccess = true;


            if (!filePath.exists()) {
                isSuccess = filePath.mkdir();
            }

            if (isSuccess) {
                Log.d("QRSuccessEnter", "Entered Successfully");
                File imagePath = new File(filePath + File.separator + name + ".jpg");
                FileOutputStream os = new FileOutputStream(imagePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);

                os.flush();
                os.close();
                Log.d("QRImageSaved", "QR Image Saved to " + filePath.toURI());
                return true;
            } else {
                Log.d("QRFailedPath", "Failed to create path");
                return false;
            }

        } catch (Exception ex) {
            Log.d("QRImageError", ex.getMessage());
            return false;
        }

    }
}
