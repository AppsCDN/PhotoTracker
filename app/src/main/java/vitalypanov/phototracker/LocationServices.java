package vitalypanov.phototracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Singleton for checking and running location service dialog if need
 * Created by Vitaly on 08.03.2018.
 */

public class LocationServices implements LocationListener {
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // The minimum distance to change Updates in meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // The minimum time between updates in milliseconds

    private static LocationServices mLocationServices;
    Context mContext;

    public static LocationServices get(Context context) {
        if (mLocationServices == null) {
            mLocationServices = new LocationServices(context);
        }
        return mLocationServices;
    }

    private LocationServices(Context context) {
        mContext = context;
    }

    /**
     * Check of GPS and network location permissions
     * @return  true - if permissions granted
     *          false - if not - and user should grant permissions in shown dialog
     */
    public boolean checkLocaionServices(){
        LocationManager locationManager = (LocationManager) mContext
                .getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            // GPS or Network is not enabled. Ask user to enable GPS/network in settings
            showLocationServicesSettingsAlert();
            return false;
        }
        return true;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    private void showLocationServicesSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.alert_gps_title);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.alert_gps_message);

        // On pressing Settings button
        alertDialog.setPositiveButton(mContext.getResources().getString(R.string.gps_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(mContext.getResources().getString(R.string.gps_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @SuppressLint("MissingPermission")
    public Location getCurrentGPSLocation(){
        try {
            Location location = null; // location
            double latitude; // latitude
            double longitude; // longitude

            LocationManager locationManager = (LocationManager) mContext
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean  isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {

                // First get GPS lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this,
                                Looper.getMainLooper());
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                return location;
                            }
                        }
                    }
                }

                // get location from Network Provider
                if (isNetworkEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this,
                                Looper.getMainLooper());
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                return location;
                            }

                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
