package vitalypanov.phototracker.flickr;

import java.util.List;

/**
 * Callback interface.
 * Implementing this, to get search result of flickr engine
 * Created by Vitaly on 20.03.2018.
 */

public interface OnFlickrSearchTaskCompleted {
    /**
     * Implement to get search result list
     * @param flickrPhotos List og photos
     */
    void onTaskCompleted(List<FlickrPhoto> flickrPhotos);
}
