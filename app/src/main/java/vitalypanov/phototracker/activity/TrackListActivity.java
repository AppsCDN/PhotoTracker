package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.MenuItem;

import vitalypanov.phototracker.TrackListFragment;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // show back button on toolbar
        //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // on pressed back button on toolbar
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}