package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;
import java.util.List;

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.Settings;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.model.TrackPhoto;

/**
 * Created by Vitaly on 02.03.2018.
 */

public class GoogleMapUtils {
    private static final String TAG = "PhotoTracker";
    public final static int SCALE_SMALL_SIZE = 150;
    public final static int SCALE_SMALL_SAMPLE_SIZE = 100; // for sample bitmap - small size
    public final static double MAP_SIZE_DEGREES = 0.03; // size of map in degrees when showing current gps location

    /**
     Draw track data and track bitmaps on google map
     */
    public static void drawTrackOnGoogleMap(final GoogleMap googleMap, final Track track, final Context context, HashMap<String, Bitmap> bitmapHashMap){
        if (Utils.isNull(googleMap )|| Utils.isNull(track)){
            return;
        }

        // getting current gps track from service
        List<TrackLocation> trackData = track.getTrackData();
        if (trackData == null || trackData.isEmpty()) {
            return;
        }
        LatLng itemPoint = new LatLng(
                ListUtils.getFirst(trackData).getLatitude(), ListUtils.getFirst(trackData).getLongitude());
        LatLng myPoint = new LatLng(
                ListUtils.getLast(trackData).getLatitude(), ListUtils.getLast(trackData).getLongitude());

        googleMap.clear();

        // start point marker
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint);
        googleMap.addMarker(itemMarker);

        // end(current) point marker
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        googleMap.addMarker(myMarker);

        Bitmap bitmapDefault = BitmapFactory.decodeResource(context.getResources(), R.drawable.picture_map);
        bitmapDefault =BitmapUtils.scaleToFitHeight(bitmapDefault, GoogleMapUtils.SCALE_SMALL_SAMPLE_SIZE);

        // all bitmap markers
        for (TrackPhoto trackPhoto :  track.getPhotoFiles()){

            TrackLocation trackLocation = trackPhoto.getTrackLocation();
            if (trackLocation == null){
                // Not possible situation!
                // At the moment of taking photo at least one location should be defined.
                Log.e(TAG, "trackBitmap.getTrackPhoto().getTrackLocation() not defined!");
            }

            Bitmap bitmap = bitmapDefault;

            if (!Utils.isNull(bitmapHashMap)
                    && !Settings.get(context).getBoolean(Settings.KEY_MAP_PERFOMANCE_SWITCH)) {
                bitmap = bitmapHashMap.get(trackPhoto.getPhotoFileName());
            }

            BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
            MarkerOptions photoMarker = new MarkerOptions()
                    .position(new LatLng(trackLocation.getLatitude(), trackLocation.getLongitude()))
                    .icon(itemBitmap)
                    .snippet(trackPhoto.getPhotoFileName());
            googleMap.addMarker(photoMarker);

        }

        PolylineOptions lines = new PolylineOptions();
        for(TrackLocation loc : trackData){
            lines.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
        }
        googleMap.addPolyline(lines);

        TrackLocation minLocation = track.getMinTrackLocation();
        TrackLocation maxTrackLocation = track.getMaxTrackLocation();
        LatLng minPoint = new LatLng(minLocation.getLatitude(), minLocation.getLongitude());
        LatLng maxPoint = new LatLng(maxTrackLocation.getLatitude(), maxTrackLocation.getLongitude());
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(minPoint)
                .include(maxPoint)
                .build();
        int margin = context.getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, margin));

        // other variants:

        /*
        // 1. This is crashed variant:
        final CameraUpdate update = CameraUpdateFactory.newLatLngBounds(bounds, margin);
        googleMap.moveCamera(update);
        */

        /*
        // 2. This is work variant but show world wide map first, and only then move camera:
        googleMap.setOnMapLoadedCallback(
                new GoogleMap.OnMapLoadedCallback() {
                    @Override
                    public void onMapLoaded() {
                        //mMap.animateCamera(update);
                        googleMap.moveCamera(update);
                    }
                });
        */
    }

    public static void drawLocationOnGoogleMap(final GoogleMap googleMap, final Location location, final Context context){
        if (Utils.isNull(googleMap)|| Utils.isNull(location)){
            return;
        }

        LatLng itemPoint = new LatLng(
                location.getLatitude(), location.getLongitude());

        googleMap.clear();

        // start point marker
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint);
        googleMap.addMarker(itemMarker);

        LatLng minPoint = new LatLng(location.getLatitude() - MAP_SIZE_DEGREES/2, location.getLongitude() - MAP_SIZE_DEGREES/2);
        LatLng maxPoint = new LatLng(location.getLatitude() + MAP_SIZE_DEGREES/2, location.getLongitude() + MAP_SIZE_DEGREES/2);
        LatLngBounds bounds = new LatLngBounds.Builder()
                .include(minPoint)
                .include(maxPoint)
                .build();
        int margin = context.getResources().getDimensionPixelSize(R.dimen.map_inset_margin);
        int width = context.getResources().getDisplayMetrics().widthPixels;
        int height = context.getResources().getDisplayMetrics().heightPixels;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, width, height, margin));

    }
}
