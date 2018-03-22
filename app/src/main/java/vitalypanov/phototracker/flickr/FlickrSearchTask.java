package vitalypanov.phototracker.flickr;

import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

import vitalypanov.phototracker.Settings;

/**
 * Created by Vitaly on 20.03.2018.
 */

public class FlickrSearchTask extends AsyncTask<LatLng, Void, Void> {
    private Context mContext;
    private OnFlickrSearchTaskCompleted mListener;    // callback interface, to signal that search task is completed
    private List<FlickrPhoto> mPhotos;          // search result
    private ProgressBar mProgressBar;

    public FlickrSearchTask(Context context, OnFlickrSearchTaskCompleted listener, ProgressBar progressBar){
        this.mContext = context;
        this.mListener = listener;
        this.mProgressBar = progressBar;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(LatLng... params) {
        if (!Settings.get(mContext).isFlickrPhotos()) {
            return null;
        }
        LatLng minPoint= params[0];
        LatLng maxPoint = params[1];

        mPhotos = null;
        List<FlickrPhoto> items = FlickrFetchr.searchPhotos(minPoint, maxPoint, mContext);
        if (items.size() > 0){
            mPhotos = items;
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        mListener.onTaskCompleted(mPhotos);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        //mProgressBar.setVisibility(View.GONE);
    }
}