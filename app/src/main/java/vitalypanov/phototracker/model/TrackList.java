package vitalypanov.phototracker.model;

import android.content.Context;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Container for list of tracks
 * Created by Vitaly on 22.02.2018.
 */

public class TrackList {
    private static TrackList mContainer;
    private Context mContext;
    private List<Track> mTracks;

    public static TrackList get(){
        if (mContainer == null) {
            mContainer = new TrackList();
        }
        return mContainer;
    }

    private TrackList(){
        mTracks = new ArrayList<Track>();
        generateTestArray();
    }

    public List<Track> getTrackList(){
        return mTracks;
    }
    /**
     * Generating some test tracks...
     */
    private void generateTestArray(){
        for (int i=0; i< 10; i++){
            Track track = new Track();
            track.setStartTime(Calendar.getInstance().getTime());
            mTracks.add(track);
        }
    }
}
