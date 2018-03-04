package vitalypanov.phototracker;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * Created by Vitaly on 03.03.2018.
 */

public class AboutDialogFragment extends Fragment {
    private static final String TAG = "PhotoTracker";
    private TextView mAppVersionView;

    public static AboutDialogFragment newInstance() {

        return new AboutDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // show app version
        mAppVersionView = (TextView)view.findViewById(R.id.app_version_view);
        mAppVersionView.setText(getResources().getText(R.string.app_version_title) + " " + getAppVersion());

        // change title and show back button
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.track_toolbar);
        AppCompatActivity parentActivity =(AppCompatActivity)getActivity();
        parentActivity.setSupportActionBar(toolbar);
        parentActivity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        parentActivity.getSupportActionBar().setTitle(R.string.app_about_title);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().finish();
            }
        });

        return view;
    }

    /**
     * Getting app version text
     * @return
     */
    private String getAppVersion(){
        String versionText = "";
        try {
            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            versionText =pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionText;
    }


    /**
     * Show About dialog
     */
    private void updateAboutDialogUI(){

    }
}