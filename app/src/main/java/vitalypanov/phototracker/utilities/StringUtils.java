package vitalypanov.phototracker.utilities;

/**
 * Created by Vitaly on 06.03.2018.
 */

public class StringUtils {
    public static boolean isNullOrBlank(String s)
    {
        return (s==null || s.trim().equals(""));
    }
    public static String noNullStr(String s) { return ("null".equals(s)) ? null : s; }
}
