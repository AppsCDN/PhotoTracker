package vitalypanov.phototracker.export;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Window;

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

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.export.utilities.JsonWriter;
import vitalypanov.phototracker.export.utilities.SyncHelper;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.utilities.DateUtils;
import vitalypanov.phototracker.utilities.StringUtils;
import vitalypanov.phototracker.utilities.Utils;

/**
 * Export track data to RunKeeper.com
 * Created by Vitaly on 11.03.2018.
 */

public class RunKeeperExport {
    private static final String TAG = "PhotoTracker";
    private static String REST_URL = "https://api.runkeeper.com";   // rest api url

    private Track mTrack;                   // Track data to export
    private Context mContext;

    private String mAccessToken;            // Access to runkeeper rest api
    private String mFitnessActivitiesUrl;   // uri
    private String mExternalId;             // if of the created activity in runkeeper.com (not using yet)

    public RunKeeperExport(Track track, Context context) {
        mTrack = track;
        mContext = context;
    }

    public String getAccessToken() {
        return mAccessToken;
    }

    public void setAccessToken(String accessToken) {
        mAccessToken = accessToken;
    }

    /**
     * Export to runkeeper
     */
    public void export(){
        // checking access token first
        if (StringUtils.isNullOrBlank(mAccessToken)){
            return;
        }

        // start export
        AssyncUploadRunKeeper assyncUploadRunKeeper = new AssyncUploadRunKeeper();
        assyncUploadRunKeeper.execute();
    }

    /**
     * Assync exporting to runkeeper.com
     */
    private class AssyncUploadRunKeeper extends AsyncTask<Void, Void, Void> {

        private ProgressDialog mSpinner;
        private boolean mUploadResult;

        @Override
        protected Void doInBackground(Void... params) {
            // if bitmaps not loaded yet...
            mUploadResult = upload(1);
            return null;
        }

        @Override
        protected void onPreExecute() {
            mUploadResult = false;
            mSpinner = new ProgressDialog(mContext);
            mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mSpinner.setMessage(mContext.getResources().getString(R.string.runkeeper_progress_message));
            mSpinner.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mSpinner.dismiss();
            AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
            alertDialog.setTitle(mContext.getResources().getString(mUploadResult ? R.string.runkeeper_fininsh_title : R.string.runkeeper_fininsh_fail_title));
            alertDialog.setMessage(mContext.getResources().getString(mUploadResult ? R.string.runkeeper_fininsh_message : R.string.runkeeper_fininsh_fail_message));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, mContext.getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
        }
    }

    private boolean connect() {
        if (Utils.isNull(mAccessToken)) {
            return false;
        }

        if (mFitnessActivitiesUrl != null) {
            return true;
        }

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
            conn = (HttpURLConnection) newurl.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.addRequestProperty("Authorization", "Bearer " + mAccessToken);
            conn.addRequestProperty("Content-type",
                    "application/vnd.com.runkeeper.NewFitnessActivity+json");
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
        return false;
    }

    private void export(long activityId, Writer writer) throws IOException {

        //ActivityEntity ae = new ActivityEntity();
        //ae.readByPrimaryKey(mDB, activityId);
        long startTime = mTrack.getStartTime().getTime();
        double distance = mTrack.getDistance() * 1000; // runkeeper store dostance in meters
        long duration = mTrack.getDuration()/1000; // runkeeper store time in seconds (without milliseconds)
        String comment = mTrack.getComment();
        try {
            JsonWriter w = new JsonWriter(writer);
            w.beginObject();
            w.name("type").value("Other");
            w.name("equipment").value("None");
            w.name("start_time").value(DateUtils.formatTime(startTime));
            w.name("total_distance").value(distance);
            w.name("duration").value(duration);
            if (comment != null && comment.length() > 0) {
                w.name("notes").value(comment);
            }
            exportPath("path", activityId, startTime, w);
            w.name("post_to_facebook").value(false);
            w.name("post_to_twitter").value(false);
            w.endObject();
        } catch (IOException e) {
            throw e;
        }
    }

    private void exportPath(String name, long activityId, long startTime, JsonWriter w)
            throws IOException {
        w.name(name);
        w.beginArray();
        boolean bFirstItem = true;
        long lIndex = 0;
        for (TrackLocation trackLocation : mTrack.getTrackData()) {
            w.beginObject();
            long timeStamp = (trackLocation.getTimeStamp().getTime() - startTime)/1000; // runkeeper store time in seconds (without milli)
            if (timeStamp>0) {
                w.name("timestamp").value(timeStamp);
            } else {
                // Incorrect situation:
                // timestamp of TrackLocation object is bigger than Start Time of whole track
                w.name("timestamp").value(0);
            }
            w.name("latitude").value(trackLocation.getLatitude());
            w.name("longitude").value(trackLocation.getLongitude());
            w.name("altitude").value(trackLocation.getAltitude());
            lIndex++;
            if (bFirstItem) {
                w.name("type").value("start");
                bFirstItem = false;
            }else if (lIndex ==  mTrack.getTrackData().size()){
                w.name("type").value("end");
            } else {
                w.name("type").value("gps");
            }
            // other values: "pause", "resume", "manual"
            w.endObject();
        }
        w.endArray();

        // trying to upload image... unsuccessfully :(
        /*
        w.name("images");
        w.beginArray();
            w.beginObject();
                w.name("timestamp").value(2);
                w.name("latitude").value(44.64809096);
                w.name("longitude").value(33.53846836);
                w.name("uri").value("https://trip-photo.runkeeper.com/b1c7hRAyQPpuQR7JPi8Wu5xQ.jpg");
                w.name("thumbnail_uri").value("https://trip-photo.runkeeper.com/b1c7hRAyQPpuQR7JPi8Wu5xQ_small.jpg");
            w.endObject();
        w.endArray();
        */
    }

}
