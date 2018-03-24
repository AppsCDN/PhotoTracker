package vitalypanov.phototracker;

import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.others.BindTrackerGPSService;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackGoogleMapFragment extends TrackerSupportMapFragment implements ViewPageUpdater, BindTrackerGPSService {
    private TrackerGPSService mService;

    public static RunningTrackGoogleMapFragment newInstance() {
        return new RunningTrackGoogleMapFragment();
    }

    @Override
    public Track getTrack() {
        return !Utils.isNull(mService)? mService.getCurrentTrack() : null;
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_map_running_track;
    }

    @Override
    public void onPageSelected() {
        // update photo bitmaps fo and redraw map when selecting tab
        super.startUpdateUI();
    }

    @Override
    public void onBindService(TrackerGPSService service) {
        mService = service;
    }

}