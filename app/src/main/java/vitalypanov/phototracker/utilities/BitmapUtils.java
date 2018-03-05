package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import java.io.File;
import java.io.IOException;

/**
 * Created by Vitaly on 27.02.2018.
 */

public class BitmapUtils {

    /**
     * Scale bitmap in track according provided scaleWidth and return it
     * @param bitmapFileName    File name of loading bitmap
     * @param context           Context
     * @return                  Bitmap (original from file not scaled!)
     */
    public static Bitmap loadBitmap(String bitmapFileName, Context context) throws IOException {
        if (bitmapFileName.isEmpty() || context == null){
            return null;
        }

        Bitmap bitmap =null;
        File currentPhotoFile = FileUtils.getPhotoFile(context, bitmapFileName);
        if (currentPhotoFile != null && currentPhotoFile.exists()){
            // read bitmap from file
            bitmap = BitmapFactory.decodeFile(currentPhotoFile.getPath());

            // to avoid: Caused by: java.lang.OutOfMemoryError: Failed to allocate a 48794892 byte allocation with 16777216 free bytes and 29MB until OOM
            /*BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            bitmap = BitmapFactory.decodeFile(currentPhotoFile.getPath(), options);
            */

            // Reading EXIF information from bitmap file and rotate if need it
            ExifInterface exif = new ExifInterface(currentPhotoFile.getPath());
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (rotation != 0f) {
                // need to rotate loaded bitmap due to EXIF info
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                matrix.preRotate(rotationInDegrees);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
        }
        return bitmap;
    }

    /**
     * Convert EXIF orientation to normal degrees
     * (for future image rotation)
     *
     * @param exifOrientation
     * @return num of degrees to rotate if need it
     */
    private static int exifToDegrees(int exifOrientation) {
        switch (exifOrientation){
            case ExifInterface.ORIENTATION_ROTATE_90:   return 90;
            case ExifInterface.ORIENTATION_ROTATE_180:  return 180;
            case ExifInterface.ORIENTATION_ROTATE_270:  return 270;
            default: return 0;
        }
    }

    // Scale and maintain aspect ratio given a desired width
    // BitmapScaler.scaleToFitWidth(bitmap, 100);
    public static Bitmap scaleToFitWidth(Bitmap b, int width)
    {
        float factor = width / (float) b.getWidth();
        return Bitmap.createScaledBitmap(b, width, (int) (b.getHeight() * factor), true);
    }


    // Scale and maintain aspect ratio given a desired height
    // BitmapScaler.scaleToFitHeight(bitmap, 100);
    public static Bitmap scaleToFitHeight(Bitmap b, int height)
    {
        float factor = height / (float) b.getHeight();
        return Bitmap.createScaledBitmap(b, (int) (b.getWidth() * factor), height, true);
    }

}

