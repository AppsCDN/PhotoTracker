package vitalypanov.phototracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.os.Binder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.NotificationCompat;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.util.ArrayList;
import java.util.List;
import android.content.ServiceConnection;

/**
 * Created by Vitaly on 25.08.2017.
 */

public class PhotoTrackerGPSService extends IntentService  implements LocationListener {
    private static final String TAG = "PhotoTrackerGPSService";
    private static final int UPDATE_INTERVAL = 1000*10;// 10 seconds (10 seconds is for emulator device. On real android device minimum value is 60 seconds :( )
    public static final String ACTION_SHOW_NOTIFICATION = "photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public List<Location> getCurrentTrack() {
        return currentTrack;
    }

    private List<Location> currentTrack = new ArrayList<>();

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    boolean startTrackingNotified =false;


    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //  meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 20; // seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        PhotoTrackerGPSService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PhotoTrackerGPSService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static Intent newIntent(Context context){
        return new Intent(context, PhotoTrackerGPSService.class);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static void setServiceAlarm(Context context, boolean isOn, Intent i){
        //Intent i = PhotoTrackerGPSService.newIntent(context);
        //context.bindService(i, serviceConnection, Context.BIND_AUTO_CREATE);
        PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        if (isOn){
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime(), UPDATE_INTERVAL, pi);
        }else {
            alarmManager.cancel(pi);
            pi.cancel();
        }
        PhotoTrackerPrefernces.setAlarmOn(context, isOn);
    }

    public static boolean isServiceAlarmOn(Context context){
        Intent i = PhotoTrackerGPSService.newIntent(context);
        PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
        return pi != null;
    }

    public PhotoTrackerGPSService() {
        super(TAG);
    }

    /**
     * Function to check GPS/wifi enabled
     * @return boolean
     * */
    public boolean canGetLocation() {
        return this.canGetLocation;
    }

    /**
     * Function to show settings alert dialog
     * On pressing Settings button will lauch Settings Options
     * */
    public void showSettingsAlert(){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);

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

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (!isNetworkAvailableAndConnected()){
            return;
        }
        showNotification();

        while(true){
            putCurrentGPSLocation();
            try {
                Thread.sleep(UPDATE_INTERVAL);
            } catch (InterruptedException ex) {
                Log.i("Failed", "", ex);
            }
        }

    }

    private void showNotification(){
        if (startTrackingNotified){
            return;
        }
        Resources resources = getResources();
        Intent i = PhotoTrackerActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        Bitmap appBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        Notification notification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.photo_tracker_process_title))
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setLargeIcon(appBitmap)
                .setContentTitle(resources.getString(R.string.photo_tracker_process_title))
                .setContentText(resources.getString(R.string.photo_tracker_process_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0, notification);
        sendBroadcast(new Intent((ACTION_SHOW_NOTIFICATION)), PERM_PRIVATE);
        startTrackingNotified = true;
    }

    private void showBackgroundNotification(int requestCode, Notification notification){
        Intent i = new Intent(ACTION_SHOW_NOTIFICATION);
        i.putExtra(REQUEST_CODE, requestCode);
        i.putExtra(NOTIFICATION, notification);
        sendOrderedBroadcast(i, PERM_PRIVATE, null, null,
                Activity.RESULT_OK, null, null);
    }

    private boolean isNetworkAvailableAndConnected(){
        ConnectivityManager cm = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        boolean isNetworkAvailable = cm.getActiveNetworkInfo() != null;
        boolean isNetworkConnected = isNetworkAvailable &&
                cm.getActiveNetworkInfo().isConnected();
        return isNetworkConnected;
    }

    /**
     * Getting and storing into array gps coordinates of current location
     */
    public void putCurrentGPSLocation() {
        try {
            Location location = null; // location
            double latitude; // latitude
            double longitude; // longitude

            locationManager = (LocationManager) this
                    .getSystemService(LOCATION_SERVICE);

            // getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                this.canGetLocation = true;

                // First get GPS lat/long using GPS Services
                if (isGPSEnabled) {
                    if (location == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("GPS Enabled", "GPS Enabled");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
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
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                        Log.d("Network", "Network");
                        if (locationManager != null) {
                            location = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }

                        }
                    }
                }
                // processing location...
                processLocation(location);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Store location in track
     * @param location
     */
    private void processLocation(Location location){
        if (location== null) {
            return;
        }
        // getting last already stored location in list
        Location lastLocation = null;
        if (currentTrack != null && !currentTrack.isEmpty()) {
            lastLocation = currentTrack.get(currentTrack.size()-1);
        }
        // last location is empty - it's the first location in track - store it
        if (lastLocation == null) {
            currentTrack.add(location);
            return;
        }
        // not the first location in track
        // add gps location to current track if it differ from last already stored value
        if (lastLocation.getLatitude() != location.getLatitude() || lastLocation.getLongitude() != location.getLongitude()) {
            currentTrack.add(location);
        }
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
