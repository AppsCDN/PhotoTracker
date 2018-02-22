package vitalypanov.phototracker;

import android.location.Location;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Track class
 * Created by Vitaly on 22.02.2018.
 */

public class PhotoTrack {

    private Date mStartTime;
    private Date mEndTime;
    private String mComment;
    private List<Location> trackData = new ArrayList<>();

    public Date getStartTime() {
        return mStartTime;
    }

    public void setStartTime(Date startTime) {
        this.mStartTime = startTime;
    }

    public Date getEndTime() {
        return mEndTime;
    }

    public void setEndTime(Date endTime) {
        this.mEndTime = endTime;
    }

    public String getComment() {
        return mComment;
    }

    public void setComment(String comment) {
        this.mComment = comment;
    }

    public List<Location> getTrackData() {
        return trackData;
    }

    public void setTrackData(List<Location> data) {
        this.trackData = data;
    }

}
