package vitalypanov.phototracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;

/**
 * Singleton for checking and running location service dialog if need
 * Created by Vitaly on 08.03.2018.
 */

public class LocationServices {
    private static LocationServices mLocationServices;
    Activity mActivity;


    public static LocationServices get(Activity context) {
        if (mLocationServices == null) {
            mLocationServices = new LocationServices(context);
        }
        return mLocationServices;
    }

    private LocationServices(Activity activity) {
        mActivity = activity;
    }

    /**
     * Check of GPS and network location permissions
     * @return  true - if permissions granted
     *          false - if not - and user should grant permissions in shown dialog
     */
    public boolean checkLocaionServices(){
        LocationManager locationManager = (LocationManager) mActivity
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(mActivity);

        // Setting Dialog Title
        alertDialog.setTitle(R.string.alert_gps_title);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.alert_gps_message);

        // On pressing Settings button
        alertDialog.setPositiveButton(mActivity.getResources().getString(R.string.gps_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mActivity.startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(mActivity.getResources().getString(R.string.gps_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }
}
