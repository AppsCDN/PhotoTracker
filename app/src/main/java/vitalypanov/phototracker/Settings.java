package vitalypanov.phototracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import vitalypanov.phototracker.utilities.Utils;

/**
 * Singleton for progrma setting
 * Created by Vitaly on 07.03.2018.
 */

public class Settings {
    public static final String KEY_MAP_PERFOMANCE_SWITCH = "map_performance_switch";
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

    public boolean getBoolean(String sKey){
        SharedPreferences sharedPref =
                PreferenceManager.getDefaultSharedPreferences(mContext);
        Boolean value = sharedPref.getBoolean
                (sKey, false);
        return value;
    }
}
