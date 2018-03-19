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
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;

import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.others.BindTrackerGPSService;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.BitmapHandler;
import vitalypanov.phototracker.utilities.GoogleMapUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackGoogleMapFragment extends Fragment implements ViewPageUpdater, BindTrackerGPSService, GoogleMap.OnMarkerClickListener {
    private static final String TAG = "PhotoTracker";

    private SupportMapFragment mapFragment = null;
    private RelativeLayout mLoadingFrame;
    private GoogleMap mGoogleMap;
    private HashMap<String, Bitmap> mBitmapHashMap;

    private TrackerGPSService mService;

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
        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
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
                if (!Settings.get(getActivity()).getBoolean(Settings.KEY_MAP_PERFOMANCE_SWITCH)) {
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
        if (mapFragment==null){
            return;
        }
        GoogleMapUtils.initMapControls(mapFragment);
        final RunningTrackGoogleMapFragment thisForCallback = this;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setOnMarkerClickListener(thisForCallback);
                if (mService == null){
                    return;
                }
                GoogleMapUtils.drawTrackOnGoogleMap(mGoogleMap, mService.getCurrentTrack(), getContext(), mBitmapHashMap);
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
        String photoFileName = marker.getSnippet();
        if (    photoFileName == null
                || mService == null
                || mService.getCurrentTrack() == null
                || mService.getCurrentTrack().getPhotoFiles().isEmpty()) {
            return false;
        }
        Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), mService.getCurrentTrack().getUUID(), (ArrayList<TrackPhoto>) mService.getCurrentTrack().getPhotoFiles(), photoFileName);
        startActivity(intent);
        return false;
    }

}