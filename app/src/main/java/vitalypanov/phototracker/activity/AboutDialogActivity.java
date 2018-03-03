package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import vitalypanov.phototracker.AboutDialogFragment;

/**
 * Created by Vitaly on 03.03.2018.
 */

public class AboutDialogActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";

    public static Intent newIntent(Context context){
        return new Intent(context, AboutDialogActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return AboutDialogFragment.newInstance();
    }
}