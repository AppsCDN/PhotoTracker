package vitalypanov.phototracker;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;

/**
 * All need it permissions of Photo Tracker
 * Created by Vitaly on 05.03.2018.
 */

public class PhotoTrackerPermissions {
    Fragment mParentContextFragment;

    private static final int PERMISSIONS_REQUEST = 1;
    private static String[] PHOTO_TRACKER_PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,       // GPS
            Manifest.permission.ACCESS_COARSE_LOCATION,     // GPS approximate location
            Manifest.permission.WRITE_EXTERNAL_STORAGE      // Writing photo into public media directory
    };

    public PhotoTrackerPermissions(Fragment context) {
        mParentContextFragment = context;
    }

    /**
     * Check all need it permissions
     * @return
     */
    public  boolean hasPermissions(){
        for (String perm : PHOTO_TRACKER_PERMISSIONS){
            if (!hasPermisson(perm)){
                // found at least one not granted permission - exit
                return false;
            }
        }
        return true;
    }

    /**
     * Request all need it permissions at one time
     */
    public void requestPermissions(){
        if (!hasPermissions()){
            mParentContextFragment.requestPermissions(PHOTO_TRACKER_PERMISSIONS, PERMISSIONS_REQUEST);
        }
    }

    private boolean hasPermisson (String perm){
        return (PackageManager.PERMISSION_GRANTED == mParentContextFragment.getActivity().checkSelfPermission(perm));
    }


}
