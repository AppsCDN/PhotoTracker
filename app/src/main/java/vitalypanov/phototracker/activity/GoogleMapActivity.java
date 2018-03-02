package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;

import java.util.UUID;

import vitalypanov.phototracker.GoogleMapFragment;

public class GoogleMapActivity extends SingleFragmentActivity {
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";

    public static Intent newIntent(Context context, UUID uuid){
        Intent intent = new Intent(context, GoogleMapActivity.class);
        intent.putExtra(EXTRA_TRACK_UUID, uuid);
        return intent;
    }

    @Override
    protected Fragment createFragment() {
        UUID uuid = (UUID) getIntent().getSerializableExtra(EXTRA_TRACK_UUID);
        return GoogleMapFragment.newInstance(uuid);
    }
}