package vitalypanov.phototracker;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackGoogleMapFragment extends SupportMapFragment implements ViewPageUpdater{
    private static final String TAG = "PhotoTracker";
    private Bitmap mMapImage;
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
        setHasOptionsMenu(true);

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        Intent i = TrackerGPSService.newIntent(getActivity());
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                TrackerGPSService.LocalBinder binder = (TrackerGPSService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                // after service bound we can update map
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.googlemap_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_current_location:
                updateGoogleMapUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Updating assync google map and save map object into local variable
     * for future drawing
     */
    private void updatMapAsync(){
        getMapAsync(new OnMapReadyCallback() {
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
        List<Location> currentTrack = mService.getCurrentTrack().getTrackData();
        if (currentTrack == null || currentTrack.isEmpty()) {
            return;
        }
        LatLng itemPoint = new LatLng(
                Lists.getFirst(currentTrack).getLatitude(), Lists.getFirst(currentTrack).getLongitude());
        LatLng myPoint = new LatLng(
                Lists.getLast(currentTrack).getLatitude(), Lists.getLast(currentTrack).getLongitude());

        //BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        mMap.clear();
        mMap.addMarker(itemMarker);
        mMap.addMarker(myMarker);

        PolylineOptions lines = new PolylineOptions();
        for(Location loc : currentTrack){
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
