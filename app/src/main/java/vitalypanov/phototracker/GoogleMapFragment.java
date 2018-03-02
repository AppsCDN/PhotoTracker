package vitalypanov.phototracker;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.UUID;

import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.utilities.GoogleMapUtils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class GoogleMapFragment extends Fragment {
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";
    private SupportMapFragment mapFragment = null;
    private GoogleMap mMap;
    private Track mTrack;

    public static GoogleMapFragment newInstance(UUID uuid) {
        Bundle args = new Bundle();
        args.putSerializable(EXTRA_TRACK_UUID, uuid);
        GoogleMapFragment googleMapFragment = new GoogleMapFragment();
        googleMapFragment.setArguments(args);
        return googleMapFragment;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        UUID uuid = (UUID)getArguments().getSerializable(EXTRA_TRACK_UUID);
        mTrack = TrackDbHelper.get(getActivity()).getTrack(uuid);
        mTrack.loadCashedBitmaps(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View v = layoutInflater.inflate(R.layout.fragment_map_track, container, false);
        mapFragment = (SupportMapFragment)getChildFragmentManager().findFragmentById(R.id.google_map_fragment);
        //mapFragment.getView().setVisibility(View.GONE);
        updatMapAsync();
        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        //getActivity().invalidateOptionsMenu();
    }

    /**
     * Updating assync google map and save map object into local variable
     * for future drawing
     */
    private void updatMapAsync(){
        if (mapFragment==null){
            return;
        }
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mMap = googleMap;
                GoogleMapUtils.updateGoogleMapUI(mMap, mTrack, getContext());
                //mapFragment.getView().setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    /*
    @Override
    public void onPageSelected() {
        // load photo bitmaps for showing on google map
        if (mTrack != null) {
            mTrack.loadCashedBitmaps(getContext());
        }
        // redraw map when selecting tab
        updatMapAsync();
    }
    */
}
