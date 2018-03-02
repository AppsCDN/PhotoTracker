package vitalypanov.phototracker.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.WindowManager;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar when showing images
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected Fragment createFragment() {
        UUID uuid = (UUID) getIntent().getSerializableExtra(EXTRA_TRACK_UUID);
        return GoogleMapFragment.newInstance(uuid);
    }
}