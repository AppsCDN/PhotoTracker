package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  Singleton for bitmaps cache in memory.
 *  For faster UI working.
 *
 *
 * Created by Vitaly on 28.02.2018.
 */

public class BitmapHandler {
    private static final int MAX_CACHE_SIZE = 10; //  max bitmaps count in cache
    private static BitmapHandler mBitmapHandler;
    private Context mContext;

    /**
     * Cache for holding images
     * Not used now :(
     *
     * Using cache it's not good idea :(((
     * If use cache it cause crash:
     * Caused by: java.lang.OutOfMemoryError: Failed to allocate a 48794892 byte allocation with 16777216 free bytes and 29MB until OOM
     *
     */
    private final LinkedHashMap<String, Bitmap> mBitmaps = new LinkedHashMap<String, Bitmap>() {
        @Override
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public static BitmapHandler get(Context context) {
        if (mBitmapHandler == null) {
            mBitmapHandler = new BitmapHandler(context);
        }
        return mBitmapHandler;
    }

    /**
     * private constructor for Singleton
     * @param context
     */
    private BitmapHandler(Context context) {
        mContext = context;
    }

    /**
     * Load bitmap from file and put it into cache (no any scaling).
     * If bitmap is already in cache - just return it.
     * @param sKeyPhotoFileName
     * @return
     */
    public Bitmap getBitmapOriginal(String sKeyPhotoFileName) {
        Bitmap bitmap = mBitmaps.get(sKeyPhotoFileName);
        if (bitmap!= null){
            // bitmap was found in cache - return it
            return bitmap;
        }
        // else load bitmap from file, put it in cache and then return...
        try {
            bitmap = BitmapUtils.loadBitmap(sKeyPhotoFileName, mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // Bitmap cache it's not working idea  - so turn it off temporary
        // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //mBitmaps.put(sKeyPhotoFileName, bitmap);
        return bitmap;
    }

    /**
     * Get original bitmap and scale it according provided Width value.
     *
     * @param sKeyPhotoFileName bitmap file name
     * @param scaleWidth   scale value
     * @return
     */
    public Bitmap getBitmapScaleToFitWidth(String sKeyPhotoFileName, int scaleWidth) {
        Bitmap bitmap = this.getBitmapOriginal(sKeyPhotoFileName);
        float factor = scaleWidth / (float) bitmap.getWidth();
        return Bitmap.createScaledBitmap(bitmap, scaleWidth, (int) (bitmap.getHeight() * factor), true);
    }

    /**
     * Get original bitmap and scale it according provided Height value.
     *
     * @param sKeyPhotoFileName bitmap file name
     * @param scaleHeight   scale value
     * @return
     */
    public Bitmap getBitmapScaleToFitHeight(String sKeyPhotoFileName, int scaleHeight) {
        Bitmap bitmap = this.getBitmapOriginal(sKeyPhotoFileName);
        float factor = scaleHeight / (float) bitmap.getHeight();
        return Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * factor), scaleHeight, true);
    }

    /**
     * Get original bitmap and scale it according provided size.
     *
     * @param sKeyPhotoFileName bitmap file name
     * @param scaleSize   scale value
     * @return
     */
    public Bitmap getBitmapScaleToSize(String sKeyPhotoFileName, int scaleSize) {
        Bitmap bitmap = this.getBitmapOriginal(sKeyPhotoFileName);
        return  bitmap.getWidth() < bitmap.getHeight() ?
                BitmapUtils.scaleToFitHeight(bitmap, scaleSize) // portrait
                :
                BitmapUtils.scaleToFitWidth(bitmap, scaleSize); // landscape
    }

}
