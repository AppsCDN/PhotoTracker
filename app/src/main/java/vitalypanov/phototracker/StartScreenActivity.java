package vitalypanov.phototracker;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;

public class StartScreenActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";

    public static Intent newIntent(Context context){
        return new Intent(context, StartScreenActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return StartScreenFragment.newInstance();
    }
}