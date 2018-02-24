package vitalypanov.phototracker;

import android.app.AlertDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v7.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;

/**
 * Created by Vitaly on 25.08.2017.
 */

public class TrackerGPSService extends Service  implements LocationListener {
    private static final String TAG = "TrackerGPSService";
    private static final int UPDATE_INTERVAL = 1000*10;// 10 seconds (10 seconds is for emulator device. On real android device minimum value is 60 seconds :( )
    private static final int UPDATE_DB_INTERVAL = 1000*60*5;// is 5 minutes interval for updating in Db
    public static final String ACTION_SHOW_NOTIFICATION = "photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public Track getCurrentTrack() {
        return currentTrack;
    }

    private Track currentTrack;

    // flag for GPS status
    boolean isGPSEnabled = false;

    // flag for network status
    boolean isNetworkEnabled = false;

    // flag for GPS status
    boolean canGetLocation = false;

    // The minimum distance to change Updates in meters
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; //  meters

    // The minimum time between updates in milliseconds
    private static final long MIN_TIME_BW_UPDATES = 1000 * 20; // seconds

    // Declaring a Location Manager
    protected LocationManager locationManager;

    // Binder given to clients
    private final IBinder mBinder = new LocalBinder();

    // Timer for getting GPS coordinates
    private Timer timer;
    private TimerTask timerTask;

    // Timer for updating data in database
    private Timer dbTimer;
    private TimerTask dbTimerTask;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 0, UPDATE_INTERVAL); //
    }

    public void initializeTimerTask() {
        buildStubNotification();
        timerTask = new TimerTask() {
            public void run() {
                // Foreground mode should be set for the service mandatory!!! (this line is the main line of the service! :) )
                // Staying in foreground to prevent service from closing by Android system
                startForeground(1, stubNotification);
                putCurrentGPSLocation();
            }
        };
    }

    public void startDbTimer() {
        //set a new Timer
        dbTimer = new Timer();
        initializeDbTimerTask();
        dbTimer.schedule(dbTimerTask, UPDATE_DB_INTERVAL, UPDATE_DB_INTERVAL);
    }

    public void initializeDbTimerTask() {
        buildStubNotification();
        dbTimerTask = new TimerTask() {
            public void run() {
                TrackDbHelper.get(getApplicationContext()).updateTrack(currentTrack);
            }
        };
    }

    /*
    public void stopDbTimerTask() {
        if (dbTimer != null) {
            dbTimer.cancel();
            dbTimer = null;
        }
    }
    */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        currentTrack = new Track();
        currentTrack.setStartTime(Calendar.getInstance().getTime());
        TrackDbHelper.get(getApplicationContext()).insertTrack(currentTrack);
        startTimer();
        startDbTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Last update data in db before exiting from service
        TrackDbHelper.get(getApplicationContext()).updateTrack(currentTrack);
        super.onDestroy();
    }

    // stub notification for getting service to foreground mode to prevent ActivityManager to stop our service
    Notification stubNotification;
    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        TrackerGPSService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TrackerGPSService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public static Intent newIntent(Context context){
        return new Intent(context, TrackerGPSService.class);
    }

    public TrackerGPSService() {
        super();
        currentTrack = new Track();
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

    /**
     * Build notification object for setForeground() function
     */
    private void buildStubNotification(){
        Resources resources = getResources();
        Intent i = RunningTrackPagerActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        Bitmap appBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        stubNotification = new NotificationCompat.Builder(this)
                .setTicker(resources.getString(R.string.app_name))
                .setSmallIcon(R.mipmap.ic_walk)
                .setLargeIcon(appBitmap)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(resources.getString(R.string.photo_tracker_process_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
        /*
        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(this);
        notificationManagerCompat.notify(0, notification);
        sendBroadcast(new Intent((ACTION_SHOW_NOTIFICATION)), PERM_PRIVATE);
        */
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
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this,
                                Looper.getMainLooper());
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
                                MIN_DISTANCE_CHANGE_FOR_UPDATES,
                                this,
                                Looper.getMainLooper());
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
        if (currentTrack != null && !currentTrack.getTrackData().isEmpty()) {
            lastLocation = currentTrack.getLastTrackItem();
        }
        // last location is empty - it's the first location in track - store it
        if (lastLocation == null) {
            currentTrack.addTrackItem(location);
            return;
        }
        // not the first location in track
        // add gps location to current track if it differ from last already stored value
        if (lastLocation.getLatitude() != location.getLatitude() || lastLocation.getLongitude() != location.getLongitude()) {
            currentTrack.addTrackItem(location);
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
