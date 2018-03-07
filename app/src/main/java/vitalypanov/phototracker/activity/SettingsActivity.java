package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import vitalypanov.phototracker.SettingsFragment;

/**
 * Created by Vitaly on 03.03.2018.
 */

public class SettingsActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";

    public static Intent newIntent(Context context){
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return SettingsFragment.newInstance();
    }
}