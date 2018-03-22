package vitalypanov.phototracker.flickr;

import java.util.List;

/**
 * Singleton for holding search result of flickr.com engine
 * Need to have for transferring photos between fragments.
 * Created by Vitaly on 21.03.2018.
 */

public class FlickrHolder {
    private static FlickrHolder mFlickrHolder;

    private List<FlickrPhoto> mFlickrPhotos;

    public static FlickrHolder get() {
        if (mFlickrHolder == null) {
            mFlickrHolder = new FlickrHolder();
        }
        return mFlickrHolder;
    }

    private FlickrHolder() {
    }

    public List<FlickrPhoto> getFlickrPhotos() {
        return mFlickrPhotos;
    }

    public void setFlickrPhotos(List<FlickrPhoto> flickrPhotos) {
        mFlickrPhotos = flickrPhotos;
    }
}
