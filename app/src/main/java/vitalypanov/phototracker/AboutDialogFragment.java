package vitalypanov.phototracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by Vitaly on 03.03.2018.
 */

public class AboutDialogFragment extends Fragment {
    private static final String TAG = "PhotoTracker";

    public static AboutDialogFragment newInstance() {
        return new AboutDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_start_screen, container, false);
        return view;
    }

    /**
     * Show About dialog
     */
    private void updateAboutDialogUI(){

    }
}