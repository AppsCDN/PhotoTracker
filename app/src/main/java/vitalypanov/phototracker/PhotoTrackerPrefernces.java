package vitalypanov.phototracker;

import android.content.Context;
import android.preference.PreferenceManager;

/**
 * Created by Vitaly on 23.08.2017.
 */

public class PhotoTrackerPrefernces {
    private static final String PREF_IS_ALARM_ON = "isAlarmOn";

    public static boolean isAlarmOn(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(PREF_IS_ALARM_ON, false);
    }

    public static void setAlarmOn(Context context, boolean isOn){
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(PREF_IS_ALARM_ON, isOn)
                .apply();
    }
}
