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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Vitaly on 29.08.2017.
 */

public class PhotoTrackerFragment extends Fragment {
    private static final String TAG = "PhotoTracker";
    private static final int GPS_TIMER_DELAY = 1000;
    private static final int GPS_TIMER_INTERVAL = 10*1000;
    private GoogleApiClient mClient;
    //private GoogleMap mMap;
    private ProgressDialog mProgressDialog;
    private Bitmap mMapImage;
    //private GalleryItem mMapItem;
    private Location mCurrentLocation;
    GPSTracker gps;
    Timer timer;
    TimerTask mTimerTask;
    private List<Location> mCurrentTrack = new ArrayList<>();

    public static PhotoTrackerFragment newInstance() {
        return new PhotoTrackerFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

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
        /*
        getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                updateUI();
            }
        });
        */
        //mProgressDialog = new ProgressDialog(getActivity());
        //mProgressDialog.setTitle(R.string.search_progress);

    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        mClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_phototracker, menu);

        MenuItem searchItem = menu.findItem(R.id.action_start);
        searchItem.setEnabled(mClient.isConnected());
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
                updateLocation();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Start recording track
     */
    private void startTrack() {
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
        // clear all saved tracking data
        mCurrentTrack.clear();
    }

    private void updateLocation(){
        // create class object
        gps = new GPSTracker(getActivity());

        // check if GPS enabled
        if(gps.canGetLocation()){

            Location location = gps.getLocation();
            // add gps location to current track
            mCurrentTrack.add(location);

            // show current location
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            // \n is for new line
            Toast.makeText(getActivity().getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();

        }else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
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

