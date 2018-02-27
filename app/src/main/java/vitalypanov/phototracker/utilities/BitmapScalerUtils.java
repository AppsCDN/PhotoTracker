package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;

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
     * @param context       context
     */
    public static void updatePhoto(Track track, ImageView imageView, int scaleWidth, Context context){
        imageView.setImageDrawable(null);
        if (track== null
                || track.getLastPhotoItem() == null
                || context == null){
            return;
        }
        File currentPhotoFile = track.getPhotoFile(context,track.getLastPhotoItem().getPhotoFileName());
        if (currentPhotoFile != null && currentPhotoFile.exists()){
            Bitmap bitmap = BitmapFactory.decodeFile(currentPhotoFile.getPath());
            Bitmap bMapScaled = BitmapScalerUtils.scaleToFitWidth(bitmap, scaleWidth);
            imageView.setImageBitmap(bMapScaled);
        }
    }

}

