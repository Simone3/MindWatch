package it.polimi.aui.auiapp.utils;

import android.content.Context;
import android.text.TextUtils;

import java.text.DateFormatSymbols;
import java.util.Arrays;
import java.util.Random;
import java.util.Set;

import it.polimi.aui.auiapp.R;

/**
 * Helpers used throughout the application
 */
public class Commons
{
    public static final int MILLISECONDS_IN_HOUR = 3600000;

    /**
     * Hour+minutes to string
     * @param hours hours (0-23)
     * @param minutes minutes (0-59)
     * @return the encoded time
     */
    public static String encodeTime(int hours, int minutes)
    {
        return (hours<10 ? "0"+hours : hours)+":"+(minutes<10 ? "0"+minutes : minutes);
    }

    /**
     * String to hour+minutes
     * @param time string representing time (HH:MM)
     * @return the decoded time (0 => hour, 1 => minutes)
     */
    public static int[] decodeTime(String time)
    {
        String[] values = time.split(":");

        if(values.length==2) return new int[]{Integer.parseInt(values[0]), Integer.parseInt(values[1])};
        else return new int[]{0, 0};
    }

    /**
     * Given a set of days, orders them in an array
     * @param daysSet the unordered game names
     * @return ordered array of days
     */
    public static String[] orderDaysFromSet(Set<String> daysSet)
    {
        String[] daysArray = new String[daysSet.size()];
        int i = 0;
        for(String day: getAllDaysArray())
        {
            if(daysSet.contains(day))
            {
                daysArray[i] = day;
                i++;
            }
        }
        return daysArray;
    }

    /**
     * Given a list of days, returns a comma-separated string
     * @param context activity context
     * @param days array of day names
     * @return comma-separated string
     */
    public static String weekdaysArrayToString(Context context, String[] days)
    {
        if(days==null || days.length==0) return context.getString(R.string.days_none);
        else if(days.length==7) return context.getString(R.string.days_all);
        else
        {
            return TextUtils.join(", ", days);
        }
    }

    /**
     * Time interval to string
     * @param context application context
     * @param from starting hour
     * @param to ending hour
     * @return string that describes the time interval
     */
    public static String hoursToString(Context context, int from, int to)
    {
        return context.getString(R.string.hours_interval, from, to);
    }

    /**
     * Random integer between the two given values
     * @param min minimum value (included)
     * @param max maximum value (included)
     * @return random integer
     */
    public static int randInt(int min, int max)
    {
        return (new Random()).nextInt((max - min) + 1) + min;
    }

    /**
     * Returns the array of all weekdays names
     * @return array of all weekdays names
     */
    public static String[] getAllDaysArray()
    {
        String[] daysWithEmptyFirstPosition = (new DateFormatSymbols()).getShortWeekdays();
        return Arrays.copyOfRange(daysWithEmptyFirstPosition, 1, daysWithEmptyFirstPosition.length);
    }
}
