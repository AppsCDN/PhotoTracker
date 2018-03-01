package vitalypanov.phototracker.utilities;

import android.graphics.Bitmap;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *  Singleton for bitmaps cache in memory.
 *  For faster UI working.
 *
 * Created by Vitaly on 28.02.2018.
 */

public class BitmapCache {
    private static final int MAX_CACHE_SIZE = 500; // max bitmaps count in cache
    private static BitmapCache mBitmapCache;
    private final LinkedHashMap<String, Bitmap> mBitmaps = new LinkedHashMap<String, Bitmap>() {
        @Override
        protected boolean removeEldestEntry(final Map.Entry eldest) {
            return size() > MAX_CACHE_SIZE;
        }
    };

    public static BitmapCache get() {
        if (mBitmapCache == null) {
            mBitmapCache = new BitmapCache();
        }
        return mBitmapCache;

    }

    public LinkedHashMap<String, Bitmap> getBitmaps() {
        return mBitmaps;
    }

    private BitmapCache() {
    }

}
