package vitalypanov.phototracker.flickr;

import java.util.List;

/**
 * Created by Vitaly on 20.03.2018.
 */

public interface OnFlickrSearchTaskCompleted {
    void onTaskCompleted(List<FlickrPhoto> flickrPhotos);
}
