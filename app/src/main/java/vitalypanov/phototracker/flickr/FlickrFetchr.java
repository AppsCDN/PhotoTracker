package vitalypanov.phototracker.flickr;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import vitalypanov.phototracker.Settings;

/**
 * Created by Vitaly on 22.08.2017.
 *
 * Examples for getting photos are bellow
 * Recent function:
 * https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=4f721bbafa75bf6d2cb5af54f937bb70&format=json&nojsoncallback=1
 * Search function (by "cat" keyword):
 * https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=4f721bbafa75bf6d2cb5af54f937bb70&format=json&nojsoncallback=1&text=cat
 *
 * Api key is from link:
 * https://github.com/tkunstek/android-big-nerd-ranch/blob/master/PhotoGallery/src/com/bignerdranch/android/photogallery/FlickrFetchr.java
 * Thanks! :)
 */

public class FlickrFetchr {
    private static final String TAG = "FlickFetcher";
    private static final String API_KEY = "4f721bbafa75bf6d2cb5af54f937bb70";
    private static final String SEARCH_METHOD = "flickr.photos.search";
    private static final Uri END_POINT = Uri
            .parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key", API_KEY)
            .appendQueryParameter("format", "json")
            .appendQueryParameter("nojsoncallback", "1")
            .appendQueryParameter("extras", "url_s,geo")
            .build();

    /**
     * Download photo headers
     * ALL PAGES
     * @param minPoint
     * @param maxPoint
     * @return
     */
    public static List<FlickrPhoto> searchPhotos(LatLng minPoint, LatLng maxPoint, Context context){
        List<FlickrPhoto> resultItems = new ArrayList<>();
        try {
            // first page show always
            int page = 1;

            String url = buildUrl(minPoint, maxPoint, page);
            String jsonString = getUrlString(url);
            JSONObject jsonBody = new JSONObject(jsonString);
            JSONObject photosJsonObject =jsonBody.getJSONObject("photos");
            int pages = photosJsonObject.getInt("pages");
            List<FlickrPhoto> items = new ArrayList<>();
            parseItems(items, jsonBody);
            resultItems.addAll(items);

            // calculate number of photo pages which we will show to user:
            pages = 1 + (int)((pages-1)*((double)Settings.get(context).getFlickrPhotosPercent()) / 100.) ;

            for (page = 2; page <= pages; page++){
                url = buildUrl(minPoint, maxPoint, page);
                jsonString = getUrlString(url);
                jsonBody = new JSONObject(jsonString);
                items = new ArrayList<>();
                parseItems(items, jsonBody);
                resultItems.addAll(items);
            }
            Log.i(TAG,"");
        } catch (IOException ex){
            Log.e(TAG, "Failed to fetch items " + ex.toString());
        } catch (JSONException ex) {
            Log.e(TAG, "Failed to parse JSON " + ex.toString());
        }
        return resultItems;
    }

    /**
     * Load bytes from url
     * @param urlSpec
     * @return  Bitmap byte array
     * @throws IOException
     */
    public static byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage() + ": with" + urlSpec);
            }
            int bytesRead = 0;
            byte [] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) >0 ){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return out.toByteArray();

        } finally {
            connection.disconnect();
        }
    }

    /**
     * Get bytes from url
     * @param urlSpec
     * @return
     * @throws IOException
     */
    private static String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    /**
     * Build url for bbox search
     * @param minPoint      left upper conner
     * @param maxPoint    right bottom conner
     * @return
     */
    private static String buildUrl(LatLng minPoint, LatLng maxPoint, int page){
        Uri.Builder uriBuilder = END_POINT.buildUpon()
                .appendQueryParameter("method", SEARCH_METHOD)
                .appendQueryParameter("bbox", minPoint.longitude + "," + minPoint.latitude + "," + maxPoint.longitude + "," + maxPoint.latitude) // box in which we are searching
                .appendQueryParameter("extras", "geo, url_c, url_m, url_s") // photo sizes
                .appendQueryParameter("page", "" + page);                   // page number to return
        return uriBuilder.build().toString();
    }

    /**
     * Parse from json to list
     * @param items     Result  - list of photo items
     * @param jsonBody  Source  - json from flickr.com
     * @throws IOException
     * @throws JSONException
     */
    private static void parseItems(List<FlickrPhoto> items, JSONObject jsonBody) throws IOException, JSONException {
        Gson gson = new Gson(); // got gson parsing
        JSONObject photosJsonObject =jsonBody.getJSONObject("photos");
        JSONArray photosJsonArray =photosJsonObject.getJSONArray("photo");

        for (int i = 0; i< photosJsonArray.length(); i++){
            JSONObject photoJsonObject = photosJsonArray.getJSONObject(i);
            FlickrPhoto item = gson.fromJson(photoJsonObject.toString(), FlickrPhoto.class);
            items.add(item);
        }
    }
}