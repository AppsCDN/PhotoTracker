package vitalypanov.phototracker;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
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
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import vitalypanov.phototracker.activity.RunningTrackPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;

/**
 * Created by Vitaly on 25.08.2017.
 */

public class TrackerGPSService extends Service  implements LocationListener {
    // Service constants:
    private static final String TAG = "PhotoTracker GPSService";
    private static final String NOTIFICATION_CHANNEL_ID = "TrackerGPSService_ID";
    private static final int UPDATE_INTERVAL = 1000*10;// 10 seconds (10 seconds is for emulator device. On real android device minimum value is 60 seconds :( )
    private static final int DELAY_INTERVAL = 1000*3;// 3 seconds delay before time start
    private static final int UPDATE_DB_INTERVAL = 1000*60*5;// is 5 minutes interval for updating in Db
    public static final String ACTION_SHOW_NOTIFICATION = "photogallery.SHOW_NOTIFICATION";
    public static final String PERM_PRIVATE = "photogallery.PRIVATE";
    public static final String REQUEST_CODE = "REQUEST_CODE";
    public static final String NOTIFICATION = "NOTIFICATION";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // The minimum distance to change Updates in meters
    private static final long MIN_TIME_BW_UPDATES = 1000 * 10; // The minimum time between updates in milliseconds

    // Service private:
    private Track currentTrack;
    private boolean isGPSEnabled = false;   // flag for GPS status
    private boolean isNetworkEnabled = false;   // flag for network status
    private boolean canGetLocation = false;         // flag for GPS status
    private final IBinder mBinder = new LocalBinder();  // Binder given to clients
    private Notification mStubNotification; // stub notification for getting service to foreground mode to prevent ActivityManager to stop our service
    private Timer timer;    // Timer for getting GPS coordinates
    private TimerTask timerTask;
    private Timer dbTimer;  // Timer for updating data in database
    private TimerTask dbTimerTask;

    // Service protected:
    protected LocationManager locationManager;  // Declaring a Location Manager

    // Service public:
    public Track getCurrentTrack() {
        return currentTrack;
    }

    /**
     * Run new track
     * @param context
     * @return
     */
    public static Intent newIntent(Context context){
        return new Intent(context, TrackerGPSService.class);
    }

    /**
     * Rerun axisting track
     * @param context
     * @param trackUUID
     * @return
     */
    public static Intent newIntent(Context context, UUID trackUUID){
        Intent intent = new Intent(context, TrackerGPSService.class);
        intent.putExtra(EXTRA_TRACK_UUID, trackUUID);
        return intent;
    }
    /**
     * Timer for regular requesting gps coordinates
     */
    public void startTimer() {
        timer = new Timer();
        initializeTimerTask();
        timer.schedule(timerTask, DELAY_INTERVAL, UPDATE_INTERVAL); //DELAY_INTERVAL should be provided
    }

    public void initializeTimerTask() {
        buildStubNotification();
        startForeground(1, mStubNotification);
        timerTask = new TimerTask() {
            public void run() {
                // Foreground mode should be set for the service mandatory!!! (this line is the main line of the service! :) )
                // Staying in foreground to prevent service from closing by Android system
                putCurrentGPSLocation();
            }
        };
    }

    /**
     * Timer for regular writing gps coordinates into db
     */
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

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        UUID uuid = (UUID)intent.getSerializableExtra(EXTRA_TRACK_UUID);
        if (uuid != null)
        {   // Continue already existing track
            currentTrack = TrackDbHelper.get(getApplicationContext()).getTrack(uuid);
            currentTrack.setEndTime(new Date());
            TrackDbHelper.get(getApplicationContext()).updateTrack(currentTrack);
        } else {
            // Create new track
            currentTrack = new Track();
            currentTrack.setStartTime(Calendar.getInstance().getTime());
            TrackDbHelper.get(getApplicationContext()).insertTrack(currentTrack);
        }
        startTimer();
        startDbTimer();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // track is ended
        currentTrack.setEndTime(Calendar.getInstance().getTime());
        // Last update data in db before exiting from service
        TrackDbHelper.get(getApplicationContext()).updateTrack(currentTrack);
        super.onDestroy();
    }

    /**
     * External call for forcing write to db
     */
    public void forceWriteToDb(){
        TrackDbHelper.get(getApplicationContext()).updateTrack(currentTrack);
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public TrackerGPSService getService() {
            // Return this instance of LocalService so clients can call public methods
            return TrackerGPSService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // For android Oreo should specify notification channel.
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, TAG, NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription(TAG);
            notificationManager.createNotificationChannel(notificationChannel);
        }

        Resources resources = getResources();
        Intent i = RunningTrackPagerActivity.newIntent(this);
        PendingIntent pi = PendingIntent.getActivity(this, 0, i, 0);
        Bitmap appBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher_notification);
        mStubNotification = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setTicker(resources.getString(R.string.app_name))
                .setLargeIcon(appBitmap)
                .setSmallIcon(R.mipmap.ic_steps)
                .setContentTitle(resources.getString(R.string.app_name))
                .setContentText(resources.getString(R.string.photo_tracker_process_text))
                .setContentIntent(pi)
                .setAutoCancel(true)
                .build();
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
    @SuppressLint("MissingPermission")
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
                TrackLocation trackLocation = new TrackLocation(location.getLongitude(), location.getLatitude(),
                        location.getAltitude(), Calendar.getInstance().getTime());
                processLocation(trackLocation);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Store location in track
     * @param trackLocation
     */
    private void processLocation(TrackLocation trackLocation){
        if (trackLocation== null) {
            return;
        }
        // getting last already stored location in list
        TrackLocation trackLastLocation = null;
        if (currentTrack != null && !currentTrack.getTrackData().isEmpty()) {
            trackLastLocation = currentTrack.getLastTrackItem();
        }
        // last location is empty - it's the first location in track - store it
        if (trackLastLocation == null) {
            currentTrack.addTrackItem(trackLocation);
            return;
        }
        // not the first location in track
        // add gps location to current track if it differ from last already stored value
        if (trackLastLocation.getLatitude() != trackLocation.getLatitude() || trackLastLocation.getLongitude() != trackLocation.getLongitude()) {
            currentTrack.addTrackItem(trackLocation);
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
