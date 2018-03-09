package vitalypanov.phototracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import vitalypanov.phototracker.activity.GoogleMapActivity;
import vitalypanov.phototracker.activity.RunningTrackPagerActivity;
import vitalypanov.phototracker.activity.TrackImagesPagerActivity;
import vitalypanov.phototracker.database.TrackDbHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.model.TrackPhoto;
import vitalypanov.phototracker.utilities.AssyncBitmapLoaderTask;
import vitalypanov.phototracker.utilities.StringUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Created by Vitaly on 25.02.2018.
 */

public class TrackListFragment  extends Fragment {
    private static final String TAG = "PhotoTracker";
    private static final int REQUEST_CODE_IMAGES_PAGER = 0;
    private static final int REQUEST_CODE_RUNKEEPER_AUTH = 1;
    private RecyclerView mTrackRecyclerView;
    private TrackAdapter mAdapter;
    private Callbacks mCallbacks;

    // runkeeper variables
    private String mAccessToken;
    private String mFitnessActivitiesUrl;
    private String mExternalId;

    // Interface for activity host
    public interface Callbacks{
        void onTrackSelected(Track crime);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
       //mCallbacks = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks =null;
    }

    public static TrackListFragment newInstance() {
        return new TrackListFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_list, container, false);
        mTrackRecyclerView = (RecyclerView)view.
                findViewById(R.id.crime_recycler_view);
        mTrackRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        // add divider
        DividerItemDecoration divider = new DividerItemDecoration(mTrackRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.custom_divider));
        mTrackRecyclerView.addItemDecoration(divider);
        updateUI();
        return view;
    }

    public void updateUI(){
        TrackDbHelper trackDbHelper = TrackDbHelper.get(getActivity());
        List<Track> tracks = trackDbHelper.getTracks();
        if (mAdapter == null) {
            mAdapter = new TrackAdapter(tracks);
            mTrackRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setTracks(tracks);
            mAdapter.notifyDataSetChanged();
        }
    }

    // ViewHolder
    private class TrackHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener{

        Track mTrack;
        TrackAdapter mTrackAdapter;
        private TextView mStartDateTextView;
        private TextView mStartTimeTextView;
        private ImageButton mDeleteButton;
        private TextView mDistanceTextView;
        private TextView mDurationTextView;
        private ImageButton mTrackMapButton;
        private ImageButton mTrackContinueButton;
        private ImageButton mRunKeeperButton;
        private RelativeLayout mTrackPhotoLayout;
        private ImageView mTrackPhotoImageView;
        private TextView mImageCounterTextView;
        private RelativeLayout mLoadingPanel;
        private TextView mCommentTextView;

        public TrackHolder(View itemView ){
            super(itemView);
            itemView.setOnClickListener(this);
            mStartDateTextView = (TextView)itemView.findViewById(R.id.list_item_start_date_text_view);
            mStartTimeTextView = (TextView)itemView.findViewById(R.id.list_item_start_time_text_view);
            mDistanceTextView = (TextView)itemView.findViewById(R.id.list_item_distance_text_view);
            mDurationTextView = (TextView) itemView.findViewById(R.id.list_item_duration_text_view);
            mTrackMapButton = (ImageButton) itemView.findViewById(R.id.list_item_track_map_button);
            mTrackMapButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = GoogleMapActivity.newIntent(getActivity(), mTrack.getUUID());
                    startActivity(intent);
                }
            });

            mTrackContinueButton = (ImageButton) itemView.findViewById(R.id.list_item_track_continue_button);
            mTrackContinueButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Run service within existing track object

                    // first check location services
                    if (!LocationServices.get(getActivity()).checkLocaionServices()){
                        return;
                    }
                    // show warning message before continue recording of the track
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.continue_track_confirm_title);
                    String sMessage = getResources().getString(R.string.continue_track_confirm_message) +
                            "\n" +
                            "\n" + mTrack.getStartDateFormatted() + " " + mTrack.getStartTimeFormatted()+
                            "\n" + mTrack.getDistanceFormatted() + " " + getResources().getString(R.string.distance_metrics) + ". " +
                            mTrack.getDurationTimeFormatted() + " " + getResources().getString(R.string.duration) + "." +
                            (mTrack.getComment()!= null? "\n" + mTrack.getComment() : "");
                    builder.setMessage(sMessage);
                    builder.setPositiveButton(R.string.continue_track_confirm_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // confirmed to continue existing track!

                                    // Run track recording service and provide them our selected track to continue
                                    Intent i = TrackerGPSService.newIntent(getActivity(), mTrack.getUUID());
                                    getActivity().startService(i);

                                    // third - hide current activity and show "status" activity
                                    getActivity().finish();
                                    Intent intent = RunningTrackPagerActivity.newIntent(getActivity());
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

            mRunKeeperButton = (ImageButton) itemView.findViewById(R.id.list_item_runkeeper_button);
            mRunKeeperButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO export to runkeeper.com
                    authRunKeeper(mTrack);
                }
            });

            // photo layout elements:
            mTrackPhotoLayout = (RelativeLayout) itemView.findViewById(R.id.list_item_track_photo_layout);
            mTrackPhotoImageView = (ImageView) itemView.findViewById(R.id.list_item_track_photo_image);
            mTrackPhotoImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mTrack.getPhotoFiles().size()>0) {
                        Intent intent = TrackImagesPagerActivity.newIntent(getActivity(), mTrack.getUUID(), (ArrayList< TrackPhoto>) mTrack.getPhotoFiles(), null);
                        startActivityForResult(intent, REQUEST_CODE_IMAGES_PAGER);
                    }
                }
            });
            mImageCounterTextView= (TextView) itemView.findViewById(R.id.list_item_image_counter_textview);

            // progress of loading image layout
            mLoadingPanel = (RelativeLayout) itemView.findViewById(R.id.list_item_track_loading_photo);

            mCommentTextView = (TextView) itemView.findViewById(R.id.list_item_comment_text_view);

            mDeleteButton= (ImageButton) itemView.findViewById(R.id.list_item_track_delete_button);
            mDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setCancelable(true);
                    builder.setTitle(R.string.remove_track_confirm_title);
                    String sMessage = getResources().getString(R.string.remove_track_confirm_message) +
                            "\n" +
                            "\n" + mTrack.getStartDateFormatted() + " " + mTrack.getStartTimeFormatted()+
                            "\n" + mTrack.getDistanceFormatted() + " " + getResources().getString(R.string.distance_metrics) + ". " +
                            mTrack.getDurationTimeFormatted() + " " + getResources().getString(R.string.duration) + "." +
                            (mTrack.getComment()!= null? "\n" + mTrack.getComment() : "");
                    builder.setMessage(sMessage);
                    builder.setPositiveButton(R.string.remove_track_confirm_button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    // confirm deleting - do it
                                    TrackDbHelper.get(getContext()).deleteTrack(mTrack);
                                    mTrackAdapter.removeAt(getAdapterPosition());
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
        }


        public void bindTrack(Track track, TrackAdapter trackAdapter){
            mTrack = track;
            mTrackAdapter = trackAdapter;

            mStartDateTextView.setText(mTrack.getStartDateFormatted());
            mStartTimeTextView.setText(mTrack.getStartTimeFormatted());
            mDistanceTextView.setText(mTrack.getDistanceFormatted());
            mDurationTextView.setText(mTrack.getDurationTimeFormatted());
            mCommentTextView.setText(mTrack.getComment());
            mTrackPhotoLayout.setVisibility(mTrack.getPhotoFiles().size() > 0 ? View.VISIBLE : View.GONE);
            mImageCounterTextView.setText(" " + String.valueOf(mTrack.getPhotoFiles().size()) + " ");
            mImageCounterTextView.bringToFront();
            updatePhotoUI();
        }

        private void updatePhotoUI(){
            if (Utils.isNull(mTrack)){
                return;
            }
            String photoFileName = Utils.isNull(mTrack.getLastPhotoItem()) ? "" : mTrack.getLastPhotoItem().getPhotoFileName();
            AssyncBitmapLoaderTask assyncImageViewUpdater = new AssyncBitmapLoaderTask(photoFileName,
                    mTrackPhotoImageView,
                    mTrackRecyclerView.getWidth(),
                    getContext(),
                    mLoadingPanel);
            assyncImageViewUpdater.execute();
        }

        @Override
        public void onClick(View v) {

            //mCallbacks.onTrackSelected(mTrack);
        }

    }

    /**
     * Auth to runkeeper.com
     */
    Track mTrack = null;
    private void authRunKeeper(Track track){
        mTrack = track;
        Intent intent = OAuth2Activity.newIntent(getActivity());
        startActivityForResult(intent, REQUEST_CODE_RUNKEEPER_AUTH);
    }

    /**
     * Export to runkeeper
     */
    private void exportRunKeeper(){
        if (StringUtils.isNullOrBlank(mAccessToken)){
            return;
        }

        AssyncUploadRunKeeper assyncUploadRunKeeper = new AssyncUploadRunKeeper();
        assyncUploadRunKeeper.execute();
    }

    class AssyncUploadRunKeeper extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            // if bitmaps not loaded yet...
            if (!upload(1)){
                return null;
            }
            return null;
        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }

    private static String REST_URL = "https://api.runkeeper.com";
    private boolean connect() {
        if (Utils.isNull(mAccessToken)) {
            return false;
        }

        if (mFitnessActivitiesUrl != null) {
            return true;
        }

        /**
         * Get the fitnessActivities end-point
         */
        String uri = null;
        HttpURLConnection conn = null;
        Exception ex = null;
        do {
            try {
                URL newurl = new URL(REST_URL + "/user");
                conn = (HttpURLConnection) newurl.openConnection();
                conn.setRequestProperty("Authorization", "Bearer "
                        + mAccessToken);
                conn.addRequestProperty("Content-Type", "application/vnd.com.runkeeper.User+json");
                InputStream in = new BufferedInputStream(conn.getInputStream());
                JSONObject obj = SyncHelper.parse(in);
                conn.disconnect();
                conn = null;
                uri = obj.getString("fitness_activities");
            } catch (MalformedURLException e) {
                ex = e;
            } catch (IOException e) {
                if (REST_URL.contains("https")) {
                    REST_URL = REST_URL.replace("https", "http");
                    Log.e(TAG, e.getMessage());
                    Log.e(TAG, " => retry with REST_URL: " + REST_URL);
                    continue; // retry
                }
                ex = e;
            } catch (JSONException e) {
                ex = e;
            }
            break;
        } while (true);

        if (conn != null) {
            conn.disconnect();
        }

        if (ex != null) {
            Log.e(TAG, ex.getMessage());
        }

        if (uri != null) {
            mFitnessActivitiesUrl = uri;
            return true;
        }
        //s = Synchronizer.Status.ERROR;
        //s.ex = ex;
        return false;
    }

    public boolean upload(final long mID) {

        if (!connect()) {
            return false;
        }

        /**
         * Get the fitnessActivities end-point
         */
        HttpURLConnection conn = null;
        Exception ex;
        try {
            URL newurl = new URL(REST_URL + mFitnessActivitiesUrl);
            //Log.e(Constants.LOG, "url: " + newurl.toString());
            conn = (HttpURLConnection) newurl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Authorization", "Bearer " + mAccessToken);
            conn.addRequestProperty("Content-type",
                    "application/vnd.com.runkeeper.NewFitnessActivity+json");
            //RunKeeper rk = new RunKeeper(db);
            BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                    conn.getOutputStream()));
            export(mID, w);
            w.flush();

            int responseCode = conn.getResponseCode();
            String amsg = conn.getResponseMessage();
            mExternalId = StringUtils.noNullStr(conn.getHeaderField("Location")); // fitnessActivities/1130928304

            conn.disconnect();
            conn = null;

            if (responseCode >= HttpURLConnection.HTTP_OK && responseCode < HttpURLConnection.HTTP_MULT_CHOICE) {
                // s = Status.OK;
                //s.activityId = mID;
                //s.externalIdStatus = ExternalIdStatus.OK;
                return true;
            }
            Log.e(TAG, "Error code: " + responseCode + ", amsg: " + amsg);
            ex = new Exception(amsg);
        } catch (Exception e) {
            ex = e;
        }

        if (ex != null) {
            Log.e(TAG, "Failed to upload: " + ex.getMessage());
        }

        if (conn != null) {
            conn.disconnect();
        }
        //s = Synchronizer.Status.ERROR;
        //s.ex = ex;
        //s.activityId = mID;
        return false;
    }

    private void export(long activityId, Writer writer) throws IOException {

        //ActivityEntity ae = new ActivityEntity();
        //ae.readByPrimaryKey(mDB, activityId);
        long startTime = mTrack.getStartTime().getTime();
        double distance = mTrack.getDistance();
        long duration = mTrack.getDuration();
        String comment = mTrack.getComment();
        try {
            JsonWriter w = new JsonWriter(writer);
            w.beginObject();
            /*
            Sport s = Sport.valueOf(ae.getSport());
            if (!RunKeeperSynchronizer.sport2runkeeperMap.containsKey(s)) {
                s = Sport.OTHER;
            }
            */
            w.name("type").value("Other");
            w.name("equipment").value("None");
            w.name("start_time").value(formatTime(startTime * 1000));
            w.name("total_distance").value(distance);
            w.name("duration").value(duration);
            if (comment != null && comment.length() > 0) {
                w.name("notes").value(comment);
            }
            //it seems that upload fails if writting an empty array...
            /*
            if (ae.getMaxHr()!=null) {
                w.name("heart_rate");
                w.beginArray();
                exportHeartRate(activityId, startTime, w);
                w.endArray();
            }
            */
            exportPath("path", activityId, startTime, w);
            w.name("post_to_facebook").value(false);
            w.name("post_to_twitter").value(false);
            w.endObject();
        } catch (IOException e) {
            throw e;
        }
    }

    static String formatTime(long time) {
        return new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US)
                .format(new Date(time));
    }

    private void exportPath(String name, long activityId, long startTime, JsonWriter w)
            throws IOException {
        /*
        String[] pColumns = {
                DB.LOCATION.TIME, DB.LOCATION.LATITUDE,
                DB.LOCATION.LONGITUDE, DB.LOCATION.ALTITUDE, DB.LOCATION.TYPE
        };
        Cursor cursor = mDB.query(DB.LOCATION.TABLE, pColumns,
                DB.LOCATION.ACTIVITY + " = " + activityId, null, null, null,
                null);
        */
        //if (cursor.moveToFirst())
        {
            w.name(name);
            w.beginArray();
            //startTime = cursor.getLong(0); //???
            boolean bFirstItem = true;
            long lIndex = 0;
            for (TrackLocation trackLocation : mTrack.getTrackData()) {
                w.beginObject();
                w.name("timestamp").value(
                        (startTime - startTime) / 1000);
                w.name("latitude").value(trackLocation.getLatitude());
                w.name("longitude").value(trackLocation.getLongitude());

                // Altitude not yet
                w.name("altitude").value(0);
                /*
                if (!cursor.isNull(3)) {
                    w.name("altitude").value(cursor.getDouble(3));
                }
                */
                lIndex++;
                if (bFirstItem) {
                    w.name("type").value("start");
                    bFirstItem = false;
                }else if (lIndex ==  mTrack.getTrackData().size()){
                    w.name("type").value("end");
                } else {
                    w.name("type").value("gps");
                }

                /*
                if (cursor.getLong(4) == DB.LOCATION.TYPE_START) {
                    w.name("type").value("start");
                } else if (cursor.getLong(4) == DB.LOCATION.TYPE_END) {
                    w.name("type").value("end");
                } else if (cursor.getLong(4) == DB.LOCATION.TYPE_PAUSE) {
                    w.name("type").value("pause");
                } else if (cursor.getLong(4) == DB.LOCATION.TYPE_RESUME) {
                    w.name("type").value("resume");
                } else if (cursor.getLong(4) == DB.LOCATION.TYPE_GPS) {
                    w.name("type").value("gps");
                } else {
                    w.name("type").value("manual");
                }
                */
                w.endObject();
            } //while (cursor.moveToNext());
            w.endArray();
        }
        //cursor.close();
    }

    /*
    private void exportHeartRate(long activityId, long startTime, JsonWriter w)
            throws IOException {
        String[] pColumns = {
                DB.LOCATION.TIME, DB.LOCATION.HR
        };
        Cursor cursor = mDB.query(DB.LOCATION.TABLE, pColumns,
                DB.LOCATION.ACTIVITY + " = " + activityId, null, null, null,
                null);
        if (cursor.moveToFirst()) {
            startTime = cursor.getLong(0);
            do {
                if (!cursor.isNull(1)) {
                    w.beginObject();
                    w.name("timestamp").value(
                            (cursor.getLong(0) - startTime) / 1000);
                    w.name("heart_rate").value(Integer.toString(cursor.getInt(1)));
                    w.endObject();
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
    }
    */


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK){
            return;
        }

        switch (requestCode){
            case REQUEST_CODE_IMAGES_PAGER:
                /*
                if (Utils.isNull(data)) {
                    return;
                }
                */
                mAdapter.setTracks(TrackDbHelper.get(getActivity()).getTracks());
                mAdapter.notifyDataSetChanged();
                break;
            case REQUEST_CODE_RUNKEEPER_AUTH:
                String authConfig = data.getStringExtra("auth_config");
                try {
                    JSONObject obj = new JSONObject(authConfig);
                    mAccessToken = obj.getString("access_token");
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
                // mAccessToken will be use
                exportRunKeeper();
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);;
        }
    }

    // Adapter
    private class TrackAdapter extends RecyclerView.Adapter<TrackHolder>{
        private List<Track> mTracks;

        public TrackAdapter(List<Track> tracks) {
            mTracks = tracks;
        }

        @Override
        public TrackHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            View view = layoutInflater.inflate(R.layout.list_item_track, parent, false);
            return new TrackHolder(view);
        }

        @Override
        public void onBindViewHolder(TrackHolder holder, int position) {
            Track track = mTracks.get(position);
            holder.bindTrack(track, this);
        }

        @Override
        public int getItemCount() {
            return mTracks.size();
        }

        public void setTracks(List<Track> tracks){
            mTracks = tracks;
        }

        /**
         * Delete position from list
         * @param position
         */
        public void removeAt(int position) {
            mTracks.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mTracks.size());
        }

    }
}