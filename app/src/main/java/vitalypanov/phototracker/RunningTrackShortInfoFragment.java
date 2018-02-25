package vitalypanov.phototracker;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

import vitalypanov.phototracker.model.Track;

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

        mPhotoButton = (ImageButton) v.findViewById(R.id.track_photo);
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
                // take photo
                final Intent capturePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(capturePhoto, REQUEST_PHOTO);
            }
        });

        mPauseButton = (ImageButton) v.findViewById(R.id.track_pause);
        mPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopTrack();
                getActivity().finish();
                Intent intent = StartScreenActivity.newIntent(getActivity());
                startActivity(intent);
            }
        });

        mSetingsButton = (ImageButton) v.findViewById(R.id.track_settings);
        mSetingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        updateUI();

        return v;
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
                mStartTimeTextView.setText(currentTrack.getStartTimeFormatted());
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
