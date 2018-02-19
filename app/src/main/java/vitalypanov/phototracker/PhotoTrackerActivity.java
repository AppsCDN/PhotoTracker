package vitalypanov.phototracker;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

public class PhotoTrackerActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";
    private static final int REQUEST_ERROR = 0;
    public static final int MY_PERMISSION_REQUEST_READ_FINE_LOCATION = 1;

    @Override
    protected Fragment createFragment() {
        return PhotoTrackerFragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
        int errorCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (errorCode != ConnectionResult.SUCCESS){
            Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(errorCode, this, REQUEST_ERROR,
                    new DialogInterface.OnCancelListener(){

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });
            errorDialog.show();
        }
        */
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