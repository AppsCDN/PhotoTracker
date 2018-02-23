package vitalypanov.phototracker;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;


/**
 * Container for all your recorded tracks
 * Created by Vitaly on 22.02.2018.
 */

public class TrackContainer {
    private static TrackContainer mContainer;
    private Context mContext;
    private List<Track> mTracks;


    public static TrackContainer get(Context context){
        if (mContainer == null) {
            mContainer = new TrackContainer(context);
        }
        return mContainer;
    }

    private TrackContainer(Context context){
        mContext = context.getApplicationContext();
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
