package vitalypanov.phototracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.UUID;

import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.utilities.GoogleMapUtils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class GoogleMapFragment extends Fragment implements GoogleMap.OnMarkerClickListener {
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";
    private SupportMapFragment mapFragment = null;
    private GoogleMap mGoogleMap;
    private Track mTrack;

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

        UUID uuid = (UUID)getArguments().getSerializable(EXTRA_TRACK_UUID);
        mTrack = TrackDbHelper.get(getActivity()).getTrack(uuid);
       // mTrack.loadCashedBitmaps(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View v = layoutInflater.inflate(R.layout.fragment_map_track, container, false);
        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
        updatMapAsync();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        //getActivity().invalidateOptionsMenu();
    }

    /**
     * Updating assync google map and save map object into local variable
     * for future drawing
     */
    private void updatMapAsync(){
        if (mapFragment==null){
            return;
        }
        final GoogleMapFragment thisForCallback = this;
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setOnMarkerClickListener(thisForCallback);
                GoogleMapUtils.updateGoogleMapUI(mGoogleMap, mTrack, getContext());
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
