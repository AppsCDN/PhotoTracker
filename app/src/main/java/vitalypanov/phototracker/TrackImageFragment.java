package vitalypanov.phototracker;

import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.SupportMapFragment;

import vitalypanov.phototracker.model.TrackBitmap;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.BitmapScalerUtils;

/**
 * Created by Vitaly on 01.03.2018.
 */

public class TrackImageFragment extends Fragment implements ViewPageUpdater {
    private static final String TAG = "PhotoTracker";
    private static final String ARG_BITMAP_FILENAME = "bitmap_filename";
    private SupportMapFragment mapFragment = null;
    TrackBitmap mTrackBitmap;
    private ImageView mTrackPhotoImageView;
    private RelativeLayout mLoadingPanel;

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
        String bitmapFileName = (String)getArguments().getSerializable(ARG_BITMAP_FILENAME);
        TrackPhoto trackPhoto = new TrackPhoto();
        trackPhoto.setPhotoFileName(bitmapFileName);
        mTrackBitmap = new TrackBitmap(trackPhoto);
}

    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle bundle) {
        View v = layoutInflater.inflate(R.layout.fragment_image, container, false);
        mTrackPhotoImageView = (ImageView) v.findViewById(R.id.track_photo_image);
        mLoadingPanel = (RelativeLayout) v.findViewById(R.id.track_loading_photo);
        updatePhotoUI();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void updatePhotoUI(){
        Point size = new Point();
        getActivity().getWindowManager().getDefaultDisplay().getSize(size);
        BitmapScalerUtils.updatePhotoAssync(mTrackBitmap.getTrackPhoto(), mTrackPhotoImageView, size.x, getContext(), mLoadingPanel);
    }

    @Override
    public void onPageSelected() {
        //
    }
}
