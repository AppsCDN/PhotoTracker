package vitalypanov.phototracker.flickr;

import android.net.Uri;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

import vitalypanov.phototracker.model.BasePhoto;
import vitalypanov.phototracker.utilities.StringUtils;

/**
 * Created by Vitaly on 22.08.2017.
 */

public class FlickrPhoto implements Serializable, BasePhoto {
    @SerializedName("title")
    private String mCaption;

    @SerializedName("id")
    private String mId;

    @SerializedName("url_c")
    private String mUrl_c;

    @SerializedName("url_m")
    private String mUrl_m;

    @SerializedName("url_s")
    private String mUrl_s;

    @SerializedName("owner")
    private String mOwner;

    @SerializedName("latitude")
    private double mLatitude;

    @SerializedName("longitude")
    private double mLongitude;

    public double getLatitude() {
        return mLatitude;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    @Override
    public String toString() {
        return mCaption;
    }

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }

    public String getId() {
        return mId;
    }

    public void setId(String mId) {
        this.mId = mId;
    }

    public String getUrl() {
        return StringUtils.coalesce(mUrl_c, mUrl_m, mUrl_s);
    }

    /*public void setUrl(String mUrl) {
        this.mUrl = mUrl;
    }
    */

    public String getOwner() {
        return mOwner;
    }

    public void setOwner(String owner) {
        mOwner = owner;
    }

    public Uri getPhotoPageUri(){
        return Uri.parse("http://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(mOwner)
                .appendPath(mId)
                .build();
    }

    @Override
    public String getName() {
        return getUrl();
    }
}
