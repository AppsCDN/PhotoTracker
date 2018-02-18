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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

/**
 * Created by Vitaly on 29.08.2017.
 */

public class PhotoTrackerFragment extends Fragment {
    private static final String TAG = "PhotoTracker";
    private GoogleApiClient mClient;
    //private GoogleMap mMap;
    private ProgressDialog mProgressDialog;
    private Bitmap mMapImage;
    //private GalleryItem mMapItem;
    private Location mCurrentLocation;

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
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void startTrack() {
        //mProgressDialog.show();

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PhotoTrackerActivity.MY_PERMISSION_REQUEST_READ_FINE_LOCATION);
            return; // user should start the process again after granted permissions
        }
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        //request.setNumUpdates(1);
        request.setInterval(5000);
        // permission was granted, yay! Do the contacts-related task you need to do.
        try {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mClient, request, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.i(TAG, "Got a fix:" + location);
                        }
                    });
        } catch (SecurityException ex) {
            Log.i(TAG, "SecurityException: " + ex);
        }
    }


}

