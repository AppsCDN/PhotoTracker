package vitalypanov.phototracker.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.FileProvider;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.TrackImageFragment;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.flickr.FlickrHolder;
import vitalypanov.phototracker.flickr.FlickrPhoto;
import vitalypanov.phototracker.model.BasePhoto;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.FileUtils;

/**
 * Images of the track pages
 * Created by Vitaly on 01.03.2018.
 */

public class TrackImagesPagerActivity extends AppCompatActivity {
    // which photos we want to show
    enum Modes {
        MODE_PHOTO_TRACKER,
        MODE_FLICKR
    }
    private static final String TAG = "PhotoTracker";
    private static final String EXTRA_TRACK_UUID = "phototracker.track_uuid";
    private static final String EXTRA_PHOTO_LIST = "phototracker.photo_list";
    private static final String EXTRA_PHOTO_TO_SELECT = "phototracker.photo_to_select";
    private static final String EXTRA_MODE = "phototracker.mode";
    private ViewPager mViewPager;
    private FragmentStatePagerAdapter mPagerAdapter;
    private TextView mCounterTextView;
    private ImageButton mDeleteButton;
    private ImageButton mShareButton;
    private List<BasePhoto> mTrackPhotos;
    private String mPhotoToSelectName;
    private UUID mTrackUUID;
    private Modes mMode;

    public static Intent newIntent(Context packageContext, UUID trackUUID, ArrayList<TrackPhoto> trackPhotos, String photoToSelectName){
        Intent intent = new Intent(packageContext, TrackImagesPagerActivity.class);
        intent.putExtra(EXTRA_TRACK_UUID, trackUUID);
        intent.putExtra(EXTRA_PHOTO_LIST, trackPhotos);
        intent.putExtra(EXTRA_PHOTO_TO_SELECT, photoToSelectName);
        intent.putExtra(EXTRA_MODE, Modes.MODE_PHOTO_TRACKER);
        return intent;
    }

    public static Intent newIntentFlickr(Context packageContext, UUID trackUUID, ArrayList<FlickrPhoto> trackPhotos, String photoToSelectName){
        Intent intent = new Intent(packageContext, TrackImagesPagerActivity.class);
        intent.putExtra(EXTRA_TRACK_UUID, trackUUID);
        // app crached when use parameter below for not small list :(((
        //intent.putExtra(EXTRA_PHOTO_LIST, trackPhotos);
        intent.putExtra(EXTRA_PHOTO_TO_SELECT, photoToSelectName);
        intent.putExtra(EXTRA_MODE, Modes.MODE_FLICKR);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // hide status bar when showing images
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_pager_images);

        mMode = (Modes) getIntent().getSerializableExtra(EXTRA_MODE);
        mTrackUUID= (UUID)getIntent().getSerializableExtra(EXTRA_TRACK_UUID);
        if (mMode.equals(Modes.MODE_PHOTO_TRACKER)) {
            mTrackPhotos = (ArrayList<BasePhoto>) getIntent().getSerializableExtra(EXTRA_PHOTO_LIST);
        } else {
            // getSerializableExtra doesnt work correctly for lists at least of 1000 items - it's terrible :(((
            mTrackPhotos = new ArrayList<>();
            mTrackPhotos.addAll(FlickrHolder.get().getFlickrPhotos());
        }
        mPhotoToSelectName= getIntent().getStringExtra(EXTRA_PHOTO_TO_SELECT);

        mViewPager = (ViewPager) findViewById(R.id.activity_pager_images_view_pager);
        mCounterTextView = (TextView) findViewById(R.id.activity_pager_counter_textview);
        mCounterTextView.bringToFront();

        final Activity activity = this;
        mDeleteButton = (ImageButton) findViewById(R.id.activity_pager_delete_button);
        mDeleteButton.setVisibility(mMode == Modes.MODE_PHOTO_TRACKER? View.VISIBLE : View.GONE);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setCancelable(true);
                builder.setTitle(R.string.confirm_delete_image_title);
                builder.setMessage(getResources().getString(R.string.confirm_delete_image_message));
                builder.setPositiveButton(R.string.confirm_delete_image_button_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int currentIndex = mViewPager.getCurrentItem();
                                // getting file name before delete...
                                TrackPhoto trackPhoto = (TrackPhoto)mTrackPhotos.get(currentIndex);
                                String trackPhotoFileName = trackPhoto.getPhotoFileName();
                                // delete current photo item from list...
                                mTrackPhotos.remove(currentIndex);
                                // remove photo in db...
                                Track track = TrackDbHelper.get(activity).getTrack(mTrackUUID);
                                track.getPhotoFiles().remove(currentIndex);
                                TrackDbHelper.get(activity).updateTrack(track);
                                // remove photo from internal storage...
                                File currentPhotoFile = FileUtils.getPhotoFile(activity, trackPhotoFileName);
                                currentPhotoFile.delete();
                                // need update viewpager within new data...
                                mPagerAdapter.notifyDataSetChanged();
                                updateCounterTextView();
                                // say parent activities that they need to update UI
                                setActivityResultOK();
                                // if was deleted the last one - close activity
                                if (mPagerAdapter.getCount() == 0){
                                    activity.finish();
                                }
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
                mViewPager.getCurrentItem();
            }
        });

        mShareButton = (ImageButton) findViewById(R.id.activity_pager_share_button);
        mShareButton .setVisibility(mMode == Modes.MODE_PHOTO_TRACKER? View.VISIBLE : View.GONE);
        mShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                shareCurrentPhoto();
            }
        });

        FragmentManager fragmentManager = getSupportFragmentManager();
        mPagerAdapter = new FragmentStatePagerAdapter(fragmentManager) {
            @Override
            public Fragment getItem(int position) {
                BasePhoto trackPhoto = mTrackPhotos.get(position);
                return TrackImageFragment.newInstance(trackPhoto.getName());
            }

            @Override
            public int getCount() {
                return mTrackPhotos.size();
            }

            @Override
            public void startUpdate(ViewGroup container) {
                super.startUpdate(container);
            }

            @Override
            public int getItemPosition(@NonNull Object object) {
                return PagerAdapter.POSITION_NONE;
            }
        };
        mViewPager.setAdapter(mPagerAdapter);

        // when page is changed we can update fragment UI
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Log.i(TAG, "onPageScrolled position= " + position);
            }

            @Override
            public void onPageSelected(int position) {
                ViewPageUpdater fragment = (ViewPageUpdater) mPagerAdapter.instantiateItem(mViewPager, position);
                if (fragment != null) {
                    fragment.onPageSelected();
                }
                updateCounterTextView();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                int i = 0;
            }
        });

        int position = 0;
        if (mPhotoToSelectName!= null && !mPhotoToSelectName.isEmpty()){
            // search element with provided photo name
            for(int i = 0; i< mTrackPhotos.size(); i++){
                if (mTrackPhotos.get(i).getName().equals(mPhotoToSelectName)){
                    position = i; // image was founded
                    break;
                }
            }
        }
        mViewPager.setCurrentItem(position);
        updateCounterTextView();
    }

    /**
     * Sharing photos functionality
     */
    private void shareCurrentPhoto(){
        // One current photo:
            int currentIndex = mViewPager.getCurrentItem();
            BasePhoto trackPhoto = mTrackPhotos.get(currentIndex);
            final String trackPhotoFileName = trackPhoto.getName();
            Uri uri = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".vitalypanov.phototracker.provider", FileUtils.getPhotoFile(this, trackPhotoFileName));
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.setType("image/*");
            startActivity(shareIntent);

        // Multiple photos - all photos of the track (not working for instagram :((( so not need it):
            /*
            ArrayList<Uri> files = new ArrayList<Uri>();
            for(TrackPhoto trackPhoto : mTrackPhotos ) {
                Uri uri = FileProvider.getUriForFile(getBaseContext(), getBaseContext().getApplicationContext().getPackageName() + ".vitalypanov.phototracker.provider", FileUtils.getPhotoFile(activity, trackPhoto.getPhotoFileName()));
                files.add(uri);
            }
            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            //shareIntent.setPackage("com.instagram.android");
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //shareIntent.putExtra(Intent.EXTRA_STREAM, files);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            shareIntent.setType("image/*");
            startActivity(shareIntent);
            */
    }

    private void updateCounterTextView(){
        mCounterTextView.setVisibility(mPagerAdapter.getCount() > 0? View.VISIBLE : View.GONE);
        mCounterTextView.setText(String.valueOf(mViewPager.getCurrentItem()+1) + "/" + mPagerAdapter.getCount());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        this.finish();
    }

    /**
     * Save result OK for this activity.
     * It means that we need update some data in parent activity.
     */
    private void setActivityResultOK(){
        Intent data = new Intent();
        data.putExtra(EXTRA_PHOTO_LIST, (ArrayList<BasePhoto>)mTrackPhotos);
        data.putExtra(EXTRA_TRACK_UUID, mTrackUUID);
        setResult(RESULT_OK, data);
    }

    public static ArrayList<TrackPhoto> getTrackPhotos(Intent data){
        return (ArrayList<TrackPhoto>)data.getSerializableExtra(EXTRA_PHOTO_LIST);
    }

    public static UUID getTrackID(Intent data){
        return (UUID)data.getSerializableExtra(EXTRA_TRACK_UUID);
    }
}
