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
    public static final String KEY_MAP_PERFOMANCE_SWITCH = "map_performance_switch";
    public static final String KEY_MAP_RUNKEEPER_ACCESS_TOKEN = "runkeeper_access_token";
    private static Settings mSettings;
    private Context mContext;

    public static Settings get(Context context) {
        if (Utils.isNull(mSettings)) {
            mSettings = new Settings(context);
        }
        return mSettings;
    }

    private Settings(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Get boolean value preference
     * @param sKey
     * @return
     */
    public boolean getBoolean(String sKey){
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean value = sharedPref.getBoolean(sKey, false);
        return value;
    }

    /**
     * Get string value preference
     * @param sKey
     * @return
     */
    public String getString(String sKey){
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
    public void setString(String sKey, String sValue){
        SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor1 = settings.edit();
        editor1.putString(sKey, sValue);
        editor1.commit();
    }
}