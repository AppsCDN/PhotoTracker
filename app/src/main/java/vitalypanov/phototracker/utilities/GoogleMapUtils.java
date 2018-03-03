package vitalypanov.phototracker.utilities;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackBitmap;
import vitalypanov.phototracker.model.TrackLocation;

/**
 * Created by Vitaly on 02.03.2018.
 */

public class GoogleMapUtils {
    private static final String TAG = "PhotoTracker";

    /**
     Draw on google map
     */
    public static void updateGoogleMapUI(final GoogleMap googleMap, final Track track, final Context context){
        if (googleMap == null || track == null){
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

        // all bitmap markers
        for (TrackBitmap trackBitmap :  track.getCashedBitmaps()){
            TrackLocation trackLocation = trackBitmap.getTrackPhoto().getTrackLocation();
            if (trackLocation == null){
                // Not possible situation!
                // At the moment of taking photo at least one location should be defined.
                Log.e(TAG, "trackBitmap.getTrackPhoto().getTrackLocation() not defined!");
            }
            if (trackBitmap.getBitmap()!= null) {
                BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(trackBitmap.getBitmap());
                MarkerOptions photoMarker = new MarkerOptions()
                        .position(new LatLng(trackLocation.getLatitude(), trackLocation.getLongitude()))
                        .icon(itemBitmap)
                        .snippet(trackBitmap.getTrackPhoto().getPhotoFileName());
                googleMap.addMarker(photoMarker);
            }
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
}
