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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
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
 * Created by Vitaly on 23.02.2018.
 */

public class GoogleMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener, OnFlickrSearchTaskCompleted {
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";

    private Track mTrack= null;

    private SupportMapFragment mMapFragment = null;
    private RelativeLayout mLoadingFrame;
    private HashMap <String, Bitmap> mBitmapHashMap = null;
    LatLngBounds mCurrentBounds = null;
    ArrayList<Marker> mFlickerMarkers = null;
    private ProgressBar mLoadingProgressbar;
    FlickrSearchTask mFlickrSearchTask = null;

    public static GoogleMapFragment newInstance(UUID uuid) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TRACK_UUID, uuid);
        GoogleMapFragment googleMapFragment = new GoogleMapFragment();
        googleMapFragment.setArguments(args);
        return googleMapFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View view = layoutInflater.inflate(R.layout.fragment_map_track, container, false);
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
        mLoadingFrame = (RelativeLayout) view.findViewById(R.id.google_map_loading_data_frame);
        mLoadingProgressbar = (ProgressBar) view.findViewById(R.id.loading_progressbar);
        mLoadingProgressbar.setVisibility(View.GONE);
        AssyncLoaderTask assyncLoaderTask = new AssyncLoaderTask();
        assyncLoaderTask.execute();
        return view;
    }

    class AssyncLoaderTask extends AsyncTask<Void, Void, Void> {

        public AssyncLoaderTask() {
        }

        @Override
        protected Void doInBackground(Void... params) {
            if (mTrack == null) {
                UUID uuid = (UUID) getArguments().getSerializable(EXTRA_TRACK_UUID);
                mTrack = TrackDbHelper.get(getActivity()).getTrack(uuid);
            }
            // if bitmaps not loaded yet...
            if (mBitmapHashMap == null) {
                mBitmapHashMap = new HashMap<String, Bitmap>();
                // performance setting
                if (!Settings.get(getActivity()).isMapPerformance()) {
                    // load all bitmaps of the track
                    for (TrackPhoto trackPhoto : mTrack.getPhotoFiles()) {
                        Bitmap bitmap = BitmapHandler.get(getContext()).getBitmapScaleToSize(trackPhoto.getPhotoFileName(), GoogleMapUtils.SCALE_SMALL_SIZE);
                        mBitmapHashMap.put(trackPhoto.getPhotoFileName(), bitmap);
                    }
                }
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            mLoadingFrame.setVisibility(View.VISIBLE);
            //mMapFragment.getView().findViewById(View.GONE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mLoadingFrame.setVisibility(View.GONE);
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
        final GoogleMapFragment thisForCallback = this;
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                googleMap.setOnMarkerClickListener(thisForCallback);
                GoogleMapUtils.drawTrackOnGoogleMap(googleMap, mTrack, getContext(), mBitmapHashMap);
                googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
                        if (!bounds.equals(mCurrentBounds)) {
                            mCurrentBounds = bounds;
                            if (!Utils.isNull(mFlickrSearchTask)){
                                mFlickrSearchTask.cancel(true);
                            }
                            mFlickrSearchTask = new FlickrSearchTask(getActivity(), thisForCallback);
                            mFlickrSearchTask.execute(mCurrentBounds.southwest, mCurrentBounds.northeast);
                            mLoadingProgressbar.setVisibility(View.VISIBLE);
                        }
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
                Intent intent = TrackImagesPagerActivity.newIntentFlickr(getActivity(), mTrack.getUUID(), photoName);
                startActivity(intent);
            }
        } else {
            // local photos of the track
            if (!mTrack.getPhotoFiles().isEmpty()) {
                Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), mTrack.getUUID(), (ArrayList<TrackPhoto>) mTrack.getPhotoFiles(), photoName);
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

}
