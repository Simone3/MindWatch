package it.polimi.aui.auiapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Arrays;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import it.polimi.aui.auiapp.R;
import it.polimi.aui.auiapp.model.games.Game;
import it.polimi.aui.auiapp.model.other.BrainTrainingType;

/**
 * Manages the application settings (getters and setters)
 */
public class SettingsManager
{
    private static SettingsManager instance;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    private final int DEFAULT_DIFFICULTY;
    private final int DEFAULT_GAMES_PER_DAY;
    private final String[] ALL_DAYS = Commons.getAllDaysArray();
    private final String[] DEFAULT_DAYS = ALL_DAYS;
    private final int DEFAULT_START_HOUR = 8;
    private final int DEFAULT_END_HOUR = 20;

    private SettingsManager(Context context)
    {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.editor = this.sharedPreferences.edit();
        this.context = context;
        DEFAULT_DIFFICULTY = Integer.parseInt(context.getString(R.string.default_difficulty));
        DEFAULT_GAMES_PER_DAY = Integer.parseInt(context.getString(R.string.default_frequency));
    }

    /**
     * Singleton pattern
     */
    public static synchronized SettingsManager getInstance(Context context)
    {
        if(instance == null) instance = new SettingsManager(context);
        return instance;
    }

    /**
     * Total user points (sum of points in all categories)
     * @return user points
     */
    public int getTotalUserPoints()
    {
        int total = 0;
        for(BrainTrainingType brainTrainingType: BrainTrainingType.values())
        {
            total += getUserPointsByBrainTrainingType(brainTrainingType);
        }
        return total;
    }

    /**
     * User points in the given category
     * @param brainTrainingType the games category
     * @return user points
     */
    public int getUserPointsByBrainTrainingType(BrainTrainingType brainTrainingType)
    {
        return sharedPreferences.getInt(context.getString(R.string.user_points_key, brainTrainingType.getValue()), 0);
    }

    /**
     * Getter
     * @return list of selected weekdays
     */
    public String[] getGameDays()
    {
        Set<String> daysSet = sharedPreferences.getStringSet(context.getString(R.string.days_key), new HashSet<>(Arrays.asList(DEFAULT_DAYS)));
        return Commons.orderDaysFromSet(daysSet);
    }

    /**
     * Helper that tells if today is inside the game days list
     * @return true if today is a day where games are to be scheduled
     */
    public boolean isTodayAGameDay()
    {
        String todayWeekday = ALL_DAYS[Calendar.getInstance().get(Calendar.DAY_OF_WEEK)-1];
        return Arrays.asList(getGameDays()).contains(todayWeekday);
    }

    /**
     * Getter
     * @return number of games each day
     */
    public int getGamesPerDay()
    {
        return Integer.parseInt(sharedPreferences.getString(context.getString(R.string.frequency_key), "" + DEFAULT_GAMES_PER_DAY));
    }

    /**
     * Getter
     * @return number of today games shown to the user so far
     */
    public int getGamesShownToday()
    {
        return sharedPreferences.getInt(context.getString(R.string.current_game_number_key), 0);
    }

    /**
     * Getter
     * @return number of games scheduled today
     */
    public int getGamesScheduledToday()
    {
        return sharedPreferences.getInt(context.getString(R.string.games_scheduled_today), 0);
    }

    /**
     * Getter
     * @return number of today games solved by the user so far
     */
    public int getGamesSolvedToday()
    {
        return sharedPreferences.getInt(context.getString(R.string.current_solved_games_number_key), 0);
    }

    /**
     * Getter
     * @return start hour for notifications
     */
    public int getStartHour()
    {
        return sharedPreferences.getInt(context.getString(R.string.from_button_key), DEFAULT_START_HOUR);
    }

    /**
     * Getter
     * @return end hour for notifications
     */
    public int getEndHour()
    {
        return sharedPreferences.getInt(context.getString(R.string.to_button_key), DEFAULT_END_HOUR);
    }

    /**
     * Getter
     * @return selected difficulty
     */
    public int getDifficulty()
    {
        return Integer.parseInt(sharedPreferences.getString(context.getString(R.string.difficulty_key), "" + DEFAULT_DIFFICULTY));
    }

    /**
     * Getter
     * @return points earned (if > 0) or lost (if < 0) today by the user
     */
    public int getTodayPointsBalance()
    {
        return sharedPreferences.getInt(context.getString(R.string.today_points_balance_key), 0);
    }

    /**
     * All hours from which the user can select starting and ending hour for notifications
     * @return all selectable hours of the day
     */
    public static String[] getAvailableHours()
    {
        int min = 0;
        int max = 23;
        String[] hours = new String[max-min+1];
        for(int i=min; i<=max; i++) hours[i-min] = i+"";
        return hours;
    }

    /**
     * Getter
     * @return true if it's the first application run
     */
    public boolean isFirstRun()
    {
        return sharedPreferences.getBoolean(context.getString(R.string.first_run_key), true);
    }

    /**
     * Setter
     * @param newValue the new value for the games shown today so far
     */
    public void setGamesShownToday(int newValue)
    {
        editor.putInt(context.getString(R.string.current_game_number_key), newValue);
        editor.commit();
    }

    /**
     * Adds 1 to the games shown today so far
     */
    public void increaseGamesShownToday()
    {
        setGamesShownToday(getGamesShownToday() + 1);
    }

    /**
     * Setter
     * @param newValue the new value for the games solved today so far
     */
    public void setGamesSolvedToday(int newValue)
    {
        editor.putInt(context.getString(R.string.current_solved_games_number_key), newValue);
        editor.commit();
    }

    /**
     * Adds 1 to the games solved today so far
     */
    public void increaseGamesSolvedToday()
    {
        setGamesSolvedToday(getGamesSolvedToday() + 1);
    }

    /**
     * Setter
     * @param from start hour for notifications
     * @param to end hour for notifications
     */
    public void updateHours(int from, int to)
    {
        editor.putInt(context.getString(R.string.from_button_key), from);
        editor.putInt(context.getString(R.string.to_button_key), to);
        editor.commit();
    }

    /**
     * Manages user points after a game conclusion
     * @param correct true if user answered correctly
     * @param game the game shown to the user
     */
    public void updateUserPointsAfterGame(boolean correct, Game game)
    {
        // Get current points
        int currentPoints = this.getUserPointsByBrainTrainingType(game.getBrainTrainingType());

        // Compute change based on success and difficulty
        int change = game.getPointsChangeAfterAnswer(correct);

        // Store value in today's points balance
        setTodayPointsBalance(change+getTodayPointsBalance());

        // Compute and update user points in the game category
        int newPoints = currentPoints + change;
        if(newPoints<0) newPoints = 0;
        editor.putInt(context.getString(R.string.user_points_key, game.getBrainTrainingType().getValue()), newPoints);
        editor.commit();
    }

    /**
     * Setter
     * @param newValue new value for today's points balance
     */
    private void setTodayPointsBalance(int newValue)
    {
        editor.putInt(context.getString(R.string.today_points_balance_key), newValue);
        editor.commit();
    }

    /**
     * Setter
     * @param isFirstRun true if it's the first application run
     */
    public void setFirstRun(boolean isFirstRun)
    {
        editor.putBoolean(context.getString(R.string.first_run_key), isFirstRun);
        editor.commit();
    }

    /**
     * Resets the daily counters (e.g. games shown today) to the default values
     */
    public void resetDailyCounters()
    {
        setGamesShownToday(0);
        setGamesSolvedToday(0);
        setTodayPointsBalance(0);
    }

    /**
     * The name of the current user level, based on the amount of total points
     * @return the localized name of the user level
     */
    public String getUserLevelName()
    {
        // Get points and names from resources
        int points = getTotalUserPoints();
        String[] minPoints = context.getResources().getStringArray(R.array.user_levels_min_points);
        String[] levelNames = context.getResources().getStringArray(R.array.user_level_names);

        // Get right name given the current user points
        String name = "";
        for(int i=0; i<minPoints.length; i++)
        {
            if(Integer.parseInt(minPoints[i])>points)
            {
                name = (i==0) ? levelNames[0] : levelNames[i-1];
                break;
            }
            else if(i==minPoints.length-1)
            {
                name = levelNames[minPoints.length-1];
            }
        }

        return name;
    }

    /**
     * Setter
     * @param newValue new value for today's scheduled games number
     */
    public void setGamesScheduledTodayNumber(int newValue)
    {
        editor.putInt(context.getString(R.string.games_scheduled_today), newValue);
        editor.commit();
    }
}
