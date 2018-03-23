package vitalypanov.phototracker;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.SupportMapFragment;

import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.AssyncBitmapLoaderTask;
import vitalypanov.phototracker.utilities.TouchImageView;

/**
 * Created by Vitaly on 01.03.2018.
 */

public class TrackImageFragment extends Fragment implements ViewPageUpdater {
    private static final String TAG = "PhotoTracker";
    private static final String ARG_BITMAP_FILENAME = "bitmap_filename";
    private SupportMapFragment mapFragment = null;
    private TouchImageView mTrackPhotoImageView;
    private RelativeLayout mLoadingPanel;
    private String mBitmapFileName;

    public static TrackImageFragment newInstance(String bitmapFileName) {
        Bundle args = new Bundle();
        // putting filename into bundle
        args.putSerializable(ARG_BITMAP_FILENAME, bitmapFileName);

        TrackImageFragment fragment = new TrackImageFragment();
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // getting filename from bundle
        mBitmapFileName = (String)getArguments().getSerializable(ARG_BITMAP_FILENAME);
    }

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View v = layoutInflater.inflate(R.layout.fragment_image, container, false);
        mTrackPhotoImageView = (TouchImageView) v.findViewById(R.id.track_photo_image);
        mLoadingPanel = (RelativeLayout) v.findViewById(R.id.track_loading_photo);
        updatePhotoUI();
        return v;
    }

    private void updatePhotoUI(){
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        AssyncBitmapLoaderTask assyncImageViewUpdater = new AssyncBitmapLoaderTask(mBitmapFileName, mTrackPhotoImageView, size.x, getContext(), mLoadingPanel);
        assyncImageViewUpdater.execute();
    }

    @Override
    public void onPageSelected() {
        //
    }
}
