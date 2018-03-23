package vitalypanov.phototracker;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.UUID;

import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class GoogleMapFragment extends TrackerSupportMapFragment {
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";
    private Track mTrack= null;

    public static GoogleMapFragment newInstance(UUID uuid) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TRACK_UUID, uuid);
        GoogleMapFragment googleMapFragment = new GoogleMapFragment();
        googleMapFragment.setArguments(args);
        return googleMapFragment;
    }

    @Override
    public Track getTrack() {
        if (Utils.isNull(mTrack)) {
            UUID uuid = (UUID) getArguments().getSerializable(EXTRA_TRACK_UUID);
            mTrack = TrackDbHelper.get(getActivity()).getTrack(uuid);
        };
        return mTrack;
    }

    @Override
    public int getLayoutResourceId() {
        return R.layout.fragment_map_track;
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        return super.onCreateView(layoutInflater, container, bundle);
    }
}
