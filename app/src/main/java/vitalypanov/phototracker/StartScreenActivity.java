package vitalypanov.phototracker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

public class StartScreenActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";
    private static final int REQUEST_ERROR = 0;
    public static final int MY_PERMISSION_REQUEST_READ_FINE_LOCATION = 1;

    public static Intent newIntent(Context context){
        return new Intent(context, StartScreenActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return StartScreenFragment.newInstance();
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