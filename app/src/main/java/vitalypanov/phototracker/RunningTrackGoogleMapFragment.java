package vitalypanov.phototracker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import vitalypanov.phototracker.model.TrackBitmap;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.utilities.ListUtils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackGoogleMapFragment extends Fragment implements ViewPageUpdater{
    private static final String TAG = "PhotoTracker";
    private SupportMapFragment mapFragment = null;
    private GoogleMap mMap;

    TrackerGPSService mService;
    boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection ;

    public static RunningTrackGoogleMapFragment newInstance() {
        return new RunningTrackGoogleMapFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View v = layoutInflater.inflate(R.layout.fragment_map_running_track, container, false);
        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
        updatMapAsync();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        //getActivity().invalidateOptionsMenu();
        Intent i = TrackerGPSService.newIntent(getActivity());
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                TrackerGPSService.LocalBinder binder = (TrackerGPSService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                // after service bound we can load photos
                mService.getCurrentTrack().loadCashedBitmaps(getContext());
                // ...and update map
                updatMapAsync();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }


        };
        getActivity().bindService(i, mConnection, 0); //Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
    }

    /**
     * Updating assync google map and save map object into local variable
     * for future drawing
     */
    private void updatMapAsync(){
        if (mapFragment==null){
            return;
        }
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateGoogleMapUI();
            }
        });
    }

    /**
     Draw on google map
     */
    private void updateGoogleMapUI(){
        if (mMap == null ){
            return;
        }

        // service which holds track data should be not empty
        if (mService == null){
            return;
        }

        // getting current gps track from service
        List<TrackLocation> trackData = mService.getCurrentTrack().getTrackData();
        if (trackData == null || trackData.isEmpty()) {
            return;
        }
        LatLng itemPoint = new LatLng(
                ListUtils.getFirst(trackData).getLatitude(), ListUtils.getFirst(trackData).getLongitude());
        LatLng myPoint = new LatLng(
                ListUtils.getLast(trackData).getLatitude(), ListUtils.getLast(trackData).getLongitude());

        mMap.clear();

        // start point marker
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint);
        mMap.addMarker(itemMarker);

        // end(current) point marker
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        mMap.addMarker(myMarker);

        // all bitmap markers
        for (TrackBitmap trackBitmap :  mService.getCurrentTrack().getCashedBitmaps()){
            TrackLocation trackLocation = trackBitmap.getTrackPhoto().getTrackLocation();
            BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(trackBitmap.getBitmap());
            MarkerOptions photoMarker = new MarkerOptions()
                    .position(new LatLng(trackLocation.getLatitude(), trackLocation.getLongitude()))
                    .icon(itemBitmap)
                    ;
            mMap.addMarker(photoMarker);
        }

        PolylineOptions lines = new PolylineOptions();
        for(TrackLocation loc : trackData){
            lines.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
        mMap.addPolyline(lines);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoint)
                .include(myPoint)
                .build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);

        //mMap.animateCamera(update);
        mMap.moveCamera(update);

    }

    @Override
    public void onPageSelected() {
        // redraw map when selecting tab
        updatMapAsync();
    }
}
