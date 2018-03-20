package vitalypanov.phototracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import vitalypanov.phototracker.utilities.Utils;

/**
 * Singleton for program settings
 * Created by Vitaly on 07.03.2018.
 */

public class Settings {
    private static final String KEY_MAP_PERFOMANCE_SWITCH = "map_performance_switch";
    private static final String KEY_MAP_FLICKR_PHOTOS_SWITCH = "flickr_switch";
    private static final String KEY_MAP_RUNKEEPER_ACCESS_TOKEN = "runkeeper_access_token";
    private static Settings mSettings;
    private Context mContext;

    public static Settings get(Context context) {
        if (Utils.isNull(mSettings)) {
            mSettings = new Settings(context);
        }
        return mSettings;
    }

    public boolean isMapPerformance(){
        Boolean defaultVale = mContext.getResources().getBoolean(R.bool.map_perfromance_default_false);
        return getBoolean(Settings.KEY_MAP_PERFOMANCE_SWITCH, defaultVale);
    }

    public boolean isFlickrPhotos(){
        Boolean defaultVale = mContext.getResources().getBoolean(R.bool.flickr_photos_default_true);
        return getBoolean(Settings.KEY_MAP_FLICKR_PHOTOS_SWITCH, defaultVale);
    }

    public String getRunkeeperAccessToken(){
        return getString(Settings.KEY_MAP_RUNKEEPER_ACCESS_TOKEN);
    }

    public void setRunkeeperAccessToken(String accessToken){
        setString(Settings.KEY_MAP_RUNKEEPER_ACCESS_TOKEN, accessToken);
    }

    private Settings(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Get boolean value preference
     * @param sKey
     * @return
     */
    private boolean getBoolean(String sKey, boolean defaultValue){
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean value = sharedPref.getBoolean(sKey, defaultValue);
        return value;
    }

    /**
     * Get string value preference
     * @param sKey
     * @return
     */
    private String getString(String sKey){
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        String value = sharedPref.getString(sKey, "");
        return value;
    }

    /**
     * Change string pref value
     * @param sKey
     * @param sValue
     */
    private void setString(String sKey, String sValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor1 = settings.edit();
        editor1.putString(sKey, sValue);
        editor1.commit();
    }
}