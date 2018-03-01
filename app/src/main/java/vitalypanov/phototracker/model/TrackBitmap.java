package vitalypanov.phototracker.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;

import vitalypanov.phototracker.utilities.BitmapCache;
import vitalypanov.phototracker.utilities.BitmapScalerUtils;
import vitalypanov.phototracker.utilities.FileUtils;

/**
 * TrackPhoto object with cashed bitmap
 * Used for fast showing all bitmaps of the track
 *
 * Created by Vitaly on 28.02.2018.
 */

public class TrackBitmap {
    private static int SCALE_SMALL_SIZE = 150;
    TrackPhoto mTrackPhoto;
    Bitmap mBitmap;

    public TrackBitmap() {
    }

    public TrackBitmap(TrackPhoto trackPhoto) {
        this.mTrackPhoto = trackPhoto;
    }

    public TrackPhoto getTrackPhoto() {
        return mTrackPhoto;
    }

    public void setTrackPhoto(TrackPhoto trackPhoto) {
        mTrackPhoto = trackPhoto;
    }

    public Bitmap getBitmap() { return mBitmap; }

    public void loadBitmap(Context context) {
        // first check cache
        Bitmap bitmap = BitmapCache.get().getBitmaps().get(mTrackPhoto.getPhotoFileName());
        if (bitmap != null) {
            // bitmap exists in cache - exiting
            mBitmap = bitmap;
            return;
        }
        // bitmap not exist in cache - loading it from file and scale
        File currentPhotoFile = FileUtils.getPhotoFile(context, mTrackPhoto.getPhotoFileName());
        if (currentPhotoFile != null && currentPhotoFile.exists()) {
            Bitmap bitmapFromFile = BitmapFactory.decodeFile(currentPhotoFile.getPath());
            bitmap = bitmapFromFile.getWidth() < bitmapFromFile.getHeight() ?
                    BitmapScalerUtils.scaleToFitHeight(bitmapFromFile, SCALE_SMALL_SIZE) // portrait
                    :
                    BitmapScalerUtils.scaleToFitWidth(bitmapFromFile, SCALE_SMALL_SIZE); // landscape
            BitmapCache.get().getBitmaps().put(mTrackPhoto.getPhotoFileName(), bitmap);
            mBitmap = bitmap;
        }
    }
}
