package vitalypanov.phototracker.utilities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import vitalypanov.phototracker.R;
import vitalypanov.phototracker.Settings;
import vitalypanov.phototracker.flickr.FlickrPhoto;
import vitalypanov.phototracker.model.Track;
import vitalypanov.phototracker.model.TrackLocation;
import vitalypanov.phototracker.model.TrackPhoto;

/**
 * Created by Vitaly on 02.03.2018.
 */

public class GoogleMapUtils {
    private static final String TAG = "PhotoTracker";
    public final static int SCALE_SMALL_SIZE = 150;
    public final static int SCALE_SMALL_SAMPLE_SIZE = 100; // sample bitmap - small size
    public final static int SCALE_FLICKR_SMALL_SAMPLE_SIZE = 50; // sample bitmap for photos from flickr - muvh more small size
    public final static double MAP_SIZE_DEGREES = 0.03; // size of map in degrees when showing current gps location
    public final static LatLngBounds MAP_ZERO_BOUNDS = new LatLngBounds(new LatLng(0,0), new LatLng(0,0));

    // Google map controls:
    public static final String GOOGLEMAP_COMPASS = "GoogleMapCompass";                   // [4]
    public static final String GOOGLEMAP_TOOLBAR = "GoogleMapToolbar";                   // [3]
    public static final String GOOGLEMAP_ZOOMIN_BUTTON = "GoogleMapZoomInButton";        // [2]child[0]
    public static final String GOOGLEMAP_ZOOMOUT_BUTTON = "GoogleMapZoomOutButton";      // [2]child[1]
    public static final String GOOGLEMAP_MYLOCATION_BUTTON = "GoogleMapMyLocationButton";// [0]

    /**
     * Init google map controls:
     * My location button
     * Zoom in/out buttons
     * @param mapFragment
     */
    public static void initMapControls(final SupportMapFragment mapFragment){
        if (Utils.isNull(mapFragment)){
            return;
        }
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(true);
                googleMap.getUiSettings().setZoomControlsEnabled(true);
                // do moving some controls on google map - it possible only after UI will be ready - so use post method
                mapFragment.getView().post(new Runnable() {
                    @Override
                    public void run() {
                        // move zoom controls to bottom of my location control
                        if (!Utils.isNull(mapFragment) && !Utils.isNull(mapFragment.getView())){
                            View locationView = mapFragment.getView().findViewWithTag(GoogleMapUtils.GOOGLEMAP_MYLOCATION_BUTTON);
                            GoogleMapUtils.moveZoomControls(mapFragment.getView(), locationView.getLeft(), locationView.getHeight() + locationView.getTop() * 2, -1, -1, false, false);
                        }
                    }
                });
            }
        });
    }

    public static void shutdownMapControls(final SupportMapFragment mapFragment){
        if (Utils.isNull(mapFragment)){
            return;
        }
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(GoogleMap googleMap) {
                googleMap.setMyLocationEnabled(false);
            }
        });
    }

    /**
     Draw track data and track bitmaps on google map
     */
    public static void drawTrackOnGoogleMap(final GoogleMap googleMap, final Track track, final Context context, HashMap<String, Bitmap> bitmapHashMap){

        if (Utils.isNull(googleMap )|| Utils.isNull(track)){
            return;
        }

        // getting current gps track from service
        if (Utils.isNull(track.getTrackData()) || track.getTrackData().isEmpty()) {
            return;
        }

        googleMap.clear();

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
                    && !Settings.get(context).isMapPerformance()) {
                Bitmap bitmapFromFile = bitmapHashMap.get(trackPhoto.getPhotoFileName());
                if (!Utils.isNull(bitmapFromFile)){
                    bitmap = bitmapFromFile;
                }
            }

            BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(bitmap);
            MarkerOptions photoMarker = new MarkerOptions()
                    .position(new LatLng(trackLocation.getLatitude(), trackLocation.getLongitude()))
                    .icon(itemBitmap)
                    .snippet(trackPhoto.getPhotoFileName());
            googleMap.addMarker(photoMarker);

        }

        // prepare track data for showing on google map: convert to LatLng array
        List<LatLng> points = track.getTrackDataAsLatLng();
        // smooth track if need
        points = Settings.get(context).isMapSmoothTrack()? smoothTrack(points) : points;

        // add polyline to map
        PolylineOptions polylineOptions = new PolylineOptions().addAll(points);
        googleMap.addPolyline(polylineOptions);

        // Start and End point markers set...
        LatLng itemPoint = new LatLng(
                ListUtils.getFirst(points).latitude, ListUtils.getFirst(points).longitude);
        LatLng myPoint = new LatLng(
                ListUtils.getLast(points).latitude, ListUtils.getLast(points).longitude);
        MarkerOptions itemMarker = new MarkerOptions()
                .position(itemPoint);
        googleMap.addMarker(itemMarker);
        MarkerOptions myMarker = new MarkerOptions()
                .position(myPoint);
        googleMap.addMarker(myMarker);

        // Calculate view map bounds regarding max and min coordinates of track data
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

    /**
     * Make track data more snooth (by adding new points)
     * @param poly
     * @return
     */
    public static List<LatLng> smoothTrack(List<LatLng> poly) {

        if (poly.get(0).latitude != poly.get(poly.size()-1).latitude || poly.get(0).longitude != poly.get(poly.size()-1).longitude){
            poly.add(new LatLng(poly.get(0).latitude,poly.get(0).longitude));
        }
        else{
            poly.remove(poly.size()-1);
        }
        poly.add(0,new LatLng(poly.get(poly.size()-1).latitude,poly.get(poly.size()-1).longitude));
        poly.add(new LatLng(poly.get(1).latitude,poly.get(1).longitude));

        Double[] lats = new Double[poly.size()];
        Double[] lons = new Double[poly.size()];

        for (int i=0;i<poly.size();i++){
            lats[i] = poly.get(i).latitude;
            lons[i] = poly.get(i).longitude;
        }

        double ax, ay, bx, by, cx, cy, dx, dy, lat, lon;
        float t;
        int i;
        List<LatLng> points = new ArrayList<>();
        // For every point
        for (i = 2; i < lats.length - 2; i++) {
            for (t = 0; t < 1; t += 0.2) {
                ax = (-lats[i - 2] + 3 * lats[i - 1] - 3 * lats[i] + lats[i + 1]) / 6;
                ay = (-lons[i - 2] + 3 * lons[i - 1] - 3 * lons[i] + lons[i + 1]) / 6;
                bx = (lats[i - 2] - 2 * lats[i - 1] + lats[i]) / 2;
                by = (lons[i - 2] - 2 * lons[i - 1] + lons[i]) / 2;
                cx = (-lats[i - 2] + lats[i]) / 2;
                cy = (-lons[i - 2] + lons[i]) / 2;
                dx = (lats[i - 2] + 4 * lats[i - 1] + lats[i]) / 6;
                dy = (lons[i - 2] + 4 * lons[i - 1] + lons[i]) / 6;
                lat = ax * Math.pow(t + 0.1, 3) + bx * Math.pow(t + 0.1, 2) + cx * (t + 0.1) + dx;
                lon = ay * Math.pow(t + 0.1, 3) + by * Math.pow(t + 0.1, 2) + cy * (t + 0.1) + dy;
                points.add(new LatLng(lat, lon));
            }
        }
        return points;

    }

    public static ArrayList<Marker> addFlickrPhotosOnGoogleMap(final GoogleMap googleMap, final List<FlickrPhoto> flickrPhotos, final Context context){
        ArrayList<Marker> markers = new ArrayList<Marker>();
        if (Utils.isNull(googleMap)|| Utils.isNull(flickrPhotos)){
            return null;
        }

        Bitmap bitmapDefault = BitmapFactory.decodeResource(context.getResources(), R.drawable.picture_map);
        bitmapDefault =BitmapUtils.scaleToFitHeight(bitmapDefault, GoogleMapUtils.SCALE_FLICKR_SMALL_SAMPLE_SIZE);
        BitmapDescriptor itemBitmap = BitmapDescriptorFactory.fromBitmap(bitmapDefault);
        for (FlickrPhoto flickrPhoto:  flickrPhotos){
            MarkerOptions photoMarker = new MarkerOptions()
                    .position(new LatLng(flickrPhoto.getLatitude(), flickrPhoto.getLongitude()))
                    .icon(itemBitmap)
                    .snippet(flickrPhoto.getUrl());
            markers.add(googleMap.addMarker(photoMarker));
        }
        return markers;

    }

    public static void moveZoomControls(View mapView, int left, int top, int right, int bottom, boolean horizontal, boolean vertical) {

        assert mapView != null;

        View zoomIn = mapView.findViewWithTag(GOOGLEMAP_ZOOMIN_BUTTON);

        // we need the parent view of the zoomin/zoomout buttons - it didn't have a tag
        // so we must get the parent reference of one of the zoom buttons
        View zoomInOut = (View) zoomIn.getParent();

        if (zoomInOut != null) {
            moveView(zoomInOut,left,top,right,bottom,horizontal,vertical);
        }
    }

    /**
     * Move the View according to the passed params.  A -1 means to skip that one.
     *
     * NOTE:  this expects the view to be inside a RelativeLayout.
     *
     * @param view - a valid view
     * @param left - the distance from the left side
     * @param top - the distance from the top
     * @param right - the distance from the right side
     * @param bottom - the distance from the bottom
     * @param horizontal - boolean, center horizontally if true
     * @param vertical - boolean, center vertically if true
     */
    private static void moveView(View view, int left, int top, int right, int bottom, boolean horizontal, boolean vertical) {
        try {
            assert view != null;

            // replace existing layout params
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT);

            if (left >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            }

            if (top >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
            }

            if (right >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_END, RelativeLayout.TRUE);
                rlp.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            }

            if (bottom >= 0) {
                rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            }

            if (horizontal) {
                rlp.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
            }

            if (vertical) {
                rlp.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            }

            rlp.setMargins(left, top, right, bottom);

            view.setLayoutParams(rlp);
        } catch (Exception ex) {
            Log.e(TAG, "moveView() - failed: " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }
    }
}
