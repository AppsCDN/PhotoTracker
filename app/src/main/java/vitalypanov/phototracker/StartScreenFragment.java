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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import vitalypanov.phototracker.activity.AboutDialogActivity;
import vitalypanov.phototracker.activity.RunningTrackPagerActivity;
import vitalypanov.phototracker.activity.TrackListActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.utilities.ServiceUtils;

/**
 * Created by Vitaly on 29.08.2017.
 */

public class StartScreenFragment extends Fragment {
    private static final String TAG = "PhotoTracker";
    // menu items id's:
    private static final int MENU_ITEM_START_TRACK = 1;
    private static final int MENU_ITEM_TRACK_LIST = 2;
    private static final int MENU_ITEM_SETTINGS = 3;
    private static final int MENU_ITEM_SEND_FEEDBACK = 4;
    private static final int MENU_ITEM_RATE_PLAY_MARKET = 5;
    private static final int MENU_ITEM_ABOUT = 6;

    // gps location permission  id's:
    private static final int LOCATION_REQUEST = 1;
    private static String[] LOCATION_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    // external storage permission id's:
    private static final int EXTERNAL_STORAGE_REQUEST = 2;
    private static String[] EXTERNAL_STORAGE_PERMISSIONS = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // Main menu items:
    PrimaryDrawerItem  mMenuStartTrack;
    PrimaryDrawerItem  mMenuTrackList;
    PrimaryDrawerItem  mMenuSettings;
    PrimaryDrawerItem  mMenuSendFeedback;
    PrimaryDrawerItem  mMenuRatePlayMarket;
    PrimaryDrawerItem  mMenuAbout;

    private Button mTrackStart;
    //private Button mTrackList;

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

        initNavigationDrawer(view);

        mTrackStart =  (Button) view.findViewById(R.id.track_start);
        mTrackStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTrack();
            }
        });

        return view;
    }

    /**
     * Init Navigation drawer main menu.
     * @param view - main view of the current fragment
     */
    private void initNavigationDrawer(View view ){
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.track_toolbar);
        AppCompatActivity parentActivity =(AppCompatActivity)getActivity();
        parentActivity.setSupportActionBar(toolbar);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mMenuStartTrack = new PrimaryDrawerItem().withName(R.string.menu_start).withIcon(R.mipmap.ic_steps).withSelectable(false).withIdentifier(MENU_ITEM_START_TRACK);
        mMenuTrackList = new PrimaryDrawerItem().withName(R.string.menu_track_list).withIcon(R.mipmap.ic_list).withSelectable(false).withIdentifier(MENU_ITEM_TRACK_LIST);
        mMenuSettings = new PrimaryDrawerItem().withName(R.string.menu_settings).withIcon(R.mipmap.ic_settings).withSelectable(false).withIdentifier(MENU_ITEM_SETTINGS).withEnabled(false);
        mMenuSendFeedback = new PrimaryDrawerItem().withName(R.string.menu_send_feedback).withIcon(R.mipmap.ic_feedback).withSelectable(false).withIdentifier(MENU_ITEM_SEND_FEEDBACK).withEnabled(false);
        mMenuRatePlayMarket = new PrimaryDrawerItem().withName(R.string.menu_rate_play_market).withIcon(R.mipmap.ic_playmarket).withSelectable(false).withIdentifier(MENU_ITEM_RATE_PLAY_MARKET).withEnabled(false);
        mMenuAbout = new PrimaryDrawerItem().withName(R.string.menu_about).withIcon(R.mipmap.ic_about).withSelectable(false).withIdentifier(MENU_ITEM_ABOUT);

        new DrawerBuilder()
            .withActivity(parentActivity)
            .withToolbar(toolbar)
            .withActionBarDrawerToggle(true)
            .withHeader(R.layout.drawer_header)
            .addDrawerItems(
                    mMenuStartTrack,
                    mMenuTrackList,
                    mMenuSettings,
                    new DividerDrawerItem(),
                    mMenuSendFeedback,
                    mMenuRatePlayMarket,
                    new DividerDrawerItem(),
                    mMenuAbout
                    ).withOnDrawerItemClickListener(
                            new Drawer.OnDrawerItemClickListener() {
                                @Override
                                public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                                    switch ((int)drawerItem.getIdentifier()) {
                                        case MENU_ITEM_START_TRACK:
                                            startTrack();
                                            break;
                                        case MENU_ITEM_TRACK_LIST:
                                            showTrackList();
                                            break;
                                        case MENU_ITEM_SETTINGS:
                                            break;
                                        case MENU_ITEM_SEND_FEEDBACK:
                                            break;
                                        case MENU_ITEM_RATE_PLAY_MARKET:
                                            break;
                                        case MENU_ITEM_ABOUT:
                                            showAboutDialog();
                                            break;
                                    }
                                    return false;
                                }
                            }
                    ).withSelectedItem(-1) // to hide selection of any menu item
            .build();
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
        updateTrackListCounterUI();
    }

    /**
     * Update tracks count value on button and other UI
     */
    private void updateTrackListCounterUI(){
        long tracksCount =  TrackDbHelper.get(getContext()).getTracksCount();
        //mTrackList.setText(getResources().getString(R.string.action_track_list) + (tracksCount > 0 ? " (" + String.valueOf(tracksCount) + ")" :""));
        mMenuTrackList.withBadge(tracksCount > 0 ? String.valueOf(tracksCount) :"");
        mMenuTrackList.withEnabled(tracksCount > 0);
    }

    /**
     * Show About dialog
     */
    private void showAboutDialog(){
        Intent intent = AboutDialogActivity.newIntent(getActivity());
        startActivity(intent);
    }
    /**
     * Show track list
     */
    private void showTrackList(){
        Intent intent = TrackListActivity.newIntent(getActivity());
        startActivity(intent);
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
}