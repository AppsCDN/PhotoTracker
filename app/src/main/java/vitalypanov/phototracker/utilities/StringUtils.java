package vitalypanov.phototracker.utilities;

import android.util.Patterns;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Vitaly on 06.03.2018.
 */

public class StringUtils {

    public static boolean isNullOrBlank(String s)
    {
        return (s==null || s.trim().equals(""));
    }

    public static String noNullStr(String s) { return ("null".equals(s)) ? null : s; }

    public static boolean isValidUrl(String url) {
        Pattern p = Patterns.WEB_URL;
        Matcher m = p.matcher(url.toLowerCase());
        return m.matches();
    }

    public static String coalesce(String ...items) {
        for(String i : items) if(!isNullOrBlank(i)) return i;
        return null;
    }
}
