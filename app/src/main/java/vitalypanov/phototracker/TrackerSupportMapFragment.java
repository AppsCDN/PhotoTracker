package vitalypanov.phototracker;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
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
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.utilities.BitmapHandler;
import vitalypanov.phototracker.utilities.GoogleMapUtils;
import vitalypanov.phototracker.utilities.StringUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Fragment with supporting of GoogleMap
 * for all fragments of app which want to use google maps
 * Created by Vitaly on 22.03.2018.
 */

public abstract class TrackerSupportMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnFlickrSearchTaskCompleted {
    private static final String TAG = "PhotoTracker";
    // Bounds of map:
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LAT1 = "SAVED_PARAM_CURRENT_BOUNDS_LAT1";
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LON1 = "SAVED_PARAM_CURRENT_BOUNDS_LON1";
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LAT2 = "SAVED_PARAM_CURRENT_BOUNDS_LAT2";
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LON2 = "SAVED_PARAM_CURRENT_BOUNDS_LON2";
    // show and may be modify photos of the track
    private static final int REQUEST_CODE_IMAGES_PAGER = 0;

    private SupportMapFragment mMapFragment = null;
    private RelativeLayout mLoadingFrame;
    private HashMap <String, Bitmap> mBitmapHashMap = null;
    LatLngBounds mCurrentBounds = null;
    ArrayList<Marker> mFlickerMarkers = null;
    private ProgressBar mLoadingProgressbar;
    FlickrSearchTask mFlickrSearchTask = null;

    /**
     * Override this method to return correct track object or null
     * @return Track object - can be null
     */
    public abstract Track getTrack();

    /**
     * Override this method to set resource layout id of your fragment
     * This template should be contain next controls id's:
     *  RelativeLayout: google_map_loading_data_frame(Mandatory. frame which holds googlemap and progressbar) )
     *  ProgressBar:    large_loading_progressbar   (Can be skipped.Large progressbar which show before googlemap will be loading, )
     *  fragment:       google_map_fragment         (Mandatory.The main fragment for google map object. )
     *  ProgressBar:    loading_progressbar         (Mandatory.Small progress bar for showing of flickr download progress. )
     * @return Resource id
     */
    public abstract int getLayoutResourceId();

    /**
     * Call this method in subclass if you want to force map update manually
     */
    protected void updatePhotoUI(){
        AssyncLoaderTask assyncLoaderTask = new AssyncLoaderTask();
        assyncLoaderTask.execute();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentBounds = new LatLngBounds.Builder()
                    .include(new LatLng(savedInstanceState.getDouble(SAVED_PARAM_CURRENT_BOUNDS_LAT1), savedInstanceState.getDouble(SAVED_PARAM_CURRENT_BOUNDS_LON1))) // Northeast
                    .include(new LatLng(savedInstanceState.getDouble(SAVED_PARAM_CURRENT_BOUNDS_LAT2), savedInstanceState.getDouble(SAVED_PARAM_CURRENT_BOUNDS_LON2))) // Southwest
                    .build();
        }
        // app permissions - check and grant if need it
        Permissions permissions = new Permissions(this);
        if (!permissions.hasPermissions()){
            permissions.requestPermissions();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (!Utils.isNull(mCurrentBounds)) {
            outState.putDouble(SAVED_PARAM_CURRENT_BOUNDS_LAT1, mCurrentBounds.northeast.latitude);
            outState.putDouble(SAVED_PARAM_CURRENT_BOUNDS_LON1, mCurrentBounds.northeast.longitude);
            outState.putDouble(SAVED_PARAM_CURRENT_BOUNDS_LAT2, mCurrentBounds.southwest.latitude);
            outState.putDouble(SAVED_PARAM_CURRENT_BOUNDS_LON2, mCurrentBounds.southwest.longitude);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View view = layoutInflater.inflate(getLayoutResourceId(), container, false);
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
        mLoadingFrame = (RelativeLayout) view.findViewById(R.id.google_map_loading_data_frame);
        mLoadingProgressbar = (ProgressBar) view.findViewById(R.id.loading_progressbar);
        mLoadingProgressbar.setVisibility(View.GONE);
        updatMapAsyncCurrentLocation();
        updatePhotoUI();
        return view;
    }

    /**
     * Get current location and move camera to this location
     * Init - called only one time, when fragment is created. Next time bounds of map will save and restore.
     */
    private void updatMapAsyncCurrentLocation(){
        if (mMapFragment==null){
            return;
        }
        if (!LocationServices.get(getActivity()).checkLocaionServices()) {
            return;
        }
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                if (Utils.isNull(mCurrentBounds)) {
                    Location location = LocationServices.get(getActivity()).getCurrentGPSLocation();
                    if (!Utils.isNull(location)) {
                        LatLng minPoint = new LatLng(location.getLatitude() - GoogleMapUtils.MAP_SIZE_DEGREES / 2, location.getLongitude() - GoogleMapUtils.MAP_SIZE_DEGREES / 2);
                        LatLng maxPoint = new LatLng(location.getLatitude() + GoogleMapUtils.MAP_SIZE_DEGREES / 2, location.getLongitude() + GoogleMapUtils.MAP_SIZE_DEGREES / 2);
                        mCurrentBounds = new LatLngBounds.Builder()
                                .include(minPoint)
                                .include(maxPoint)
                                .build();
                    }
                }
                int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mCurrentBounds, width, height, margin));
            }
        });
    }

    /**
     * Load track bitmaps in assync mode. And postexecute - update goggle map
     */
    class AssyncLoaderTask extends AsyncTask<Void, Void, Void> {

        public AssyncLoaderTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            // if bitmaps not loaded yet...
            if (mBitmapHashMap == null) {
                mBitmapHashMap = new HashMap<String, Bitmap>();
                // performance setting
                if (!Settings.get(getActivity()).isMapPerformance()) {
                    // load all bitmaps of the track
                    if (!Utils.isNull(getTrack())) {
                        for (TrackPhoto trackPhoto : getTrack().getPhotoFiles()) {
                            Bitmap bitmap = BitmapHandler.get(getContext()).getBitmapScaleToSize(trackPhoto.getPhotoFileName(), GoogleMapUtils.SCALE_SMALL_SIZE);
                            mBitmapHashMap.put(trackPhoto.getPhotoFileName(), bitmap);
                        }
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            if (!Utils.isNull(mLoadingFrame)) {
                mLoadingFrame.setVisibility(View.VISIBLE);
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!Utils.isNull(mLoadingFrame)) {
                mLoadingFrame.setVisibility(View.GONE);
            }
            updateTrackDataUI();
        }
    }

    /**
     * Updating assync google map and save map object into local variable
     * for future drawing
     */
    private void updateTrackDataUI(){
        if (mMapFragment ==null){
            return;
        }
        final TrackerSupportMapFragment thisForCallback = this;
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setOnMarkerClickListener(thisForCallback);
                if (!Utils.isNull(getTrack())) {
                    // Draw Track data and Track bitmaps on google map...
                    GoogleMapUtils.drawTrackOnGoogleMap(googleMap, getTrack(), getContext(), mBitmapHashMap);
                }
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        // Start flickr.com search....
                        startFlickrSearch();
                    }
                });
            }
        });
    }

    /**
     * Drawing flickr's photo markers on google map
     * First remove the old ones.
     */
    private void updatePhotoFlickrUI(){
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
                if (!Utils.isNull(FlickrHolder.get().getFlickrPhotos()) && FlickrHolder.get().getFlickrPhotos().size() >0) {
                    mFlickerMarkers = GoogleMapUtils.addFlickrPhotosOnGoogleMap(googleMap, FlickrHolder.get().getFlickrPhotos(), getContext());
                }
            }
        });
    }


    @Override
    public void onResume() {
        super.onResume();
        GoogleMapUtils.initMapControls(mMapFragment);
        startFlickrSearch();
    }

    @Override
    public void onPause() {
        GoogleMapUtils.shutdownMapControls(mMapFragment);
        super.onPause();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String photoName = marker.getSnippet();
        if (StringUtils.isNullOrBlank(photoName)){
            return false;
        }
        if (StringUtils.isValidUrl(photoName)) {
            // photos from flicker
            if (!Utils.isNull(FlickrHolder.get().getFlickrPhotos()) && !FlickrHolder.get().getFlickrPhotos().isEmpty()) {
                Intent intent = TrackImagesPagerActivity.newIntentFlickr(getActivity(), !Utils.isNull(getTrack())? getTrack().getUUID() : null, photoName);
                startActivity(intent);
            }
        } else {
            // local photos of the track (can modify it)
            if (!Utils.isNull(getTrack()) && !Utils.isNull(getTrack().getPhotoFiles()) && !getTrack().getPhotoFiles().isEmpty()) {
                Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), getTrack().getUUID(), (ArrayList<TrackPhoto>) getTrack().getPhotoFiles(), photoName);
                startActivityForResult(intent, REQUEST_CODE_IMAGES_PAGER);
            }
        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        switch (requestCode){
            case REQUEST_CODE_IMAGES_PAGER:
                if(!Utils.isNull(getTrack()) && !Utils.isNull(data)) {
                    getTrack().setPhotoFiles(TrackImagesPagerActivity.getTrackPhotos(data));
                    updatePhotoUI();
                    setActivityResultOK(); // say other activities in stack: need to update
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);;
        }
    }


    @Override
    public void onTaskCompleted(List<FlickrPhoto> flickrPhotos) {
        FlickrHolder.get().setFlickrPhotos(flickrPhotos);
        updatePhotoFlickrUI();
        mLoadingProgressbar.setVisibility(View.GONE);
    }

    /**
     * Request flickr's photos.
     * First cancel previous search if it's still running.
     */
    private void startFlickrSearch(){
        final TrackerSupportMapFragment thisForCallback = this;
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                if (!bounds.equals(GoogleMapUtils.MAP_ZERO_BOUNDS) && !bounds.equals(mCurrentBounds)) {
                    mCurrentBounds = bounds;
                }
                // if running previous - cancel it
                if (!Utils.isNull(mFlickrSearchTask)){
                    mFlickrSearchTask.cancel(true);
                }
                if (!Utils.isNull(mCurrentBounds)){
                    mFlickrSearchTask = new FlickrSearchTask(getActivity(), thisForCallback);
                    mFlickrSearchTask.execute(mCurrentBounds.southwest, mCurrentBounds.northeast);
                    mLoadingProgressbar.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * Save result OK for this activity.
     * It means that we need update some data in parent activity.
     */
    private void setActivityResultOK(){
        getActivity().setResult(getActivity().RESULT_OK);
    }
}