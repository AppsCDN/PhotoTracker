package vitalypanov.phototracker;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Binder;

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
    private Bitmap mMapImage;
    private GoogleMap mMap;
    private static final int LOCATION_REQUEST = 1;
    private static String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    PhotoTrackerGPSService mService;
    boolean mBound = false;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection ;

    public static PhotoTrackerFragment newInstance() {
        return new PhotoTrackerFragment();
    }

    private boolean hasPermisson (String perm){
        return (PackageManager.PERMISSION_GRANTED == getActivity().checkSelfPermission(perm));
    }

    private  boolean canAccessLocation(){
        return (hasPermisson(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        setHasOptionsMenu(true);
        if (!canAccessLocation()){
            requestPermissions(LOCATION_PERMISSIONS, LOCATION_REQUEST);
        }
        updatMapAsync();;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        Intent i = PhotoTrackerGPSService.newIntent(getActivity());
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                PhotoTrackerGPSService.LocalBinder binder = (PhotoTrackerGPSService.LocalBinder) service;
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
        inflater.inflate(R.menu.fragment_phototracker, menu);
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
                updateGoogleMapUI();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    /**
     * Start recording track
     */
    private void startTrack() {
        checkPermissions();
        Intent i = PhotoTrackerGPSService.newIntent(getActivity());
        getActivity().startService(i);
        getActivity().bindService(i, mConnection, 0);
    }

    /**
     * Stop recording track
     */
    private void stopTrack(){
        Intent i = PhotoTrackerGPSService.newIntent(getActivity());
        getActivity().stopService(i);
    }

    private void checkPermissions(){
        LocationManager locationManager = (LocationManager) getActivity()
                .getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager
                .isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            // GPS or Network is not enabled. Ask user to enable GPS/network in settings
            showSettingsAlert();
        }
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    private void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle("GPS is settings");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
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
        List<Location> currentTrack = mService.getCurrentTrack();
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
}

