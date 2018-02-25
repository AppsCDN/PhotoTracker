package vitalypanov.phototracker.model;

/**
 * Custom Location class for storing longitude and latitude.
 *
 * p.s.
 * It made because of application was somtimes crashed,  when I trying to process standard Location objects with Gson
 *  I get following error: A/libc: Fatal signal 11 (SIGSEGV), code 128, fault addr 0x0 in tid 26221 (FinalizerDaemon)
 *
 * Created by Vitaly on 25.02.2018.
 */

public class TrackLocation {

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
}
