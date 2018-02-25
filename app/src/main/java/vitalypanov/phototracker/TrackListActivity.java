package vitalypanov.phototracker;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

/**
 * Created by Vitaly on 25.02.2018.
 */

public class TrackListActivity  extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";

    public static Intent newIntent(Context context){
        return new Intent(context, TrackListActivity.class);
    }

    @Override
    protected Fragment createFragment() {
        return TrackListFragment.newInstance();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }
}