package vitalypanov.phototracker.utilities;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.ImageWriteException;
import org.apache.sanselan.formats.jpeg.exifRewrite.ExifRewriter;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TagInfo;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;
import org.apache.sanselan.formats.tiff.constants.TiffDirectoryConstants;
import org.apache.sanselan.formats.tiff.constants.TiffFieldTypeConstants;
import org.apache.sanselan.formats.tiff.write.TiffOutputDirectory;
import org.apache.sanselan.formats.tiff.write.TiffOutputField;
import org.apache.sanselan.formats.tiff.write.TiffOutputSet;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import vitalypanov.phototracker.model.TrackLocation;

/**
 * Created by Vitaly on 25.03.2018.
 */

public class GeoTagUtils {
    /**
     * Forcelly geo tagging photo file within provided coordinates
     * @param filename      Photo which will be geo tagged
     * @param geoTag      Coordinates
     */
    static public boolean setGeoTag(File filename, TrackLocation geoTag) {
        if (Utils.isNull(geoTag)) {
            return false;
        }
        TiffOutputSet exif = getSanselanOutputSet(filename, TiffConstants.DEFAULT_TIFF_BYTE_ORDER);
        if (!Utils.isNull(exif)){
            try {
                //writeExifInformation(exif);

                writeExifLocation(exif, geoTag);

                // save the exif back into the image
                saveExifToFile(filename, exif);
            } catch (IOException | ImageWriteException | ImageReadException e) {
                //FulcrumLogger.log(e);
            }
        }

        return true;
    }

    // ------------------------------------------------------------------------
    // Private Methods
    // ------------------------------------------------------------------------

    private static TiffOutputSet getSanselanOutputSet(File jpegImageFile, int defaultByteOrder) {
        try {
            TiffImageMetadata metadata = EXIFUtils.getImageMetadata(jpegImageFile);
            TiffOutputSet outputSet = metadata == null ? null : metadata.getOutputSet();

            // If JPEG file contains no EXIF metadata, create an empty set
            // of EXIF metadata. Otherwise, use existing EXIF metadata to
            // keep all other existing tags
            return outputSet == null ?
                    new TiffOutputSet(metadata == null ? defaultByteOrder : metadata.contents.header.byteOrder) :
                    outputSet;
        }
        catch ( IOException | ImageWriteException | ImageReadException e ) {

            return null;
        }
    }

    private static void writeExifLocation(TiffOutputSet exif, TrackLocation location) {
        // if the exif already has location or we have no location to set, exit
        if ( location == null ) {
            return;
        }

        try {
            TiffOutputDirectory gps = exif.getOrCreateGPSDirectory();

            boolean hasLocation = gps.findField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF) != null &&
                    gps.findField(TiffConstants.GPS_TAG_GPS_LATITUDE) != null &&
                    gps.findField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF) != null &&
                    gps.findField(TiffConstants.GPS_TAG_GPS_LONGITUDE) != null;

            TiffOutputField field;

            if ( !hasLocation ) {
                double longitude = location.getLongitude();
                double latitude = location.getLatitude();

                String longitudeRef = longitude < 0.0 ? "W" : "E";
                longitude = Math.abs(longitude);

                String latitudeRef = latitude < 0.0 ? "S" : "N";
                latitude = Math.abs(latitude);

                // add longitude ref
                field = TiffOutputField.create(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF, exif.byteOrder, longitudeRef);
                gps.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE_REF);
                gps.add(field);

                // add latitude ref
                field = TiffOutputField.create(TiffConstants.GPS_TAG_GPS_LATITUDE_REF, exif.byteOrder, latitudeRef);
                gps.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE_REF);
                gps.add(field);

                field = TiffOutputField.create(TiffConstants.GPS_TAG_GPS_LONGITUDE, exif.byteOrder, toDMS(longitude));
                gps.removeField(TiffConstants.GPS_TAG_GPS_LONGITUDE);
                gps.add(field);

                field = TiffOutputField.create(TiffConstants.GPS_TAG_GPS_LATITUDE, exif.byteOrder, toDMS(latitude));
                gps.removeField(TiffConstants.GPS_TAG_GPS_LATITUDE);
                gps.add(field);
            }

            if ( !Utils.isNull(location.getAltitude())) {
                double altitude = location.getAltitude();

                int altitudeRef = altitude < 0.0 ?
                        TiffConstants.GPS_TAG_GPS_ALTITUDE_REF_VALUE_BELOW_SEA_LEVEL :
                        TiffConstants.GPS_TAG_GPS_ALTITUDE_REF_VALUE_ABOVE_SEA_LEVEL;

                altitude = Math.abs(altitude);

                TagInfo altitudeRefTag = new TagInfo("GPS Altitude Ref", 5,
                        TiffFieldTypeConstants.FIELD_TYPE_DESCRIPTION_BYTE, 1,
                        TiffDirectoryConstants.EXIF_DIRECTORY_GPS);

                // add altitude ref
                field = TiffOutputField.create(altitudeRefTag, exif.byteOrder, (byte) altitudeRef);
                gps.removeField(altitudeRefTag);
                gps.add(field);

                // add altitude
                // the altitude tag is defined incorrectly in sanselan, it should have length 1, not -1
                TagInfo altitudeTag = new TagInfo("GPS Altitude", 6,
                        TiffFieldTypeConstants.FIELD_TYPE_DESCRIPTION_RATIONAL, 1,
                        TiffDirectoryConstants.EXIF_DIRECTORY_GPS);

                field = TiffOutputField.create(altitudeTag, exif.byteOrder, new Double[] {
                        altitude
                });
                gps.removeField(altitudeTag);
                gps.add(field);
            }

            /*
            if ( location.hasAccuracy() ) {
                double accuracy = location.getAccuracy();

                // sanselan doesn't define the H Positioning Error tag, so we manually define it at offset 31
                // http://www.sno.phy.queensu.ca/~phil/exiftool/TagNames/GPS.html
                // iOS writes to this field and it has some level of standardization
                TagInfo accuracyTag = new TagInfo("GPS H Positioning Error", 31,
                        TiffFieldTypeConstants.FIELD_TYPE_DESCRIPTION_RATIONAL, 1,
                        TiffDirectoryConstants.EXIF_DIRECTORY_GPS);

                // add accuracy
                field = TiffOutputField.create(accuracyTag, exif.byteOrder, new Double[] {
                        accuracy
                });
                gps.removeField(accuracyTag);
                gps.add(field);

                // add accuracy to the DOP field too
                // the GPS DOP tag is defined incorrectly in sanselan, it should have length 1, not -1
                TagInfo dopTag = new TagInfo("GPS DOP", 11,
                        TiffFieldTypeConstants.FIELD_TYPE_DESCRIPTION_RATIONAL, 1,
                        TiffDirectoryConstants.EXIF_DIRECTORY_GPS);

                field = TiffOutputField.create(dopTag, exif.byteOrder, new Double[] { accuracy });
                gps.removeField(dopTag);
                gps.add(field);
            }
            */
        }
        catch ( ImageWriteException e ) {
            //FulcrumLogger.log(e);
        }
    }

    private static Double[] toDMS(double input) {
        double degrees, minutes, seconds, remainder;

        degrees = (double) ((long) input);

        remainder = input % 1.0;
        remainder *= 60.0;

        minutes = (double) ((long) remainder);

        remainder %= 1.0;

        seconds = remainder * 60.0;

        return new Double[] {
                degrees, minutes, seconds
        };
    }

    private static void saveExifToFile(File imageFile, TiffOutputSet exif)
            throws IOException, ImageWriteException, ImageReadException {
        String tempFileName = imageFile.getAbsolutePath() + ".tmp";
        File tempFile = new File(tempFileName);

        BufferedOutputStream tempStream = new BufferedOutputStream(new FileOutputStream(tempFile));
        new ExifRewriter().updateExifMetadataLossless(imageFile, tempStream, exif);
        tempStream.close();

        if ( imageFile.delete() ) {
            tempFile.renameTo(imageFile);
        }
    }

}
