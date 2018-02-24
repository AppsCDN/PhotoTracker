package vitalypanov.phototracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import vitalypanov.phototracker.database.TrackerDatabaseHelper;


/**
 * Container for all your recorded tracks
 * Created by Vitaly on 22.02.2018.
 */

public class TrackContainer {
    private static TrackContainer mContainer;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private List<Track> mTracks;


    public static TrackContainer get(Context context){
        if (mContainer == null) {
            mContainer = new TrackContainer(context);
        }
        return mContainer;
    }

    private TrackContainer(Context context){
        mContext = context.getApplicationContext();
        mDatabase = new TrackerDatabaseHelper (mContext).getWritableDatabase();
        mTracks = new ArrayList<Track>();
        generateTestArray();
    }

    /**
     * Generating some test data...
     */
    private void generateTestArray(){
        for (int i=0; i< 10; i++){
            Track track = new Track();
            track.setStartTime(Calendar.getInstance().getTime());
            mTracks.add(track);
        }
    }
}
