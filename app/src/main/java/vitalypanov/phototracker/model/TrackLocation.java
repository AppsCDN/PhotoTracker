package vitalypanov.phototracker.model;

import java.io.Serializable;

/**
 * Custom Location class for storing longitude and latitude.
 *
 * p.s.
 * It made because of application was somtimes crashed,  when I trying to process standard Location objects with Gson
 *  I get following error: A/libc: Fatal signal 11 (SIGSEGV), code 128, fault addr 0x0 in tid 26221 (FinalizerDaemon)
 *
 * Created by Vitaly on 25.02.2018.
 */

public class TrackLocation implements Serializable{

    private double mLongitude;
    private double mLatitude;

    public TrackLocation() {
    }

    public TrackLocation(double mLongitude, double mLatitude) {
        this.mLongitude = mLongitude;
        this.mLatitude = mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double mLongitude) {
        this.mLongitude = mLongitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double mLatitude) {
        this.mLatitude = mLatitude;
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
        double lat1 = this.getLatitude();
        double lon1 = this.getLongitude();
        double lat2 = toLocation.getLatitude();
        double lon2 = toLocation.getLongitude();
        // Altitudes not using
        double el1 = 0; // altitude of this location point
        double el2 = 0; // altitude of toLocation point

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
