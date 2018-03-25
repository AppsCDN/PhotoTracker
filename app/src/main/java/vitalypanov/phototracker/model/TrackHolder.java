package vitalypanov.phototracker.model;

/**
 * Singleton for holding current working track
 * Need to have for transferring track data between fragments (fucking android  :((( )
 * Created by Vitaly on 25.03.2018.
 */

public class TrackHolder {
    private static TrackHolder mTrackHolder;

    private Track mTrack;

    public static TrackHolder get() {
        if (mTrackHolder == null) {
            mTrackHolder = new TrackHolder();
        }
        return mTrackHolder;
    }

    private TrackHolder() {
    }

    public Track getTrack() {
        return mTrack;
    }

    public void setTrack(Track track) {
        mTrack = track;
    }
}
