package vitalypanov.phototracker;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Vitaly on 29.08.2017.
 */

public class PhotoTrackerFragment extends SupportMapFragment {
    private static final String TAG = "PhotoTracker";
    private static final int GPS_TIMER_DELAY = 1000;
    private static final int GPS_TIMER_INTERVAL = 10*1000;
    private Location mCurrentLocation;
    GPSTracker gps;
    Timer timer;
    TimerTask mTimerTask;
    private List<Location> mCurrentTrack = new ArrayList<>();
    private Bitmap mMapImage;
    private GoogleMap mMap;

    public static PhotoTrackerFragment newInstance() {
        return new PhotoTrackerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        /*
        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {

                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .build();
        */

        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateUI();
            }
        });
        //mProgressDialog = new ProgressDialog(getActivity());
        //mProgressDialog.setTitle(R.string.search_progress);

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        //mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
       // mClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_phototracker, menu);

        //MenuItem searchItem = menu.findItem(R.id.action_start);
        //searchItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_start:
                startTrack();
                return true;
            case R.id.action_stop:
                stopTrack();
                return true;
            case R.id.action_current_location:
                updateUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Start recording track
     */
    private void startTrack() {
        // clear all saved tracking data
        mCurrentTrack.clear();

        if(timer != null){
            timer.cancel();
        }

        timer = new Timer();
        mTimerTask = new PhotoTrackerTimerTask();

        timer.schedule(mTimerTask, GPS_TIMER_DELAY, GPS_TIMER_INTERVAL);
    }

    /**
     * Stop recording track
     */
    private void stopTrack(){
        if (timer!=null){
            timer.cancel();
            timer = null;
        }
    }

    private void updateLocation(){
        // create class object
        gps = new GPSTracker(getActivity());

        // check if GPS enabled
        if(gps.canGetLocation()){

            Location lastLocation = null;
            Location currLocation = gps.getLocation();
            if (currLocation==null){
                Toast.makeText(getActivity().getApplicationContext(), "GPS signal is not too good on your current position, please get better location.", Toast.LENGTH_LONG).show();
                return;
            }
            // add gps location to current track
            if (mCurrentTrack != null && !mCurrentTrack.isEmpty()) {
                lastLocation = mCurrentTrack.get(mCurrentTrack.size()-1);
            }

            if (lastLocation!= null){
                // add only if differ against last stored location
                if (lastLocation.getLatitude() != currLocation.getLatitude() || lastLocation.getLongitude() != currLocation.getLongitude()){
                    mCurrentTrack.add(currLocation);
                }
            } else {
                // first location in track
                mCurrentTrack.add(currLocation);
            }

            // show current location
            double latitude = currLocation.getLatitude();
            double longitude = currLocation.getLongitude();
            // \n is for new line
            Toast.makeText(getActivity().getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }

    private void updateUI(){
        if (mMap == null ){
            return;
        }

        if (mCurrentTrack == null || mCurrentTrack.isEmpty()) {
            return;
        }
        LatLng itemPoint = new LatLng(
                Lists.getFirst(mCurrentTrack).getLatitude(), Lists.getFirst(mCurrentTrack).getLongitude());
        LatLng myPoint = new LatLng(
                Lists.getLast(mCurrentTrack).getLatitude(), Lists.getLast(mCurrentTrack).getLongitude());

        //BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(mMapImage);
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        mMap.clear();
        mMap.addMarker(itemMarker);
        mMap.addMarker(myMarker);

        PolylineOptions lines = new PolylineOptions();
        for(Location loc : mCurrentTrack){
            lines.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
        mMap.addPolyline(lines);

        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(itemPoint)
                .include(myPoint)
                .build();
        int margin = getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        mMap.animateCamera(update);

    }

    /**
     * Timer for gps tracking
     */
    class PhotoTrackerTimerTask extends TimerTask {
        @Override
        public void run() {

            getActivity().runOnUiThread (new Runnable(){

                @Override
                public void run() {
                    updateLocation();
                }});
        }
    }

}

