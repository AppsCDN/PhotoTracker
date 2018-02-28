package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;

import vitalypanov.phototracker.model.Track;

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
     * @param track         Track object - from which we take LAST photo - and draw it on image view
     * @param imageView     Which image view we place photo
     * @param scaleWidth    Which width bitmap of photo will be scaled
     * @param context       Context
     */
    public static void updatePhoto(Track track, ImageView imageView, int scaleWidth, Context context){
        imageView.setImageBitmap(getScaledBitmap(track, scaleWidth, context));
    }

    /**
     * Assync version of updatePhoto.
     * Drawing photo on image view control.
     * Do drawing with scaling within provided scaleWidth value
     * All params are the same as in updatePhoto
     * @param loadingPanel  Animated progress picture which shows when image loads (can be null)
     */
    public static void updatePhotoAssync(Track track, ImageView imageView, int scaleWidth, Context context, RelativeLayout loadingPanel){
        AssyncBitmapLoaderTask assyncImageViewUpdater = new AssyncBitmapLoaderTask(track, imageView, scaleWidth, context, loadingPanel);
        assyncImageViewUpdater.execute();
    }

    /**
     * Scale last bitmap in track according provided scaleWidth and return it
     * @param track         Track object - from which we take LAST photo - and draw it on image view
     * @param scaleWidth    Which width bitmap of photo will be scaled
     * @param context       Context
     * @return
     */
    public static Bitmap getScaledBitmap(Track track, int scaleWidth, Context context) {
        Bitmap bitmap =null;
        if (track== null
                || track.getLastPhotoItem() == null
                || context == null){
            return bitmap;
        }
        File currentPhotoFile = track.getPhotoFile(context,track.getLastPhotoItem().getPhotoFileName());
        if (currentPhotoFile != null && currentPhotoFile.exists()){
            Bitmap bitmapFromFile = BitmapFactory.decodeFile(currentPhotoFile.getPath());
            bitmap = BitmapScalerUtils.scaleToFitWidth(bitmapFromFile, scaleWidth);
        }
        return bitmap;
    }

}

