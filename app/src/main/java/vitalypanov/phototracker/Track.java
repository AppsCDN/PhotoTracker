package vitalypanov.phototracker;

import android.location.Location;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Track class
 * Created by Vitaly on 22.02.2018.
 */

public class Track {
    private final String LEAD_ZEROS_TIME_FORMAT = "%02d";
    private final String DISTANCE_COVERED_FORMAT ="%.03f";
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

    public Location getLastTrackItem() {
        return Lists.getLast(trackData);
    }

    void addTrackItem(Location locationItem) {
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
