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
    private UUID mUUID;       // unique id of the track
    private Date mStartTime;// start time when track was started
    private Date mEndTime;  //... when finished recording
    private String mComment;// user comment if provided
    private double mDistance;// Cashed distance value of the track (need recalculate when mTrackData are updated)
    private List<TrackLocation> mTrackData = new ArrayList<>(); // gps locations data of the track
    private List<TrackPhoto> mPhotoFiles = new ArrayList<>(); // names of photo files attached to the track

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
     * @param UUID - track UUID in database
     */
    public Track(UUID UUID) {
        mUUID = UUID;
        mStartTime = new Date();
        mEndTime = null;
        mTrackData = new ArrayList<>();
        mPhotoFiles = new ArrayList<>();
    }

    public UUID getUUID() { return mUUID;}

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
        return mTrackData;
    }

    public void setTrackData(List<TrackLocation> data) {
        this.mTrackData = data;
    }

    public TrackLocation getLastTrackItem() {
        return ListUtils.getLast(mTrackData);
    }

    public void addTrackItem(TrackLocation locationItem) {
        mTrackData.add(locationItem);
    }

    public List<TrackPhoto> getPhotoFiles() {
        return mPhotoFiles;
    }

    public void setPhotoFiles(List<TrackPhoto> photoFiles) {
        this.mPhotoFiles = photoFiles;
    }

    public void addPhotoItem(String photoFileName, TrackLocation trackLocation) {
        TrackPhoto trackPhoto = new TrackPhoto(photoFileName, trackLocation);
        mPhotoFiles.add(trackPhoto);
    }

    public TrackPhoto getLastPhotoItem() {
        return ListUtils.getLast(mPhotoFiles);
    }

    /**
     * Calculate full distance of the track.
     * Rounded to full kilometers
     * @return track distance
     */
    private double getRealDistance(){
        float distance = 0;
        TrackLocation prevLocationItem = ListUtils.getFirst(mTrackData);
        for (int index = 1; index< mTrackData.size(); index++){
            TrackLocation currLocationItem = mTrackData.get(index);
            distance += currLocationItem.distanceTo(prevLocationItem);
            prevLocationItem = currLocationItem;
        }
        return (double)((long)distance)/1000.;
    }

    /**
     * Calculate new location with min longtitude and latidude in track
     * @return
     */
    public TrackLocation getMinTrackLocation(){
        return getMinMaxTrackLocation(false);
    }

    /**
     * Calculate new location with max longtitude and latidude in track
     * @return
     */
    public TrackLocation getMaxTrackLocation(){
        return getMinMaxTrackLocation(true);
    }

    private TrackLocation getMinMaxTrackLocation(boolean bMaxCalculate){
        if (mTrackData == null){
            return null;
        }
        TrackLocation trackLocationResult = ListUtils.getFirst(mTrackData);
        double longitude = trackLocationResult.getLongitude();
        double latitude = trackLocationResult.getLatitude();
        for (int index = 1; index< mTrackData.size(); index++){
            TrackLocation currLocationItem = mTrackData.get(index);
            if (bMaxCalculate) {
                if (currLocationItem.getLongitude() > longitude) {
                    longitude = currLocationItem.getLongitude();
                }
                if (currLocationItem.getLatitude() > latitude) {
                    latitude = currLocationItem.getLatitude();
                }
            } else {
                if (currLocationItem.getLongitude() < longitude){
                    longitude = currLocationItem.getLongitude();
                }
                if (currLocationItem.getLatitude() < latitude){
                    latitude = currLocationItem.getLatitude();
                }
            }
        }
        // for this object it doesnt matter which altitude or timestamp
        return new TrackLocation(longitude, latitude, 0, new Date());
    }

    /**
     * Recalculate distance value
     */
    public void recalcDistance(){
        mDistance = getRealDistance();
    }

    public long getDuration(){
        return mEndTime.getTime() - mStartTime.getTime();
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
     * Start time formatted
     * @return
     */
    public String getStartTimeShortFormatted() {
        return DateUtils.getShortTimeFormatted(mStartTime);
    }


    /**
     * Date time formatted
     * @return
     */
    public String getStartDateFormatted() {
        return DateUtils.getDateFormatted(mStartTime);
    }

    /**
     * Create new photo file name - for new photo in track
     * @return
     */
    public String getNewPhotoFileName(){
        Calendar currentDateTime = Calendar.getInstance();
        int year = currentDateTime.get(Calendar.YEAR);
        int month = currentDateTime.get(Calendar.MONTH);
        int day = currentDateTime.get(Calendar.DAY_OF_MONTH);
        int hour = currentDateTime.get(Calendar.HOUR_OF_DAY);
        int minute = currentDateTime.get(Calendar.MINUTE);
        int second = currentDateTime.get(Calendar.SECOND);
        return "IMG_" +
                String.valueOf(year) + String.valueOf(month) + String.valueOf(day) + "_" +
                String.valueOf(hour) + String.valueOf(minute) + String.valueOf(second) + ".jpg";
    }
}
