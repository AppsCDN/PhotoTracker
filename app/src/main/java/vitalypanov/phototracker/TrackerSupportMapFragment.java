package vitalypanov.phototracker;

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
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LAT1 = "SAVED_PARAM_CURRENT_BOUNDS_LAT1";
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LON1 = "SAVED_PARAM_CURRENT_BOUNDS_LON1";
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LAT2 = "SAVED_PARAM_CURRENT_BOUNDS_LAT2";
    private static final String SAVED_PARAM_CURRENT_BOUNDS_LON2 = "SAVED_PARAM_CURRENT_BOUNDS_LON2";

    private SupportMapFragment mMapFragment = null;
    private RelativeLayout mLoadingFrame;
    private HashMap <String, Bitmap> mBitmapHashMap = null;
    LatLngBounds mCurrentBounds = null;
    ArrayList<Marker> mFlickerMarkers = null;
    private ProgressBar mLoadingProgressbar;
    FlickrSearchTask mFlickrSearchTask = null;

    public abstract Track getTrack();

    public abstract int getLayoutResourceId();

    protected void startAssyncLoader(){
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
        updatMapAsyncPosCurrentLocation();
        startAssyncLoader();
        return view;
    }

    // TODO maybe some refactoring need in the method below
    private void updatMapAsyncPosCurrentLocation(){
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
            //mMapFragment.getView().findViewById(View.GONE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (!Utils.isNull(mLoadingFrame)) {
                mLoadingFrame.setVisibility(View.GONE);
            }
            updatMapAsync();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
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
        final TrackerSupportMapFragment thisForCallback = this;
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setOnMarkerClickListener(thisForCallback);
                if (!Utils.isNull(getTrack())) {
                    GoogleMapUtils.drawTrackOnGoogleMap(googleMap, getTrack(), getContext(), mBitmapHashMap);
                }
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        startFlickrSearch();
                    }
                });
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
            // local photos of the track
            if (!Utils.isNull(getTrack()) && !Utils.isNull(getTrack().getPhotoFiles()) && !getTrack().getPhotoFiles().isEmpty()) {
                Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), getTrack().getUUID(), (ArrayList<TrackPhoto>) getTrack().getPhotoFiles(), photoName);
                startActivity(intent);
            }
        }
        return false;
    }

    @Override
    public void onTaskCompleted(List<FlickrPhoto> flickrPhotos) {
        FlickrHolder.get().setFlickrPhotos(flickrPhotos);
        updatFlickrMapAsync();
        mLoadingProgressbar.setVisibility(View.GONE);
    }

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
}
