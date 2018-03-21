package vitalypanov.phototracker;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.flickr.FlickrHolder;
import vitalypanov.phototracker.flickr.FlickrPhoto;
import vitalypanov.phototracker.flickr.FlickrSearchTask;
import vitalypanov.phototracker.flickr.OnFlickrSearchTaskCompleted;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.others.BindTrackerGPSService;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.BitmapHandler;
import vitalypanov.phototracker.utilities.GoogleMapUtils;
import vitalypanov.phototracker.utilities.StringUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackGoogleMapFragment extends Fragment implements ViewPageUpdater, BindTrackerGPSService, GoogleMap.OnMarkerClickListener, OnFlickrSearchTaskCompleted {
    private static final String TAG = "PhotoTracker";

    private SupportMapFragment mMapFragment = null;
    private RelativeLayout mLoadingFrame;
    private HashMap<String, Bitmap> mBitmapHashMap;
    private TrackerGPSService mService;
    LatLngBounds mCurrentBounds = null;
    ArrayList<Marker> mFlickerMarkers = null;

    public static RunningTrackGoogleMapFragment newInstance() {
        return new RunningTrackGoogleMapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View v = layoutInflater.inflate(R.layout.fragment_map_running_track, container, false);
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
        mLoadingFrame = (RelativeLayout) v.findViewById(R.id.google_map_loading_data_frame);
        loadBitmapsAndUpdateMapAssync();
        return v;
    }

    class AssyncLoaderAndUpdateMapTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // if bitmaps not loaded yet...
            if (!Utils.isNull(mService)
                    && !Utils.isNull(mService.getCurrentTrack())
                    && !Utils.isNull(mService.getCurrentTrack().getPhotoFiles())
                    && (Utils.isNull(mBitmapHashMap)
                        || ( !Utils.isNull(mBitmapHashMap) && mService.getCurrentTrack().getPhotoFiles().size() != mBitmapHashMap.size())
                        )
                ) {
                mBitmapHashMap = new HashMap<String, Bitmap>();
                if (!Settings.get(getActivity()).isMapPerformance()) {
                    for (TrackPhoto trackPhoto : mService.getCurrentTrack().getPhotoFiles()) {
                        mBitmapHashMap.put(trackPhoto.getPhotoFileName(), BitmapHandler.get(getContext()).getBitmapScaleToSize(trackPhoto.getPhotoFileName(), GoogleMapUtils.SCALE_SMALL_SIZE));
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() { mLoadingFrame.setVisibility(View.VISIBLE);  }

        @Override
        protected void onPostExecute(Void aVoid) {
            mLoadingFrame.setVisibility(View.GONE);
            updatMapAsync();
            startFlickrSearch();
        }
    }

    private void loadBitmapsAndUpdateMapAssync(){
        AssyncLoaderAndUpdateMapTask assyncLoaderTask = new AssyncLoaderAndUpdateMapTask();
        assyncLoaderTask.execute();
    }

    /**
     * Updating assync google map and save map object into local variable
     * for future drawing
     */
    private void updatMapAsync(){
        if (mMapFragment ==null){
            return;
        }
        GoogleMapUtils.initMapControls(mMapFragment);
        final RunningTrackGoogleMapFragment thisForCallback = this;
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setOnMarkerClickListener(thisForCallback);
                if (mService == null){
                    return;
                }
                GoogleMapUtils.drawTrackOnGoogleMap(googleMap, mService.getCurrentTrack(), getContext(), mBitmapHashMap);
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        startFlickrSearch();
                    }
                });
            }
        });
    }
    private void startFlickrSearch(){
        final RunningTrackGoogleMapFragment thisForCallback = this;
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                if (!bounds.equals(GoogleMapUtils.MAP_ZERO_BOUNDS) && !bounds.equals(mCurrentBounds)) {
                    mCurrentBounds = bounds;
                }
                new FlickrSearchTask(getActivity(), thisForCallback).execute(mCurrentBounds.southwest, mCurrentBounds.northeast);
            }
        });
    }

    private void updatFlickrMapAsync(){
        if (mMapFragment ==null){
            return;
        }
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                if (!Utils.isNull(mFlickerMarkers)){
                    for (Marker marker : mFlickerMarkers){
                        marker.remove();
                    }
                }
                if (!Utils.isNull(FlickrHolder.get().getFlickrPhotos()) && !FlickrHolder.get().getFlickrPhotos().isEmpty()) {
                    mFlickerMarkers = GoogleMapUtils.addFlickrPhotosOnGoogleMap(googleMap, FlickrHolder.get().getFlickrPhotos(), getContext());
                }
            }
        });
    }

    @Override
    public void onPageSelected() {
        // load photo bitmaps for showing on google map
        // and redraw map when selecting tab
        loadBitmapsAndUpdateMapAssync();
    }

    @Override
    public void onBindService(TrackerGPSService service) {
        mService = service;
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String photoName = marker.getSnippet();
        if (StringUtils.isNullOrBlank(photoName)
                || mService == null
                || mService.getCurrentTrack() == null) {
            return false;
        }
        if (StringUtils.isValidUrl(photoName)) {
            // photos from flicker
            if (!Utils.isNull(FlickrHolder.get().getFlickrPhotos()) && !FlickrHolder.get().getFlickrPhotos().isEmpty()) {
                Intent intent = TrackImagesPagerActivity.newIntentFlickr(getActivity(), mService.getCurrentTrack().getUUID(), photoName);
                startActivity(intent);
            }
        } else {
            if (!mService.getCurrentTrack().getPhotoFiles().isEmpty()) {
                Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), mService.getCurrentTrack().getUUID(), (ArrayList<TrackPhoto>) mService.getCurrentTrack().getPhotoFiles(), photoName);
                startActivity(intent);
            }
        }
        return false;
    }

    @Override
    public void onTaskCompleted(List<FlickrPhoto> flickrPhotos) {
        FlickrHolder.get().setFlickrPhotos(flickrPhotos);
        updatFlickrMapAsync();
    }

}