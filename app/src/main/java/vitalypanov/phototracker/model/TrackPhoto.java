package vitalypanov.phototracker.model;

import java.io.Serializable;

/**
 * Photo image of the track
 * Created by Vitaly on 27.02.2018.
 */

public class TrackPhoto implements Serializable, BasePhoto{
    String mPhotoFileName;
    TrackLocation mTrackLocation;

    public TrackPhoto() {
    }

    public TrackPhoto(String photoFileName, TrackLocation trackLocation) {
        mTrackLocation = trackLocation;
        this.mPhotoFileName = photoFileName;
    }

    public TrackLocation getTrackLocation() {
        return mTrackLocation;
    }

    public void setTrackLocation(TrackLocation trackLocation) {
        this.mTrackLocation = trackLocation;
    }

    public String getPhotoFileName() {
        return mPhotoFileName;
    }

    public void setPhotoFileName(String photoFileName) {
        this.mPhotoFileName = photoFileName;
    }

    @Override
    public String getName() {
        return getPhotoFileName();
    }
}
