package vitalypanov.phototracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SeekBarPreference;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import org.json.JSONException;
import org.json.JSONObject;

import vitalypanov.phototracker.export.utilities.OAuth2Activity;
import vitalypanov.phototracker.utilities.StringUtils;


/**
 * Created by Vitaly on 03.03.2018.
 */

public class SettingsFragment extends PreferenceFragmentCompat {
    private static final String TAG = "PhotoTracker";
    private static final int REQUEST_CODE_RUNKEEPER_AUTH = 1;

    private ImageButton mRunkeeperButton;

    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Preference pref = findPreference(Settings.KEY_MAP_RUNKEEPER_ACCESS_TOKEN);
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                if (!StringUtils.isNullOrBlank(Settings.get(getActivity()).getRunkeeperAccessToken())) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.runkeeper_already_configured_title);
                    builder.setMessage(getResources().getString(R.string.runkeeper_already_configured_message));
                    builder.setPositiveButton(R.string.runkeeper_already_configured_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // confirmed - do it
                                    runAuthRunkeeper();
                                }
                            });
                    builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // cancel confirmation dialog - do nothing
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return false;
                }

                runAuthRunkeeper();
                return false;
            }
        });

        final SeekBarPreference flickrPercent = (SeekBarPreference)findPreference(Settings.KEY_MAP_FLICKR_PHOTOS_PERCENT);
        flickrPercent.setEnabled(Settings.get(getActivity()).isFlickrPhotos());

        final SwitchPreference flickrSwitch = (SwitchPreference)findPreference(Settings.KEY_MAP_FLICKR_PHOTOS_SWITCH);
        flickrSwitch.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                flickrPercent.setEnabled(((boolean)newValue));
                return true;
            }
        });
    }

    /**
     * Running auth RunKeeper dialog
     */
    private void runAuthRunkeeper(){
        Intent intent = OAuth2Activity.newIntent(getActivity());
        startActivityForResult(intent, REQUEST_CODE_RUNKEEPER_AUTH);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // change title and show back button
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.settings_toolbar);
        AppCompatActivity parentActivity =(AppCompatActivity)getActivity();
        parentActivity.setSupportActionBar(toolbar);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parentActivity.getSupportActionBar().setTitle(R.string.menu_settings);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        switch (requestCode){
            case REQUEST_CODE_RUNKEEPER_AUTH:
                String authConfig = data.getStringExtra("auth_config");

                try {
                    JSONObject obj = new JSONObject(authConfig);
                    String accessToken  = obj.getString("access_token");
                    if (!StringUtils.isNullOrBlank(accessToken)) {
                        // save token to preference
                        Settings.get(getActivity()).setRunkeeperAccessToken(accessToken);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);;
        }
    }
}