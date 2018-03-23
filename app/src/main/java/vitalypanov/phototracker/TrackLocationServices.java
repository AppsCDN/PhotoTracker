package vitalypanov.phototracker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import static android.content.Context.LOCATION_SERVICE;

/**
 * Checking and running location service dialog if need
 * Created by Vitaly on 08.03.2018.
 */

public class TrackLocationServices{
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // The minimum distance to change Updates in meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // The minimum time between updates in milliseconds

    /**
     * Check if GPS and network location services are on
     * Show warning dialog if turn off
     * @return  true - if permissions granted
     *          false - if not - and user should grant permissions in shown dialog
     */
    public static boolean checkLocaionServices(final Context context){
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        // getting GPS status
        if (!locationManager
                .isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            // GPS or Network is not enabled. Ask user to enable GPS/network in settings
            showLocationServicesSettingsAlert(context);
            return false;
        }
        return true;
    }

    /**
     * Show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    private static void showLocationServicesSettingsAlert(final Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // Setting Dialog Title
        builder.setTitle(R.string.alert_gps_title);

        // Setting Dialog Message
        builder.setMessage(R.string.alert_gps_message);

        // On pressing Settings button
        builder.setPositiveButton(context.getResources().getString(R.string.gps_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                context.startActivity(intent);
            }
        });

        // on pressing cancel button
        builder.setNegativeButton(context.getResources().getString(R.string.gps_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("MissingPermission")
    public static Location getCurrentGPSLocation(final Context context){
        try {
            Location location = null; // location
            double latitude; // latitude
            double longitude; // longitude

            LocationManager locationManager = (LocationManager) context
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            boolean isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            boolean  isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);


            LocationListener listener = new LocationListener(){

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
            };

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
                                listener,
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
                                listener,
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
}
