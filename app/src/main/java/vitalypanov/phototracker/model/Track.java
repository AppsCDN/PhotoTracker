package vitalypanov.phototracker.model;

import android.location.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import vitalypanov.phototracker.utilities.Lists;

/**
 * Track class
 * Created by Vitaly on 22.02.2018.
 */

public class Track {
    private UUID mId;       // unique id of the track
    private Date mStartTime;// start time when track was started
    private Date mEndTime;  //... when finished recording
    private String mComment;// user comment if provided
    private List<Location> trackData = new ArrayList<>(); // gps locations data of the track

    // Format constants
    private final String LEAD_ZEROS_TIME_FORMAT = "%02d";
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

    public List<Location> getTrackData() {
        return trackData;
    }

    public void setTrackData(List<Location> data) {
        this.trackData = data;
    }

    public Location getLastTrackItem() {
        return Lists.getLast(trackData);
    }

    public void addTrackItem(Location locationItem) {
        trackData.add(locationItem);
    }

    /**
     * Calculate full distance of the track.
     * Rounded to full kilometers
     * @return track distance
     */
    public double getDistance(){
        float distance = 0;
        Location prevLocationItem = Lists.getFirst(trackData);
        for (int index = 1; index< trackData.size(); index++){
            Location currLocationItem = trackData.get(index);
            distance += currLocationItem.distanceTo(prevLocationItem);
            prevLocationItem = currLocationItem;
        }
        return (double)((long)distance)/1000.;
    }

    /**
     * Duration time of the track
     * (time since start time until now
     * @return duration time
     */
    public String getDurationTimeFormatted(){
        Date currentTime = Calendar.getInstance().getTime();
        long mills = currentTime.getTime() - mStartTime.getTime();
        int hours = (int) (mills/(1000 * 60 * 60));
        int minutes = (int) (mills/(1000*60)) % 60;
        long seconds = (int) (mills / 1000) % 60;
        return (String.format(LEAD_ZEROS_TIME_FORMAT, hours) + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, minutes)  + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, seconds));
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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(mStartTime);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        return (String.format(LEAD_ZEROS_TIME_FORMAT, hours) + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, minutes)  + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, seconds));
    }
}
