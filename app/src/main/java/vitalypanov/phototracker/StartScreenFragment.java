package vitalypanov.phototracker;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import vitalypanov.phototracker.activity.RunningTrackPagerActivity;
import vitalypanov.phototracker.activity.TrackListActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.utilities.ServiceUtils;

/**
 * Created by Vitaly on 29.08.2017.
 */

public class StartScreenFragment extends Fragment {
    private static final String TAG = "PhotoTracker";
    public static final int MY_PERMISSION_REQUEST_READ_FINE_LOCATION = 1;
    private static final int REQUEST_ERROR = 0;
    private static final int LOCATION_REQUEST = 1;
    private static final int EXTERNAL_STORAGE_REQUEST = 2;
    private static String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    private static String[] EXTERNAL_STORAGE_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Button mTrackStart;
    private Button mTrackList;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection ;

    public static StartScreenFragment newInstance() {
        return new StartScreenFragment();
    }

    private boolean hasPermisson (String perm){
        return (PackageManager.PERMISSION_GRANTED == getActivity().checkSelfPermission(perm));
    }

    private  boolean canAccessLocation(){
        return (hasPermisson(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private  boolean canAccessExternalStorage(){
        return (hasPermisson(Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);
        setHasOptionsMenu(true);
        if (!canAccessLocation()){
            requestPermissions(LOCATION_PERMISSIONS, LOCATION_REQUEST);
        }
        if (!canAccessExternalStorage()){
            requestPermissions(EXTERNAL_STORAGE_PERMISSIONS, EXTERNAL_STORAGE_REQUEST);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_screen, container, false);

        mTrackStart =  (Button) view.findViewById(R.id.track_start);
        mTrackStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTrack();
            }
        });

        mTrackList =  (Button) view.findViewById(R.id.track_list);
        mTrackList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = TrackListActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });
        return view;
    }

      @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        // if service already running we should start running activity above current
        if (ServiceUtils.isServiceRunning(
                  getActivity().getApplicationContext(),
                  TrackerGPSService.class)){
            Intent intent = RunningTrackPagerActivity.newIntent(getActivity());
            startActivity(intent);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        // update tracks count value on button
        if (mTrackList!= null) {
            mTrackList.setText(getResources().getString(R.string.action_track_list) + " (" + String.valueOf(TrackDbHelper.get(getContext()).getTracksCount()) + ")");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.startscreen_menu, menu);
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

    /**
     * Start recording track
     */
    private void startTrack() {
        // first check permissions of location services
        if (!checkLocaionServices()){
            // if no permissions - exit
            return;
        }
        // otherwise start GPS service
        Intent i = TrackerGPSService.newIntent(getActivity());
        getActivity().startService(i);
        Intent intent = RunningTrackPagerActivity.newIntent(getActivity());
        startActivity(intent);
    }

    /**
     * Check of GPS and network location permissions
     * @return  true - if permissions granted
     *          false - if not - and user should grant permissions in shown dialog
     */
    private boolean checkLocaionServices(){
        LocationManager locationManager = (LocationManager) getActivity()
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
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(getActivity());

        // Setting Dialog Title
        alertDialog.setTitle(R.string.alert_gps_title);

        // Setting Dialog Message
        alertDialog.setMessage(R.string.alert_gps_message);

        // On pressing Settings button
        alertDialog.setPositiveButton(getResources().getString(R.string.gps_settings), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        });

        // on pressing cancel button
        alertDialog.setNegativeButton(getResources().getString(R.string.gps_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults); switch (requestCode) {
            case MY_PERMISSION_REQUEST_READ_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "permission granted");

                } else {
                    Log.i(TAG, "permission denied");
                }
                return;
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}