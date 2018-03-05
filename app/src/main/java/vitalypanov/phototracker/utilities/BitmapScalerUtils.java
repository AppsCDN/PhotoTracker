package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.IOException;

import vitalypanov.phototracker.model.TrackPhoto;

/**
 * Created by Vitaly on 27.02.2018.
 */

public class BitmapScalerUtils {
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

    /**
     * Drawing photo on image view control.
     * Do drawing with scaling within provided scaleWidth value
     * @param trackPhoto    Track photo object - draw it on image view
     * @param imageView     Which image view we place photo
     * @param scaleWidth    Which width bitmap of photo will be scaled
     * @param context       Context
     */
    public static void updatePhoto(TrackPhoto trackPhoto, ImageView imageView, int scaleWidth, Context context){
        try {
            imageView.setImageBitmap(getScaledBitmap(trackPhoto, scaleWidth, context));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Assync version of updatePhoto.
     * Drawing photo on image view control.
     * Do drawing with scaling within provided scaleWidth value
     * All params are the same as in updatePhoto
     * @param loadingPanel  Animated progress picture which shows when image loads (can be null)
     */
    public static void updatePhotoAssync(TrackPhoto trackPhoto, ImageView imageView, int scaleWidth, Context context, RelativeLayout loadingPanel){
        AssyncBitmapLoaderTask assyncImageViewUpdater = new AssyncBitmapLoaderTask(trackPhoto, imageView, scaleWidth, context, loadingPanel);
        assyncImageViewUpdater.execute();
    }

    /**
     * Scale bitmap in track according provided scaleWidth and return it
     * @param trackPhoto    Track photo object - and draw it on image view
     * @param scaleWidth    Which width bitmap of photo will be scaled
     * @param context       Context
     * @return
     */
    public static Bitmap getScaledBitmap(TrackPhoto trackPhoto, int scaleWidth, Context context) throws IOException {
        Bitmap bitmap =null;
        if (trackPhoto== null || context == null){
            return bitmap;
        }
        File currentPhotoFile = FileUtils.getPhotoFile(context,trackPhoto.getPhotoFileName());
        if (currentPhotoFile != null && currentPhotoFile.exists()){
            // read bitmap from file
            Bitmap bitmapFromFile = BitmapFactory.decodeFile(currentPhotoFile.getPath());
            // Reading EXIF information from bitmap file and rotate if need it
            ExifInterface exif = new ExifInterface(currentPhotoFile.getPath());
            int rotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            if (rotation != 0f) {
                // need to rotate loaded bitmap due to EXIF info
                int rotationInDegrees = exifToDegrees(rotation);
                Matrix matrix = new Matrix();
                matrix.preRotate(rotationInDegrees);
                bitmapFromFile = Bitmap.createBitmap(bitmapFromFile, 0, 0, bitmapFromFile.getWidth(), bitmapFromFile.getHeight(), matrix, true);
            }
            // scale bitmap adjust screen width
            bitmap = BitmapScalerUtils.scaleToFitWidth(bitmapFromFile, scaleWidth);
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
}

