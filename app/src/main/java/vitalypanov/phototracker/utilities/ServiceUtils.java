package vitalypanov.phototracker.utilities;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Created by Vitaly on 26.02.2018.
 */

public class ServiceUtils {
    private static String LOG_TAG = ServiceUtils.class.getName();

    public static boolean isServiceRunning(Context context, Class<?> serviceClass){
        final ActivityManager activityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);

        for (RunningServiceInfo runningServiceInfo : services) {
            Log.d(LOG_TAG, String.format("Service:%s", runningServiceInfo.service.getClassName()));
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())){
                if(runningServiceInfo.foreground) {
                    return true;
                }
            }
        }
        return false;
    }
}
