package vitalypanov.phototracker.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Custom Location class for storing longitude, latitude and etc.
 *
 * p.s.
 * It made because of application was somtimes crashed,  when I trying to process standard Location objects with Gson
 * I got following error: A/libc: Fatal signal 11 (SIGSEGV), code 128, fault addr 0x0 in tid 26221 (FinalizerDaemon)
 *
 * Created by Vitaly on 25.02.2018.
 */

public class TrackLocation implements Serializable{

    private Date mTimeStamp;
    private double mLongitude;
    private double mLatitude;
    private double mAltitude;

    public TrackLocation() {
    }

    public TrackLocation(double longitude, double latitude, double altitude, Date timeStamp) {
        this.mLongitude = longitude;
        this.mLatitude = latitude;
        this.mAltitude = altitude;
        this.mTimeStamp = timeStamp;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        this.mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        this.mLatitude = latitude;
    }

    public Date getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        mTimeStamp = timeStamp;
    }

    public double getAltitude() {
        return mAltitude;
    }

    public void setAltitude(double altitude) {
        mAltitude = altitude;
    }

    /**
     * Calculate distance between two points in latitude and longitude taking
     * into account height difference. If you are not interested in height
     * difference pass 0.0. Uses Haversine method as its base.
     *
     * lat1, lon1 Start point lat2, lon2 End point el1 Start altitude in meters
     * el2 End altitude in meters
     * @returns Distance in Meters
     */
    public double distanceTo(TrackLocation toLocation) {
        double distance = 0;
        if (toLocation == null) {
            // second location not provided - exit
            return distance;
        }
        // this point
        double lat1 = this.getLatitude();
        double lon1 = this.getLongitude();
        double el1 = this.getAltitude();

        // location to which we want to calc distance
        double lat2 = toLocation.getLatitude();
        double lon2 = toLocation.getLongitude();
        double el2 = toLocation.getAltitude();

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
