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
import java.util.UUID;

import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.utilities.BitmapHandler;
import vitalypanov.phototracker.utilities.GoogleMapUtils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class GoogleMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";

    private SupportMapFragment mMapFragment = null;
    private RelativeLayout mLoadingFrame;

    private GoogleMap mGoogleMap;
    private Track mTrack= null;
    private HashMap <String, Bitmap> mBitmapHashMap = null;

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
        View v = layoutInflater.inflate(R.layout.fragment_map_track, container, false);
        mMapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
        mLoadingFrame = (RelativeLayout) v.findViewById(R.id.google_map_loading_data_frame);
        AssyncLoaderTask assyncLoaderTask = new AssyncLoaderTask();
        assyncLoaderTask.execute();
        return v;
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
                if (!Settings.get(getActivity()).getBoolean(Settings.KEY_MAP_PERFOMANCE_SWITCH)) {
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
        final GoogleMapFragment thisForCallback = this;
        mMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setOnMarkerClickListener(thisForCallback);
                GoogleMapUtils.updateGoogleMapUI(mGoogleMap, mTrack, getContext(), mBitmapHashMap);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        String photoFileName = marker.getSnippet();
        if (photoFileName!= null && mTrack.getPhotoFiles().size()>0) {
            Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), (ArrayList<TrackPhoto>) mTrack.getPhotoFiles(), photoFileName);
            startActivity(intent);
        }
        return false;
    }
}
