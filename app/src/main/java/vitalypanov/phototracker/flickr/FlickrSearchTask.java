package vitalypanov.phototracker.flickr;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by Vitaly on 20.03.2018.
 */

public class FlickrSearchTask extends AsyncTask<LatLng, Void, Void> {
    private OnFlickrSearchTaskCompleted mListener;    // callback interface, to signal that search task is completed
    private List<FlickrPhoto> mPhotos;          // search result

    public FlickrSearchTask(OnFlickrSearchTaskCompleted listener){
        this.mListener = listener;
    }

    @Override
    protected Void doInBackground(LatLng... params) {
        LatLng minPoint= params[0];
        LatLng maxPoint = params[1];

        mPhotos = null;
        List<FlickrPhoto> items = FlickrFetchr.searchPhotos(minPoint, maxPoint);
        if (items.size() > 0){
            mPhotos = items;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.onTaskCompleted(mPhotos);
    }
}