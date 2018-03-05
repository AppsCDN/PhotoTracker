package vitalypanov.phototracker;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
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

import vitalypanov.phototracker.activity.StartScreenActivity;
import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.others.GenericFileProvider;
import vitalypanov.phototracker.others.ViewPageUpdater;
import vitalypanov.phototracker.utilities.BitmapHandler;
import vitalypanov.phototracker.utilities.FileUtils;
import vitalypanov.phototracker.utilities.MessageUtils;

/**
 * Created by Vitaly on 23.02.2018.
 */

public class RunningTrackShortInfoFragment  extends Fragment implements ViewPageUpdater {
    private static final String TAG = "PhotoTracker";
    private static final int UPDATE_INTERVAL = 1000*1;// each second update interface

    private static final int REQUEST_PHOTO = 1;

    private TrackerGPSService mService;
    private boolean mBound = false;

    private TextView mStartTimeTextView;
    private TextView mDurationTimeTextView;
    private TextView mDistanceTextView;
    private EditText mCommentEditText;

    private ImageButton mPhotoButton;
    private ImageButton mPauseButton;
    private ImageButton mSetingsButton;

    private String mCurrentPhotoFileName;

    private RelativeLayout mTrackPhotoLayout;
    private TextView mPhotoCounterTextView;
    private ImageView mTrackPhotoImage;


    private Timer timer;
    private TimerTask timerTask;

    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection ;

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
        setRetainInstance(true);// !!!! MUST HAVE THIS LINE
                                // (or should save/restore all members of this fragment - for example: mCurrentPhotoFileName)
                                // for properly working with camera intent
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
                    Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), (ArrayList<TrackPhoto>) mService.getCurrentTrack().getPhotoFiles(), null);
                    startActivity(intent);
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

            }
        });

        updateUI();
        //updatePhotoUI();

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

        final Intent capturePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        mCurrentPhotoFileName = mService.getCurrentTrack().getNewPhotoFileName();
        File currentPhotoFile = FileUtils.getPhotoFile(getContext(),mCurrentPhotoFileName);
        Uri uri = GenericFileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".vitalypanov.phototracker.provider", currentPhotoFile);
        capturePhoto.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(capturePhoto, REQUEST_PHOTO);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }
        switch (requestCode){
            case REQUEST_PHOTO:
                AssyncUpdatePhotoAfterTakePhotoTask assyncUpdatePhotoAfterTakePhotoTask = new AssyncUpdatePhotoAfterTakePhotoTask();
                assyncUpdatePhotoAfterTakePhotoTask.execute();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);;
        }

    }

    /**
     * Assync update image after take photo
     *
     */
    private class AssyncUpdatePhotoAfterTakePhotoTask extends AsyncTask<Void, Void, Void> {

        public AssyncUpdatePhotoAfterTakePhotoTask() {
        }

        @Override
        protected void onPreExecute() {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTrackPhotoImage.setImageBitmap(null);
                }
            });
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // wait for service and Activity correct objects
            while (mService == null
                    || mService.getCurrentTrack() == null
                    || getActivity() == null) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if (mService!= null && mService.getCurrentTrack() != null) {
                // check for null service is needed because after take photo the fragment can not exists
                File currentPhotoFile = FileUtils.getPhotoFile(getActivity(), mCurrentPhotoFileName);
                if (currentPhotoFile != null && currentPhotoFile.exists()) {
                    mService.getCurrentTrack().addPhotoItem(mCurrentPhotoFileName, mService.getCurrentTrack().getLastTrackItem());
                    TrackDbHelper.get(getActivity()).updateTrack(mService.getCurrentTrack());
                } else {
                    mCurrentPhotoFileName = null;
                }
                updatePhotoUI();
            }
            return null;
        }


    }

    private void updatePhotoUI(){
        if (mService == null || mService.getCurrentTrack() == null){
            return;
        }
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Track currentTrack = mService.getCurrentTrack();
                mTrackPhotoImage.setImageBitmap(BitmapHandler.get(getContext()).getBitmapScaleToFitWidth(currentTrack.getLastPhotoItem().getPhotoFileName(),  mTrackPhotoImage.getWidth()));
                mPhotoCounterTextView.setVisibility(currentTrack.getPhotoFiles().size() > 0 ? View.VISIBLE : View.GONE);
                mPhotoCounterTextView.setText(" " + String.valueOf(currentTrack.getPhotoFiles().size()) + " ");
                mPhotoCounterTextView.bringToFront();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        getActivity().invalidateOptionsMenu();
        Intent i = TrackerGPSService.newIntent(getActivity());
        mConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName componentName, IBinder service) {
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                TrackerGPSService.LocalBinder binder = (TrackerGPSService.LocalBinder) service;
                mService = binder.getService();
                mBound = true;
                updateUI();
                updatePhotoUI();
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                mBound = false;
            }


        };
        getActivity().bindService(i, mConnection, 0); //Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(mConnection);
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
        // uodate data in controls
        updateUI();
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
}
