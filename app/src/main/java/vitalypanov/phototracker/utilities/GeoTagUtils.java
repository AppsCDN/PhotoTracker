package vitalypanov.phototracker.utilities;

import android.media.ExifInterface;

import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;

/**
 * Created by Vitaly on 25.03.2018.
 */

public class GeoTagUtils {
    /**
     * Forcelly geo tagging photo file within provided coordinates
     * @param filename      Photo which will be geo tagged
     * @param geoTag      Coordinates
     */
    static public boolean setGeoTag(File filename, LatLng geoTag) {
        if (geoTag != null) {
            try {
                ExifInterface exif = new ExifInterface(
                        filename.getAbsolutePath());

                double latitude = Math.abs(geoTag.latitude);
                double longitude = Math.abs(geoTag.longitude);

                int num1Lat = (int) Math.floor(latitude);
                int num2Lat = (int) Math.floor((latitude - num1Lat) * 60);
                double num3Lat = (latitude - ((double) num1Lat + ((double) num2Lat / 60))) * 3600000;

                int num1Lon = (int) Math.floor(longitude);
                int num2Lon = (int) Math.floor((longitude - num1Lon) * 60);
                double num3Lon = (longitude - ((double) num1Lon + ((double) num2Lon / 60))) * 3600000;

                String lat = num1Lat + "/1," + num2Lat + "/1," + num3Lat + "/1000";
                String lon = num1Lon + "/1," + num2Lon + "/1," + num3Lon + "/1000";

                if (geoTag.latitude > 0) {
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "N");
                } else {
                    exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, "S");
                }

                if (geoTag.longitude > 0) {
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "E");
                } else {
                    exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, "W");
                }

                exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, lat);
                exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, lon);

                exif.saveAttributes();

            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        } else {
            return false;
        }
        return true;
    }
}
