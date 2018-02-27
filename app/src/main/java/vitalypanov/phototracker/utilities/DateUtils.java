package vitalypanov.phototracker.utilities;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Vitaly on 26.02.2018.
 */

public class DateUtils {
    // Format constants
    private static final String LEAD_ZEROS_TIME_FORMAT = "%02d";

    /**
     * Start time formatted
     * @return
     */
    public static String getTimeFormatted(Date dDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dDate);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        int seconds = calendar.get(Calendar.SECOND);
        return (String.format(LEAD_ZEROS_TIME_FORMAT, hours) + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, minutes)  + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, seconds));
    }

    /**
     * Start time formatted short
     * Only hours and minutes
     * @return
     */
    public static String getShortTimeFormatted(Date dDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(dDate);
        int hours = calendar.get(Calendar.HOUR_OF_DAY);
        int minutes = calendar.get(Calendar.MINUTE);
        return (String.format(LEAD_ZEROS_TIME_FORMAT, hours) + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, minutes));
    }

    public static String getDurationFormatted (long mills){
        int hours = (int) (mills/(1000 * 60 * 60));
        int minutes = (int) (mills/(1000*60)) % 60;
        long seconds = (int) (mills / 1000) % 60;
        return (String.format(LEAD_ZEROS_TIME_FORMAT, hours) + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, minutes)  + ":" +
                String.format(LEAD_ZEROS_TIME_FORMAT, seconds));
    }

    /**
     * Start time formatted
     * @return
     */
    public static String getDateFormatted(Date dDate) {
        Format formatter = new SimpleDateFormat("dd MMMM yyyy");
        return formatter.format(dDate);
    }
}
