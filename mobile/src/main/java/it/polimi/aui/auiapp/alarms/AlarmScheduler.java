package it.polimi.aui.auiapp.alarms;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Calendar;
import java.util.Date;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.utils.Commons;
import it.polimi.aui.auiapp.utils.SettingsManager;

/**
 * Manages the creation of the application's alarms:
 * - repeating midnight alarm that sets up the day's random alarms for the games
 * - game alarm that makes the phone send a game to the wearable
 */
public class AlarmScheduler
{
    private static final int MIN_AVERAGE_INTERVAL_BETWEEN_GAMES_MS = Commons.MILLISECONDS_IN_HOUR/2;
    private static AlarmScheduler instance = null;

    private AlarmScheduler() {}

    /**
     * Singleton pattern
     */
    public synchronized static AlarmScheduler getInstance()
    {
        if(instance==null) instance = new AlarmScheduler();
        return instance;
    }

    /**
     * Helper to setup a single (non-repeating) alarm
     * @param context the activity context
     * @param date the date for the alarm to schedule
     * @param action the action name (the one used by AlarmReceiver to understand the alarm type)
     * @param uniqueName the alarm unique name
     */
    private void scheduleSingleAlarm(Context context, Date date, String action, String uniqueName)
    {
        // Get alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Build intent for the AlarmReceiver
        PendingIntent pendingIntent = buildPendingIntentForAlarms(context, action, uniqueName);

        // Schedule the alarm at the given date
        alarmManager.set(AlarmManager.RTC_WAKEUP, date.getTime(), pendingIntent);
    }

    /**
     * Helper to setup a repeating alarm
     * @param context the activity context
     * @param date the date for the alarm to schedule
     * @param intervalMilliseconds the interval in milliseconds between two calls of the alarm
     * @param action the action name (the one used by AlarmReceiver to understand the alarm type)
     * @param uniqueName the alarm unique name
     */
    private void scheduleRepeatingAlarm(Context context, Date date, long intervalMilliseconds, String action, String uniqueName)
    {
        // Get alarm manager
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        // Build intent for the AlarmReceiver
        PendingIntent pendingIntent = buildPendingIntentForAlarms(context, action, uniqueName);

        // Schedule the alarm at the given date
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, date.getTime(), intervalMilliseconds, pendingIntent);
    }

    /**
     * Helper to build an alarm pending intent
     */
    private PendingIntent buildPendingIntentForAlarms(Context context, String action, String uniqueName)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setType(uniqueName);
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    /**
     * Creates today game alarms at random times, based on the user settings
     * @param context the activity context
     * @param midnight true if it's called by the midnight alarm, false if not (in the latter case it considers the number of games that were already shown today)
     */
    void setupTodayAlarms(Context context, boolean midnight)
    {
        // Get current time
        Date nowDate = new Date();
        Calendar now = Calendar.getInstance();
        now.setTime(nowDate);

        // Get settings manager
        SettingsManager settingsManager = SettingsManager.getInstance(context);

        // Manage completed games count
        int currentGamesToday;
        if(midnight)
        {
            currentGamesToday = 0;
            settingsManager.resetDailyCounters();
        }
        else
        {
            currentGamesToday = settingsManager.getGamesShownToday();
        }

        // Need to schedule alarms only if today is a selected day
        if(settingsManager.isTodayAGameDay())
        {
            // Get settings needed to schedule the alarms
            int gamesPerDay = settingsManager.getGamesPerDay();
            int startHour = settingsManager.getStartHour();
            if(!midnight && now.get(Calendar.HOUR_OF_DAY)>=startHour) startHour = now.get(Calendar.HOUR_OF_DAY)+1;
            int endHour = settingsManager.getEndHour();

            // Stop here if we are past the ending hour or there's some problem with the hours
            if(now.get(Calendar.HOUR_OF_DAY)>=endHour || startHour>=endHour) return;

            // Get the random times in which to schedule the alarms
            int alarmsToSchedule = gamesPerDay - currentGamesToday;
            Date[] alarmDates = this.computeAlarmTimes(alarmsToSchedule, startHour, endHour);

            // Update the scheduled games count
            settingsManager.setGamesScheduledTodayNumber(alarmDates.length);

            // Schedule every alarm
            for(int i=0; i<alarmDates.length; i++)
            {
                scheduleSingleAlarm(context, alarmDates[i], context.getString(R.string.notification_action), context.getString(R.string.alarm_unique_name_prefix) + i);
            }
        }

        // Otherwise simply set the number of scheduled games to 0
        else settingsManager.setGamesScheduledTodayNumber(0);
    }

    /**
     * Helper that computes the random times for the game alarms
     * @param alarmsToSchedule number of alarms to schedule
     * @param startHour hour of the day that starts the interval in which to schedule the alarms
     * @param endHour hour of the day that ends the interval in which to schedule the alarms
     * @return array of dates for the alarms; note that size may be less than "alarmsToSchedule", if there are too many alarms to schedule in a small time period
     */
    private Date[] computeAlarmTimes(int alarmsToSchedule, int startHour, int endHour)
    {
        // Return immediately if no alarms to schedule
        if(alarmsToSchedule<=0) return new Date[0];

        // Initialize result array
        Date[] result = new Date[alarmsToSchedule];

        // Get milliseconds since Epoch
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        long midnightMilliseconds = calendar.getTimeInMillis();

        // Cast values to float to avoid problems with divisions
        float alarmsToScheduleFloat = (float) alarmsToSchedule;
        float startHourFloat = (float) startHour;
        float endHourFloat = (float) endHour;

        // Compute the average number of milliseconds between two sequential alarms
        int rateMilliseconds = Math.round((endHourFloat - startHourFloat)/alarmsToScheduleFloat * Commons.MILLISECONDS_IN_HOUR);

        // If this interval is too small for some reason, let's decrease the number of games
        if(rateMilliseconds<MIN_AVERAGE_INTERVAL_BETWEEN_GAMES_MS)
        {
            int newAlarmsToSchedule = Math.round((endHourFloat-startHourFloat)/MIN_AVERAGE_INTERVAL_BETWEEN_GAMES_MS*Commons.MILLISECONDS_IN_HOUR)-1;
            return computeAlarmTimes(newAlarmsToSchedule, startHour, endHour);
        }

        // For each position of the array set a random date in the correct interval
        int bound = Math.round(0.4f * rateMilliseconds);
        long timeMilliseconds = midnightMilliseconds + startHour * Commons.MILLISECONDS_IN_HOUR;
        int increment;
        for(int i=0; i<alarmsToSchedule; i++)
        {
            increment = Commons.randInt((i==0 ? 0 : -bound), (i==alarmsToSchedule-1 ? 0 : bound));
            result[i] = new Date(timeMilliseconds + increment);
            timeMilliseconds += rateMilliseconds;
        }

        // Return the complete array
        return result;
    }

    /**
     * Sets up the midnight repeating alarm that will trigger the computation of the game alarms each day
     */
    private void scheduleMidnightRepeatingAlarm(Context context)
    {
        // Get next midnight
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        // Schedule alarm at midnight repeating every 24 hours
        scheduleRepeatingAlarm(context, calendar.getTime(), 24 * Commons.MILLISECONDS_IN_HOUR, context.getString(R.string.midnight_action), context.getString(R.string.alarm_unique_name_prefix)+"midnight");
    }

    /**
     * Starts all application alarms (repeating midnight and today's game alarms)
     */
    public void startAllAlarms(Context context)
    {
        // Set midnight alarm
        AlarmScheduler.getInstance().scheduleMidnightRepeatingAlarm(context);

        // Set daily alarms (passing false as parameter: it's not midnight)
        AlarmScheduler.getInstance().setupTodayAlarms(context, false);
    }
}
