package vitalypanov.phototracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import vitalypanov.phototracker.activity.SettingsActivity;
import vitalypanov.phototracker.activity.StartScreenActivity;
import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.others.BindTrackerGPSService;
import vitalypanov.phototracker.others.GenericFileProvider;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.BitmapHandler;
import vitalypanov.phototracker.utilities.FileUtils;
import vitalypanov.phototracker.utilities.GeoTagUtils;
import vitalypanov.phototracker.utilities.MessageUtils;
import vitalypanov.phototracker.utilities.ServiceUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackShortInfoFragment  extends Fragment implements ViewPageUpdater, BindTrackerGPSService {
    private static final String TAG = "PhotoTracker";
    private static final String SAVED_PARAM_CURRENT_PHOTO_FILE = "PARAM_CURRENT_PHOTO_FILE";
    private static final int UPDATE_INTERVAL = 1000*1;// each second update interface
    private static final int REQUEST_PHOTO = 1;
    private static final int REQUEST_CODE_IMAGES_PAGER = 0;

    private TextView mStartTimeTextView;
    private TextView mDurationTimeTextView;
    private TextView mDistanceTextView;
    private EditText mCommentEditText;
    private ImageButton mPhotoButton;
    private ImageButton mPauseButton;
    private ImageButton mSetingsButton;
    private RelativeLayout mTrackPhotoLayout;
    private TextView mPhotoCounterTextView;
    private ImageView mTrackPhotoImage;

    private TrackerGPSService mService;

    private String mCurrentPhotoFileName;

    private Timer timer;
    private TimerTask timerTask;

    private void initCamera(){

    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 1000, UPDATE_INTERVAL); //
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                updateTimerUI();
            }
        };
    }

    public static RunningTrackShortInfoFragment newInstance() {
        // creating fragment...
        return new RunningTrackShortInfoFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentPhotoFileName = savedInstanceState.getString(SAVED_PARAM_CURRENT_PHOTO_FILE);
        }
        setRetainInstance(true);
        startTimer();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shortinfo_running_track, container, false);
        mStartTimeTextView = (TextView)v.findViewById(R.id.start_time_text_view);
        mDurationTimeTextView= (TextView)v.findViewById(R.id.duration_time_text_view);
        mDistanceTextView= (TextView)v.findViewById(R.id.distance_text_view);

        mCommentEditText= (EditText) v.findViewById(R.id.comment_text);
        mCommentEditText.setInputType(InputType.TYPE_NULL);
        mCommentEditText.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                // on long touch - show input keyboard
                mCommentEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                mCommentEditText.requestFocus();
                return false;
            }
        });
        mCommentEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        // on OK button - hide focus
                        mCommentEditText.setInputType(InputType.TYPE_NULL);
                        // after user commented - we should save it into database
                        if (mService!= null){
                            mService.getCurrentTrack().setComment(mCommentEditText.getText().toString());
                            mService.forceWriteToDb();
                        }
                    }
                return false;
            }
        });
        mCommentEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (mService!=null && mService.getCurrentTrack() !=null){
                    mService.getCurrentTrack().setComment(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }


        });

        mTrackPhotoLayout= (RelativeLayout) v.findViewById(R.id.track_photo_layout);

        mPhotoCounterTextView = (TextView) v.findViewById(R.id.photo_counter_textview);
        mPhotoCounterTextView.setVisibility(View.GONE);
        mPhotoCounterTextView.bringToFront();

        mTrackPhotoImage = (ImageView) v.findViewById(R.id.track_photo_image);
        mTrackPhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mService.getCurrentTrack().getPhotoFiles().size()>0) {
                    Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), mService.getCurrentTrack().getUUID(), (ArrayList<TrackPhoto>) mService.getCurrentTrack().getPhotoFiles(), (String)mTrackPhotoImage.getTag());
                    startActivityForResult(intent, REQUEST_CODE_IMAGES_PAGER);
                }
            }
        });

        mPhotoButton = (ImageButton) v.findViewById(R.id.track_photo);
        final Intent capturePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        PackageManager packageManager = getActivity().getPackageManager();
        //mCurrentPhotoFile = mService.getCurrentTrack().getNewPhotoFile(getContext());
        //mCurrentPhotoFile != null &&
        boolean canTakePhoto = capturePhoto.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // take photo
                takePhoto();
            }
        });

        mPauseButton = (ImageButton) v.findViewById(R.id.track_pause);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setCancelable(true);
                builder.setTitle(R.string.pause_service_confirm_title);
                String sMessage = getResources().getString(R.string.pause_service_confirm_message);
                builder.setMessage(sMessage);
                builder.setPositiveButton(R.string.pause_service_confirm_button_ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // confirm pausing - do it
                                stopTrack();
                                getActivity().finish();
                                /* if start screen not shown yet - show it
                                    to avoid multiple running startscreen activities we have android:launchMode="singleInstance" flag in manifest
                                 */
                                Intent intent = StartScreenActivity.newIntent(getActivity());
                                startActivity(intent);
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
            }
        });

        mSetingsButton = (ImageButton) v.findViewById(R.id.track_settings);
        mSetingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showSettingsDialog();
            }
        });

        updateUI();
        updatePhotoUI();

        return v;
    }

    /**
     * Taking photo
     */
    private void takePhoto(){
        // Service not bind yet - exit
        if (mService==null || mService.getCurrentTrack() == null){
            return;
        }

        // gps location not defined yet (at least one) - notify user and exit
        if (mService.getCurrentTrack().getLastTrackItem() == null){
            MessageUtils.ShowMessageBox(R.string.no_location_title, R.string.no_location_message, getContext());
            return;
        }

        // Intent variant:
        final Intent capturePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCurrentPhotoFileName = mService.getCurrentTrack().getNewPhotoFileName();
        File currentPhotoFile = FileUtils.getPhotoFile(getContext(),mCurrentPhotoFileName);
        Uri uri = GenericFileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".vitalypanov.phototracker.provider", currentPhotoFile);
        capturePhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(capturePhoto, REQUEST_PHOTO);

        /*
        // Camera API variant:
        TrackHolder.get().setTrack(mService.getCurrentTrack());
        Intent intent = CameraActivity.newIntent(getActivity(), TrackHolder.get().getTrack().getUUID());
        startActivityForResult(intent, REQUEST_PHOTO);
        */
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        switch (requestCode){
            case REQUEST_PHOTO:

                // Intent variant:
                AssyncUpdatePhotoAfterTakePhotoTask assyncUpdatePhotoAfterTakePhotoTask = new AssyncUpdatePhotoAfterTakePhotoTask();
                assyncUpdatePhotoAfterTakePhotoTask.execute();

                /*
                // Camera API variant:
                if (Utils.isNull(data)) {
                    return;
                }
                mService.getCurrentTrack().getPhotoFiles().addAll(CameraActivity.getTrackPhotos(data));
                AssyncUpdatePhotoAfterTakePhotoTask assyncUpdatePhotoAfterTakePhotoTask = new AssyncUpdatePhotoAfterTakePhotoTask();
                assyncUpdatePhotoAfterTakePhotoTask.execute();
                */
                break;
            case REQUEST_CODE_IMAGES_PAGER:
                if (Utils.isNull(data)) {
                    return;
                }
                mService.getCurrentTrack().setPhotoFiles(TrackImagesPagerActivity.getTrackPhotos(data));
                updatePhotoUI();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);;
        }

    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(SAVED_PARAM_CURRENT_PHOTO_FILE, mCurrentPhotoFileName);
        super.onSaveInstanceState(outState);
    }

    /**
     * Assync save and update image after take photo
     *
     */
    private class AssyncUpdatePhotoAfterTakePhotoTask extends AsyncTask<Void, Void, Void> {

        public AssyncUpdatePhotoAfterTakePhotoTask() {
        }

        @Override
        protected void onPreExecute() {
            clearPhotoUI();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // wait for service and Activity correct objects
            waitForServiceIsUp();
            if (mService!= null && mService.getCurrentTrack() != null) {
                // Intent variant:
                // check for null service is needed because after take photo the fragment can be not existed
                File currentPhotoFile = FileUtils.getPhotoFile(getActivity(), mCurrentPhotoFileName);
                if (currentPhotoFile != null && currentPhotoFile.exists()) {
                    // forcelly geo tagging recieved photo
                    TrackLocation trackLocation = mService.getCurrentTrack().getLastTrackItem();
                    currentPhotoFile.setWritable(true);
                    GeoTagUtils.setGeoTag(currentPhotoFile, trackLocation);
                    // save link of created photo to the track
                    mService.getCurrentTrack().addPhotoItem(mCurrentPhotoFileName, trackLocation);
                    TrackDbHelper.get(getActivity()).updateTrack(mService.getCurrentTrack());
                } else {
                    mCurrentPhotoFileName = null;
                }

                /*
                // Camera API variant:
                TrackDbHelper.get(getActivity()).updateTrack(mService.getCurrentTrack());
                */
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            updatePhotoUI();
        }

        /**
         * Wait for service and Activity correct objects
         */
        private void waitForServiceIsUp(){

            while (mService == null
                    || mService.getCurrentTrack() == null
                    || getActivity() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }


    }

    /**
     * Assync update image after take photo
     *
     */
    private class AssyncUpdatePhotoUI extends AsyncTask<Void, Void, Void> {
        Bitmap mBitmap;
        String mPhotoFileName;

        public AssyncUpdatePhotoUI() {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // wait for service and Activity correct objects
            //waitForServiceIsUp();
            if (Utils.isNull(mService) || Utils.isNull(mService.getCurrentTrack()) || Utils.isNull(getActivity())){
                return null;
            }
            Track currentTrack = mService.getCurrentTrack();
            mBitmap = null;
            mPhotoFileName = null;
            DisplayMetrics metrics = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            if (!Utils.isNull(currentTrack.getLastPhotoItem())){
                mBitmap = BitmapHandler.get(getContext()).getBitmapScaleToFitWidth(currentTrack.getLastPhotoItem().getPhotoFileName(), metrics.widthPixels);
                mPhotoFileName = currentTrack.getLastPhotoItem().getPhotoFileName();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            // update bitmap
            if (Utils.isNull(mTrackPhotoImage)){
                return;
            }
            mTrackPhotoImage.post(new Runnable() {
                @Override
                public void run() {
                    // Need post method here due to mTrackPhotoImage.getWidth() using. Before post - it always is 0.
                    mTrackPhotoImage.setImageBitmap(mBitmap);
                    mTrackPhotoImage.setTag(mPhotoFileName);
                }
            });
            updatePhotoCounter();
        }
    }

    /**
     * Update photo counter
     */
    private void updatePhotoCounter(){
        if (Utils.isNull(getActivity())){
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (Utils.isNull(mService) || Utils.isNull(mService.getCurrentTrack())){
                    return;
                }
                mPhotoCounterTextView.setVisibility(mService.getCurrentTrack().getPhotoFiles().size() > 0 ? View.VISIBLE : View.GONE);
                mPhotoCounterTextView.setText(" " + String.valueOf(mService.getCurrentTrack().getPhotoFiles().size()) + " ");
                mPhotoCounterTextView.bringToFront();
            }
        });
    }

    /**
     * Update photo in UI
     */
    private void updatePhotoUI(){
        AssyncUpdatePhotoUI assyncUpdatePhotoUI = new AssyncUpdatePhotoUI();
        assyncUpdatePhotoUI.execute();
    }

    /**
     * Clear photo in UI
     */
    private void clearPhotoUI(){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTrackPhotoImage.setImageBitmap(null);
                mTrackPhotoImage.setTag(null);
            }
        });
    }

    /**
     * Stop recording track
     */
    private void stopTrack(){
        Intent i = TrackerGPSService.newIntent(getActivity());
        getActivity().stopService(i);
    }

    @Override
    public void onPageSelected() {
        updateUI();
        updatePhotoUI();
    }

    @Override
    public void onBindService(TrackerGPSService service) {
        mService = service;
        updatePhotoUI();
    }

    /**
     *  Update timer UI controls by Service data
     */
    private void updateTimerUI(){
        if (mService == null || mService.getCurrentTrack() == null || getActivity() == null){
            return;
        }
        // update UI is possible only in main thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Track currentTrack = mService.getCurrentTrack();
                mStartTimeTextView.setText(currentTrack.getStartTimeShortFormatted());
                mDurationTimeTextView.setText(currentTrack.getDurationTimeStillRunningFormatted());
                currentTrack.recalcDistance();
                mDistanceTextView.setText(String.valueOf(currentTrack.getDistanceFormatted()));
            }
        });

    }

    /**
     *  Update all controls in UI by Service data
     * (include timer UI controls)
     */
    private void updateUI(){
        if (mService == null || mService.getCurrentTrack() == null || getActivity() == null){
            return;
        }
        // update UI is possible only in main thread
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                updateTimerUI();
                mCommentEditText.setText(mService.getCurrentTrack().getComment());
            }
        });
    }

    /**
     * Show Settings dialog
     */
    private void showSettingsDialog(){
        startActivity(SettingsActivity.newIntent(getActivity()));
    }

    @Override
    public void onResume() {
        super.onResume();
        // if service is not running - back press
        if (!ServiceUtils.isServiceRunning(
                getActivity().getApplicationContext(),
                TrackerGPSService.class)){
            getActivity().finish();
            startActivity(StartScreenActivity.newIntent(getActivity()));
            return;
        };
    }
}
