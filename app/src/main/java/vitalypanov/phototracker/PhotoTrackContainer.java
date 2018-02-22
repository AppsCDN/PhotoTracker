package vitalypanov.phototracker;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Calendar;


/**
 * Container for all your recorded tracks
 * Created by Vitaly on 22.02.2018.
 */

public class PhotoTrackContainer {
    private static PhotoTrackContainer mContainer;
    private Context mContext;
    private List<PhotoTrack> mTracks;


    public static PhotoTrackContainer get(Context context){
        if (mContainer == null) {
            mContainer = new PhotoTrackContainer(context);
        }
        return mContainer;
    }

    private PhotoTrackContainer(Context context){
        mContext = context.getApplicationContext();
        mTracks = new ArrayList<PhotoTrack>();
        generateTestArray();
    }

    /**
     * Generating some test data...
     */
    private void generateTestArray(){
        for (int i=0; i< 10; i++){
            PhotoTrack track = new PhotoTrack();
            track.setStartTime(Calendar.getInstance().getTime());
            mTracks.add(track);
        }
    }
}
