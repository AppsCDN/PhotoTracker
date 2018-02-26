package vitalypanov.phototracker.model;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import vitalypanov.phototracker.utilities.DateUtils;
import vitalypanov.phototracker.utilities.ListUtils;

/**
 * Track class
 * Created by Vitaly on 22.02.2018.
 */

public class Track {
    private UUID mId;       // unique id of the track
    private Date mStartTime;// start time when track was started
    private Date mEndTime;  //... when finished recording
    private String mComment;// user comment if provided
    private double mDistance;// Cashed distance value of the track (need recalculate when trackData are updated)
    private List<TrackLocation> trackData = new ArrayList<>(); // gps locations data of the track

    // Distance format
    private final String DISTANCE_COVERED_FORMAT ="%.03f";

    /**
     * For newlly created tracks - generate Id
     */
    public Track() {
        this(UUID.randomUUID());
    }

    /**
     * For already exists tracks - provide Id in parameter
     * @param id - track id in database
     */
    public Track(UUID id) {
        mId = id;
        mStartTime = new Date();
        mEndTime = new Date();
        trackData = new ArrayList<>();
    }

    public UUID getId() { return mId;}

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

    public double getDistance() {
        return mDistance;
    }

    public void setDistance(double mDistance) {
        this.mDistance = mDistance;
    }

    public List<TrackLocation> getTrackData() {
        return trackData;
    }

    public void setTrackData(List<TrackLocation> data) {
        this.trackData = data;
    }

    public TrackLocation getLastTrackItem() {
        return ListUtils.getLast(trackData);
    }

    public void addTrackItem(TrackLocation locationItem) {
        trackData.add(locationItem);
    }

    /**
     * Calculate full distance of the track.
     * Rounded to full kilometers
     * @return track distance
     */
    private double getRealDistance(){
        float distance = 0;
        TrackLocation prevLocationItem = ListUtils.getFirst(trackData);
        for (int index = 1; index< trackData.size(); index++){
            TrackLocation currLocationItem = trackData.get(index);
            distance += currLocationItem.distanceTo(prevLocationItem);
            prevLocationItem = currLocationItem;
        }
        return (double)((long)distance)/1000.;
    }

    /**
     * Recalculate distance value
     */
    public void recalcDistance(){
        mDistance = getRealDistance();
    }

    /**
     * Duration time of the track
     * difference between startTime and endTime
     * @return duration time
     */
    public String getDurationTimeFormatted(){
        return formatDuration(mEndTime.getTime() - mStartTime.getTime());
    }
    /**
     * Duration time of the still running track
     * (time since start time until now)
     * @return duration time
     */
    public String getDurationTimeStillRunningFormatted(){
        Date currentTime = Calendar.getInstance().getTime();
        return formatDuration(currentTime.getTime() - mStartTime.getTime());
    }

    private String formatDuration(long mills){
        return DateUtils.getDurationFormatted(mills);
    }

    /**
     * Distance formatted in kilometers
     * @return
     */
    public String getDistanceFormatted(){
        return String.format(DISTANCE_COVERED_FORMAT, getDistance());
    }

    /**
     * Start time formatted
     * @return
     */
    public String getStartTimeFormatted() {
        return DateUtils.getTimeFormatted(mStartTime);
    }

    /**
     * Date time formatted
     * @return
     */
    public String getStartDateFormatted() {
        return DateUtils.getDateFormatted(mStartTime);
    }
}
